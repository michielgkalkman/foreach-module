package org.taHjaj.wo.foreach;

import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;

import java.io.File;

public class SupportingMojoTestCase extends AbstractMojoTestCase {
    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    @Override
    public Mojo lookupMojo(String goal, File pom ) throws Exception {
        return super.lookupMojo( goal, pom);
    }
}
