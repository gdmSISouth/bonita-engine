/**
 * Copyright (C) 2012 BonitaSoft S.A.
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
package org.bonitasoft.engine.search.descriptor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bonitasoft.engine.bpm.comment.ArchivedCommentsSearchDescriptor;
import org.bonitasoft.engine.core.process.comment.model.archive.SAComment;
import org.bonitasoft.engine.core.process.comment.model.archive.builder.SACommentBuilder;
import org.bonitasoft.engine.core.process.comment.model.builder.SCommentBuilders;
import org.bonitasoft.engine.core.process.instance.model.builder.BPMInstanceBuilders;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.identity.model.builder.SUserBuilder;
import org.bonitasoft.engine.persistence.PersistentObject;

/**
 * @author Hongwen Zang
 */
public class SearchArchivedCommentsDescriptor extends SearchEntityDescriptor {

    private final Map<String, FieldDescriptor> searchEntityKeys;

    private final Map<Class<? extends PersistentObject>, Set<String>> archivedCommentsAllFields;

    SearchArchivedCommentsDescriptor(final BPMInstanceBuilders instanceBuilders, final SCommentBuilders sCommentBuilders, final SUserBuilder userBuilder) {
        SACommentBuilder saCommentBuilder = sCommentBuilders.getSACommentBuilder();
        searchEntityKeys = new HashMap<String, FieldDescriptor>(5);
        searchEntityKeys.put(ArchivedCommentsSearchDescriptor.PROCESS_INSTANCE_ID,
                new FieldDescriptor(SAComment.class, saCommentBuilder.getProcessInstanceIdKey()));
        searchEntityKeys.put(ArchivedCommentsSearchDescriptor.POSTED_BY_ID, new FieldDescriptor(SAComment.class, saCommentBuilder.getUserIdKey()));
        searchEntityKeys.put(ArchivedCommentsSearchDescriptor.ID, new FieldDescriptor(SAComment.class, saCommentBuilder.getIdKey()));
        searchEntityKeys.put(ArchivedCommentsSearchDescriptor.POSTDATE, new FieldDescriptor(SAComment.class, saCommentBuilder.getPostDateKey()));
        searchEntityKeys.put(ArchivedCommentsSearchDescriptor.USER_NAME, new FieldDescriptor(SUser.class, userBuilder.getUserNameKey()));

        archivedCommentsAllFields = new HashMap<Class<? extends PersistentObject>, Set<String>>(1);
        final Set<String> archivedCommentFields = new HashSet<String>(1);
        archivedCommentFields.add(saCommentBuilder.getContentKey());
        archivedCommentsAllFields.put(SAComment.class, archivedCommentFields);
    }

    @Override
    protected Map<String, FieldDescriptor> getEntityKeys() {
        return searchEntityKeys;
    }

    @Override
    protected Map<Class<? extends PersistentObject>, Set<String>> getAllFields() {
        return archivedCommentsAllFields;
    }

}
