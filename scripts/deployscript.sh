#!/bin/bash


get_abs_filename() {
  # $1 : relative filename
  echo "$(cd "$(dirname "$1")" && pwd)/$(basename "$1")"
}



needExit="false"
echo "Checking Whether Prerequisites are present"
echo "Checking for Java"
type java >/dev/null 2>&1 
if [ $? -ne 0 ] ; then
    echo "Java is not Present. sudo apt-get install openjdk-7-jdk"
    needExit="true"
else
    echo "Java is installed"
fi

# we may need a better check than this for tomcat7-user
echo "Checking for Tomcat User"
if [ -f /etc/init.d/tomcat7* ]; then
    echo " Tomcat is installed"
else 
    echo " Tomcat is not installed. Install using sudo apt-get install tomcat7-user"
    needExit="true"
fi

echo "Checking for MySQL"
type mysql >/dev/null 2>&1 
if [ $? -ne 0 ] ; then
    echo "MySQL is not Present. sudo apt-get install mysql-server"
    needExit="true"
else
    echo "MySQL is installed"
fi

echo "Checking for Apache"
type apache2 >/dev/null 2>&1 
if [ $? -ne 0 ] ; then
    echo "Apache is not Present. Install using sudo apt-get install apache2 php5 phpmyadmin"
    needExit="true"
else
    echo "Apache is installed"
fi

echo "Checking Whether MYSQL Service is Running or not"
UP=$(pgrep mysql | wc -l);
if [ "$UP" -ne 1 ];
then
        echo "MySQL is down.";
        sudo service mysql start

else
        echo "MySQL Service is running.";
fi

echo "Checking Whether Tomcat Service is Running or not"
UP=$(pgrep tomcat7 | wc -l);
if [ "$UP" -ne 1 ];
then
        echo "Tomcat is down.";
        sudo service tomcat7 start

else
        echo "Tomcat7 is running";
fi

echo "Checking for IBR-DTN Daemon"
if [ -f /etc/init.d/ibrdtn* ]; then
    echo " IBR-DTN is installed"
else 
    echo " IBR-DTN is not installed. Install IBR-DTN from https://trac.ibr.cs.tu-bs.de/project-cm-2012-ibrdtn/wiki/download#DebianUbuntuRepository"
    needExit="true"
fi

distFile="./dist.tar.gz"
if [ ! -f ${distFile} ]; then
    echo "dist.tar.gz not found. checking arguments"
    if [ ! -f ${1} ]; then
        echo "Distribution file not found. Should be in cwd or specified as argument."
        needExit="true"
    else
        distFile=${1}
    fi
fi 

distFile=$(get_abs_filename "${distFile}")

if [ "${needExit}" = "true" ]; then
    echo "Fix prior errors. Aborting.\n\n"
    exit
fi

read -p "Enter the base directory for SWIFiIC Installation Eg. /opt : " base_directory
read -p "Enter deployment identifier (e.g. location code / pin) : " deploy_code
 
# deploy_code and base_directory should not be empty
if [ "x${base_directory}" = "x"  ]; then
	echo "Empty base folder"
	exit
fi
base_directory="${base_directory}/swifiic"

if [ "x${deploy_code}" = "x"  ]; then
        echo "Empty deployment Identifier"
        exit
fi

echo; read -p "Enter login for MySQL for root access : " root_login
echo; read -p "Enter password for MySQL for root access : " root_pass
echo; read -p "Choose as  password for MySQL for SWiFiIC access : " -s swifiic_pass
echo; read -p "Re-enter your choice for password for MySQL for SWiFiIC access : " -s swifiic_pass2

if [ "${swifiic_pass}" != "${swifiic_pass2}" ];
then
	echo "Password mismatch"
	exit
fi

echo; echo;echo
echo ; echo "======== Starting The SWiFiIC setup under ${base_directory} using ${distFile} ====="
mkdir /tmp/deploy
cd /tmp/deploy
tar -zxvf ${distFile}
if [ ! -d "dist" ]; then
    echo "Content of ${distFile} did not have a dist folder"
    exit
fi

cd ./dist

sudo mkdir -p ${base_directory}
if [ $? -ne 0 ] ; then
    echo "fatal. does an incomplete prior deployment exist?"
    exit
else
    echo "Success Creating Directory"
fi

sudo groupadd swifiic
sudo useradd -s /sbin/nologin -g swifiic -d  ${base_directory} swifiic

echo "Moving files to ${base_directory}"
#incorrect
sudo cp -R * ${base_directory}/
echo "Creating SOA HubServer"
cd ${base_directory}/
sudo tomcat7-instance-create -p 18090 -c 18009 HubSrvr
sudo mv hub/HubSrvr.war ${base_directory}/HubSrvr/webapps


echo "export SWIFIIC_HUB_BASE=${base_directory}" | sudo tee ${base_directory}/properties/setEnv.sh 
echo "export SWIFIIC_HUB_BASE=${base_directory}" | sudo tee >> ${base_directory}/.bashrc

# add the password to the end of dbConnection.properties
cat ${base_directory}/properties/dbConnection.properties | grep -v dbPassword > /tmp/dbTemp
sudo rm ${base_directory}/properties/dbConnection.properties
sudo mv /tmp/dbTemp  ${base_directory}/properties/dbConnection.properties
sudo echo "dbPassword 	= 	${swifiic_pass}" >> ${base_directory}/properties/dbConnection.properties

sudo mkdir ${base_directory}/log ${base_directory}/pid


mysql -u ${root_login} -p${root_pass} -e "source ${base_directory}/scripts/initialSchema.sql"
mysql -u ${root_login} -p${root_pass} -e "CREATE USER 'swifiic'@'localhost' IDENTIFIED BY '${swifiic_pass}' ; GRANT ALL PRIVILEGES ON swifiic.* TO 'swifiic'@'localhost';"

sudo chown -R swifiic:swifiic ${base_directory}
sudo chmod 775 ${base_directory}

sudo mv /etc/ibrdtn/ibrdtnd.conf /etc/ibrdtn/ibrdtnd.conf.orig.$(date +%Y%m%d_%H%M%S)
echo "local_uri = dtn://${deploy_code}.dtn" | sudo tee /etc/ibrdtn/ibrdtnd.conf
sudo cat >> /etc/ibrdtn/ibrdtnd.conf << "EOF"
logfile = /var/log/ibrdtn/ibrdtn.log
fragmentation = yes
stats_traffic = yes
blob_path = /tmp
storage_path = /var/spool/ibrdtn/bundles
discovery_address = ff02::142 224.0.0.142
discovery_announce = 1
discovery_crosslayer = yes
net_interfaces = wlan0
net_rebind = yes
net_autoconnect = 3600
net_internet = eth0
tcp_idle_timeout = 0
p2p_ctrlpath = /var/run/wpa_supplicant/wlan0
routing = epidemic
routing_forwarding = yes
net_wlan0_type = tcp # we want to use TCP as protocol
net_wlan0_interface = wlan0 # listen on interface eth0
net_wlan0_port = 4556
EOF
sudo mkdir -p /var/spool/ibrdtn/bundles
sudo chmod 777 /var/spool/ibrdtn/bundles  

sudo mkdir -p /var/log/msngr /var/log/suta /var/log/soa
sudo chmod 777 /var/log/msngr /var/log/suta /var/log/soa
# cleanup the temp folder
rm -rf /tmp/deploy

echo "======== Completed The SWiFiIC setup under ${base_directory}====="
echo
exit

