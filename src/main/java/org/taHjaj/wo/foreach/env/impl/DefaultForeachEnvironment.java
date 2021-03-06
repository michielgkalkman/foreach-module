package org.taHjaj.wo.foreach.env.impl;

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
import org.taHjaj.wo.foreach.env.ForeachEnvironment;

import java.io.File;
import java.util.Locale;

/**
 *
 */
public class DefaultForeachEnvironment
    implements ForeachEnvironment
{

    private File mavenHome;

    private File javaHome;

    private File localRepositoryDirectory;

    private Settings settings;

    private String mavenExecutorId = DEFAULT_MAVEN_EXECUTOR_ID;

    private Locale locale = Locale.ENGLISH;

    @Override
    public File getMavenHome()
    {
        return mavenHome;
    }

    @Override
    public Settings getSettings()
    {
        return settings;
    }

    public DefaultForeachEnvironment setMavenHome(File mavenHome )
    {
        this.mavenHome = mavenHome;
        return this;
    }

    public DefaultForeachEnvironment setSettings(Settings settings )
    {
        this.settings = settings;
        return this;
    }

    @Override
    public String getMavenExecutorId()
    {
        return mavenExecutorId;
    }

    public DefaultForeachEnvironment setMavenExecutorId(String mavenExecutorId )
    {
        this.mavenExecutorId = mavenExecutorId;
        return this;
    }

    @Override
    public File getJavaHome()
    {
        return javaHome;
    }

    public DefaultForeachEnvironment setJavaHome(File javaHome )
    {
        this.javaHome = javaHome;
        return this;
    }

    @Override
    public File getLocalRepositoryDirectory()
    {
        File localRepo = localRepositoryDirectory;

        if ( localRepo == null && settings != null) {
            final String localRepository = settings.getLocalRepository();
            if (localRepository != null) {
                localRepo = new File(localRepository).getAbsoluteFile();
            }
        }

        return localRepo;
    }

    public DefaultForeachEnvironment setLocalRepositoryDirectory(File localRepositoryDirectory )
    {
        this.localRepositoryDirectory = localRepositoryDirectory;
        return this;
    }

    @Override
    public Locale getLocale()
    {
        return locale;
    }

    public DefaultForeachEnvironment setLocale(Locale locale )
    {
        this.locale = locale;
        return this;
    }
}
