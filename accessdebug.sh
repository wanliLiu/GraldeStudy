#!/usr/bin/env bash

echo "开始进入Gradle groovy的调试模式"

./gradlew assembleDebug -Dorg.greadle.daemon=false -Dorg.gradle.debug=true