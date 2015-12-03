#!/bin/bash
echo "Checking Whether Prerequisites are present"
echo "Checking for Java"
type java >/dev/null 2>&1 
if [ $? -ne 0 ] ; then
    echo "Java is not Present. sudo apt-get install openjdk-7-jdk"
    exit
else
    echo "Java is installed"
fi

# we may need a better check than this for tomcat7-user
echo "Checking for Tomcat User"
if [ -f /etc/init.d/tomcat7* ]; then
    echo " Tomcat is installed"
else 
    echo " Tomcat is not installed. Install using sudo apt-get install tomcat7-user"
exit
fi

echo "Checking for MySQL"
type mysql >/dev/null 2>&1 
if [ $? -ne 0 ] ; then
    echo "MySQL is not Present. sudo apt-get install mysql-server"
    exit
else
    echo "MySQL is installed"
fi

echo "Checking for Apache"
type apache2 >/dev/null 2>&1 
if [ $? -ne 0 ] ; then
    echo "Apache is not Present. Install using sudo apt-get install apache2 php5 phpmyadmin"
    exit
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
exit
fi
mkdir ./deploy
cd deploy
tar -zxvf ../dist.tar.gz
cd ./dist
read -p "Enter the base directory for SWIFiIC Installation Eg. /opt/swifiic : " base_directory
read -p "Enter login for MySQL for root access : " root_login
read -p "Enter password for MySQL for root access : " root_pass
read -p "Choose as  password for MySQL for SWiFiIC access : " swifiic_pass
read -p "Re-enter your choice for password for MySQL for SWiFiIC access : " swifiic_pass2

# todo - TODO - XXX base_directory should not be empty


# todo - TODO - XXX verify that the password for swifiic_pass and swifiic_pass2 are same


sudo mkdir -p ${base_directory}
if [ $? -ne 0 ] ; then
    echo "fatal"
    exit
else
    echo "Success Creating Directory"
fi
echo "Moving files to ${base_directory}"
sudo cp -R * ${base_directory}/
echo "Creating SOA HubServer"
cd ${base_directory}/
sudo tomcat7-instance-create -p 18080 -c 18005 HubSrvr
sudo mv hub/HubSrvr.war -d ${base_directory}/HubSrvr

echo "Copying Swifiic Base Hub"

export Install_Path=${base_directory}

echo "export SWIFIIC_HUB_BASE=${base_directory}" | sudo tee ${base_directory}/properties/setEnv.sh
sudo chmod 755 ${base_directory}/properties/setEnv.sh

# add the password to the end of dbConnection.properties
# dbPassword	=	aarthi
cat ${base_directory}/properties/dbConnection.properties | grep -v dbPassword > /tmp/dbTemp
sudo rm ${base_directory}/properties/dbConnection.properties
sudo mv /tmp/dbTemp  ${base_directory}/properties/dbConnection.properties
sudo echo "dbPassword 	= 	${swifiic_pass}" >> ${base_directory}/properties/dbConnection.properties


mysql -u ${root_login} -p${root_pass} -e "source ${base_directory}/scripts/initialSchema.sql"
mysql -u ${root_login} -p${root_pass} -e "CREATE USER 'swifiic'@'localhost' IDENTIFIED BY \'${swifiic_pass}\'; GRANT ALL PRIVILEGES ON swifiic.* TO 'swifiic'@'localhost';"


# XXX TODO grant access to "swifiic" and ${swifiic_pass} to the schema craeted above

exit

