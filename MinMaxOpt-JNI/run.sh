#!/bin/bash

# Set directories
SRC_DIR="src"
NATIVE_DIR="native"
BUILD_DIR="build"
LIB_NAME="libMinMaxJNI.so"
CLASS_NAME="MinMaxJNI"
HEADER_FILE="$NATIVE_DIR/$CLASS_NAME.h"
TEXT_FILES_DIR="challenge-1"

# Ensure build directory exists
mkdir -p $BUILD_DIR

# Clean old files
echo "Cleaning old files..."
rm -f $BUILD_DIR/$LIB_NAME $HEADER_FILE $SRC_DIR/*.class

# Step 1: Compile Java file and generate JNI header
echo "Compiling Java file and generating JNI header..."
javac -h $NATIVE_DIR $SRC_DIR/$CLASS_NAME.java -d $BUILD_DIR
if [ $? -ne 0 ]; then
    echo "Java compilation failed."
    exit 1
fi

# Check if JNI header is generated
if [ ! -f "$HEADER_FILE" ]; then
    echo "JNI header file not found!"
    exit 1
fi

# Step 2: Compile C++ code into shared library
echo "Compiling C++ code..."
g++ -std=c++17 -fPIC -shared -o $BUILD_DIR/$LIB_NAME \
    $NATIVE_DIR/$CLASS_NAME.cpp \
    -I"$JAVA_HOME/include" -I"$JAVA_HOME/include/linux" \
    -mavx512f -mavx512bw

if [ $? -ne 0 ]; then
    echo "C++ compilation failed."
    exit 1
fi

# Step 3: Run Java program with the native library
echo "Running Java program..."
export LD_LIBRARY_PATH=$BUILD_DIR:$LD_LIBRARY_PATH
java -Djava.library.path=$BUILD_DIR -cp $BUILD_DIR $CLASS_NAME $TEXT_FILES_DIR

if [ $? -ne 0 ]; then
    echo "Java execution failed."
    exit 1
fi

echo "Execution completed successfully."
