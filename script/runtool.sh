#!/bin/bash
#echo $0
rootdir=$(dirname $0) # Now, at /root/conf
rootdir=$(dirname $rootdir) # Now, at /root
echo $rootdir
jars=/home/fengyu/dev/jars/jsoup-1.6.3.jar
classpath=$jars:$rootdir/bin
cmd="java -cp $classpath $@"
echo $cmd
$cmd