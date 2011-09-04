#!/bin/sh
git log|egrep -v "^commit" | egrep -v "^Author:"|egrep -v "^[ \t]*$"|egrep -v "^Date"
