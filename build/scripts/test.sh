#/bin/bash

# Create a Bundle project & associated Bundle Integration Test project
ruby create -n java -t $HOME/tmp -o org.pouzinsociety -a bundle
# Create a deployment plan
#ruby create -n proxy -t $HOME/tmp -o org.pouzinsociety -a plan
