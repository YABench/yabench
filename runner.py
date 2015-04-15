#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import argparse
import json
import subprocess
import os
import errno
import shutil
from string import Template
from processTimer import *

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

QUERY_4_ENGINE='engine.query'
QUERY_4_ORACLE='oracle.query'
INPUTSTREAM_PREFIX = 'SG_'
QUERYRESULTS_PREFIX = 'QR_'
PERFORMANCERESULTS_PREFIX = 'P_'
QUERY_PREFIX = "QUERY_"
ORACLE_OUTPUT_PREFIX = "ORACLE_"

def make_sure_path_exists(path):
    try:
        os.makedirs(path)
    except OSError as exception:
        if exception.errno != errno.EEXIST:
            raise

def gen_test_config(config, test_config):
    new_dict = config.copy()
    del new_dict['tests']
    new_dict.update(test_config)
    return new_dict

def prepare_query(resultsDir, config, queryFile):
    try:
        with open(queryFile) as f:
            t = Template(f.read())

        qF = "{}/{}{}".format(resultsDir, QUERY_PREFIX, config['name'])
        with open(qF, 'w') as q:
            query = t.substitute(config['vars'])
            q.write(query)
        return qF
    except IOError:
        print('Can\'t open {} file!'.format(queryFile))

def runGenerator(resultsDir, config):
    run_args = ["java", "-jar", config['exec']['stream-generator']]
    run_args.extend(["-dest", "{}/{}{}".format(resultsDir, INPUTSTREAM_PREFIX, config['name'])]);
    run_args.extend(["-name", config['generator']]);
    run_args.extend(["-duration", config['duration']]);
    run_args.extend(["-seed", config['seed']]);
    run_args.extend(["-stations", config['stations']]);
    run_args.extend(["-interval", config['interval']]);
    if 'max_temp' in config:
        run_args.extend(["-max_temp", config['max_temp']]);
    if 'min_temp' in config:
        run_args.extend(["-min_temp", config['min_temp']]);

    print(run_args)
    return subprocess.check_call(run_args)

def runEngine(testDir, resultsDir, config):
    run_args = ["java", "-jar", config['exec']['engine']]
    run_args.extend(["-dest", "{}/{}{}".format(resultsDir, QUERYRESULTS_PREFIX, config['name'])])
    run_args.extend(["-query", prepare_query(resultsDir, config, "{}/{}".format(testDir, QUERY_4_ENGINE))])
    run_args.extend(["-source", "{}/{}{}".format(resultsDir, INPUTSTREAM_PREFIX, config['name'])])

    print(run_args)

    ptimer = ProcessTimer(run_args, "{}/{}{}".format(resultsDir, PERFORMANCERESULTS_PREFIX, config['name']))

    try:
        ptimer.execute()
        #poll as often as possible; otherwise the subprocess might
        # "sneak" in some extra memory usage while you aren't looking
        while ptimer.poll():
            polltime = round(ptimer.t2-ptimer.t1,3)
            time.sleep(.500-polltime)
    finally:
        #make sure that we don't leave the process dangling?
        ptimer.close()

    return True
    #return subprocess.check_call(run_args)

def runOracle(testDir, resultsDir, config):
    run_args = ["java", "-jar", config['exec']['oracle']]
    run_args.extend(["--queryresults", "{}/{}{}".format(resultsDir, QUERYRESULTS_PREFIX, config['name'])])
    run_args.extend(["--output", "{}/{}{}".format(resultsDir, ORACLE_OUTPUT_PREFIX, config['name'])])
    run_args.extend(["--inputstream", "{}/{}{}".format(resultsDir, INPUTSTREAM_PREFIX, config['name'])])
    run_args.extend(["--query", "{}/{}".format(testDir, QUERY_4_ORACLE)])
    for key in config['vars']:
        run_args.extend(["-P" + key + "=" + config['vars'][key]])
    if 'graceful' in config:
        run_args.extend(["-graceful", config['graceful']])

    print(run_args)
    return subprocess.check_call(run_args)

def main():
    parser = argparse.ArgumentParser(description='Run tests')
    parser.add_argument('testDir')
    parser.add_argument('--onlyoracle', help='only re-run the oracle',
                        action='store_true')
    parser.add_argument('--withoutoracle', help='without the oracle',
                        action='store_true')
    parser.add_argument('--rm_prev', help='remove previous results',
                        action='store_true')
    parser.add_argument('--test')

    args = parser.parse_args()

    resultsDir = args.testDir + '/results'
    if args.rm_prev:
        shutil.rmtree(resultsDir, ignore_errors=True)
    make_sure_path_exists(resultsDir)

    try:
        with open(args.testDir + '/config.json') as configFile:
            config = json.load(configFile)
            for test_config in config['tests']:
                new_config = gen_test_config(config, test_config)
                if (args.test and args.test == new_config['name']) or not args.test:
                    if args.onlyoracle:
                        runOracle(args.testDir, resultsDir, new_config)
                    else:
                        runGenerator(resultsDir, new_config)
                        runEngine(args.testDir, resultsDir, new_config)
                        if not args.withoutoracle:
                            runOracle(args.testDir, resultsDir, new_config)
                else:
                    continue;
    except IOError:
        print("Can\'t open {}/config.json file".format(args.testDir))

if __name__ == '__main__':
    main()
