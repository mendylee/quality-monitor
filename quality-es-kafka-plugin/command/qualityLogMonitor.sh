#!/bin/bash
INSTALL_PATH=/opt/deploy/quality-es-kafka-plugin

PROCNAME_LIST="quality-es-kafka-plugin"
for PROCNAME in $PROCNAME_LIST;
do
let PROCNUM=$(ps -eo uid,pid,cmd|grep -w ${PROCNAME}|grep -vw grep |wc -l)
if [ "$PROCNUM" -le "0" ]; then
  case  ${PROCNAME} in
    "quality-es-kafka-plugin") ${INSTALL_PATH}/start.sh;;
  esac
else
  echo "> $PROCNAME already start, do nothing ... "
fi
done
