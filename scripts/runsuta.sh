#!/bin/bash

DIR=$(dirname "$0")
source ${DIR}/../properties/setEnv.sh
defArgs="-Dfile.encoding=UTF-8"
# commonInclude=./hub/lib/in-swifiic-hub.jar:./hub/lib/commons-codec-1.8.jar:./hub/lib/mysql-connector-java-5.1.29-bin.jar:./hub/lib/simple-xml-2.7.1.jar
commonInclude=./hub/lib/in-swifiic-hub.jar:./lib/hub/commons-codec-1.8.jar:./lib/hub/mysql-connector-java-5.1.29-bin.jar:./lib/common/simple-xml-2.7.1.jar:./lib/hub/log4j-api-2.2.jar:./lib/hub/log4j-core-2.2.jar:./lib/hub/log4j-jcl-2.2.jar:./lib/hub/log4j-jul-2.2.jar

proj=suta/suta-hub.jar
classToRun=in.swifiic.plat.app.suta.hub.Suta
echo java ${defArgs} -classpath ./${proj}:${commonInclude} ${classToRun} 
java ${defArgs} -classpath ./${proj}:${commonInclude} ${classToRun} 
