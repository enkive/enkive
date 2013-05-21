#!/bin/sh

# export ENKIVE_CONSOLE_LOGGING=full
export ENKIVE_MAIN=com.linuxbox.enkive.importer.FileDirReader

if [ $# -eq 0 ] ;then
    echo "Usage: $0 email-directory [host [port]]"
    echo "    default host is localhost"
    echo "    default port is 2526"
    exit 1
fi

host=localhost
port=2526

directory=`makeAbsolute $1`

if [ $# -ge 2 ] ;then
    host="$2"
fi

if [ $# -ge 3 ] ;then
    port="$3"
fi

. $(dirname $0)/enkive-common.sh

runIt $directory $host $port
