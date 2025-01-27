#!/bin/bash -e

mvn clean install
mvn dependency:copy-dependencies
cd target
java -jar photon-1.0-SNAPSHOT.jar

echo "Press enter to close"
read