# Copyright 2019 Google LLC
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
#!/bin/bash

set -e  # Exit immediately if a command exits with a non-zero status.
projects=("aab-simple" "analytics" "cookie-api"
    "install-api" "service" "storage-api" "urlless")

for p in ${projects[@]}; do
   echo
   echo
   echo Building $p
   echo "====================================================================="

   pushd $p > /dev/null  # Silent pushd
   ./gradlew $@ | sed "s@^@$p @"  # Prefix every line with directory
   code=${PIPESTATUS[0]}
   if [ "$code" -ne "0" ]; then
       exit $code
   fi
   popd > /dev/null  # Silent popd
done

echo
echo "All done"
