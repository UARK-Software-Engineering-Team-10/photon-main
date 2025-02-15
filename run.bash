#!/bin/bash -e

source /etc/profile.d/maven.sh
mvn clean package
#mvn dependency:copy-dependencies
cd target
java -jar photon.jar

echo "Press enter to close"
read