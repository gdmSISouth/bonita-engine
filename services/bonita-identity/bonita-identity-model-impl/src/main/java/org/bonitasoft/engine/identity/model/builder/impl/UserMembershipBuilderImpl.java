/**
 * Copyright (C) 2011 BonitaSoft S.A.
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
package org.bonitasoft.engine.identity.model.builder.impl;

import org.bonitasoft.engine.identity.model.SUserMembership;
import org.bonitasoft.engine.identity.model.builder.UserMembershipBuilder;
import org.bonitasoft.engine.identity.model.impl.SUserMembershipImpl;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public class UserMembershipBuilderImpl implements UserMembershipBuilder {

    private SUserMembershipImpl userMembership;

    static final String ID = "id";

    static final String USER_ID = "userId";

    static final String ROLE_ID = "roleId";

    static final String GROUP_ID = "groupId";

    static final String ASSIGNED_BY = "assignedBy";

    static final String ASSIGNED_DATE = "assignedDate";

    @Override
    public UserMembershipBuilder createNewInstance(final long userId, final long groupId, final long roleId) {
        userMembership = new SUserMembershipImpl(userId, groupId, roleId);
        return this;
    }

    @Override
    public UserMembershipBuilder setAssignedBy(final long assignedBy) {
        userMembership.setAssignedBy(assignedBy);
        return this;
    }

    @Override
    public UserMembershipBuilder setAssignedDate(final long assignDate) {
        userMembership.setAssignedDate(assignDate);
        return this;
    }

    @Override
    public SUserMembership done() {
        return userMembership;
    }

    @Override
    public String getIdKey() {
        return ID;
    }

    @Override
    public String getUserIdKey() {
        return USER_ID;
    }

    @Override
    public String getRoleIdKey() {
        return ROLE_ID;
    }

    @Override
    public String getGroupIdKey() {
        return GROUP_ID;
    }

    @Override
    public String getAssignedByKey() {
        return ASSIGNED_BY;
    }

    @Override
    public String getAssignedDateKey() {
        return ASSIGNED_DATE;
    }

}
