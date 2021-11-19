#!/bin/bash
mode=$1
echo "mode 0 - run client in localhost zookeeper"
echo "mode 1 - run client in unique zookeeper servers"
echo "executing in mode: $mode"

export ZOOBINDIR=~/apache-zookeeper-3.6.2-bin/bin

if [[ -z "$ZOOBINDIR" ]]
then
	echo "Error!! ZOOBINDIR is not set" 1>&2
	exit 1
fi

. "$ZOOBINDIR"/zkEnv.sh

# TODO Include your ZooKeeper connection string here. Make sure there are no spaces.
var=0
if [ $mode -eq $var ]
then
  export ZKSERVER=localhost:2181,localhost:2182,localhost:2183
else
  export ZKSERVER=lab2-10.cs.mcgill.ca:21804,lab2-11.cs.mcgill.ca:21804,lab2-13.cs.mcgill.ca:21804
fi

java -cp "$CLASSPATH":../task:.: distclient.DistClient "$2"
