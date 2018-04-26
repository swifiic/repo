sudo kill -9 $(ps aux | grep 'swifiic' | awk '{print $2}')
#sudo rm -rf /opt/swifiic/
ant
mv release.tar.gz scripts/
cd scripts
sudo ./simpleinstall.sh
