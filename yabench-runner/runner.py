#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import argparse
import json
import subprocess
import os
import errno
import shutil
import csv
import glob
from string import Template
from processTimer import *

###############################################################################
# Disables buffering for sys.stdout
###############################################################################
class Unbuffered(object):
   def __init__(self, stream):
       self.stream = stream
   def write(self, data):
       self.stream.write(data)
       self.stream.flush()
   def __getattr__(self, attr):
       return getattr(self.stream, attr)

import sys
sys.stdout = Unbuffered(sys.stdout)

###############################################################################
# Constants
###############################################################################
QUERY_4_ENGINE='engine.query'
QUERY_4_ORACLE='oracle.query'
INPUTSTREAM_PREFIX = 'SG_'
QUERYRESULTS_PREFIX = 'QR_'
PERFORMANCERESULTS_PREFIX = 'P_'
QUERY_PREFIX = "QUERY_"
ORACLE_OUTPUT_PREFIX = "ORACLE_"
BOXPLOTS_OUTPUT_PREFIX = "BOXPLOTS_"

###############################################################################
# Utility classes and functions
###############################################################################
def make_sure_path_exists(path):
    '''
    Checks whether the given folder exists.
    '''
    try:
        os.makedirs(path)
    except OSError as exception:
        if exception.errno != errno.EEXIST:
            raise
            
def arg_to_dict(arg, separator='='):
    new_dict = dict();
    for a in arg:
        arr = a.split(separator)
        new_dict[arr[0]] = arr[1]
    return new_dict

def gen_test_config(config, test_config, cli_config):
    new_dict = config.copy()
    if 'vars' in config:
        new_vars = config['vars'].copy()
    else:
        new_vars = dict()

    del new_dict['tests']
    del new_dict['vars']

    new_dict.update(test_config)
    if 'vars' in test_config:
        new_vars.update(test_config['vars'])
    new_dict['vars'] = new_vars
    
    if 'runs' not in new_dict:
        new_dict['runs'] = '1'
        
    if 'enginename' not in new_dict:
        new_dict['enginename'] = 'unknown'
        
    if 'boxplots' not in new_dict:
        new_dict['boxplots'] = 'false'

    if cli_config is not None:
        for c in cli_config:
            new_dict.update(c)
    return new_dict

def prepare_query(resultsDir, config, queryFile):
    '''
    Reads a query template from the given file and resolves the variables.
    '''
    try:
        with open(queryFile) as f:
            t = Template(f.read())

        qF = "{}/{}{}{}".format(resultsDir, QUERY_PREFIX, config['name'], config['suffix'])
        with open(qF, 'w') as q:
            query = t.substitute(config['vars'])
            q.write(query)
        return qF
    except IOError:
        print('Can\'t open {} file!'.format(queryFile))

class JavaLikeProps(argparse.Action):
    '''
    An action parsing Java like properties from CLI.

    Examples of Java like properties:
        * -Djava.awt.headless=true
        * -Djava.net.useSystemProxies=true
    '''
    def __init__(self, option_strings, dest, nargs=None, separator='=', **kwargs):
        self.separator = separator

        super(JavaLikeProps, self).__init__(option_strings, dest, **kwargs)

    def __call__(self, parser, namespace, values, option_string=None):
        value = ''.join(values);
        array = values.split(self.separator)
        if(len(array) > 2 or len(array) < 2):
            print('Wrong format of Java like properties: ' + value)
        else:
            v = {array[0]: array[1]}
            D = namespace.D
            if D is None:
                D = [v]
            else:
                D.append(v)
            setattr(namespace, self.dest, D)

###############################################################################

def runGenerator(resultsDir, config):
    destination = "{}/{}{}{}".format(resultsDir, INPUTSTREAM_PREFIX, config['name'], config['suffix'])

    run_args = ["java", "-jar", config['exec.generator']]
    run_args.extend(["-dest", destination]);
    run_args.extend(["-name", config['generator']]);
    run_args.extend(["-duration", config['duration']]);
    run_args.extend(["-seed", config['seed']]);
    run_args.extend(["-stations", config['stations']]);
    run_args.extend(["-interval", config['interval']]);
    if 'max_temp' in config:
        run_args.extend(["-max_temp", config['max_temp']]);
    if 'min_temp' in config:
        run_args.extend(["-min_temp", config['min_temp']]);

    config['inputstream'] = destination

    print(run_args)
    return subprocess.check_call(run_args)

def runEngine(testDir, resultsDir, config):
    run_args = ["java", "-jar", config['exec.engine']]
    run_args.extend(["-dest", "{}/{}{}{}".format(resultsDir, QUERYRESULTS_PREFIX, config['name'], config['suffix'])])
    run_args.extend(["-query", prepare_query(resultsDir, config, "{}/{}".format(testDir, QUERY_4_ENGINE))])
    run_args.extend(["-source", config['inputstream']])

    print(run_args)

    ptimer = ProcessTimer(run_args, "{}/{}{}{}".format(resultsDir, PERFORMANCERESULTS_PREFIX, config['name'], config['suffix']),config)

    try:
        ptimer.execute()
        #poll as often as possible; otherwise the subprocess might
        # "sneak" in some extra memory usage while you aren't looking
        while ptimer.poll():
            temp = round(ptimer.t2-ptimer.t1,3)
            polltime = temp if temp < .500 else .500
            time.sleep(.500-polltime)
    finally:
        #make sure that we don't leave the process dangling?
        ptimer.close()

    return True
    #return subprocess.check_call(run_args)

def runOracle(testDir, resultsDir, config):
    run_args = ["java", "-jar", config['exec.oracle']]
    run_args.extend(["--queryresults", "{}/{}{}{}".format(resultsDir, QUERYRESULTS_PREFIX, config['name'], config['suffix'])])
    run_args.extend(["--output", "{}/{}{}{}".format(resultsDir, ORACLE_OUTPUT_PREFIX, config['name'], config['suffix'])])
    run_args.extend(["--inputstream", config['inputstream']])
    run_args.extend(["--query", "{}/{}".format(testDir, QUERY_4_ORACLE)])
    for key in config['vars']:
        run_args.extend(["-P" + key + "=" + config['vars'][key]])
    if 'graceful' in config:
        run_args.extend(["-graceful", config['graceful']])
    if 'singleresult' in config:
        run_args.extend(["-singleresult", config['singleresult']])

    print(run_args)
    return subprocess.check_call(run_args)
    
def runBoxplotsDeprecated(resultsDir, config):
    #script to run r-script for boxplotting
    #build r-script argument string
    rargs = ""
    base_dir = os.path.abspath('.')
    script_dir = os.path.dirname(os.path.realpath(__file__))
    #split and join resultsDir to make it work for different os
    resultsDir = resultsDir.split('/')
    resultsDir = os.path.join(*resultsDir)
    for i in range(int(config['runs'])):
        filename = "{}{}{}".format(ORACLE_OUTPUT_PREFIX, config['name'], i+1)
        rargs+="{}{}".format(os.path.abspath(os.path.join(base_dir, resultsDir, filename)),',')
    
    rargs = rargs[:-1]
    destination = os.path.abspath(os.path.join(base_dir, resultsDir))
    
    rscript_dir = os.path.join(script_dir, "boxplots.R")
   
    run_args = ["Rscript", rscript_dir, rargs, destination, config['name']]
    print(run_args)

    return subprocess.check_call(run_args)
    
def runBoxplots(resultsDir, config):
    globdict = {}
    base_dir = os.path.abspath('.')
    resultsDir = resultsDir.split('/')
    resultsDir = os.path.join(*resultsDir)
    #oracleresults = sorted(glob.glob(os.path.join(base_dir,resultsDir,ORACLE_OUTPUT_PREFIX+config['name']) + "*"), key = lambda name: int(name[len(base_dir+os.sep+resultsDir+os.sep+ORACLE_OUTPUT_PREFIX+config['name']):]))
    oracleresults = sorted(glob.glob(os.path.join(base_dir,resultsDir,ORACLE_OUTPUT_PREFIX+config['name']) + "*"))
    print(resultsDir)
    oracledict = {}
    wincount = 1
    for filen in oracleresults:
        f = open(filen)
        csv_f = csv.reader(f,delimiter=',')
        dictkey = filen.split(os.sep)[-1]
        
        for row in csv_f:
            if wincount in globdict:
                globdict[wincount][0].append(float(row[0]))
                globdict[wincount][1].append(float(row[1]))
                globdict[wincount][2].append(float(row[7]))
            else:
                globdict[wincount] = [[],[],[]]
                globdict[wincount][0].append(float(row[0]))
                globdict[wincount][1].append(float(row[1]))
                globdict[wincount][2].append(float(row[7]))
            wincount = wincount+1
        wincount=1
    
    filename = "{}{}".format(BOXPLOTS_OUTPUT_PREFIX, config['name'])
    with open(os.path.join(resultsDir, filename), 'w') as the_file:
        #write engine name in first line
        the_file.write(config['enginename']+'\n')
        for win, value in globdict.items():
            row = ";".join([','.join(map(str, x)) for x in value])
            the_file.write(row+'\n')


###############################################################################
# Main functions
###############################################################################
def parse_arguments():
    parser = argparse.ArgumentParser(description='Run tests')
    parser.add_argument('testDir')
    parser.add_argument('--onlyoracle', help='only re-run the oracle',
                        action='store_true')
    parser.add_argument('--onlyboxplots', help='only create boxplots (oracle files already need to be available)',
                        action='store_true')
    parser.add_argument('--withoutoracle', help='without the oracle',
                        action='store_true')
    parser.add_argument('--rm_prev', help='remove previous results',
                        action='store_true')
    parser.add_argument('--test', help='name of a particular test')
    parser.add_argument('-D', help='Java like properties overriding config.json, e.g. -Dexec.engine=yabench-cqels.jar',
                        action=JavaLikeProps)

    return parser.parse_args()

def main():
    args = parse_arguments()

    resultsDir = args.testDir + '/results'
    if args.rm_prev:
        shutil.rmtree(resultsDir, ignore_errors=True)
    make_sure_path_exists(resultsDir)

    try:
        with open(args.testDir + '/config.json') as configFile:
            config = json.load(configFile)
            for test_config in config['tests']:
                #Merge the setting
                new_config = gen_test_config(config, test_config, args.D)

                #flag to check if config contains parameter for inputstream (is needed later)
                inputstream = True if 'inputstream' in new_config else False
                
                if not args.onlyboxplots:
                    print('not boxplots only')
                    for counter in range(int(new_config['runs'])):
                        if int(new_config['runs']) == 1:
                            new_config['suffix'] = ''
                        else:
                            new_config['suffix'] = counter+1
                        #delete inputstream from config if it was only added during runtime and not in the initial config (this is checked above) to ensure a new stream is generated for a new run (may be needed later if we want to run with different seeds)
                        if not inputstream:
                            new_config.pop('inputstream', None)
                      
                        if (args.test and args.test == new_config['name']) or not args.test:
                            if args.onlyoracle:
                                if 'inputstream' not in new_config:
                                    new_config['inputstream'] = "{}/{}".format(resultsDir, INPUTSTREAM_PREFIX + new_config['name'] + str(new_config['suffix']))
                                runOracle(args.testDir, resultsDir, new_config)
                            else:
                                if 'inputstream' not in new_config:
                                    #Generate a new input stream, otherwise
                                    #use input stream from 'inputstream' setting.
                                    runGenerator(resultsDir, new_config)
                                runEngine(args.testDir, resultsDir, new_config)
                                if not args.withoutoracle:
                                    runOracle(args.testDir, resultsDir, new_config)
                        else:
                            continue;
                else:
                    #create boxplots only if runs > 1 and boxplots = true
                    if new_config['boxplots'].lower() == 'true' and int(new_config['runs']) > 1:
                        runBoxplots(resultsDir, new_config)
    except IOError:
        print("Can\'t open {}/config.json file".format(args.testDir))

if __name__ == '__main__':
    main()
