#!/usr/bin/env python3
#
# Copyright 2025, The Android Open Source Project
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

import re
import sys
import os

RELNOTE_REQUIRED_MSG = """
Commit to unbundled repositories must contain the `Relnote:` tag.  It must
match the regex:

    %s

The Relnote: stanza is free-form and should describe what developers need to
know about your change.  If the change isn't meaningful externally, you
can set the Relnote: stanza to be N/A for the commit to not be included
in release notes.

For multiline release notes, you need to include a starting and closing quote. For example:

Relnote: "Added a new API `Class#getSize` to get the size of the class.
    This is useful if you need to know the size of the class."
"""


def main():
  """ Makes sure that the commit message given as first argument contains the `Relnote:` tag.
  """
  desc = sys.argv[1]

  field = "Relnote"
  regex = rf"^{field}: .+$"
  check_re = re.compile(regex, re.IGNORECASE)

  found = []
  for line in desc.splitlines():
      if check_re.match(line):
          found.append(line)

  if not found:
      print(RELNOTE_REQUIRED_MSG % (regex))
      sys.exit(1)
  else:
      return None

if __name__ == "__main__":
    main()
