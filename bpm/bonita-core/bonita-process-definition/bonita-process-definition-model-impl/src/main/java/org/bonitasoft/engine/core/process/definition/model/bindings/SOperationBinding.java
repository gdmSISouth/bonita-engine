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
package org.bonitasoft.engine.core.process.definition.model.bindings;

import java.util.Map;

import org.bonitasoft.engine.core.operation.model.SLeftOperand;
import org.bonitasoft.engine.core.operation.model.SOperatorType;
import org.bonitasoft.engine.core.operation.model.builder.SOperationBuilder;
import org.bonitasoft.engine.core.operation.model.builder.SOperationBuilders;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.xml.ElementBinding;
import org.bonitasoft.engine.xml.SXMLParseException;

/**
 * @author Elias Ricken de Medeiros
 */
public class SOperationBinding extends ElementBinding {

    private final SOperationBuilders sOperationBuilders;

    private SExpression rightOperand;

    private SLeftOperand leftOperand;

    private String operator;

    private SOperatorType operatorType;

    public SOperationBinding(final SOperationBuilders sOperationBuilders) {
        this.sOperationBuilders = sOperationBuilders;
    }

    @Override
    public void setChildObject(final String name, final Object value) throws SXMLParseException {
        if (XMLSProcessDefinition.OPERATION_RIGHT_OPERAND.equals(name)) {
            rightOperand = (SExpression) value;
        }
        if (XMLSProcessDefinition.OPERATION_LEFT_OPERAND.equals(name)) {
            leftOperand = (SLeftOperand) value;
        }
    }

    @Override
    public Object getObject() {
        final SOperationBuilder sOperationBuilder = sOperationBuilders.getSOperationBuilder().createNewInstance();
        sOperationBuilder.setOperator(operator);
        sOperationBuilder.setRightOperand(rightOperand);
        sOperationBuilder.setType(operatorType);
        sOperationBuilder.setLeftOperand(leftOperand);
        return sOperationBuilder.done();
    }

    @Override
    public String getElementTag() {
        return XMLSProcessDefinition.OPERATION_NODE;
    }

    @Override
    public void setChildElement(final String name, final String value, final Map<String, String> attributes) throws SXMLParseException {

    }

    @Override
    public void setAttributes(final Map<String, String> attributes) throws SXMLParseException {
        operator = attributes.get(XMLSProcessDefinition.OPERATION_OPERATOR);
        operatorType = SOperatorType.valueOf(attributes.get(XMLSProcessDefinition.OPERATION_OPERATOR_TYPE));
    }

}
