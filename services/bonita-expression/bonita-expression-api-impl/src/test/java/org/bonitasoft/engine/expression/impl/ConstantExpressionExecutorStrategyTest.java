/**
 * Copyright (C) 2013 BonitaSoft S.A.
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
package org.bonitasoft.engine.expression.impl;

import static org.junit.Assert.assertNotNull;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

import org.bonitasoft.engine.expression.exception.SExpressionDependencyMissingException;
import org.bonitasoft.engine.expression.exception.SExpressionEvaluationException;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.expression.model.impl.SExpressionImpl;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Celine Souchet
 * 
 */
public class ConstantExpressionExecutorStrategyTest {

    private ConstantExpressionExecutorStrategy strategy;

    @Before
    public void setup() {
        strategy = new ConstantExpressionExecutorStrategy();
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.expression.impl.ConstantExpressionExecutorStrategy#evaluate(org.bonitasoft.engine.expression.model.SExpression, java.util.Map, java.util.Map)}
     * .
     * 
     * @throws SExpressionDependencyMissingException
     * @throws SExpressionEvaluationException
     * @throws ParseException
     */
    @Cover(classes = { ConstantExpressionExecutorStrategyTest.class }, concept = BPMNConcept.EXPRESSIONS, keywords = { "Evaluate", "Expression", "Constant",
            "Date" }, jira = "ENGINE-1559, ENGINE-1099")
    @Test
    public final void evaluateDate() throws SExpressionEvaluationException, SExpressionDependencyMissingException,
            ParseException {
        final SExpression sExpression = buildExpression("2013-07-18T14:49:26.86+02:00", SExpression.TYPE_CONSTANT, Date.class.getName(), null, null);

        final Date result = (Date) strategy.evaluate(sExpression, null, null);
        assertNotNull(result);
    }

    @Cover(classes = { ConstantExpressionExecutorStrategyTest.class }, concept = BPMNConcept.EXPRESSIONS, keywords = { "Evaluate", "Expression", "Constant",
            "Date" }, jira = "ENGINE-1559, ENGINE-1099")
    @Test
    public final void evaluateDateWithoutTimeZone() throws SExpressionEvaluationException, SExpressionDependencyMissingException,
            ParseException {
        final SExpression sExpression = buildExpression("2013-07-18T14:49:26.86", SExpression.TYPE_CONSTANT, Date.class.getName(), null, null);

        final Date result = (Date) strategy.evaluate(sExpression, null, null);
        assertNotNull(result);
    }

    @Cover(classes = { ConstantExpressionExecutorStrategyTest.class }, concept = BPMNConcept.EXPRESSIONS, keywords = { "Evaluate", "Expression", "Constant",
            "Date" }, jira = "ENGINE-1559, ENGINE-1099")
    @Test
    public final void evaluateDateWithoutMilliseconds() throws SExpressionEvaluationException, SExpressionDependencyMissingException,
            ParseException {
        final SExpression sExpression = buildExpression("2013-07-18T14:49:26+02:00", SExpression.TYPE_CONSTANT, Date.class.getName(), null, null);

        final Date result = (Date) strategy.evaluate(sExpression, null, null);
        assertNotNull(result);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.expression.impl.ConstantExpressionExecutorStrategy#evaluate(java.util.List, java.util.Map, java.util.Map)}.
     */
    @Test
    public final void evaluateListOfSExpressionMapOfStringObjectMapOfIntegerObject() {
        // TODO : not yet implemented
    }

    @Test
    public final void validate() throws Exception {
        // TODO : not yet implemented
    }

    @Test
    public final void getExpressionKind() throws Exception {
        // TODO : not yet implemented
    }

    @Test
    public final void mustPutEvaluatedExpressionInContext() throws Exception {
        // TODO : not yet implemented
    }

    private SExpression buildExpression(final String content, final String expressionType, final String returnType, final String interpreter,
            final List<SExpression> dependencies) {
        final SExpressionImpl eb = new SExpressionImpl();
        eb.setName(content);
        eb.setContent(content);
        eb.setExpressionType(expressionType);
        eb.setInterpreter(interpreter);
        eb.setReturnType(returnType);
        eb.setDependencies(dependencies);
        return eb;
    }

}
