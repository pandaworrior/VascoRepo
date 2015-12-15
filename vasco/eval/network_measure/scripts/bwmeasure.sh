#!/bin/bash

# this script to measure the rtt between hosts

hostIP=$1
numOfIter=$2

while [ $numOfIter -gt 0 ]
do
iperf -c $hostIP -i 10 -t 10 -f m
sleep 30
numOfIter=$[$numOfIter-1]
done
