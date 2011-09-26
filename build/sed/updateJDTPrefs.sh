#!/bin/bash

[[ -d $1 ]] || { echo "$0 <dirname>\n\t<dirname> must be specified.\nThis will be used as the root for searching for\n\torg.eclipse.jdt.core.prefs\nfiles to update the Java Compliance value." >&2; exit 1; }

find $1. -name org.eclipse.jdt.core.prefs  -type f -print | xargs sed -f jdtPrefsTo_1_6.sed  -i ""
