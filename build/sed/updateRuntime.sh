#!/bin/bash

[[ -d $1 ]] || { echo "$0 <dirname>\n\t<dirname> must be specified.\nThis will be used as the root for searching for\n\torg.eclipse.wst.common.project.facet.core.xml\nfiles to update the runtime value." >&2; exit 1; }

find $1. -name org.eclipse.wst.common.project.facet.core.xml  -type f -print | xargs sed -f virgoRuntime.sed  -i ""
