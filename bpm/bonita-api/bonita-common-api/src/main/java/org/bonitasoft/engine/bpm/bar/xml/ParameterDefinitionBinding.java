/*
 * Copyright (C) 2012 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 */
package org.bonitasoft.engine.bpm.bar.xml;

import java.util.Map;

import org.bonitasoft.engine.bpm.parameter.impl.ParameterDefinitionImpl;
import org.bonitasoft.engine.io.xml.XMLParseException;

/**
 * @author Baptiste Mesta
 */
public class ParameterDefinitionBinding extends NamedElementBinding {

    private String description;

    private String type;

    @Override
    public void setAttributes(final Map<String, String> attributes) throws XMLParseException {
        super.setAttributes(attributes);
        type = attributes.get(XMLProcessDefinition.PARAMETER_TYPE);
    }

    @Override
    public Object getObject() {
        final ParameterDefinitionImpl parameterDefinitionImpl = new ParameterDefinitionImpl(name, type);
        parameterDefinitionImpl.setDescription(description);
        return parameterDefinitionImpl;
    }

    @Override
    public String getElementTag() {
        return XMLProcessDefinition.PARAMETER_NODE;
    }

    @Override
    public void setChildElement(final String name, final String value, final Map<String, String> attributes) throws XMLParseException {
        if (XMLProcessDefinition.DESCRIPTION.equals(name)) {
            description = value;
        }
    }

    @Override
    public void setChildObject(final String name, final Object value) throws XMLParseException {

    }

}
