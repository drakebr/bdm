#!/bin/bash

trap 'kill -TERM $PID' TERM INT
echo Options: $BDM_OPTS
java $BDM_OPTS -cp "/opt/bdm/lib/*" com.bdmplatform.Application /opt/bdm/template.conf &
PID=$!
wait $PID
trap - TERM INT
wait $PID
EXIT_STATUS=$?
