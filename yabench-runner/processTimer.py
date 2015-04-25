#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import psutil
import os
import subprocess
import time

class ProcessTimer:
  def __init__(self,command,resultsFile):
    self.command = command
    self.resultsFile = resultsFile
    self.execution_state = False
    
    self.results = open(self.resultsFile, 'w')
    self.results.write("{},{},{},{},{}\n".format("time","rss_mem","cpu_percent","mem_percent","threads"))

  def execute(self):

    self.t1 = None
    self.t2 = None
    self.t0 = time.time()
    self.p = subprocess.Popen(self.command,shell=False)
    self.execution_state = True

  def poll(self):
    if not self.check_execution_state():
      return False

    self.t1 = time.time()

    try:
      pp = psutil.Process(self.p.pid)

      #obtain a list of the subprocess and all its descendants
      descendants = list(pp.get_children(recursive=True))
      descendants = descendants + [pp]

      rss_memory = 0

      #calculate and sum up the memory of the subprocess and all its descendants 
      for descendant in descendants:
        try:
		  #details: https://code.google.com/p/psutil/wiki/Documentation
          mem_info = descendant.get_memory_info()

          rss_memory += (mem_info[0] / 1024 / 1024)
          
        except psutil.NoSuchProcess:
          #sometimes a subprocess descendant will have terminated between the time
          # we obtain a list of descendants, and the time we actually poll this
          # descendant's memory usage.
          pass
      self.results.write("{},{},{},{},{}\n".format(round(self.t1-self.t0,1),round(rss_memory,2),pp.cpu_percent(interval=0.1),round(pp.memory_percent(),2),pp.num_threads()))
      self.results.flush()
      os.fsync(self.results)
      self.t2 = time.time()

    except psutil.NoSuchProcess:
      return self.check_execution_state()


    return self.check_execution_state()

  def is_running(self):
    return psutil.pid_exists(self.p.pid) and self.p.poll() == None
  def check_execution_state(self):
    if not self.execution_state:
      return False
    if self.is_running():
      return True
    self.executation_state = False
    self.t1 = time.time()
    return False

  def close(self,kill=False):
    try:
      pp = psutil.Process(self.p.pid)
      if kill:
        pp.kill()
      else:
        pp.terminate()
    except psutil.NoSuchProcess:
      print("process already dead")
    except OSError.ProcessLookupError as e:
      print("process already dead")