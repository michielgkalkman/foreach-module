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

import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.util.Map;

/**
 * Run the integration tests for the project to verify that it builds before committing.
 *
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 */
public abstract class AbstractRunGoalsPhase
    extends AbstractReleasePhase
{
    /**
     * Component to assist in executing Maven.
     */
    @Requirement( role = MavenExecutor.class )
    private Map<String, MavenExecutor> mavenExecutors;

    public ReleaseResult execute(ForeachDescriptor releaseDescriptor, ReleaseEnvironment releaseEnvironment,
                                 File workingDirectory, String additionalArguments )
        throws ReleaseExecutionException
    {
        ReleaseResult result = new ReleaseResult();

        try
        {
            String goals = getGoals( releaseDescriptor );
            if ( !StringUtils.isEmpty( goals ) )
            {
                logInfo( result, "Executing goals '" + goals + "'..." );

                final String mavenExecutorId = releaseEnvironment.getMavenExecutorId();
                MavenExecutor mavenExecutor = mavenExecutors.get( mavenExecutorId);

                if ( mavenExecutor == null )
                {
                    throw new ReleaseExecutionException(
                        "Cannot find Maven executor with id: "
                            + mavenExecutorId
                            + ", options are: "
                            + mavenExecutors.keySet()
                    );
                }

                File executionRoot;
                String pomFileName;
                final String pomFileName1 = releaseDescriptor.getPomFileName();
                if ( pomFileName1 != null )
                {
                    File rootPom = new File( workingDirectory, pomFileName1);
                    executionRoot = rootPom.getParentFile();
                    pomFileName = rootPom.getName();
                }
                else
                {
                    executionRoot = workingDirectory;
                    pomFileName = null;
                }
                
                mavenExecutor.executeGoals( executionRoot, goals, releaseEnvironment,
                                            releaseDescriptor.isInteractive(), additionalArguments,
                                            pomFileName, result );
            }
        }
        catch ( MavenExecutorException e )
        {
            throw new ReleaseExecutionException( e.getMessage(), e );
        }

        result.setResultCode( ReleaseResult.SUCCESS );

        return result;
    }

    protected abstract String getGoals( ForeachDescriptor releaseDescriptor );

    protected String getAdditionalArguments( ForeachDescriptor releaseDescriptor )
    {
        StringBuilder builder = new StringBuilder();

        final String additionalArguments = releaseDescriptor.getAdditionalArguments();
        if ( additionalArguments != null )
        {
            builder.append(additionalArguments);
        }

        return builder.length() > 0 ? builder.toString().trim() : null;
    }

    /**
     * Determines the path of the working directory. By default, this is the
     * checkout directory. For some SCMs, the project root directory is not the
     * checkout directory itself, but a SCM-specific subdirectory.
     *
     * @param checkoutDirectory            The checkout directory as java.io.File
     * @param relativePathProjectDirectory The relative path of the project directory within the checkout
     *                                     directory or ""
     * @return The working directory
     */
    protected File determineWorkingDirectory( File checkoutDirectory, String relativePathProjectDirectory )
    {
        File workingDirectory = checkoutDirectory;

        if ( StringUtils.isNotEmpty( relativePathProjectDirectory ) )
        {
            workingDirectory = new File( checkoutDirectory, relativePathProjectDirectory );
        }

        return workingDirectory;
    }
}
