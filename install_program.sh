#!/bin/bash

# chmod +x install_java.sh
# Sudo ./install_java.sh

set -e

# Install Java
echo "------------------------------------------"
echo "Updating package list and installing Java..."
echo "------------------------------------------"
sudo apt-get install -y default-jre default-jdk

echo "------------------------------------------"
echo "Installation successful. Checking Java version..."
echo "------------------------------------------"
java -version
javac -version

#Yes, This only instals java, Lol.

echo "------------------------------------------"
echo "Executable ready!!"
echo "------------------------------------------"
echo "To use the .jar, you need to do so using the command:"
echo " java -jar /path/to/the/program/Paginas.jar "

