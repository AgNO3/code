#!/bin/bash

set -e

LIFETIME="1 year"
SIGNCERT="$HOME/.m2/license.pem"
SIGNKEY="$HOME/.m2/sign.key"

SUBJECT="$1"

if [ -z "$SUBJECT" ]
then
	echo "Usage $0: <subject> [<description> [<validity period>]]" >&2
	exit -1
fi

if [ ! -z "$2" ]
then
	DESCRIPTION="$2"
fi

if [ ! -z "$3" ]
then
	LIFETIME="$2"
fi

EXPDATE=$(date -d "$LIFETIME" +%FT%T%:::z)



TMPDIR=$(mktemp -d)

function cleanup() {
	if [ -d "$TMPDIR" ]
	then
		rm -Rf "$TMPDIR"
	fi
}
trap cleanup EXIT

LICID=$(uuidgen)



ISSUEDATE=$(date +%FT%T%:::z)

SERVICE_TYPES="urn:agno3:1.0:hostconfig,urn:agno3:1.0:orchestrator,urn:agno3:1.0:fileshare"

cat > $TMPDIR/license.properties <<EOD
id: $LICID
description: $DESCRIPTION
issued: $ISSUEDATE
expires: $EXPDATE
subject: $SUBJECT
types: $SERVICE_TYPES
EOD

cat >> $TMPDIR/license.properties

openssl cms -sign -nodetach -signer "$SIGNCERT" -inkey "$SIGNKEY" $CERTSFILEOPT -in "$TMPDIR/license.properties" -out "$TMPDIR/license.lic" -outform PEM -binary

cat "$TMPDIR/license.lic" | grep -v -- "----" | tr -d '\n'

echo >&2
echo "License data:" >&2

openssl cms -verify -noverify -in "$TMPDIR/license.lic" -inform PEM -binary -noout >&2
