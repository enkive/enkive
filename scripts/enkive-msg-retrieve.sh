#!/bin/sh

. $(dirname $0)/enkive-common.sh

# export ENKIVE_CONSOLE_LOGGING=full
export ENKIVE_MAIN=com.linuxbox.enkive.tool.mongodb.MongoDBMsgRetriever

if [ $# -ne 1 ] ;then
    echo "Usage: $0 message-id"
    exit 1
fi

messageid=$1

runIt $messageid
