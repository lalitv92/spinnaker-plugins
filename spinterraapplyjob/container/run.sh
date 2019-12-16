#!/bin/bash

#nohup java -Dspring.config.location=/home/terraspin/opsmx/app/config/application.properties  -jar /home/terraspin/artifact/TerraSpin.jar > /home/terraspin/artifact/terraspin.log 2>&1 &

java -Dspring.config.location=/home/terraspin/opsmx/app/config/application.properties  -jar /home/terraspin/artifact/TerraSpin.jar

#tail -f /home/terraspin/artifact/terraspin.log &

#while :; do
#  sleep 100
#  # For Debugging, Docker should alive!
#done
echo "started terraspin apply service..."
