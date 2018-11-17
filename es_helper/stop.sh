#!/bin/bash
RemoveCrontab()
{
  username=`whoami`
  crontab -l > /tmp/logtaskcronfile.${username}.1
  sed /es_remove_log/d /tmp/logtaskcronfile.${username}.1 > /tmp/logtaskcronfile.${username}
  
  crontab -r
  
  crontab /tmp/logtaskcronfile.${username}
  rm -rf /tmp/logtaskcronfile.${username}*
}

RemoveCrontab
