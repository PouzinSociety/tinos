#/bin/bash

# Create a Bundle project & associated Bundle Integration Test project
ruby create -n proxy -t $HOME/tmp -o org.tssg -a bundle
# Create a deployment plan
ruby create -n proxy -t $HOME/tmp -o org.tssg -a plan
