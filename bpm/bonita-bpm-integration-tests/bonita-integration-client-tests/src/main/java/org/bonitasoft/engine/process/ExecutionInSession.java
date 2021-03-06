package org.bonitasoft.engine.process;

import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.test.APITestUtil;

/**
 * @author Baptiste Mesta
 */
public abstract class ExecutionInSession {

    private APISession session;

    private final String password;

    private final String username;

    public APISession getSession() {
        return session;
    }

    public ExecutionInSession() {
        username = null;
        password = null;
    }

    public ExecutionInSession(final String username, final String password) {
        this.username = username;
        this.password = password;
    }

    public abstract void run() throws Exception;

    public void setSession(final APISession session) {
        this.session = session;
    }

    void executeInSession() throws Exception {
        final APISession session;
        if (username != null) {
            session = APITestUtil.loginDefaultTenant(username, password);
        } else {
            session = APITestUtil.loginDefaultTenant();
        }
        setSession(session);
        run();
        APITestUtil.logoutTenant(session);
    }

}
