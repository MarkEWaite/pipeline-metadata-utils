package hudson;

import hudson.init.InitMilestone;
import hudson.model.Hudson; //only needed until Hudson reference is removed from the ExtensionList.
import jenkins.model.Jenkins;
import org.jenkinsci.infra.tools.HyperLocalPluginManager;

import static org.mockito.Mockito.*;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Mock for the Jenkins/Hudson object required for processing
 * the various steps.  This became very important after introducing
 * mocking as pretty much every class wants to know what's happening
 * within Jenkins.  Nothing here actually starts the process, and
 * provides a very bland "Jenkins" startup
 *
 * This mock technically should be for Jenkins.class.  However,
 * ExtensionList pretty much requires that the object also be of
 * type Hudson.  Since Mockito works by creating a subclass of the
 * desired class, it has to be of Hudson.  This should be changed
 * when ExtensionList no longer requires a Hudon object.
 */
public class MockJenkins {
    private MockExtensionLists mockLookup = new MockExtensionLists();

    /**
     * There are a few methods that need to be mocked in order for setup to work properly:
     *     * getPluginManager -> must return HyperLocalPluginManager
     *     * getInitLevel     -> COMPLETED; Jenkins is "setup" as soon as the pm is populated
     *     * getExtensionList -> use the MockExtensionLists
     *     * getPlugin        -> get the Plugin information from HyperLocalPluginManager
     */
    public Jenkins getMockJenkins(HyperLocalPluginManager pm) {
        Jenkins mockJenkins = mock(Hudson.class); //required by ExtensionList
        when(mockJenkins.getPluginManager()).thenReturn(pm);
        when(mockJenkins.getInitLevel()).thenReturn(InitMilestone.COMPLETED);

        doAnswer(new Answer<ExtensionList>() {
            @Override
            public ExtensionList answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                return mockLookup.getMockExtensionList(pm, mockJenkins, (Class) args[0]);
            }
        }).when(mockJenkins).getExtensionList(any(Class.class));

        doAnswer(new Answer<Plugin>() {
            @Override
            public Plugin answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                PluginWrapper p = pm.getPlugin((Class) args[0]);
                if(p==null)     return null; //not actually loaded; might need an override
                return p.getPlugin();
            }
        }).when(mockJenkins).getPlugin(any(Class.class));

        return mockJenkins;
    }
}