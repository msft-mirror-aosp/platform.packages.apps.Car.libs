#!/usr/bin/env python3
#
# Copyright 2023, The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

import sys
import os

# List of folders that will be excluded from the style check.
EXCLUDED_FOLDERS = ['car-media-extensions']

def main():
  """ Runs checkstyle.py over all the folders under libs except the ones in EXCLUDED_FOLDERS.
  """
  repo_root = sys.argv[1]
  sha = sys.argv[2]

  folders = ""
  for f in os.listdir():
    if os.path.isdir(f) and f not in EXCLUDED_FOLDERS:
      folders += f + " "

  check_style_script = repo_root + "/prebuilts/checkstyle/checkstyle.py"
  command = check_style_script + " --sha " + sha + " --file_whitelist " + folders
  status = os.system(command)

  if status != 0:
    sys.exit(1)

if __name__ == "__main__":
    main()
