package org.taHjaj.wo.foreach;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
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

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.plexus.components.cipher.PlexusCipherException;
import org.taHjaj.wo.foreach.descriptor.impl.ForeachDescriptorBuilder;
import org.taHjaj.wo.foreach.env.ForeachEnvironment;
import org.taHjaj.wo.foreach.env.impl.DefaultForeachEnvironment;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Goal which executes goals only on modules, not on aggregator poms.
 */
@Mojo(name = "foreach", aggregator = true, requiresDependencyResolution = ResolutionScope.COMPILE, defaultPhase = LifecyclePhase.VERIFY, threadSafe = true)
public final class ForeachModuleMojo
    extends AbstractMojo
{
    /**
     */
    @Parameter( defaultValue = "${basedir}", readonly = true, required = true )
    private File basedir;

    /**
     */
    @Parameter( defaultValue = "${project}", readonly = true, required = true )
    protected MavenProject project;

    /**
     * The {@code M2_HOME} parameter to use for forked Maven invocations.
     *
     * @since 2.0-beta-8
     */
    @Parameter( defaultValue = "${maven.home}" )
    private File mavenHome;

    /**
     * The {@code JAVA_HOME} parameter to use for forked Maven invocations.
     *
     * @since 2.0-beta-8
     */
    @Parameter( defaultValue = "${java.home}" )
    private File javaHome;

    /**
     * The command-line local repository directory in use for this build (if specified).
     *
     * @since 2.0-beta-8
     */
    @Parameter ( defaultValue = "${maven.repo.local}" )
    private File localRepoDirectory;

    /**
     * Role hint of the {@link MavenExecutor} implementation to use.
     *
     * @since 2.0-beta-8
     */
    @Parameter( defaultValue = "invoker", property = "mavenExecutorId" )
    private String mavenExecutorId;

    /**
     */
    @Parameter( defaultValue = "${settings}", readonly = true, required = true )
    private Settings settings;

    /**
     */
    @Parameter( defaultValue = "${reactorProjects}", readonly = true, required = true )
    private List<MavenProject> reactorProjects;

    /**
     * Goals to run as part of the preparation step, after transformation but before committing. Space delimited.
     */
    @Parameter( defaultValue = "clean verify", property = "foreach.goals" )
    private String goals;

    /**
     * The file name of the POM to execute any goals against. As of version 3.0.0, this defaults to the name of
     * POM file of the project being built.
     */
    @Parameter( property = "pomFileName", defaultValue = "${project.file.name}" )
    private String pomFileName;

    /**
     * @since 2.0
     */
    @Parameter(defaultValue = "${session}", readonly = true, required = true)
    private MavenSession session;

    /**
     * Additional arguments to pass to the Maven executions, separated by spaces.
     */
    @Parameter( property = "arguments" )
    private String arguments;

    public void execute()
        throws MojoExecutionException {

        // goals may be splitted into multiple line in configuration.
        // Let's build a single line command
        if ( goals != null )
        {
            goals = StringUtils.join( StringUtils.split( goals ), " " );
        }

        try
        {
            // Note that the working directory here is not the same as in the release configuration, so don't reuse that
            ForeachDescriptorBuilder releaseDescriptor = createReleaseDescriptor();

            releaseDescriptor.setGoals( goals );

            new GoalsRunner(getLog()).execute( releaseDescriptor.build(), getReleaseEnvironment(),
                    getReactorProjects());
        }
        catch (PlexusCipherException e )
        {
            throw new MojoExecutionException( e.getMessage(), e );
        }
    }

    /**
     * Gets the environment settings configured for this release.
     *
     * @return The release environment, never <code>null</code>.
     */
    protected ForeachEnvironment getReleaseEnvironment()
    {
        return new DefaultForeachEnvironment().setSettings( settings )
                .setJavaHome( javaHome )
                .setMavenHome( mavenHome )
                .setLocalRepositoryDirectory( localRepoDirectory )
                .setMavenExecutorId( mavenExecutorId );
    }

    /**
     * Creates the release descriptor from the various goal parameters.
     *
     * @return The release descriptor, never <code>null</code>.
     */
    protected ForeachDescriptorBuilder createReleaseDescriptor()
    {
        ForeachDescriptorBuilder descriptor = new ForeachDescriptorBuilder();

        Path workingDirectory = getCommonBasedir( reactorProjects );

        descriptor.setWorkingDirectory( workingDirectory.toFile().getAbsolutePath() );

        Path rootBasedir = basedir.toPath();
        if ( rootBasedir.equals( workingDirectory ) )
        {
            descriptor.setPomFileName( pomFileName );
        }
        else
        {
            descriptor.setPomFileName( workingDirectory.relativize( rootBasedir ).resolve( pomFileName ).toString() );
        }

        descriptor.setAdditionalArguments( this.arguments );

        List<String> profileIds = session.getRequest().getActiveProfiles();

        if ( !profileIds.isEmpty() )
        {
            List<String> profiles = new ArrayList<>( profileIds );

            descriptor.setActivateProfiles( profiles );
        }
        return descriptor;
    }

    static Path getCommonBasedir( List<MavenProject> reactorProjects )
    {
        Path basePath = reactorProjects.get( 0 ).getBasedir().toPath();

        for ( MavenProject reactorProject : reactorProjects )
        {
            Path matchPath = reactorProject.getBasedir().toPath();
            while ( !basePath.startsWith( matchPath ) )
            {
                matchPath = matchPath.getParent();
            }
            basePath = matchPath;
        }

        return basePath;
    }

    /**
     * Gets the list of projects in the build reactor.
     *
     * @return The list of reactor project, never <code>null</code>.
     */
    public List<MavenProject> getReactorProjects()
    {
        return reactorProjects;
    }
}
