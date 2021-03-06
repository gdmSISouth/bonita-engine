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
package org.bonitasoft.engine.identity.model.builder.impl;

import org.bonitasoft.engine.identity.model.SProfileMetadataDefinition;
import org.bonitasoft.engine.identity.model.builder.ProfileMetadataDefinitionBuilder;
import org.bonitasoft.engine.identity.model.impl.SProfileMetadataDefinitionImpl;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public class ProfileMetadataDefinitionBuilderImpl implements ProfileMetadataDefinitionBuilder {

    private SProfileMetadataDefinitionImpl entity;

    static final String ID = "id";

    static final String NAME = "name";

    static final String DESCRIPTION = "description";

    static final String DISPLAY_NAME = "displayName";

    @Override
    public ProfileMetadataDefinitionBuilder setName(final String name) {
        entity.setName(name);
        return this;
    }

    @Override
    public ProfileMetadataDefinitionBuilder setDisplayName(final String displayName) {
        entity.setDisplayName(displayName);
        return this;
    }

    @Override
    public ProfileMetadataDefinitionBuilder setDescription(final String description) {
        entity.setDescription(description);
        return this;
    }

    @Override
    public ProfileMetadataDefinitionBuilder setId(final long id) {
        entity.setId(id);
        return this;
    }

    @Override
    public ProfileMetadataDefinitionBuilder createNewInstance() {
        entity = new SProfileMetadataDefinitionImpl();
        return this;
    }

    @Override
    public SProfileMetadataDefinition done() {
        return entity;
    }

    @Override
    public String getIdKey() {
        return ID;
    }

    @Override
    public String getNameKey() {
        return NAME;
    }

    @Override
    public String getDisplayNameKey() {
        return DISPLAY_NAME;
    }

    @Override
    public String getDescriptionKey() {
        return DESCRIPTION;
    }

}
