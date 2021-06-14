#!/bin/bash

[[ -n "$DEBUG" ]] && set -x

dir=$(dirname $0)
appname=$(basename $0)

cd $dir

action=$1

if [ x$2 = x ]
then
  wait=n
else
  wait=y
  wait_seconds=$2
fi

#. $appname.conf
url=http://localhost:9080/acme
app=acme-app
java=/usr/bin/java

if [ "x$action" = x ]
then
  echo "start stop status"
  exit 1
fi

status() {
  curl -s $url/api/about | grep '1.0' >/dev/null 2>&1
  if [ $? -eq 0 ]
  then
    return 0
  else
    return 1
  fi
}

live_pid() {
  live=$(ps h -o pid,cmd -p $1)
  if [ $? -ne 0 ]
  then
    return 1
  fi
  echo $live | grep $app >/dev/null
  if [ $? -ne 0 ]
  then
    return 1
  fi
  return 0
}

wait_up() {
  if [ ! -f pid ]
  then
    return 1
  fi
  pid=$(cat pid)
  live_pid $pid
  if [ $? -ne 0 ]
  then
    return 1
  fi
  n=$wait_seconds
  while [ $n -ne 0 ]
  do
    status
    if [ $? -eq 0 ]
    then
      return 0
    fi
    sleep 1
    n=$(( $n - 1 ))
  done
  return 1
}

wait_down() {
  if [ ! -f pid ]
  then
    return 0
  fi
  pid=$(cat pid)
  live_pid $pid
  if [ $? -ne 0 ]
  then
    return 0
  fi
  n=$wait_seconds
  while [ $n -ne 0 ]
  do
    status
    if [ $? -eq 1 ]
    then
      live_pid $pid
      if [ $? -ne 0 ]
      then
        return 0
      fi
    fi
    sleep 1
    n=$(( $n - 1 ))
  done
  return 1
}

if [ $action = start ]
then
  if [ -f pid ]
  then
    pid=$(cat pid)
    live_pid $pid
    if [ $? -eq 0 ]
    then
      status
      if [ $? -eq 0 ]
      then
        echo "started"
        exit 0
      fi
      if [ $wait = y ]
      then
        wait_up
        if [ $? -eq 0 ]
        then
          echo "started"
          exit 0
        else
          echo "timeout"
          exit 1
        fi
      else
        echo "starting"
      fi
      exit 0
    fi
  fi
  cmd="$java -Djava.io.tmpdir=/tmp -Djava.awt.headless=true -Dsun.misc.URLClassPath.disableJarChecking=true -jar $app.jar"
  nohup $cmd >/dev/null 2>&1 &
  pid=$!
  echo $pid >pid
  if [ $wait = y ]
  then
    wait_up
    if [ $? -eq 0 ]
    then
      echo "started"
      exit 0
    else
      echo "timeout"
      exit 1
    fi
  else
    echo "starting"
  fi
  exit 0
fi

if [ $action = stop ]
then
  if [ ! -f pid ]
  then
    echo "stopped"
    exit 0
  fi
  pid=$(cat pid)
  live_pid $pid
  if [ $? -eq 0 ]
  then
    curl -X POST $url/actuator/shutdown >/dev/null 2>&1
  else
    echo "stopped"
    exit 0
  fi
  if [ $wait = y ]
  then
    wait_down
    if [ $? -eq 0 ]
    then
      echo "stopped"
      exit 0
    else
      live_pid $pid
      if [ $? -eq 0 ]
      then
        kill $pid
        echo "killed"
        sleep 5
        live_pid $pid
        if [ $? -eq 0 ]
        then
          exit 1
        else
          exit 0
        fi        
      fi
      echo "timeout"
      exit 1
    fi
  else
    echo "stopping"
  fi
  exit 0
fi

if [ $action = status ]
then
  if [ ! -f pid ]
  then
    echo "down"
    exit 0
  fi
  pid=$(cat pid)
  live_pid $pid
  if [ $? -eq 1 ]
  then
    echo "down"
    exit 0
  fi
  status
  if [ $? -eq 0 ]
  then
    echo up
  else
    echo down
  fi
  exit 0
fi

echo "start stop status"
exit 1
