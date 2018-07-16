#!/bin/sh
#
# Description: Minecraft Music Extractor
#
# Original version from https://minecraft.gamepedia.com/Tutorials/Sound_directory
#

USER=`whoami`
VERSION="1.13"
PYTHONEXE=python

# Usage via --help
if [ "$1" == "--help" ]
then
  echo "Usage: $0 [--help] [OUTPUT_DIR] [VERSION] [ASSETS_DIR]"
  echo "Extract minecraft audio files"
  echo ""
  echo "Example: $0 /tmp/minecraftaudio 1.13"
  exit 0
fi

# check for test mode, which skips actual extract of files
TEST=""
if [ "$1" == "--test" ]
then
  echo "Testing mode enabled!"
  TEST=echo
  shift 1
fi

# determine platform
# from https://stackoverflow.com/questions/3466166/how-to-check-if-running-in-cygwin-mac-or-linux
UNAME="$(/bin/uname -s)"
case "${UNAME}" in
  Linux*)     PLATFORM=Linux;;
  Darwin*)    PLATFORM=Mac;;
  CYGWIN*)    PLATFORM=Cygwin;;
  MINGW*)     PLATFORM=MinGw;;
  *)          PLATFORM="UNKNOWN:${unameOut}"
esac

# set default paths based on platform
if [ "${PLATFORM}" == "Cygwin" ]
then
  ASSETS_DIR="/cygdrive/c/Users/${USER}/AppData/Roaming/.minecraft/assets"
  OUTPUT_DIR="/cygdrive/c/Users/${USER}/tmp/minecraft_audio"
elif [ "${PLATFORM}" == "Mac" ]
then
  ASSETS_DIR="/Users/${USER}/Library/Application Support/minecraft/assets"
  OUTPUT_DIR="/Users/${USER}/Desktop/minecraft_audio"
elif [ "${PLATFORM}" == "Linux" ]
then	
  ASSETS_DIR="${HOME}/.minecraft/assets"
  OUTPUT_DIR="${HOME}/tmp/minecraft_audio"
elif [ "${PLATFORM}" = "MinGw" ]
then
  ASSETS_DIR="/c/Users/${USER}/AppData/Roaming/.minecraft/assets"
  OUTPUT_DIR="/c/Users/${USER}/tmp/minecraft_audio"
else
  echo Unknown OS type, unable to set default file locations
fi

# find python
which "${PYTHONEXE}" >& /dev/null
if [ $? -ne 0 ]
then
  if [ "${PYTHON}" != "" -a -x "${PYTHON}" ]
  then
    PYTHONEXE="${PYTHON}"
  else
    echo "Requires ${PYTHONEXE}, which was not found in path or PYTHON env variable!"
    exit 4
  fi
fi

# get options
if [ $# -ge 1 ]
then
  OUTPUT_DIR="${1}"
fi

if [ $# -ge 2 ]
then
  VERSION="${2}"
fi

if [ $# -ge 3 ]
then
  ASSETS_DIR="${3}"
fi

JSON_FILE="${ASSETS_DIR}/indexes/${VERSION}.json"

# check if needed dirs and files exist
if [ ! -d "${OUTPUT_DIR}" ]
then
  echo "OUTPUT_DIR \"${OUTPUT_DIR}\" does not exist!"
  exit 1
fi

if [ ! -d "${ASSETS_DIR}" ]
then
  echo "ASSETS_DIR \"${ASSETS_DIR}\" does not exist!"
  exit 2
fi

if [ ! -f "${JSON_FILE}" ]
then
  echo "JSON_FILE \"${JSON_FILE}\" does not exist!"
  exit 3
fi

#for ENTRY in `cat "$JSON_FILE" | "${PYTHONEXE}" -c 'import sys,json; from pprint import pprint; data = json.load(sys.stdin); pprint(data);' | grep music | awk -F\' '{print $2 "," $6}'`
#cat "$JSON_FILE" | "${PYTHONEXE}" -c 'import sys,json; from pprint import pprint; data = json.load(sys.stdin); pprint(data);'
#cat "$JSON_FILE" | "${PYTHONEXE}" -c 'import sys,json; from pprint import pprint; data = json.load(sys.stdin); pprint(data);' | grep music

SOUNDS=sounds
#Limit to music and not all sounds:
#SOUNDS=music

# Extract!
for ENTRY in `cat "${JSON_FILE}" | "${PYTHONEXE}" -c 'import sys,json; from pprint import pprint; data = json.load(sys.stdin); pprint(data);' | grep ${SOUNDS} | awk -F\' '{print $2 "," $6}'`
do
  echo "Processing ${ENTRY}..."
  echo ${ENTRY} | cut -d, -f1 
  FILENAME=`echo ${ENTRY} | cut -d, -f1`
  FILEHASH=`echo ${ENTRY} | cut -d, -f2`

  #SUBDIR=`echo ${FILENAME} | sed -E 's/\/[a-z0-9]+\..+//'`
  SUBDIR=`dirname "${FILENAME}"`
  #Locate the file in the assets directory structure
  FULLPATH_HASHFILE=`find "${ASSETS_DIR}" -name "${FILEHASH}"`

  #Copy the file

  ${TEST} mkdir -p  "${OUTPUT_DIR}/${SUBDIR}"
  ${TEST} cp "${FULLPATH_HASHFILE}" "${OUTPUT_DIR}/${FILENAME}"
done
