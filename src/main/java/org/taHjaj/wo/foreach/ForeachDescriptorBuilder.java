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
 * @since 3.0.0
 */
public class ForeachDescriptorBuilder
{
    /**
     * Hides inner logic of the release descriptor
     * 
     * @author Robert Scholte
     *
     */
    public static final class BuilderForeachDescriptor implements ForeachDescriptor
    {

        /**
         *
         *             Additional arguments to pass to any executed
         * Maven process.
         *
         */
        private String additionalArguments;

        /**
         *
         *             The file name of the POM to pass to any executed
         * Maven process.
         *
         */
        private String pomFileName;

        /**
         *
         *             Whether the release process is interactive and
         * the release manager should be prompted to
         *             confirm values, or whether the defaults are used
         * regardless.
         *
         */
        private boolean interactive = true;

        /**
         *
         *             The goals to execute in perform phase for the
         * release.
         *
         */
        private String goals;

        /**
         *
         *             The directory where the release is performed.
         *
         */
        private String workingDirectory;

        /**
         * Field activateProfiles.
         */
        private java.util.List<String> activateProfiles;

        private BuilderForeachDescriptor()
        {
        }

        @Override
        public String getPomFileName() {
            return pomFileName;
        }

        /**
         * Set the file name of the POM to pass to any executed Maven
         * process.
         *
         * @param pomFileName
         */
        public void setPomFileName( String pomFileName )
        {
            this.pomFileName = pomFileName;
        } //-- void setPomFileName( String )

        /**
         * Get additional arguments to pass to any executed Maven
         * process.
         *
         * @return String
         */
        public String getAdditionalArguments()
        {
            return this.additionalArguments;
        } //-- String getAdditionalArguments()

        /**
         * Set additional arguments to pass to any executed Maven
         * process.
         *
         * @param additionalArguments
         */
        public void setAdditionalArguments( String additionalArguments )
        {
            this.additionalArguments = additionalArguments;
        } //-- void setAdditionalArguments( String )

        /**
         * Get whether the release process is interactive and the
         * release manager should be prompted to
         *             confirm values, or whether the defaults are used
         * regardless.
         *
         * @return boolean
         */
        public boolean isInteractive()
        {
            return this.interactive;
        } //-- boolean isInteractive()

        /**
         * Set whether the release process is interactive and the
         * release manager should be prompted to
         *             confirm values, or whether the defaults are used
         * regardless.
         *
         * @param interactive
         */
        public void setInteractive( boolean interactive )
        {
            this.interactive = interactive;
        } //-- void setInteractive( boolean )

        /**
         * Get the goals to execute.
         *
         * @return String
         */

        public String getGoals()
        {
            return this.goals;
        }

        /**
         * Set the goals to execute.
         *
         * @param goals
         */
        public void setGoals( String goals )
        {
            this.goals = goals;
        }


        /**
         * Get the directory where the release is performed.
         *
         * @return String
         */
        public String getWorkingDirectory()
        {
            return this.workingDirectory;
        } //-- String getWorkingDirectory()

        /**
         * Set the directory where the release is performed.
         *
         * @param workingDirectory
         */
        public void setWorkingDirectory( String workingDirectory )
        {
            this.workingDirectory = workingDirectory;
        } //-- void setWorkingDirectory( String )


        /**
         * Method getActivateProfiles.
         *
         * @return List
         */
        public java.util.List<String> getActivateProfiles()
        {
            if ( this.activateProfiles == null )
            {
                this.activateProfiles = new java.util.ArrayList<String>();
            }

            return this.activateProfiles;
        } //-- java.util.List<String> getActivateProfiles()

        /**
         * Set list of profiles to activate.
         *
         * @param activateProfiles
         */
        public void setActivateProfiles( java.util.List<String> activateProfiles )
        {
            this.activateProfiles = activateProfiles;
        } //-- void setActivateProfiles( java.util.List )

    }


    private final BuilderForeachDescriptor releaseDescriptor;
    
    public ForeachDescriptorBuilder()
    {
        this.releaseDescriptor = new BuilderForeachDescriptor();
    }



    public ForeachDescriptorBuilder setAdditionalArguments(String additionalArguments )
    {
        releaseDescriptor.setAdditionalArguments( additionalArguments );
        return this;
    }

    public ForeachDescriptorBuilder setPomFileName(String pomFileName )
    {
        releaseDescriptor.setPomFileName( pomFileName );
        return this;
    }

    public ForeachDescriptorBuilder setGoals(String performGoals )
    {
        releaseDescriptor.setGoals( performGoals );
        return this;
    }

    public ForeachDescriptorBuilder setWorkingDirectory(String workingDirectory )
    {
        releaseDescriptor.setWorkingDirectory( workingDirectory );
        return this;
    }

    public ForeachDescriptorBuilder setActivateProfiles(List<String> profiles )
    {
        releaseDescriptor.setActivateProfiles( profiles );
        return this;
    }

    BuilderForeachDescriptor build()
    {
        return releaseDescriptor;
    }
}
