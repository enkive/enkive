#!/bin/bash
#
# Description: Stress locking in Enkive
#
# Procedure:
#	- Create a scratch directory with $NUM_MESSAGES identical messages
#	- Run $NUM_IMPORTERS in parallel on the test dir
#	- Verify none of them crash
#

# Locations
SCRIPTDIR=$(dirname "$0")
echo $SCRIPTDIR
ENKIVE_TOP=$(dirname $(dirname $(dirname $(realpath ${SCRIPTDIR}))))
echo $ENKIVE_TOP

# Default arguments
NUM_MESSAGES=1000
NUM_CLIENTS=5
DATADIR="${SCRIPTDIR}/datadir"
HOST="localhost"
PORT=2526

# Set usage output
USAGE="[-h |--help] [-d | --dataset] [-c <num_clients> | --client-count=<num_clients>] [-m <num_messages> | --message-count=<num_messages>] [<datadir>]"
LONGUSAGE="\t-h, --help\n\t\tPrint this help message
\t-d, --dataset\n\t\tDon't run the test, create the dataset
\t-c <num_clients>, --client-count=<num_clients>\n\t\tUse <num_clients> clients (defaults to ${NUM_CLIENTS})
\t-m <num_messages>, --message-count=<num_messages>\n\t\tUse <num_messages> messages (defaults to ${NUM_MESSAGES})
\t<datadir>\n\t\tDirectory for dataset (defaults to ${DATADIR})"

# Standard functions
source ${ENKIVE_TOP}/scripts/enkive-common.sh

# Script name
ME=$(basename $0)

# Parse arguments
ARGS=`getopt -o hdc:m: --long help,dataset,client-count:,message-count: -n "${ME}" -- "$@"`

if [ $? != 0 ] ; then
	usage 
fi
eval set -- "$ARGS"

while true ; do
	case "$1" in
		-h|--help) usage; shift ;;
		-d|--dataset) DATASET="yes"; shift ;;
		-c|--client-count) NUM_CLIENTS=$2 ; shift 2 ;;
		-m|--message-count) NUM_MESSAGES=$2 ; shift 2 ;;
		--) shift ; break ;;
		* ) usage "Invalid argument $1";;
	esac
done

# Remaining arguments are in $1, $2, etc. as normal
if [ -n "$1" ]; then
	DATADIR="$1"
fi

DATADIR=$(realpath $DATADIR)
SCRIPTDIR=$(realpath $SCRIPTDIR)

# Make sure we're in the right directory
cd ${SCRIPTDIR}

if [ -n "${DATASET}" ]; then
	echo "Making dataset of size ${NUM_MESSAGES} in ${DATADIR}"
	rm -rf "${DATADIR}"
	mkdir -p "${DATADIR}"
	for i in $(seq -w 1 ${NUM_MESSAGES}); do
		cp "${SCRIPTDIR}/template.eml" "${DATADIR}/$i.eml"
	done
	exit;
fi

if [ ! -d "${DATADIR}" ]; then
	usage "No dataset; please run with --dataset"
fi

# export ENKIVE_CONSOLE_LOGGING=full
export ENKIVE_MAIN=com.linuxbox.enkive.importer.FileDirReader

PIDS=
for i in $(seq 1 ${NUM_CLIENTS}); do
	echo "Starting client $i..."
	runIt ${DATADIR} ${HOST} ${PORT} &
	PIDS="$! ${PIDS}"
done

FAIL=0

for pid in ${PIDS}; do
	wait $job || let "FAIL+=1"
done

if [ "$FAIL" == "0" ]; then
	echo "Test $ME succeeded"
else
	echo "Test $ME FAILED! ($FAIL)"
	exit 1
fi
