#!/bin/bash

# configure-project.sh: Configures the "sdk.dir" and "ndk.dir" directories in
#                       `local.properties`
# ANDROID_HOME     - The location of the android SDK, stored in `AndroidAgent`
# ANDROID_NDK_HOME - The location of the android NDK, stored in `AndroidAgent`

FILE=local.properties

if test -f "$FILE"; then
    echo "${FILE} exists already, replacing existing sdk.dir and ndk.dir"
    sed -i -r "s!sdk.dir=(.*)!sdk.dir=${ANDROID_HOME}!g" $FILE
    sed -i -r "s!ndk.dir=(.*)!ndk.dir=${ANDROID_NDK_HOME}!g" $FILE
else
    echo "sdk.dir="$ANDROID_HOME >> local.properties
    echo "ndk.dir="$ANDROID_NDK_HOME >> local.properties
fi

echo "$ANDROID_HOME has been added as sdk.dir to ${FILE}"
echo "$ANDROID_NDK_HOME has been added as ndk.dir to ${FILE}"
