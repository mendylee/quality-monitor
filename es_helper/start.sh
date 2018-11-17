#!/bin/bash
function UpdateCrontab()
{
  #delete old config
  username=`whoami`
  crontab -l > /tmp/logtaskcronfile.${username}.1
  sed /es_remove_log/d /tmp/logtaskcronfile.${username}.1 > /tmp/logtaskcronfile.${username}
  crontab -r
  
  echo "00 2 * * * python /opt/deploy/task/es_remove_log.py >>/opt/deploy/task/remove_task.log 2>&1" >> /tmp/logtaskcronfile.${username}
  
  crontab /tmp/logtaskcronfile.${username}
  rm -rf /tmp/logtaskcronfile.${username}*
}

UpdateCrontab
