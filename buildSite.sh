#!/bin/bash
rm -fR fakesite
mkdir fakesite
cp -R target/site/* fakesite
for module in `ls -d org.sat4j.*` ; do mv $module/target/site fakesite/$module ; done
