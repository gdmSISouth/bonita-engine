package org.bonitasoft.engine.bpm;

import static org.junit.Assert.assertNotNull;

import org.bonitasoft.engine.api.APIAccessor;
import org.bonitasoft.engine.api.CommandAPI;
import org.bonitasoft.engine.api.IdentityAPI;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.api.impl.APIAccessorImpl;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.BeforeClass;
import org.junit.Test;

public class APIAccessorTest {

    public static APIAccessor apiAccessor;

    @BeforeClass
    public static void beforeClass() throws BonitaException {
        apiAccessor = new APIAccessorImpl();
    }

    @Cover(classes = APIAccessor.class, concept = BPMNConcept.NONE, keywords = { "Accessor", "Identity", "API" }, story = "Get IdentityAPI.", jira = "ENGINE-612")
    @Test
    public void testGetIdentityAPI() throws Exception {
        final IdentityAPI identityAPI = apiAccessor.getIdentityAPI();
        assertNotNull(identityAPI);
    }

    @Cover(classes = APIAccessor.class, concept = BPMNConcept.NONE, keywords = { "Accessor", "Process", "API" }, story = "Get ProcessAPI.", jira = "ENGINE-612")
    @Test
    public void testGetProcessAPI() throws Exception {
        final ProcessAPI processAPI = apiAccessor.getProcessAPI();
        assertNotNull(processAPI);
    }

    @Cover(classes = APIAccessor.class, concept = BPMNConcept.NONE, keywords = { "Accessor", "Command", "API" }, story = "Get CommandAPI.", jira = "ENGINE-612")
    @Test
    public void testGetCommandAPI() throws Exception {
        final CommandAPI commandAPI = apiAccessor.getCommandAPI();
        assertNotNull(commandAPI);
    }

}
