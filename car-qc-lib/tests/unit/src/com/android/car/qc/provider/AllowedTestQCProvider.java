/*
 * Copyright (C) 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.car.qc.provider;

import java.util.HashSet;
import java.util.Set;

public class AllowedTestQCProvider extends TestQCProvider {
    @Override
    protected Set<String> getAllowlistedPackages() {
        Set<String> allowlist = new HashSet<>();
        allowlist.add("com.android.car.qc.tests.unit");
        return allowlist;
    }
}