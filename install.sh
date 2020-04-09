#!/bin/sh

mvn install:install-file \
    -Dfile=reach.jar \
    -DgroupId=org.gk \
    -DartifactId=reach \
    -Dversion=0.1.0 \
    -Dpackaging=jar

