package org.taHjaj.wo.foreach;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.plexus.components.cipher.PlexusCipherException;
import org.taHjaj.wo.foreach.descriptor.ForeachDescriptor;
import org.taHjaj.wo.foreach.env.ForeachEnvironment;
import org.taHjaj.wo.foreach.exceptions.ForeachExecutionException;
import org.taHjaj.wo.foreach.exceptions.MavenExecutorException;
import org.taHjaj.wo.foreach.exec.MavenExecutor;
import org.taHjaj.wo.foreach.exec.impl.invoker.InvokerMavenExecutor;
import org.taHjaj.wo.foreach.phase.AbstractRunGoalsPhase;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Runs the supplied goals for each module.
 */
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
    protected String getGoals( ForeachDescriptor foreachDescriptor )
    {
        return foreachDescriptor.getGoals();
    }

    @Override
    public ForeachResult execute(ForeachDescriptor foreachDescriptor, ForeachEnvironment foreachEnvironment,
                                 List<MavenProject> reactorProjects )
    {
        final String mavenExecutorId = foreachEnvironment.getMavenExecutorId();
        getLogger().info( String.format( "mavenexecutorid: %s%n", mavenExecutorId));

        Map<String, ForeachResult> reactorProjectName2ForeachResult = new HashMap<>();

        try {
            for (MavenProject mavenProject : reactorProjects) {
                try {
                    final List<String> modules = mavenProject.getModules();
                    boolean fHasModules = !modules.isEmpty();

                    final File basedir = mavenProject.getBasedir();
                    if (fHasModules) {
                        getLogger().info(String.format("Project %s has modules - skipped%n", basedir.getCanonicalPath()));
                    } else {
                        getLogger().info(String.format("Project %s has no modules - executing goals %s%n",
                                basedir.getCanonicalPath(), foreachDescriptor.getGoals()));
                        final ForeachResult foreachResult = runLogic(foreachDescriptor, foreachEnvironment, mavenProject, false);
                        getLogger().info(String.format("Project %s has no modules - executing goals %s results in code %d%n",
                                basedir.getCanonicalPath(), foreachDescriptor.getGoals(), foreachResult.getResultCode()));

                        reactorProjectName2ForeachResult.put(mavenProject.getName(), foreachResult);

                        if (foreachResult.getResultCode() == ForeachResult.ERROR) {
                            return foreachResult;
                        }
                    }
                } catch (IOException | ForeachExecutionException e) {
                    final ForeachResult foreachResult = new ForeachResult();
                    foreachResult.appendError(e);
                    getLogger().error(String.format("Error executing %s%n", mavenProject.getName()), e);
                    reactorProjectName2ForeachResult.put(mavenProject.getName(), foreachResult);
                    return foreachResult;
                }
            }
        } finally {
            getLogger().info(org.apache.maven.shared.utils.StringUtils.center( "foreach", 60, "-"));
            reactorProjectName2ForeachResult.forEach((key, value) -> {
                final int resultCode = value.getResultCode();
                final String resultCodeString;
                if (resultCode == ForeachResult.ERROR) {
                    resultCodeString = "ERROR";
                } else if (resultCode == ForeachResult.SUCCESS) {
                    resultCodeString = "SUCCESS";
                } else {
                    resultCodeString = "UNDEFINED";
                }
                getLogger().info(String.format("%-30s%30s", key, resultCodeString).replace(' ', '.'));
            });
            getLogger().info(org.apache.maven.shared.utils.StringUtils.center( "foreach", 60, "-"));
        }

        return null;
    }

    private ForeachResult runLogic(ForeachDescriptor foreachDescriptor, ForeachEnvironment foreachEnvironment,
                                   MavenProject reactorProject, boolean simulate )
            throws ForeachExecutionException
    {
        String pomFileName = foreachDescriptor.getPomFileName();
        String additionalArguments = getAdditionalArguments( foreachDescriptor );

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
            ForeachResult result = new ForeachResult();

            logDebug( result, "Additional arguments: " + additionalArguments );

            logInfo( result, "Executing perform goals  - since this is simulation mode these goals are skipped." );

            return result;
        }

        return execute( foreachDescriptor, foreachEnvironment, reactorProject.getBasedir(), additionalArguments );
    }

    @Override
    public ForeachResult simulate(ForeachDescriptor foreachDescriptor, ForeachEnvironment foreachEnvironment,
                                  List<MavenProject> reactorProjects ) {
        reactorProjects.forEach( mavenProject -> {
            try {
                System.out.printf("mavenProject: %s%n", mavenProject.getBasedir().getCanonicalPath());
                runLogic( foreachDescriptor, foreachEnvironment, mavenProject, true );
            } catch (IOException | ForeachExecutionException e) {
                e.printStackTrace();
            }
        });
        return null;    }

    @Override
    public ForeachResult execute(ForeachDescriptor foreachDescriptor, ForeachEnvironment foreachEnvironment,
                                 File workingDirectory, String additionalArguments )
            throws ForeachExecutionException
    {
        final String pomFileName1 = foreachDescriptor.getPomFileName();
        final String mavenExecutorId = foreachEnvironment.getMavenExecutorId();
        final String goals = foreachDescriptor.getGoals();

        ForeachResult result = new ForeachResult();

        try
        {
            if ( !StringUtils.isEmpty( goals ) )
            {
                logInfo( result, "Executing goals '" + goals + "'..." );

                MavenExecutor mavenExecutor = mavenExecutors.get(mavenExecutorId);

                if ( mavenExecutor == null )
                {
                    throw new ForeachExecutionException(
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
                mavenExecutor.executeGoals( executionRoot, goals, foreachEnvironment,
                        foreachDescriptor.isInteractive(), additionalArguments,
                        pomFileName, result );
            }
        }
        catch ( MavenExecutorException e )
        {
            throw new ForeachExecutionException( e.getMessage(), e );
        }

        result.setResultCode( ForeachResult.SUCCESS );

        return result;
    }
}
