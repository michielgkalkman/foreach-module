package org.taHjaj.wo;

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

import org.apache.maven.artifact.ArtifactUtils;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;
import org.apache.maven.shared.release.*;
import org.apache.maven.shared.release.config.ReleaseDescriptorBuilder;
import org.apache.maven.shared.release.config.ReleaseUtils;
import org.apache.maven.shared.release.env.DefaultReleaseEnvironment;
import org.apache.maven.shared.release.env.ReleaseEnvironment;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.plexus.components.cipher.PlexusCipherException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Goal which touches a timestamp file.
 *
 * @goal touch
 * 
 * @phase process-sources
 */
@Mojo(name = "foreach", aggregator = true, requiresDependencyResolution = ResolutionScope.COMPILE, defaultPhase = LifecyclePhase.VERIFY, threadSafe = true)
public class ForeachModuleMojo
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
     * Role hint of the {@link org.apache.maven.shared.release.exec.MavenExecutor} implementation to use.
     *
     * @since 2.0-beta-8
     */
    @Parameter( defaultValue = "invoker", property = "mavenExecutorId" )
    private String mavenExecutorId;

    /**
     * The role-hint for the {@link org.apache.maven.shared.release.strategy.Strategy}
     * implementation used to specify the phases per goal.
     *
     * @since 3.0.0
     * @see org.apache.maven.shared.release.strategies.DefaultStrategy
     */
    @Parameter( defaultValue = "default", property = "releaseStrategyId" )
    private String releaseStrategyId;

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
    @Parameter( defaultValue = "${session}", readonly = true, required = true )
    protected MavenSession session;

    /**
     * Additional arguments to pass to the Maven executions, separated by spaces.
     */
    @Parameter( alias = "prepareVerifyArgs", property = "arguments" )
    private String arguments;

    /**
     */
    @Component
    protected ReleaseManager releaseManager;

    public void execute()
        throws MojoExecutionException, MojoFailureException {
        reactorProjects.forEach( mavenProject -> {
            try {
                System.out.printf("mavenProject: %s%n", mavenProject.getBasedir().getCanonicalPath());
                getLog().info(String.format("mavenProject: %s%n", mavenProject.getBasedir().getCanonicalPath()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        // goals may be splitted into multiple line in configuration.
        // Let's build a single line command
        if ( goals != null )
        {
            goals = StringUtils.join( StringUtils.split( goals ), " " );
        }

        try
        {
            // Note that the working directory here is not the same as in the release configuration, so don't reuse that
            ReleaseDescriptorBuilder releaseDescriptor = createReleaseDescriptor();

            createGoals();
            releaseDescriptor.setPerformGoals( goals );

            ReleasePerformRequest performRequest  = new ReleasePerformRequest();
            performRequest.setReleaseDescriptorBuilder( releaseDescriptor );
            performRequest.setReleaseEnvironment( getReleaseEnvironment() );
            performRequest.setReactorProjects( getReactorProjects() );
            performRequest.setReleaseManagerListener( new DefaultReleaseManagerListener( getLog(), false ) );
            performRequest.setDryRun( false );

            new GoalsRunner(getLog()).execute(ReleaseUtils.buildReleaseDescriptor(releaseDescriptor), getReleaseEnvironment(),
                    getReactorProjects());
        }
        catch (ReleaseExecutionException | PlexusCipherException e )
        {
            throw new MojoExecutionException( e.getMessage(), e );
        }
    }

    /**
     * Gets the environment settings configured for this release.
     *
     * @return The release environment, never <code>null</code>.
     */
    protected ReleaseEnvironment getReleaseEnvironment()
    {
        return new DefaultReleaseEnvironment().setSettings( settings )
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
    protected ReleaseDescriptorBuilder createReleaseDescriptor()
    {
        ReleaseDescriptorBuilder descriptor = new ReleaseDescriptorBuilder();

        Path workingDirectory;
        try
        {
            workingDirectory = getCommonBasedir( reactorProjects );
        }
        catch ( IOException e )
        {
            throw new RuntimeException( e.getMessage() );
        }
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

        for ( MavenProject project : reactorProjects )
        {
            String versionlessKey = ArtifactUtils.versionlessKey( project.getGroupId(), project.getArtifactId() );
            descriptor.putOriginalVersion( versionlessKey, project.getVersion() );
        }

        descriptor.setAdditionalArguments( this.arguments );

        List<String> profileIds = session.getRequest().getActiveProfiles();
        String additionalProfiles = getAdditionalProfiles();

        if ( !profileIds.isEmpty() || StringUtils.isNotBlank( additionalProfiles ) )
        {
            List<String> profiles = new ArrayList<>( profileIds );

            if ( additionalProfiles != null )
            {
                profiles.addAll( Arrays.asList( additionalProfiles.split( "," ) ) );
            }

            descriptor.setActivateProfiles( profiles );
        }

        descriptor.setReleaseStrategyId( releaseStrategyId );

        return descriptor;
    }

    static Path getCommonBasedir( List<MavenProject> reactorProjects )
            throws IOException
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

    /** Just here so it may be overridden by StageReleaseMojo */
    void createGoals()
    {
        if ( goals == null )
        {
            // set default
            goals = "deploy";
            if ( project.getDistributionManagement() != null
                    && project.getDistributionManagement().getSite() != null )
            {
                goals += " site-deploy";
            }
        }
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

    /**
     * Gets the comma separated list of additional profiles for the release build.
     *
     * @return additional profiles to enable during release
     */
    protected String getAdditionalProfiles()
    {
        return null;
    }
}
