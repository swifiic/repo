# deb_packages for swifiic
  Package name: ***swiffic***
  
  **About:** The package will install following dependencies, required for swifiic setup:
  
    - [ ] tomcat8
    - [ ] mysql-server
    - [ ] openjdk-8-jdk
    - [ ] jre
 
## Installation
  1. Add the follwoing line in  
  > /etc/apt/sources.list 
  
  ``` https://github.com/aayush4vedi/deb_packages/raw/master ./```
  2. Update
  
  ``` sudo apt-get update && sudo apt-get dist-upgrade -y ```
  3. Install(root access required)
  
  ``` apt-get install swifiic ```


## Troubleshooting
```diff
- Error “E:Encountered a section with no Package: header, E:Problem with MergeList …….” ```


 # Fix it by this steps ..  


1- Clean up..
  * ```sudo apt-get clean ```
  * ```sudo apt-get autoclean ```
  * ```sudo apt-get purge ```
  * ```sudo apt-get autoremove -y```
  * ```sudo rm -fv /var/lib/apt/lists/* ```

2- Full update..
  * ``` sudo apt-get update ```
  * ``` sudo apt-get dist-upgrade -y ```

3. Reboot 
