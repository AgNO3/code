#!/bin/bash

MYPATH=$(dirname $0)

USER_LIMIT=20
SUBJECT="$1"
DESCRIPTION="Free ($USER_LIMIT Users)"

if [ -z "$SUBJECT" ]
then
	echo "Usage $0: <subject> [<user_limit> [<validity period>]]" >&2
	exit -1
fi

if [ ! -z "$2" ]
then
	USER_LIMIT="$2"
fi

echo "limit.totalUsers: $USER_LIMIT" | $MYPATH/make-license.sh "$SUBJECT" "$DESCRIPTION" "$3"
