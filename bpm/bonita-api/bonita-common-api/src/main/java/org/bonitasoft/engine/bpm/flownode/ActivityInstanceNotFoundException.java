/**
 * Copyright (C) 2011-2013 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.engine.bpm.flownode;

import org.bonitasoft.engine.exception.NotFoundException;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public class ActivityInstanceNotFoundException extends NotFoundException {

    private static final long serialVersionUID = -5980531959067888526L;

    public ActivityInstanceNotFoundException(final long activityInstanceId) {
        super("activity with id " + activityInstanceId + " not found");
    }

    public ActivityInstanceNotFoundException(final long activityInstanceId, final Exception e) {
        super("activity with id " + activityInstanceId + " not found", e);
    }

    public ActivityInstanceNotFoundException(final Throwable cause) {
        super(cause);
    }

}
