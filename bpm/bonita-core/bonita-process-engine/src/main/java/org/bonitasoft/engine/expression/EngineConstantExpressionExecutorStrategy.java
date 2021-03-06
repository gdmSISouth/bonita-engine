/**
 * Copyright (C) 2012 BonitaSoft S.A.
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.expression;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.api.impl.APIAccessorImpl;
import org.bonitasoft.engine.connector.EngineExecutionContext;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityReadException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceReadException;
import org.bonitasoft.engine.core.process.instance.model.SActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.SHumanTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.data.instance.api.DataInstanceContainer;
import org.bonitasoft.engine.expression.exception.SExpressionDependencyMissingException;
import org.bonitasoft.engine.expression.exception.SExpressionEvaluationException;
import org.bonitasoft.engine.expression.exception.SInvalidExpressionException;
import org.bonitasoft.engine.expression.model.ExpressionKind;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.session.SSessionNotFoundException;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.sessionaccessor.ReadSessionAccessor;
import org.bonitasoft.engine.sessionaccessor.SessionIdNotSetException;

/**
 * @author Matthieu Chaffotte
 * @author Elias Ricken de Medeiros
 */
public class EngineConstantExpressionExecutorStrategy implements ExpressionExecutorStrategy {

    private final ActivityInstanceService activityInstanceService;

    private final ProcessInstanceService processInstanceService;

    private final ReadSessionAccessor sessionAccessor;

    private final SessionService sessionService;

    public EngineConstantExpressionExecutorStrategy(final ActivityInstanceService activityInstanceService, final ProcessInstanceService processInstanceService,
            final SessionService sessionService, final ReadSessionAccessor sessionAccessor) {
        this.activityInstanceService = activityInstanceService;
        this.processInstanceService = processInstanceService;
        this.sessionService = sessionService;
        this.sessionAccessor = sessionAccessor;

    }

    @Override
    public Serializable evaluate(final SExpression expression, final Map<String, Object> dependencyValues, final Map<Integer, Object> resolvedExpressions)
            throws SExpressionEvaluationException, SExpressionDependencyMissingException {
        ExpressionConstants expressionConstant = ExpressionConstantsResolver.getExpressionConstantsFromName(expression.getContent());
        if (expressionConstant == null) {
            expressionConstant = ExpressionConstants.API_ACCESSOR;// just to make the expressionConstantsResolver load constants
            expressionConstant = ExpressionConstantsResolver.getExpressionConstantsFromName(expression.getContent());
        }
        if (expressionConstant == null) {
            throw new SExpressionEvaluationException(expression.getContent() + " is not a valid Engine-provided variable");
        }
        switch (expressionConstant) {
            case API_ACCESSOR:
                return getApiAccessor();
            case ENGINE_EXECUTION_CONTEXT:
                return getFromContextOrEngineExecutionContext(expressionConstant, dependencyValues);
            case ACTIVITY_INSTANCE_ID:
                return getFromContextOrEngineExecutionContext(expressionConstant, dependencyValues);
            case PROCESS_INSTANCE_ID:
                return getFromContextOrEngineExecutionContext(expressionConstant, dependencyValues);
            case PROCESS_DEFINITION_ID:
                return getFromContextOrEngineExecutionContext(expressionConstant, dependencyValues);
            case ROOT_PROCESS_INSTANCE_ID:
                return getFromContextOrEngineExecutionContext(expressionConstant, dependencyValues);
            case TASK_ASSIGNEE_ID:
                return getFromContextOrEngineExecutionContext(expressionConstant, dependencyValues);
            case LOGGED_USER_ID:
                return getLoggedUserFromSession();
            default:
                return inContext(expressionConstant, dependencyValues);
        }
    }

    protected APIAccessorImpl getApiAccessor() {
        return new APIAccessorImpl();
    }

    private Serializable getLoggedUserFromSession() throws SExpressionEvaluationException {
        try {
            return sessionService.getSession(sessionAccessor.getSessionId()).getUserId();
        } catch (final SSessionNotFoundException e) {
            throw new SExpressionEvaluationException(e);
        } catch (final SessionIdNotSetException e) {
            throw new SExpressionEvaluationException(e);
        }
    }

    private Serializable getFromContextOrEngineExecutionContext(final ExpressionConstants expressionConstant, final Map<String, Object> dependencyValues)
            throws SExpressionEvaluationException {
        final Object object = dependencyValues.get(expressionConstant.getEngineConstantName());
        if (object == null) {
            // try to get it from an already evaluated context
            final EngineExecutionContext context = (EngineExecutionContext) dependencyValues.get(ExpressionConstants.ENGINE_EXECUTION_CONTEXT
                    .getEngineConstantName());
            if (context != null) {
                return context.getExpressionConstant(expressionConstant);
            } else {
                return evaluate(expressionConstant, dependencyValues);
            }
        } else {
            // we have it already evaluated
            return (Serializable) object;
        }
    }

    private Serializable evaluate(final ExpressionConstants expressionConstant, final Map<String, Object> dependencyValues)
            throws SExpressionEvaluationException {
        // guess it
        if (ExpressionConstants.ENGINE_EXECUTION_CONTEXT.equals(expressionConstant)) {
            return createContext(dependencyValues);
        } else if (dependencyValues.containsKey(SExpressionContext.containerTypeKey) && dependencyValues.containsKey(SExpressionContext.containerIdKey)) {
            final String containerType = (String) dependencyValues.get(SExpressionContext.containerTypeKey);
            final long containerId = (Long) dependencyValues.get(SExpressionContext.containerIdKey);
            if (ExpressionConstants.PROCESS_DEFINITION_ID.equals(expressionConstant)) {
                return (Serializable) dependencyValues.get(SExpressionContext.processDefinitionIdKey);
            } else if (DataInstanceContainer.ACTIVITY_INSTANCE.toString().equals(containerType)) {
                return evaluteUsingActivityInstanceContainer(expressionConstant, dependencyValues, containerId);
            } else {
                return evaluateUsingProcessInstanceContainer(expressionConstant, dependencyValues, containerId);
            }
        } else {
            return -1;// no container id and not processDefinition
        }
    }

    private Serializable evaluateUsingProcessInstanceContainer(final ExpressionConstants expressionConstant, final Map<String, Object> dependencyValues,
            final long containerId) throws SExpressionEvaluationException {
        if (ExpressionConstants.PROCESS_INSTANCE_ID.equals(expressionConstant)) {
            return containerId;
        } else if (ExpressionConstants.TASK_ASSIGNEE_ID.equals(expressionConstant)) {
            return -1; // the assignee is related to an user task
        } else {
            // get the process and fill the others elements
            fillDependenciesFromProcessInstance(dependencyValues, containerId);
            return getNonNullLong(expressionConstant, dependencyValues);
        }
    }

    private Serializable evaluteUsingActivityInstanceContainer(final ExpressionConstants expressionConstant, final Map<String, Object> dependencyValues,
            final long containerId) throws SExpressionEvaluationException {
        if (ExpressionConstants.ACTIVITY_INSTANCE_ID.equals(expressionConstant)) {
            return containerId;
        } else {
            // get the activity and fill the others elements
            fillDependenciesFromActivityInstance(dependencyValues, containerId);
            return getNonNullLong(expressionConstant, dependencyValues);
        }
    }

    private Serializable getNonNullLong(final ExpressionConstants expressionConstant, final Map<String, Object> dependencyValues) {
        final Serializable serializable = (Serializable) dependencyValues.get(expressionConstant.getEngineConstantName());
        return serializable == null ? -1L : serializable;
    }

    private void fillDependenciesFromProcessInstance(final Map<String, Object> dependencyValues, final long processInstanceId)
            throws SExpressionEvaluationException {
        try {
            final SProcessInstance processInstance = processInstanceService.getProcessInstance(processInstanceId);
            dependencyValues.put(ExpressionConstants.PROCESS_INSTANCE_ID.getEngineConstantName(), processInstance.getId());
            dependencyValues.put(ExpressionConstants.ROOT_PROCESS_INSTANCE_ID.getEngineConstantName(), processInstance.getRootProcessInstanceId());
        } catch (final SProcessInstanceNotFoundException e) {
            throw new SExpressionEvaluationException("Error retrieving process instance while building EngineExecutionContext as EngineConstantExpression", e);
        } catch (final SProcessInstanceReadException e) {
            throw new SExpressionEvaluationException("Error retrieving process instance while building EngineExecutionContext as EngineConstantExpression", e);
        }
    }

    private void fillDependenciesFromActivityInstance(final Map<String, Object> dependencyValues, final long activityInstanceId)
            throws SExpressionEvaluationException {
        try {
            final SActivityInstance activityInstance = activityInstanceService.getActivityInstance(activityInstanceId);
            dependencyValues.put(ExpressionConstants.PROCESS_INSTANCE_ID.getEngineConstantName(), activityInstance.getLogicalGroup(3));
            dependencyValues.put(ExpressionConstants.ROOT_PROCESS_INSTANCE_ID.getEngineConstantName(), activityInstance.getLogicalGroup(1));
            if (isHumanTask(activityInstance)) {
                final SHumanTaskInstance taskInstance = (SHumanTaskInstance) activityInstance;
                dependencyValues.put(ExpressionConstants.TASK_ASSIGNEE_ID.getEngineConstantName(), taskInstance.getAssigneeId());
            }
        } catch (final SActivityReadException e) {
            throw new SExpressionEvaluationException("Error retrieving Activity instance while building EngineExecutionContext as EngineConstantExpression", e);
        } catch (final SActivityInstanceNotFoundException e) {
            throw new SExpressionEvaluationException("Error retrieving Activity instance while building EngineExecutionContext as EngineConstantExpression", e);
        }
    }

    private boolean isHumanTask(final SActivityInstance activityInstance) {
        return SFlowNodeType.USER_TASK.equals(activityInstance.getType()) || SFlowNodeType.MANUAL_TASK.equals(activityInstance.getType());
    }

    private Serializable inContext(final ExpressionConstants expressionConstant, final Map<String, Object> dependencyValues)
            throws SExpressionEvaluationException {
        final Object object = dependencyValues.get(expressionConstant.getEngineConstantName());
        if (object == null) {
            throw new SExpressionEvaluationException("EngineConstantExpression not supported for: " + expressionConstant.getEngineConstantName());
        } else {
            return (Serializable) object;
        }
    }

    private Serializable createContext(final Map<String, Object> dependencyValues) throws SExpressionEvaluationException {
        final EngineExecutionContext ctx = new EngineExecutionContext();
        if (dependencyValues.containsKey(SExpressionContext.containerTypeKey) && dependencyValues.containsKey(SExpressionContext.containerIdKey)) {
            final String containerType = (String) dependencyValues.get(SExpressionContext.containerTypeKey);
            final long containerId = (Long) dependencyValues.get(SExpressionContext.containerIdKey);
            if (DataInstanceContainer.ACTIVITY_INSTANCE.toString().equals(containerType)) {
                updateContextFromActivityInstance(ctx, containerId);
            } else if (DataInstanceContainer.PROCESS_INSTANCE.toString().equals(containerType)) {
                updateContextFromProcessInstance(ctx, containerId);
            }
        }
        if (dependencyValues.containsKey(SExpressionContext.processDefinitionIdKey)) {
            ctx.setProcessDefinitionId((Long) dependencyValues.get(SExpressionContext.processDefinitionIdKey));
        }
        return ctx;
    }

    private void updateContextFromProcessInstance(final EngineExecutionContext ctx, final long processInstanceId) throws SExpressionEvaluationException {
        try {
            final SProcessInstance processInstance = processInstanceService.getProcessInstance(processInstanceId);
            ctx.setProcessInstanceId(processInstance.getId());
            ctx.setRootProcessInstanceId(processInstance.getRootProcessInstanceId());
        } catch (final SProcessInstanceNotFoundException e) {
            throw new SExpressionEvaluationException("Error retrieving process instance while building EngineExecutionContext as EngineConstantExpression", e);
        } catch (final SProcessInstanceReadException e) {
            throw new SExpressionEvaluationException("Error retrieving process instance while building EngineExecutionContext as EngineConstantExpression", e);
        }
    }

    private void updateContextFromActivityInstance(final EngineExecutionContext ctx, final long activityInstanceId) throws SExpressionEvaluationException {
        ctx.setActivityInstanceId(activityInstanceId);
        try {
            final SActivityInstance activityInstance = activityInstanceService.getActivityInstance(activityInstanceId);
            ctx.setProcessInstanceId(activityInstance.getParentProcessInstanceId());
            ctx.setRootProcessInstanceId(activityInstance.getRootProcessInstanceId());
            if (isHumanTask(activityInstance)) {
                ctx.setTaskAssigneeId(((SHumanTaskInstance) activityInstance).getAssigneeId());
            }
        } catch (final SActivityReadException e) {
            throw new SExpressionEvaluationException("Error retrieving Activity instance while building EngineExecutionContext as EngineConstantExpression", e);
        } catch (final SActivityInstanceNotFoundException e) {
            throw new SExpressionEvaluationException("Error retrieving Activity instance while building EngineExecutionContext as EngineConstantExpression", e);
        }
    }

    @Override
    public void validate(final SExpression expression) throws SInvalidExpressionException {
        if (ExpressionConstantsResolver.getExpressionConstantsFromName(expression.getContent()) == null) {
            throw new SInvalidExpressionException("Unable to get Engine Constant '" + expression.getContent() + "' in expression: " + expression);
        }
    }

    @Override
    public ExpressionKind getExpressionKind() {
        return KIND_ENGINE_CONSTANT;
    }

    @Override
    public List<Object> evaluate(final List<SExpression> expressions, final Map<String, Object> dependencyValues, final Map<Integer, Object> resolvedExpressions)
            throws SExpressionEvaluationException, SExpressionDependencyMissingException {
        final List<Object> results = new ArrayList<Object>();
        for (final SExpression sExpression : expressions) {
            results.add(evaluate(sExpression, dependencyValues, resolvedExpressions));
        }
        return results;
    }

    @Override
    public boolean mustPutEvaluatedExpressionInContext() {
        return true;
    }

}
