#!/bin/bash

MYPATH=$(dirname $0)

SUBJECT="$1"
DESCRIPTION="Unlimited"

if [ -z "$SUBJECT" ]
then
	echo "Usage $0: <subject> [<validity period>]" >&2
	exit -1
fi

echo "limit.totalUsers: -1" | $MYPATH/make-license.sh "$SUBJECT" "$DESCRIPTION" "$2"
