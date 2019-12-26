#!/bin/bash

#nohup java -Dspring.config.location=/home/terraspin/opsmx/app/config/application.properties  -jar /home/terraspin/artifact/TerraSpin.jar > /home/terraspin/artifact/terraspin.log 2>&1 &

#java -jar /home/terraspin/artifact/TerraSpin.jar --application.iscontainer.env=true
java -jar /home/terraspin/artifact/TerraSpin.jar

#  For Debugging, Docker should alive! Uncommment while portation to keep containe live
#while :; do echo '*print*'; sleep 5; done






