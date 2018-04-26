#!/bin/bash
action=$1
cd plat/soa
./gradlew ${action}
cd ../suta
./gradlew ${action}
cd ../../app/Bromide
./gradlew ${action}
cd ../msngr
./gradlew ${action}
