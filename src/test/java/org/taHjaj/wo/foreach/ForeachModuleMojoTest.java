package org.taHjaj.wo.foreach;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.repository.RepositorySystem;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class ForeachModuleMojoTest {

    protected ForeachModuleMojo mojo;

    private File workingDirectory;

    private SupportingMojoTestCase supportingMojoTestCase;

    @Before
    public void setUp()
            throws Exception
    {
        supportingMojoTestCase = new SupportingMojoTestCase();

        supportingMojoTestCase.setUp();

        File testFile = SupportingMojoTestCase.getTestFile("target/test-classes/mojos/clean/clean.xml");
        mojo = (ForeachModuleMojo) supportingMojoTestCase.lookupMojo( "foreach", testFile );
        workingDirectory = testFile.getParentFile();

        Map pluginContext = new HashMap();

        mojo.setPluginContext(pluginContext);
    }

    @Test
    public void execute() throws MojoExecutionException {
        // prepare
//        ArgumentCaptor<ReleaseCleanRequest> request = ArgumentCaptor.forClass( ReleaseCleanRequest.class );
//
//        ReleaseManager mock = mock( ReleaseManager.class );
//        mojo.setReleaseManager( mock );

        // execute
        mojo.execute();

        // verify
//        verify( mock ).clean( request.capture() );

//        assertEquals( mojo.getReactorProjects(), request.getValue().getReactorProjects() );

//        verifyNoMoreInteractions( mock );
    }
}