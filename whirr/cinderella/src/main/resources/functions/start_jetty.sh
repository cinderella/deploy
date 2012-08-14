#
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

function with_backoff {
  local max_attempts=${ATTEMPTS-40}
  local timeout=${TIMEOUT-2}
  local attempt=0
  local exitCode=0

  echo -n "awaiting success of ($@) "
  while (( $attempt < $max_attempts ))
  do
    set +e
    "$@" 2>/dev/null >/dev/null
    exitCode=$?
    set -e

    if [[ $exitCode == 0 ]]
    then
      break
    fi

    echo -n "."
    sleep $timeout
    attempt=$(( attempt + 1 ))
  done

  if [[ $exitCode != 0 ]]
  then
    echo " Timeout!"
  else
    echo " Done"
  fi

  return $exitCode
}

function start_jetty() {
  export JETTY_HOME=$1
  export JETTY_PORT=$2
  export JETTY_USER=$3
  
  export JAVA_OPTIONS="-Xms256m -Xmx512m -XX:PermSize=64m -XX:MaxPermSize=128m -verbose:gc"
  
  ulimit -n 4096 &&
  cd $JETTY_HOME &&
  rm -rf contexts* &&
  ./bin/jetty.sh start &&
  with_backoff curl http://localhost:${JETTY_PORT}/
  return $?
}
