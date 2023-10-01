import os
from os.path import exists

def get_dir(a="./"):
  return [a+i for i in os.listdir(a)]

def compile(a,b="./WEB-INF"):
  sp=';'
  if os.name == 'nt':
    sp = ';'
  else:
    sp = ':'
  c='javac -cp ../../lib/*'+sp+b+'/lib/*'+sp+b+'/classes/. '+a
  print(c)
  # return
  os.system(c)

def compile_dir(a, excludes):
  if(a[-1]!="/"):
    a=a+"/"
  print("compile: "+a)
  cur_dir = a
  fs = get_dir(a)
  # remove all class files first
  for i in fs:
    if os.path.isfile(i):
      if len(i)<6:
        continue
      elif i[-6:]=='.class':
        os.remove(i)
  # compilation
  for i in fs:
    if not os.path.exists(i):
      continue
    elif os.path.isfile(i):
      if(len(i)<4):
        continue
      if i[-4:]=='java':
        compile(i)
    else:
      if os.path.basename(i) not in excludes:
        compile_dir(i,excludes)

def setup_exclusions():
  exclusions = []
  exclusion_file = './.pdmdevconf'
  if(os.path.exists(exclusion_file)):
    file = open(exclusion_file,'r')
    lines = file.readlines()
    for line in lines:
      exclusions.append(line.strip())
  return exclusions

if __name__ == '__main__':
  excludes = setup_exclusions()
  print('excluding folders: %s' % (excludes));
  compile_dir("./WEB-INF/classes",excludes)