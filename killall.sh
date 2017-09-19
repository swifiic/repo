sudo kill -9 $(ps aux | grep 'swifiic' | awk '{print $2}')
