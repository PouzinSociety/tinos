#!/bin/bash

[[ -d $1 ]] || { echo "$0 <dirname>\n\t<dirname> must be specified.\nThis will be used as the root for searching for\n\tjava files to add suppress warnings" >&2; exit 1; }

find $1. -name test.java  -type f -print | xargs sed -f insertSuppress.sed  -i ""
