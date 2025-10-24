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

echo "------------------------------------------"
echo "Now installing NetBeans..."
echo "------------------------------------------"
# To install NetBeans you first need this:
sudo apt-get install -y snapd
# Then Install NetBeans using Snap
sudo snap install netbeans --classic

echo "------------------------------------------"
echo "All done! Java and NetBeans have been installed."
echo "If netbeans doesn't appear, restart the PC"
echo "------------------------------------------"
echo "Actualice java"
