#!/usr/bin/env bash

echo "开始清楚Home:$HOME"
rm -rf $HOME/.gradle/caches
echo "开始清楚./gradle"
rm -rf .gradle
echo "开始清楚了哦"
./gradlew clean
echo "搞定了"