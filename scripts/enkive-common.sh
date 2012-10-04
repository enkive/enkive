#!/bin/sh

# This script contains common code that all scripts can use. The
# calling script should export the environment variable ENKIVE_MAIN
# containing the fully qualified class that contains the static main
# method. Optionally the calling script can export
# ENKIVE_CONSOLE_LOGGING with the value "full" if the script wants full
# console logging; otherwise only ERROR and FATAL messages are sent to
# the console. Finally the script, if it's also located in the
# "scripts" sub-directory, can run this script with the following:
# 
#     sh $(dirname $0)/enkive-common.sh


# VARIABLES YOU MAY WANT TO CHANGE

# path to top level of the Java JDK (must be a JDK and not a JRE); should contain "bin", "lib", and "lib/tools.jar"
JAVA_HOME=/opt/java

# path to the top level of enkive
ENKIVE_HOME=/opt/enkive

# path to directory that contains file "libindri_jni.so"
INDRI_SO_PATH=/usr/local/lib


# TESTING OF THE ABOVE

errors=0

if [ ! -f ${JAVA_HOME}/bin/javac -o ! -f ${JAVA_HOME}/lib/tools.jar ] ;then
	echo JAVA_HOME is likely set incorrectly.
	errors=1
fi

if [ ! -d ${ENKIVE_HOME}/config -o ! -d ${ENKIVE_HOME}/lib ] ;then
	echo ENKIVE_HOME is likely set incorrectly.
	errors=1
fi

if [ ! -f ${INDRI_SO_PATH}/libindri_jni.so ] ;then
	echo INDRI_SO_PATH is likely set incorrectly.
	errors=1
fi

if [ ${errors} -eq 1 ] ;then
	echo "Exiting due to error(s)."
	exit 1
fi


# VARIABLES BUILT FROM THE ABOVE; likely no need to alter

ENKIVE_LOG_PATH=${ENKIVE_HOME}/data/logs
ENKIVE_CLASSPATH=${JAVA_HOME}/lib/tools.jar:${ENKIVE_HOME}/build/enkive.jar:${ENKIVE_HOME}/lib/*:${ENKIVE_HOME}/lib/spring/*:${ENKIVE_HOME}/lib/james-imap/*:${ENKIVE_HOME}/config

if [ "${ENKIVE_CONSOLE_LOGGING}" = "full" ] ;then
	LOG4J_CONFIG=file://${ENKIVE_HOME}/config/log4j.properties
else
	LOG4J_CONFIG=file://${ENKIVE_HOME}/config/log4j-essential.properties
fi


# LET'S DO IT!

java -cp ${ENKIVE_CLASSPATH} \
	-Dlog4j.configuration=${LOG4J_CONFIG} \
	-Djava.library.path=${INDRI_SO_PATH} \
	${ENKIVE_MAIN} $*
