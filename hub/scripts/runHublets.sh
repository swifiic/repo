#!/bin/bash

DIR=$(dirname "$0")
source ${DIR}/../properties/setEnv.sh
defArgs="-Dfile.encoding=UTF-8"
# commonInclude=./hub/lib/in-swifiic-hub.jar:./hub/lib/commons-codec-1.8.jar:./hub/lib/mysql-connector-java-5.1.29-bin.jar:./hub/lib/simple-xml-2.7.1.jar
commonInclude=${SWIFIIC_HUB_BASE}/hub/lib/in-swifiic-hub.jar:${SWIFIIC_HUB_BASE}/lib/hub/commons-codec-1.8.jar:${SWIFIIC_HUB_BASE}/lib/hub/mysql-connector-java-5.1.29-bin.jar:${SWIFIIC_HUB_BASE}/lib/common/simple-xml-2.7.1.jar:${SWIFIIC_HUB_BASE}/lib/hub/log4j-api-2.2.jar:${SWIFIIC_HUB_BASE}/lib/hub/log4j-core-2.2.jar:${SWIFIIC_HUB_BASE}/lib/hub/log4j-jcl-2.2.jar:${SWIFIIC_HUB_BASE}/lib/hub/log4j-jul-2.2.jar

echo "Processing with DIR as ${DIR} and SWIFIIC_HUB_BASE as ${SWIFIIC_HUB_BASE}"
fileList=`find ${SWIFIIC_HUB_BASE} -name hublet-setup.sh`
for setupFile in ${fileList} ; do
    echo "Processing ${setupfile}"
    source ${setupFile}
    echo "Launching ${proj} with ${classToRun}"
    command="/usr/bin/java ${defArgs} -classpath ${SWIFIIC_HUB_BASE}/${proj}:${commonInclude} ${classToRun}"
    proj_base=`echo ${proj} | cut -d / -f 1`
    logPath="${SWIFIIC_HUB_BASE}/log/${proj_base}"
    echo "Trying to create daemon for :: ${command} :: with logas at ${logPath}"
    echo "Command tried is start-stop-daemon --start -p ${SWIFIIC_HUB_BASE}/pid/${proj_base}.pid -g swifiic -c swifiic -b -m --startas /bin/bash -- -c ${command}"
    start-stop-daemon --start -p ${SWIFIIC_HUB_BASE}/pid/${proj_base}.pid -g swifiic -c swifiic -b -m --startas /bin/bash -- -c "${command} >${logPath}.out 2>${logPath}.err"
done

#proj=suta/suta-hub.jar
#classToRun=in.swifiic.plat.app.suta.hub.Suta
#echo java ${defArgs} -classpath ./${proj}:${commonInclude} ${classToRun} 
#java ${defArgs} -classpath ./${proj}:${commonInclude} ${classToRun} 
