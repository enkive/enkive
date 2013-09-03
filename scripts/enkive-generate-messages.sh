#!/bin/sh

. $(dirname $0)/enkive-common.sh

# export ENKIVE_CONSOLE_LOGGING=full
export ENKIVE_MAIN=com.linuxbox.enkive.testing.messageGenerator.TestMessageSender

if [ $# -lt 2 ] ;then
    echo "Usage: $0 email-directory count [host [port]]"
    echo "    default host is localhost"
    echo "    default port is 2526"
    exit 1
fi

host=localhost
port=2526

directory=`makeAbsolute $1`
count=$2

if [ $# -ge 3 ] ;then
    host="$3"
fi

if [ $# -ge 4 ] ;then
    port="$4"
fi

runIt $host $port $directory $count
