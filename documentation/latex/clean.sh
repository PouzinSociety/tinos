#!/bin/bash

for i in aux log toc bbl blg
do
	find . -name "*.$i"  -type f | xargs rm -f
done

