#!/bin/bash
CURR_PATH=`pwd`

function appUpdateCrontab()
{
  #delete old config
  tmpname=qualitycronfile
  username=`whoami`
  crontab -l > /tmp/tmpname.${username}.1
  sed /qualityLogMonitor/d /tmp/tmpname.${username}.1 > /tmp/tmpname.${username}
  crontab -r
  
  echo "* * * * * bash ${INSTALL_PATH}/qualityLogMonitor.sh >/dev/null 2>&1" >> /tmp/tmpname.${username}
  
  crontab /tmp/tmpname.${username}
  rm -rf /tmp/tmpname.${username}*
}

INSTALL_PATH=/opt/deploy/quality-es-kafka-plugin
cd ${INSTALL_PATH}
PROCNAME="quality-es-kafka-plugin"
PARAMER=-Dfile.encoding=utf-8
JAVA_MEM='-Xmx2048m -Xms1024m -Xmn712m'
JAVA_HOME=jre
JAVA_FILE=quality-es-kafka-plugin-1.0.0-SNAPSHOT.jar

if [ ! -x ${JAVA_HOME}/bin/java ]; then
    chmod +x ${JAVA_HOME}/bin/*
fi

let start_num=0
let MAX_TRY_COUNT=3
app_pid=`ps -eo pid,cmd | grep ${PROCNAME} | grep java | grep -v grep | sed -n '1p' | awk '{print $1}'`

if [ ! -z ${app_pid} ]; then
  echo "> ${PROCNAME} application already started, please stop it first!"
  exit 0
fi

while [ -z ${app_pid} ]; do
  ${JAVA_HOME}/bin/java ${PARAMER} ${JAVA_MEM} -jar ${JAVA_FILE} >/dev/null 2>&1 &
  sleep 1
  
  app_pid=`ps -eo pid,cmd | grep ${PROCNAME} | grep java | grep -v grep | sed -n '1p' | awk '{print $1}'`
  let start_num=$((${start_num} + 1))
  
  if [ ! -z ${app_pid} ]; then
    echo "> Start ${PROCNAME} successfully!"
  else
    echo "> Start ${PROCNAME} failure, try ${start_num} already !"
    if [ ${start_num} -gt ${MAX_TRY_COUNT} ]; then
      echo "> Exceed the ${MAX_TRY_COUNT} times, start  failure!"
      break;
    fi
  fi

done

appUpdateCrontab

cd ${CURR_PATH}
