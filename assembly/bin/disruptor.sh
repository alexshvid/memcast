#!/usr/bin/env bash

BIN=`dirname "$0"`
pushd $BIN/..

JAVA_OPTS="-server"
JAVA_OPTS="$JAVA_OPTS -Xms2048m -Xmx2048m -XX:MaxPermSize=256m"
JAVA_OPTS="$JAVA_OPTS -Xdebug -Xrunjdwp:transport=dt_socket,address=9000,suspend=n,server=y"
JAVA_OPTS="$JAVA_OPTS -Dcom.sun.management.jmxremote.port=8000 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false"
JAVA_OPTS="$JAVA_OPTS -Xss412k"

CLASSPATH='conf'

for f in lib/*.jar; do
  CLASSPATH=${CLASSPATH}:$f;
done

java -server -classpath ${CLASSPATH} $JAVA_OPTS com.shvid.memcast.disruptor.DisruptorSample $1

popd

