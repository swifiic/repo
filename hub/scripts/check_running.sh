#!/bin/bash
echo "Checking Whether Apps are prtoperly communicating"

echo " implementation pending"

read -p "Enter the base directory for SWIFiIC Installation Eg. /opt/swifiic" base_directory


source ${base_directory}/properties/setEnv.sh

# verify if all modules (Tomcat - SOA | SUTA | Msngr) are running - as processes

# verify if all of them have updated the DB or created files in last 24 hours or last one week
     # focus on fields that each of them update - TBD



