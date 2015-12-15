#!/bin/bash

# this script to measure the rtt between hosts

hostIP=$1
numOfIter=$2

while [ $numOfIter -gt 0 ]
do
ping $hostIP -c 10
sleep 30
numOfIter=$[$numOfIter-1]
done
