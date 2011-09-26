#!/bin/bash

[[ -d $1 ]] || { echo "$0 <dirname>\n\t<dirname> must be specified.\nThis will be used as the root for searching for\n\t.classpath\nfiles to remove commons.lang 2.4 dependency." >&2; exit 1; }

find $1. -name .classpath  -type f -print | xargs sed -f cleanCommons.sed  -i ""
