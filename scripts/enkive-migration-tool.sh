#!/bin/sh

# export ENKIVE_CONSOLE_LOGGING=full
export ENKIVE_MAIN=com.linuxbox.enkive.tool.DbMigrationTool

. $(dirname $0)/enkive-common.sh

runIt
