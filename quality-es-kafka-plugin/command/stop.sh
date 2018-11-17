#!/bin/bash
INSTALL_PATH=/opt/deploy/quality-es-kafka-plugin

CURR_PATH=`pwd`
appRemoveCrontab()
{
  username=`whoami`
  crontab -l > /tmp/cronfile.${username}.1
  sed /qualityLogMonitor/d /tmp/cronfile.${username}.1 > /tmp/cronfile.${username}
  
  crontab -r
  
  crontab /tmp/cronfile.${username}
  rm -rf /tmp/cronfile.${username}*
}

appRemoveCrontab
PROCNAME="quality-es-kafka-plugin"
stop_num=0
app_pid=$(ps -eo pid,cmd|grep -w ${PROCNAME}|grep -vw grep | sed -n '1p' | awk '{print $1;}')
if [ -z ${app_pid} ]; then
  app_pid=0
fi

while [ ${app_pid} -gt 0 ]; do
  echo "> Find ${PROCNAME} thread id:${app_pid}"
  kill -9 ${app_pid}
  sleep 1

  app_pid=$(ps -eo pid,cmd|grep -w ${PROCNAME}|grep -vw grep | sed -n '1p' | awk '{print $1;}')
  if [ -z ${app_pid} ]; then
    app_pid=0
  fi
done

echo "> Stop ${PROCNAME} successfully! "
