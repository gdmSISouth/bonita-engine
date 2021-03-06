package org.bonitasoft.engine.scheduler.job;

import java.io.Serializable;
import java.util.Map;

import org.bonitasoft.engine.scheduler.SJobConfigurationException;
import org.bonitasoft.engine.scheduler.StatelessJob;

/**
 * @author Matthieu Chaffotte
 */
public abstract class GroupJob implements StatelessJob {

    private static final long serialVersionUID = 1L;

    private String jobName;

    @Override
    public String getName() {
        return jobName;
    }

    @Override
    public void setAttributes(final Map<String, Serializable> attributes) throws SJobConfigurationException {
        jobName = (String) attributes.get("jobName");
    }

    @Override
    public boolean isWrappedInTransaction() {
        return true;
    }

}
