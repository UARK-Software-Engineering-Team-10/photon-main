#!/bin/bash -e

echo "Finding and deleting class files.."
find ./ -type f -name *.class -exec rm {} \;

echo "Compiling source code.."
javac *.java

echo "Running program."
java Main

echo "Program ended."
