# Development Toolchain Location
export DEV_HOME=$HOME/development

# TINOS Git Repository
export GIT_HOME=$HOME/github/tinos

# Java JDK/JRE (OSX)
export JAVA_HOME=/Library/Java/Home

# Virgo 
export SERVER_HOME=$DEV_HOME/springsource/virgo-web-server-2.1.0.M01
export SERVER_EXEC=$SERVER_HOME/bin

# STS
export STS_HOME=$DEV_HOME/springsource/sts-2.3.2.RELEASE

# Ruby Settings
export RUBYOPT=rubygems

# Ant Settings
export ANT_HOME=$DEV_HOME/build-tools/apache-ant-1.7.1
export ANT_OPTS="-Xms64m -Xmx512m -XX:PermSize=128m -XX:MaxPermSize=756m"
export ANT_EXEC=$ANT_HOME/bin

# Findbugs
export FINDBUGS_HOME=$DEV_HOME/build-tools/findbugs-1.3.9

# Setup Path
export PATH=$JAVA_HOME/bin:$ANT_EXEC:$SERVER_EXEC:$STS_HOME:$FINDBUGS_HOME:$PATH

# Alias
export EDITOR=vim
alias vi='vim'
