package org.bonitasoft.engine.archive.model;

import org.bonitasoft.engine.events.model.SHandler;
import org.bonitasoft.engine.events.model.SUpdateEvent;

public class SEmployeeHandlerImpl implements SHandler<SUpdateEvent> {

    private static final String EMPLOYEE_UPDATED = "EMPLOYEE_UPDATED";

    private boolean isUpdated = false;

    @Override
    public void execute(final SUpdateEvent updateEvent) {
        isUpdated = true;
    }

    @Override
    public boolean isInterested(final SUpdateEvent updateEvent) {
        if (updateEvent.getType().compareToIgnoreCase(EMPLOYEE_UPDATED) == 0) {
            final Employee newEmployee = (Employee) updateEvent.getObject();
            final Employee oldEmployee = (Employee) updateEvent.getOldObject();
            if (!newEmployee.getName().equals(oldEmployee.getName())) {
                return true;
            }
        }
        return false;
    }

    public boolean isUpdated() {
        return isUpdated;
    }

    public void setUpdated(final boolean isUpdated) {
        this.isUpdated = isUpdated;
    }

}
