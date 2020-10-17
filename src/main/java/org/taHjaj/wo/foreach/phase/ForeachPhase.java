package org.taHjaj.wo.foreach.phase;

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

import org.apache.maven.project.MavenProject;
import org.taHjaj.wo.foreach.exceptions.ForeachExecutionException;
import org.taHjaj.wo.foreach.exceptions.ForeachFailureException;
import org.taHjaj.wo.foreach.ForeachResult;
import org.taHjaj.wo.foreach.descriptor.ForeachDescriptor;
import org.taHjaj.wo.foreach.env.ForeachEnvironment;

import java.util.List;

/**
 * A phase in the release cycle.
 *
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 */
public interface ForeachPhase
{
    /**
     * Execute the phase.
     *
     * @param releaseDescriptor the configuration to use
     * @param releaseEnvironment the environmental configuration, such as Maven settings, Maven home, etc.
     * @param reactorProjects   the reactor projects
     * @throws ForeachExecutionException an exception during the execution of the phase
     * @throws ForeachFailureException   a failure during the execution of the phase
     * @return the release result
     */
    ForeachResult execute(ForeachDescriptor releaseDescriptor, ForeachEnvironment releaseEnvironment,
                          List<MavenProject> reactorProjects )
        throws ForeachExecutionException, ForeachFailureException;

    /**
     * Simulate the phase, but don't make any changes to the project.
     *
     * @param releaseDescriptor the configuration to use
     * @param releaseEnvironment the environmental configuration, such as Maven settings, Maven home, etc.
     * @param reactorProjects   the reactor projects
     * @throws ForeachExecutionException an exception during the execution of the phase
     * @throws ForeachFailureException   a failure during the execution of the phase
     * @return the release result
     */
    ForeachResult simulate(ForeachDescriptor releaseDescriptor, ForeachEnvironment releaseEnvironment,
                           List<MavenProject> reactorProjects )
        throws ForeachExecutionException, ForeachFailureException;

}
