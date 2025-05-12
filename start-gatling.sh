#!/bin/bash

mkfifo pipe1 pipe2 pipe3 pipe4

mvn gatling:test -Dport=8443 > pipe1 &
mvn gatling:test -Dport=8444 > pipe2 &
mvn gatling:test -Dport=8445 > pipe3 &
mvn gatling:test -Dport=8446 > pipe4 &

cat pipe1 &
cat pipe2 &
cat pipe3 &
cat pipe4 &

wait
rm pipe1 pipe2 pipe3