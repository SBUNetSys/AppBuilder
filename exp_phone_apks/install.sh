#!/bin/bash
for f in `ls`
do
    adb -d install $f
done