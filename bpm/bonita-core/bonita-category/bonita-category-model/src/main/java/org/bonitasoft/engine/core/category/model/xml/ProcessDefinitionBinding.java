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
package org.bonitasoft.engine.core.category.model.xml;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.xml.ElementBinding;
import org.bonitasoft.engine.xml.SXMLParseException;

/**
 * @author Yanyan Liu
 */
public class ProcessDefinitionBinding extends ElementBinding {

    private final List<String> processDefinitionIds = new ArrayList<String>();

    @Override
    public void setAttributes(final Map<String, String> attributes) throws SXMLParseException {
    }

    @Override
    public void setChildElement(final String name, final String value, final Map<String, String> attributes) throws SXMLParseException {
        if (XMLCategoryMapping.PROCESS_DEFINITION.equals(name)) {
            this.processDefinitionIds.add(value);
        }
    }

    @Override
    public void setChildObject(final String name, final Object value) throws SXMLParseException {
    }

    @Override
    public Object getObject() {
        return new XMLProcessDefinitionsMapping(this.processDefinitionIds);
    }

    @Override
    public String getElementTag() {
        return XMLCategoryMapping.PROCESS_DEFINITIONS;
    }

}
