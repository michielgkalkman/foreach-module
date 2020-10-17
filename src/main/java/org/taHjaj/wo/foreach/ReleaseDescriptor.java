package org.taHjaj.wo.foreach;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.util.List;

/**
 *
 * @author Robert Scholte
 */
public interface ReleaseDescriptor  {
    /**
     * Get the file name of the POM to pass to any executed Maven process.
     *
     * @return String
     */
    String getPomFileName();

    /**
     * Get additional arguments to pass to any executed Maven process.
     *
     * @return String
     */
    String getAdditionalArguments();

    /**
     * Get whether the release process is interactive and the release manager should be prompted to confirm values, or
     * whether the defaults are used regardless.
     *
     * @return boolean
     */
    boolean isInteractive();

    /**
     * Get the goals to execute in perform phase for the release.
     *
     * @return String
     */
    String getGoals();

    void setWorkingDirectory(String workingDirectory);

    void setActivateProfiles(List<String> profiles);
}
