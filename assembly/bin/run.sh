#!/usr/bin/env bash

BIN=`dirname "$0"`
cd $BIN/..

CLASSPATH='conf'

for f in lib/*.jar; do
  CLASSPATH=${CLASSPATH}:$f;
done

java -server -classpath ${CLASSPATH} com.shvid.memcast.MemcastMain $1


