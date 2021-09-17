#!/bin/bash

set -e

BASEDIR=$(dirname $0)/..

EXTRA_CLASSPATH=$(java-config -p log4j)

java -cp "$BASEDIR"/target/classes/:$EXTRA_CLASSPATH eu.agno3.runtime.security.password.internal.AhoCorasickDictionary $BASEDIR/target/password.dict "$@"
