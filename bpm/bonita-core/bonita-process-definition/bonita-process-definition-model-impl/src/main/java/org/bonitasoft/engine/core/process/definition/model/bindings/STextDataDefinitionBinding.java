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
package org.bonitasoft.engine.core.process.definition.model.bindings;

import java.util.Map;

import org.bonitasoft.engine.data.definition.model.SDataDefinition;
import org.bonitasoft.engine.data.definition.model.builder.SDataDefinitionBuilder;
import org.bonitasoft.engine.data.definition.model.builder.SDataDefinitionBuilders;
import org.bonitasoft.engine.xml.SXMLParseException;

/**
 * @author Matthieu Chaffotte
 */
public class STextDataDefinitionBinding extends SDataDefinitionBinding {

    private boolean longText;

    public STextDataDefinitionBinding(final SDataDefinitionBuilders builder) {
        super(builder);
    }

    @Override
    public void setAttributes(final Map<String, String> attributes) throws SXMLParseException {
        super.setAttributes(attributes);
        longText = Boolean.valueOf(attributes.get(XMLSProcessDefinition.TEXT_DATA_DEFINITION_LONG));
    }

    @Override
    public SDataDefinition getObject() {
        final SDataDefinitionBuilder dataDefinitionBuilder = builder.getDataDefinitionBuilder();
        final SDataDefinitionBuilder dataDefinitionImpl = dataDefinitionBuilder.createNewTextData(name).setAsLongText(longText);
        if (description != null) {
            dataDefinitionImpl.setDescription(description);
        }
        dataDefinitionImpl.setDefaultValue(defaultValue);
        dataDefinitionImpl.setTransient(Boolean.valueOf(isTransient));
        return dataDefinitionImpl.done();
    }

    @Override
    public String getElementTag() {
        return XMLSProcessDefinition.TEXT_DATA_DEFINITION_NODE;
    }

}
