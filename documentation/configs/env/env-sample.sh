# Development Environment Settings
export DEV_HOME=$HOME/work/development
export GIT_HOME=$HOME/github/tinos

# Java JDK/JRE (OSX)
export JAVA_HOME=/Library/Java/Home

# Ruby Settings
export RUBYOPT=rubygems

# Ant Settings
export ANT_HOME=$DEV_HOME/build-tools/apache-ant-1.7.1
export ANT_OPTS="-Xms64m -Xmx512m -XX:PermSize=128m -XX:MaxPermSize=756m"
export ANT_EXEC=$ANT_HOME/bin

# Setup Path(in the document)
export PATH=$JAVA_HOME/bin:$ANT_EXEC:$PATH

# Alias
export EDITOR=vim
alias vi='vim'
