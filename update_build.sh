#!/bin/bash
sed -i '' '/runtimeOnly("com.mysql:mysql-connector-j")/a\
\implementation("com.google.cloud.sql:mysql-socket-factory-connector-j-8:1.15.2")' build.gradle.kts
