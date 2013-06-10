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
JAVA_HOME=${JAVA_HOME:-"/opt/java"}

# path to the top level of enkive
ENKIVE_HOME=${ENKIVE_HOME:-"/opt/enkive"}

# user id under which Enkive is run; a good practice is to create a user specifically to run Enkive (e.g., "enkive")
ENKIVE_USER=${ENKIVE_USER:-"enkive"}

# path to directory that Indri was installed in -- typically /usr/local or /opt/indri. Expect to find underneath this directory
# lib/libindri_jni.so and share/indri/indri.jar 
INDRI_INSTALL_PATH=${INDRI_INSTALL_PATH:-"/usr/local"}


# TESTING OF THE ABOVE

errors=0

if [ ! -f ${JAVA_HOME}/bin/javac -o ! -f ${JAVA_HOME}/lib/tools.jar ] ;then
	echo "JAVA_HOME (${JAVA_HOME}) is likely set incorrectly."
	errors=1
fi

if [ ! -d ${ENKIVE_HOME}/config -o ! -d ${ENKIVE_HOME}/lib ] ;then
	echo "ENKIVE_HOME (${ENKIVE_HOME}) is likely set incorrectly."
	errors=1
fi

INDRI_LIB_PATH=${INDRI_INSTALL_PATH}/lib
INDRI_SO_PATH=${INDRI_LIB_PATH}/libindri_jni.so
if [ ! -f ${INDRI_SO_PATH} ] ;then
	echo "INDRI_INSTALL_PATH (${INDRI_INSTALL_PATH}) is likely set incorrectly; could not find ${INDRI_SO_PATH} . "
	errors=1
fi

INDRI_JAR_PATH=${INDRI_INSTALL_PATH}/share/indri/indri.jar
if [ ! -f ${INDRI_JAR_PATH} ] ;then
	echo "INDRI_INSTALL_PATH (${INDRI_INSTALL_PATH}) is likely set incorrectly; could not find ${INDRI_JAR_PATH} . "
	errors=1
fi

ENKIVE_JAR="${ENKIVE_HOME}/build/enkive.jar"
if [ ! -e ${ENKIVE_JAR} ] ;then
	# check to see if it's under build since that's where it's placed during
	# developmental builds
    ENKIVE_JAR="${ENKIVE_HOME}/enkive.jar"
    if [ ! -e ${ENKIVE_JAR} ] ;then
        echo "Could not find \"enkive.jar\" in ${ENKIVE_HOME} or ${ENKIVE_HOME}/build ."
        errors=1
    fi
else
	echo "NOTE: Using developmental build in ${ENKIVE_JAR}."
fi


if grep --silent "^${ENKIVE_USER}:" /etc/passwd ;then
	:
else
	echo "ENKIVE_USER (${ENKIVE_USER}) is likely set incorrectly."
	errors=1
fi

if [ ${errors} -eq 1 ] ;then
	echo "Exiting due to error(s)."
	exit 1
fi


# VARIABLES BUILT FROM THE ABOVE; likely no need to alter

ENKIVE_LOG_PATH=${ENKIVE_HOME}/data/logs
ENKIVE_CLASSPATH=${JAVA_HOME}/lib/tools.jar:${ENKIVE_JAR}:${INDRI_JAR_PATH}:${ENKIVE_HOME}/lib/*:${ENKIVE_HOME}/lib/spring/*:${ENKIVE_HOME}/lib/james-imap/*:${ENKIVE_HOME}/config

if [ "${ENKIVE_CONSOLE_LOGGING}" = "full" ] ;then
	LOG4J_CONFIG=file://${ENKIVE_HOME}/config/log4j.properties
else
	LOG4J_CONFIG=file://${ENKIVE_HOME}/config/log4j-essential.properties
fi


makeAbsolute() {
	if which readlink >/dev/null ;then
		readlink -f $1
	elif which realpath >/dev/null ;then
		realpath $1
	else
		echo "Need access to either 'readlink' command or 'realpath' command. Neither found."
		exit 2
	fi
}

# if is_interactive; then echo "interactive" fi
#
# Check for an interactive shell
is_interactive() {
	case $- in
		*i*)
			# Don't die in interactive shells
			return 0
			;;
		*)
			return 1
			;;
	esac
}

# command | die "message"
#
# Print a message and exit with failure
die() {
	echo "Failed: $@"
	if ! is_interactive; then
		exit 1
	fi
}

# usage "You need to provide a frobnicator"
#
# Print a message and the usage for the current script and exit with failure.
usage() {
	local myusage;
	if [ -n "${USAGE}" ]; then
		myusage=${USAGE}
	else
		myusage="No usage given"
	fi
	if [ -n "$1" ]; then
		echo "$@"
	fi
	echo ""
	echo "Usage:"
	echo "`basename $0` ${myusage}"
	if [ -n "${LONGUSAGE}" ]; then
		echo -e "${LONGUSAGE}"
	fi
	exit 1
}


# LET'S DO IT!

runIt() {
	cd ${ENKIVE_HOME}

	echo "Note: you may need to authenticate as user \"${ENKIVE_USER}\"...."

	sudo -u ${ENKIVE_USER} ${JAVA_HOME}/bin/java -cp ${ENKIVE_CLASSPATH} \
		-Dlog4j.configuration=${LOG4J_CONFIG} \
		-Djava.library.path=${INDRI_LIB_PATH} \
		${ENKIVE_MAIN} $*
}
