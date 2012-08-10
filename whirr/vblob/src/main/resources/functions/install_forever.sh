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
function install_forever() {
  # version 0.9.2 is compatible with node 0.6.10
  local VERSION=${1:-0.9.2}
  export PATH=/usr/local/bin:$PATH
  
  if which rpm &> /dev/null; then
    yum --nogpgcheck -y install gcc-c++
  fi
  
  npm install forever@${VERSION} -g --quiet -y
  return $?
}