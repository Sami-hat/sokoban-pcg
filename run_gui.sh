#!/usr/bin/env bash

# Compile the Java code
javac -d out $(find src -name "*.java")

# Run the GUI
java -cp out GUI
