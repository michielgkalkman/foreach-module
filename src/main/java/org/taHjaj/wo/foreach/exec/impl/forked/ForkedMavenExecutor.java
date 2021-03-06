package org.taHjaj.wo.foreach.exec.impl.forked;

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

import org.apache.maven.settings.Settings;
import org.apache.maven.settings.io.xpp3.SettingsXpp3Writer;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.Commandline;
import org.sonatype.plexus.components.cipher.PlexusCipherException;
import org.taHjaj.wo.foreach.*;
import org.taHjaj.wo.foreach.commandline.CommandLineFactory;
import org.taHjaj.wo.foreach.env.ForeachEnvironment;
import org.taHjaj.wo.foreach.exceptions.MavenExecutorException;
import org.taHjaj.wo.foreach.exec.AbstractMavenExecutor;
import org.taHjaj.wo.foreach.exec.MavenExecutor;

import java.io.*;
import java.util.List;

/**
 * Fork Maven to executed a series of goals.
 *
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 */
@Component( role = MavenExecutor.class, hint = "forked-path" )
public class ForkedMavenExecutor
    extends AbstractMavenExecutor
{
    /**
     * Command line factory.
     */
    @Requirement
    private CommandLineFactory commandLineFactory;

    protected ForkedMavenExecutor() throws PlexusCipherException {
        super();
    }

    /*
     * @noinspection UseOfSystemOutOrSystemErr
     */
    @Override
    public void executeGoals( File workingDirectory, List<String> goals, ForeachEnvironment releaseEnvironment,
                              boolean interactive, String additionalArguments, String pomFileName,
                              ForeachResult relResult )
        throws MavenExecutorException
    {
        String mavenPath = null;
        // if null we use the current one
        final File mavenHome = releaseEnvironment.getMavenHome();
        if ( mavenHome != null )
        {
            mavenPath = mavenHome.getAbsolutePath();
        }
        else
        {
            mavenPath = System.getProperty( "maven.home" );
        }

        File settingsFile = null;
        final Settings settings = releaseEnvironment.getSettings();
        if ( settings != null )
        {
            // Have to serialize to a file as if Maven is embedded, there may not actually be a settings.xml on disk
            try
            {
                settingsFile = File.createTempFile( "release-settings", ".xml" );
                SettingsXpp3Writer writer = getSettingsWriter();
                
                try ( FileWriter fileWriter = new FileWriter( settingsFile ) )
                {
                    writer.write( fileWriter, encryptSettings(settings) );
                }
            }
            catch ( IOException e )
            {
                throw new MavenExecutorException( "Could not create temporary file for release settings.xml", e );
            }
        }
        try
        {

            Commandline cl =
                commandLineFactory.createCommandLine( mavenPath + File.separator + "bin" + File.separator + "mvn" );

            cl.setWorkingDirectory( workingDirectory.getAbsolutePath() );

            cl.addEnvironment( "MAVEN_TERMINATE_CMD", "on" );

            cl.addEnvironment( "M2_HOME", mavenPath );

            if ( settingsFile != null )
            {
                cl.createArg().setValue( "-s" );
                cl.createArg().setFile( settingsFile );
            }

            if ( pomFileName != null )
            {
                cl.createArg().setValue( "-f" );
                cl.createArg().setValue( pomFileName );
            }

            for ( String goal : goals )
            {
                cl.createArg().setValue( goal );
            }

            if ( !interactive )
            {
                cl.createArg().setValue( "--batch-mode" );
            }

            if ( !StringUtils.isEmpty( additionalArguments ) )
            {
                cl.createArg().setLine( additionalArguments );
            }

            TeeOutputStream stdOut = new TeeOutputStream( System.out );

            TeeOutputStream stdErr = new TeeOutputStream( System.err );

            try
            {
                final String clString = cl.toString();
                relResult.appendInfo( "Executing: " + clString);
                getLogger().info( "Executing: " + clString);

                int result = executeCommandLine( cl, System.in, stdOut, stdErr );

                if ( result != 0 )
                {
                    throw new MavenExecutorException("Maven execution failed, exit code: '" + result + "'", result );
                }
            }
            catch ( CommandLineException e )
            {
                throw new MavenExecutorException( "Can't run goal " + goals, e );
            }
            finally
            {
                relResult.appendOutput( stdOut.toString() );
            }
        }
        finally
        {
            if ( settingsFile != null && settingsFile.exists() && !settingsFile.delete() )
            {
                settingsFile.deleteOnExit();
            }
        }
    }

    public void setCommandLineFactory( CommandLineFactory commandLineFactory )
    {
        this.commandLineFactory = commandLineFactory;
    }


    public static int executeCommandLine( Commandline cl, InputStream systemIn, OutputStream systemOut,
                                          OutputStream systemErr )
        throws CommandLineException
    {
        if ( cl == null )
        {
            throw new IllegalArgumentException( "cl cannot be null." );
        }

        Process p = cl.execute();

        //processes.put( new Long( cl.getPid() ), p );

        RawStreamPumper inputFeeder = null;

        if ( systemIn != null )
        {
            inputFeeder = new RawStreamPumper( systemIn, p.getOutputStream(), true );
        }

        RawStreamPumper outputPumper = new RawStreamPumper( p.getInputStream(), systemOut );
        RawStreamPumper errorPumper = new RawStreamPumper( p.getErrorStream(), systemErr );

        if ( inputFeeder != null )
        {
            inputFeeder.start();
        }

        outputPumper.start();

        errorPumper.start();

        try
        {
            int returnValue = p.waitFor();

            if ( inputFeeder != null )
            {
                inputFeeder.setDone();
            }
            outputPumper.setDone();
            errorPumper.setDone();

            //processes.remove( new Long( cl.getPid() ) );

            return returnValue;
        }
        catch ( InterruptedException ex )
        {
            //killProcess( cl.getPid() );
            throw new CommandLineException( "Error while executing external command, process killed.", ex );
        }
        finally
        {
            try
            {
                errorPumper.closeInput();
            }
            catch ( IOException e )
            {
                //ignore
            }
            try
            {
                outputPumper.closeInput();
            }
            catch ( IOException e )
            {
                //ignore
            }
            if ( inputFeeder != null )
            {
                try
                {
                    inputFeeder.closeOutput();
                }
                catch ( IOException e )
                {
                    //ignore
                }
            }
        }
    }


}
