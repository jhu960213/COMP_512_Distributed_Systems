#!/bin/bash

for i in {4000..4010}
do
  $(pwd)/runclient.sh 0 "$i"
done


