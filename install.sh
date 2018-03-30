#!/usr/bin/env bash

echo "开始删除所有的安装"
./gradlew uninstallAll
echo "开始安装FullDemoDebug到设备"
./gradlew :app:clean installFullDemoDebug