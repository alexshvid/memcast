#!/usr/bin/env bash

BIN=`dirname "$0"`
pushd $BIN/..

JAVA_OPTS="-server"
JAVA_OPTS="$JAVA_OPTS -Xms2048m -Xmx2048m -XX:MaxPermSize=256m"
JAVA_OPTS="$JAVA_OPTS -Xdebug -Xrunjdwp:transport=dt_socket,address=9000,suspend=n,server=y"
JAVA_OPTS="$JAVA_OPTS -Dcom.sun.management.jmxremote.port=8000 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false"
JAVA_OPTS="$JAVA_OPTS -Xss412k"

JAVA_OPTS="$JAVA_OPTS -XX:MaxDirectMemorySize=9G"

CLASSPATH='conf'

for f in lib/*.jar; do
  CLASSPATH=${CLASSPATH}:$f;
done

$JAVA_HOME/bin/java -server -classpath ${CLASSPATH} $JAVA_OPTS -Dcom.tc.productkey.path=$TERRACOTTA_LICENSE_KEY com.shvid.memcast.bigmem.Start $1

popd

