#!/bin/bash

for i in aux log toc
do
	find . -name "*.$i"  -type f | xargs rm -f
done

