package org.taHjaj.wo.foreach;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.plexus.components.cipher.PlexusCipherException;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GoalsRunner
        extends AbstractRunGoalsPhase {

    private final Map<String, MavenExecutor> mavenExecutors;

    public GoalsRunner(Log log) throws PlexusCipherException {
        enableLogging( new LogLogger(log));
        mavenExecutors = new HashMap<>();
        final InvokerMavenExecutor invokerMavenExecutor = new InvokerMavenExecutor();
        invokerMavenExecutor.enableLogging(getLogger());
        mavenExecutors.put( "invoker", invokerMavenExecutor);
    }

    @Override
    protected String getGoals( ReleaseDescriptor releaseDescriptor )
    {
        return releaseDescriptor.getGoals();
    }

    @Override
    public ReleaseResult execute(ReleaseDescriptor releaseDescriptor, ReleaseEnvironment releaseEnvironment,
                                 List<MavenProject> reactorProjects )
    {
        final String mavenExecutorId = releaseEnvironment.getMavenExecutorId();
        getLogger().info( String.format( "mavenexecutorid: %s%n", mavenExecutorId));

        reactorProjects.forEach( mavenProject -> {
            try {
                final List<String> modules = mavenProject.getModules();
                boolean fHasModules = !modules.isEmpty();

                if( fHasModules) {
                    getLogger().info(  String.format( "Project %s has modules - skipped%n", mavenProject.getBasedir().getCanonicalPath()));
                } else {
                    getLogger().info(  String.format( "Project %s has no modules - executing goald%n", mavenProject.getBasedir().getCanonicalPath()));
                    runLogic(releaseDescriptor, releaseEnvironment, mavenProject, false);
                }
            } catch (IOException | ReleaseExecutionException e) {
                e.printStackTrace();
            }
        });
        return null;
    }

    private ReleaseResult runLogic( ReleaseDescriptor releaseDescriptor, ReleaseEnvironment releaseEnvironment,
                                    MavenProject reactorProject, boolean simulate )
            throws ReleaseExecutionException
    {
        String pomFileName = releaseDescriptor.getPomFileName();
        String additionalArguments = getAdditionalArguments( releaseDescriptor );

        if ( pomFileName == null )
        {
            pomFileName = "pom.xml";
        }

        // ensure we don't use the release pom for the perform goals
        // ^^ paranoia? A MavenExecutor has already access to this. Probably worth refactoring.
        if ( !StringUtils.isEmpty( additionalArguments ) )
        {
            additionalArguments = additionalArguments + " -f " + pomFileName;
        }
        else
        {
            additionalArguments = "-f " + pomFileName;
        }

        if ( simulate )
        {
            ReleaseResult result = new ReleaseResult();

            logDebug( result, "Additional arguments: " + additionalArguments );

            logInfo( result, "Executing perform goals  - since this is simulation mode these goals are skipped." );

            return result;
        }

        return execute( releaseDescriptor, releaseEnvironment, reactorProject.getBasedir(), additionalArguments );
    }

    @Override
    public ReleaseResult simulate( ReleaseDescriptor releaseDescriptor, ReleaseEnvironment releaseEnvironment,
                                   List<MavenProject> reactorProjects ) {
        reactorProjects.forEach( mavenProject -> {
            try {
                System.out.printf("mavenProject: %s%n", mavenProject.getBasedir().getCanonicalPath());
                runLogic( releaseDescriptor, releaseEnvironment, mavenProject, true );
            } catch (IOException | ReleaseExecutionException e) {
                e.printStackTrace();
            }
        });
        return null;    }

    @Override
    public ReleaseResult execute( ReleaseDescriptor releaseDescriptor, ReleaseEnvironment releaseEnvironment,
                                  File workingDirectory, String additionalArguments )
            throws ReleaseExecutionException
    {
        final String pomFileName1 = releaseDescriptor.getPomFileName();
        final String mavenExecutorId = releaseEnvironment.getMavenExecutorId();
        final String goals = releaseDescriptor.getGoals();

        ReleaseResult result = new ReleaseResult();

        try
        {
            if ( !StringUtils.isEmpty( goals ) )
            {
                logInfo( result, "Executing goals '" + goals + "'..." );

                MavenExecutor mavenExecutor = mavenExecutors.get(mavenExecutorId);

                if ( mavenExecutor == null )
                {
                    throw new ReleaseExecutionException(
                            "Cannot find Maven executor with id: " + mavenExecutorId);
                }

                File executionRoot;
                String pomFileName;
                if ( pomFileName1 != null )
                {
                    File rootPom = new File( workingDirectory.getParent(), pomFileName1);
                    executionRoot = workingDirectory;
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
}
