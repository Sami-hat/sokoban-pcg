#!/usr/bin/env bash

# Exit if any command fails
set -e

javac -d out $(find src -name "*.java")

# Set number of iterations
for i in {1..250}; do
    echo "Running generation, i = $i"
    java -cp out Generator
    echo "------------------------------------------------"
done
