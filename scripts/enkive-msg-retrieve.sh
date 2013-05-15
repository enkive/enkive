#!/bin/sh

# export ENKIVE_CONSOLE_LOGGING=full
export ENKIVE_MAIN=com.linuxbox.enkive.tool.mongodb.MongoDBMsgRetriever

if [ $# -ne 1 ] ;then
    echo "Usage: $0 document-id"
    exit 1
fi

docid=$1

sh $(dirname $0)/enkive-common.sh $docid
