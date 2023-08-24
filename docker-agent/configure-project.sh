#!/bin/bash

# configure-project.sh: Configures the "sdk.dir" and "ndk.dir" directories in `local.properties`

FILE=local.properties

if test -f "$FILE"; then
    echo "${FILE} exists already, replacing existing sdk.dir and android.ndkPath"
    sed -i -r "s!sdk.dir=(.*)!sdk.dir=${ANDROID_HOME}!g" $FILE
    sed -i -r "s!android.ndkPath=(.*)!android.ndkPath=${ANDROID_NDK_HOME}!g" $FILE
else
    echo "sdk.dir="$ANDROID_HOME >> local.properties
    echo "android.ndkPath="$ANDROID_NDK_HOME >> local.properties
fi

echo "$ANDROID_HOME has been added as sdk.dir to ${FILE}"
echo "$ANDROID_NDK_HOME has been added as android.ndkPath to ${FILE}"
