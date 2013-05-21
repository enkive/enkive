#!/bin/sh

# export ENKIVE_CONSOLE_LOGGING=full
export ENKIVE_MAIN=com.linuxbox.enkive.tool.mongodb.MongoDbIndexManagerTool

. $(dirname $0)/enkive-common.sh

runIt
