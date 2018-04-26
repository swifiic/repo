#!/bin/bash


# expected to be called with clean or build as argument

command=$1

# wrapper to build android apps and hub code
# after refactor of folders in late 2017

cd android ; bash build.sh ${command} ; cd -

if [ "${command}" == "clean" ] ; then
    cd hub ; ./gradlew clean ; cd -
else
    cd hub ; ./gradlew all ; cd -
    
mv ./android/app/msngr/msngrandi/build/outputs/apk/msngrandi-debug.apk ./dist/apk/Msngr-debug.apk
mv ./android/app/Bromide/app/build/outputs/apk/debug/app-debug.apk ./dist/apk/Bromide-debug.apk
mv ./android/plat/suta/app/build/outputs/apk/debug/app-debug.apk ./dist/apk/SUTA-debug.apk
mv ./android/plat/soa/app/build/outputs/apk/debug/app-debug.apk ./dist/apk/SOA-debug.apk

tar cfz swifiic.tar.gz dist
fi
