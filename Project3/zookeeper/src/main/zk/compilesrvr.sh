#!/bin/bash

if [[ -z "$ZOOBINDIR" ]]
then
	echo "Error!! ZOOBINDIR is not set" 1>&2
	exit 1
fi

# shellcheck disable=SC2086
. $ZOOBINDIR/zkEnv.sh

javac -cp "$CLASSPATH":../task:.: $(pwd)/dist/DistProcess.java
