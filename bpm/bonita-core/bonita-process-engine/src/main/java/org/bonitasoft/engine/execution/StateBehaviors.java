/**
 * Copyright (C) 2012-2013 BonitaSoft S.A.
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
package org.bonitasoft.engine.execution;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.actor.mapping.ActorMappingService;
import org.bonitasoft.engine.actor.mapping.SActorNotFoundException;
import org.bonitasoft.engine.actor.mapping.model.SActor;
import org.bonitasoft.engine.archive.ArchiveService;
import org.bonitasoft.engine.bpm.bar.xml.XMLProcessDefinition.BEntry;
import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.bpm.connector.ConnectorState;
import org.bonitasoft.engine.bpm.model.impl.BPMInstancesCreator;
import org.bonitasoft.engine.classloader.ClassLoaderException;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionExecutor;
import org.bonitasoft.engine.core.connector.ConnectorInstanceService;
import org.bonitasoft.engine.core.connector.ConnectorService;
import org.bonitasoft.engine.core.connector.exception.SConnectorInstanceReadException;
import org.bonitasoft.engine.core.expression.control.api.ExpressionResolverService;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.filter.FilterResult;
import org.bonitasoft.engine.core.filter.UserFilterService;
import org.bonitasoft.engine.core.filter.exception.SUserFilterExecutionException;
import org.bonitasoft.engine.core.operation.OperationService;
import org.bonitasoft.engine.core.operation.model.SOperation;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.SProcessDefinitionNotFoundException;
import org.bonitasoft.engine.core.process.definition.exception.SProcessDefinitionReadException;
import org.bonitasoft.engine.core.process.definition.model.SActivityDefinition;
import org.bonitasoft.engine.core.process.definition.model.SCallActivityDefinition;
import org.bonitasoft.engine.core.process.definition.model.SConnectorDefinition;
import org.bonitasoft.engine.core.process.definition.model.SFlowElementContainerDefinition;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeDefinition;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.bonitasoft.engine.core.process.definition.model.SHumanTaskDefinition;
import org.bonitasoft.engine.core.process.definition.model.SLoopCharacteristics;
import org.bonitasoft.engine.core.process.definition.model.SMultiInstanceLoopCharacteristics;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.SReceiveTaskDefinition;
import org.bonitasoft.engine.core.process.definition.model.SSendTaskDefinition;
import org.bonitasoft.engine.core.process.definition.model.SUserFilterDefinition;
import org.bonitasoft.engine.core.process.definition.model.builder.BPMDefinitionBuilders;
import org.bonitasoft.engine.core.process.definition.model.event.SBoundaryEventDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.SCatchEventDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.SIntermediateCatchEventDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.SThrowEventDefinition;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.api.event.EventInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityCreationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityExecutionException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityModificationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityStateExecutionException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeModificationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeReadException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SWaitingEventModificationException;
import org.bonitasoft.engine.core.process.instance.model.SActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.SCallActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.SConnectorInstance;
import org.bonitasoft.engine.core.process.instance.model.SFlowElementsContainerType;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SPendingActivityMapping;
import org.bonitasoft.engine.core.process.instance.model.SReceiveTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.SSendTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.SStateCategory;
import org.bonitasoft.engine.core.process.instance.model.builder.BPMInstanceBuilders;
import org.bonitasoft.engine.core.process.instance.model.builder.SUserTaskInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.event.SBoundaryEventInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.event.handling.SWaitingEventKeyProvider;
import org.bonitasoft.engine.core.process.instance.model.event.SBoundaryEventInstance;
import org.bonitasoft.engine.core.process.instance.model.event.SCatchEventInstance;
import org.bonitasoft.engine.core.process.instance.model.event.SIntermediateCatchEventInstance;
import org.bonitasoft.engine.core.process.instance.model.event.SThrowEventInstance;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SWaitingEvent;
import org.bonitasoft.engine.data.instance.api.DataInstanceContainer;
import org.bonitasoft.engine.data.instance.api.DataInstanceService;
import org.bonitasoft.engine.data.instance.model.SDataInstance;
import org.bonitasoft.engine.data.instance.model.builder.SDataInstanceBuilders;
import org.bonitasoft.engine.execution.event.EventsHandler;
import org.bonitasoft.engine.execution.event.OperationsWithContext;
import org.bonitasoft.engine.execution.job.JobNameBuilder;
import org.bonitasoft.engine.execution.state.EndingIntermediateCatchEventExceptionStateImpl;
import org.bonitasoft.engine.execution.state.FlowNodeStateManager;
import org.bonitasoft.engine.execution.work.ExecuteConnectorOfActivity;
import org.bonitasoft.engine.execution.work.ExecuteConnectorWork;
import org.bonitasoft.engine.execution.work.InstantiateProcessWork;
import org.bonitasoft.engine.expression.exception.SExpressionDependencyMissingException;
import org.bonitasoft.engine.expression.exception.SExpressionEvaluationException;
import org.bonitasoft.engine.expression.exception.SExpressionTypeUnknownException;
import org.bonitasoft.engine.expression.exception.SInvalidExpressionException;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaSearchException;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.scheduler.SSchedulerException;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.work.WorkRegisterException;
import org.bonitasoft.engine.work.WorkService;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class StateBehaviors {

    public static final int BEFORE_ON_ENTER = 1;

    public static final int DURING_ON_ENTER = 1 << 1;

    public static final int BEFORE_ON_FINISH = 1 << 2;

    public static final int DURING_ON_FINISH = 1 << 3;

    public static final int AFTER_ON_FINISH = 1 << 4;

    private static final int BATCH_SIZE = 20;

    private static final int MAX_NUMBER_OF_RESULTS = 100;

    private final BPMInstancesCreator bpmInstancesCreator;

    private final EventsHandler eventsHandler;

    private final ActivityInstanceService activityInstanceService;

    private final UserFilterService userFilterService;

    private final ClassLoaderService classLoaderService;

    private final BPMInstanceBuilders instanceBuilders;

    private final ActorMappingService actorMappingService;

    private final ConnectorService connectorService;

    private final ExpressionResolverService expressionResolverService;

    private final ProcessDefinitionService processDefinitionService;

    private final DataInstanceService dataInstanceService;

    private final OperationService operationService;

    private ProcessExecutor processExecutor;

    private final WorkService workService;

    private final ContainerRegistry containerRegistry;

    private final FlowNodeStateManager flowNodeStateManager;

    private final ProcessInstanceService processInstanceService;

    private final ArchiveService archiveService;

    private final SDataInstanceBuilders dataInstanceBuilders;

    private final TransactionExecutor transactionExecutor;

    private final EventInstanceService eventInstanceService;

    private final SchedulerService schedulerService;

    private final TechnicalLoggerService logger;

    private final BPMDefinitionBuilders bpmDefinitionBuilders;

    private final ConnectorInstanceService connectorInstanceService;

    public StateBehaviors(final TransactionExecutor transactionExecutor, final BPMInstancesCreator bpmInstancesCreator, final EventsHandler eventsHandler,
            final ActivityInstanceService activityInstanceService, final UserFilterService userFilterService, final ClassLoaderService classLoaderService,
            final BPMInstanceBuilders instanceBuilders, final ActorMappingService actorMappingService, final ConnectorService connectorService,
            final ConnectorInstanceService connectorInstanceService, final ExpressionResolverService expressionResolverService,
            final ProcessDefinitionService processDefinitionService, final DataInstanceService dataInstanceService, final OperationService operationService,
            final WorkService workService, final ContainerRegistry containerRegistry, final FlowNodeStateManager flowNodeStateManager,
            final ProcessInstanceService processInstanceService, final ArchiveService archiveService, final SDataInstanceBuilders dataInstanceBuilders,
            final EventInstanceService eventInstanceSevice, final SchedulerService schedulerService, final TechnicalLoggerService logger,
            final BPMDefinitionBuilders bpmDefinitionBuilders) {
        super();
        this.transactionExecutor = transactionExecutor;
        this.bpmInstancesCreator = bpmInstancesCreator;
        this.eventsHandler = eventsHandler;
        this.activityInstanceService = activityInstanceService;
        this.userFilterService = userFilterService;
        this.classLoaderService = classLoaderService;
        this.instanceBuilders = instanceBuilders;
        this.actorMappingService = actorMappingService;
        this.connectorService = connectorService;
        this.connectorInstanceService = connectorInstanceService;
        this.expressionResolverService = expressionResolverService;
        this.processDefinitionService = processDefinitionService;
        this.dataInstanceService = dataInstanceService;
        this.operationService = operationService;
        this.workService = workService;
        this.containerRegistry = containerRegistry;
        this.flowNodeStateManager = flowNodeStateManager;
        this.processInstanceService = processInstanceService;
        this.archiveService = archiveService;
        this.dataInstanceBuilders = dataInstanceBuilders;
        eventInstanceService = eventInstanceSevice;
        this.schedulerService = schedulerService;
        this.logger = logger;
        this.bpmDefinitionBuilders = bpmDefinitionBuilders;
    }

    public DataInstanceContainer getParentContainerType(final SFlowNodeInstance flowNodeInstance) {
        DataInstanceContainer parentContainerType;
        if (flowNodeInstance.getLogicalGroup(2) <= 0) {
            parentContainerType = DataInstanceContainer.PROCESS_INSTANCE;
        } else {
            parentContainerType = DataInstanceContainer.ACTIVITY_INSTANCE;
        }
        return parentContainerType;
    }

    @SuppressWarnings("unchecked")
    public void mapDataOutputOfMultiInstance(final SProcessDefinition processDefinition, final SFlowNodeInstance flowNodeInstance)
            throws SActivityStateExecutionException {
        if (flowNodeInstance instanceof SActivityInstance && !SFlowNodeType.MULTI_INSTANCE_ACTIVITY.equals(flowNodeInstance.getType())) {
            final SFlowElementContainerDefinition processContainer = processDefinition.getProcessContainer();
            final SActivityDefinition activityDefinition = (SActivityDefinition) processContainer.getFlowNode(flowNodeInstance.getFlowNodeDefinitionId());
            if (activityDefinition != null) {// can be null if the activity was added in runtime
                try {
                    final SLoopCharacteristics loopCharacteristics = activityDefinition.getLoopCharacteristics();
                    if (loopCharacteristics instanceof SMultiInstanceLoopCharacteristics
                            && ((SMultiInstanceLoopCharacteristics) loopCharacteristics).getDataOutputItemRef() != null) {
                        final SMultiInstanceLoopCharacteristics miLoop = (SMultiInstanceLoopCharacteristics) loopCharacteristics;
                        final SDataInstance outputData = dataInstanceService.getDataInstance(miLoop.getDataOutputItemRef(), flowNodeInstance.getId(),
                                DataInstanceContainer.ACTIVITY_INSTANCE.name());
                        final SDataInstance loopData = dataInstanceService.getDataInstance(miLoop.getLoopDataOutputRef(), flowNodeInstance.getId(),
                                DataInstanceContainer.ACTIVITY_INSTANCE.name());
                        if (outputData != null && loopData != null) {
                            final Serializable value = loopData.getValue();
                            final int index = flowNodeInstance.getLoopCounter();
                            if (value instanceof List<?>) {
                                final List<Serializable> list = (List<Serializable>) value;
                                list.set(index, outputData.getValue());
                            } else {
                                throw new SActivityExecutionException("unable to map the ouput of the multi instanciated activity "
                                        + flowNodeInstance.getName() + " the output loop data named " + loopData.getName() + " is not a list but "
                                        + loopData.getClassName());
                            }
                            final EntityUpdateDescriptor entityUpdateDescriptor = new EntityUpdateDescriptor();
                            entityUpdateDescriptor.addField("value", value);
                            dataInstanceService.updateDataInstance(loopData, entityUpdateDescriptor);
                        }
                    }
                } catch (final SBonitaException e) {
                    throw new SActivityStateExecutionException(e);
                }
            }
        }
    }

    public void mapActors(final SFlowNodeInstance flowNodeInstance, final SFlowElementContainerDefinition processContainer)
            throws SActivityStateExecutionException {
        if (SFlowNodeType.USER_TASK.equals(flowNodeInstance.getType()) || SFlowNodeType.MANUAL_TASK.equals(flowNodeInstance.getType())) {
            try {
                final SHumanTaskDefinition humanTaskDefinition = (SHumanTaskDefinition) processContainer
                        .getFlowNode(flowNodeInstance.getFlowNodeDefinitionId());
                if (humanTaskDefinition != null) {
                    final String actorName = humanTaskDefinition.getActorName();
                    final long processDefinitionId = flowNodeInstance.getLogicalGroup(0);
                    final SUserFilterDefinition sUserFilterDefinition = humanTaskDefinition.getSUserFilterDefinition();
                    if (sUserFilterDefinition != null) {
                        mapUsingUserFilters(flowNodeInstance, humanTaskDefinition, actorName, processDefinitionId, sUserFilterDefinition);
                    } else {
                        mapUsingActors(flowNodeInstance, actorName, processDefinitionId);
                    }
                }
            } catch (final SActivityStateExecutionException e) {
                throw e;
            } catch (final Exception e) {
                throw new SActivityStateExecutionException(e);
            }
        }
    }

    private void mapUsingActors(final SFlowNodeInstance flowNodeInstance, final String actorName, final long processDefinitionId)
            throws SActorNotFoundException, SActivityCreationException {
        final SActor actor = actorMappingService.getActor(actorName, processDefinitionId);
        final SPendingActivityMapping mapping = instanceBuilders.getSPendingActivityMappingBuilder()
                .createNewInstanceForActor(flowNodeInstance.getId(), actor.getId()).done();
        activityInstanceService.addPendingActivityMappings(mapping);
    }

    private void mapUsingUserFilters(final SFlowNodeInstance flowNodeInstance, final SHumanTaskDefinition humanTaskDefinition, final String actorName,
            final long processDefinitionId, final SUserFilterDefinition sUserFilterDefinition) throws ClassLoaderException, SUserFilterExecutionException,
            SActivityStateExecutionException, SActivityCreationException, SFlowNodeNotFoundException, SFlowNodeReadException, SActivityModificationException {
        final ClassLoader processClassloader = classLoaderService.getLocalClassLoader("process", processDefinitionId);
        final SExpressionContext expressionContext = new SExpressionContext(flowNodeInstance.getId(), DataInstanceContainer.ACTIVITY_INSTANCE.name(),
                flowNodeInstance.getLogicalGroup(0));
        final FilterResult result = userFilterService.executeFilter(processDefinitionId, sUserFilterDefinition, sUserFilterDefinition.getInputs(),
                processClassloader, expressionContext, actorName);
        final List<Long> userIds = result.getResult();
        if (userIds == null || userIds.isEmpty() || userIds.contains(0l) || userIds.contains(-1l)) {
            throw new SActivityStateExecutionException("no user id returned by the user filter " + sUserFilterDefinition + " on activity "
                    + humanTaskDefinition.getName());
        }
        for (final Long userId : userIds) {
            final SPendingActivityMapping mapping = instanceBuilders.getSPendingActivityMappingBuilder()
                    .createNewInstanceForUser(flowNodeInstance.getId(), userId).done();
            activityInstanceService.addPendingActivityMappings(mapping);
        }
        if (userIds.size() == 1 && result.shouldAutoAssignTaskIfSingleResult()) {
            activityInstanceService.assignHumanTask(flowNodeInstance.getId(), userIds.get(0));
        }
    }

    public void handleCatchEvents(final SProcessDefinition processDefinition, final SFlowNodeInstance flowNodeInstance) throws SActivityStateExecutionException {
        // handle catch event
        if (flowNodeInstance instanceof SIntermediateCatchEventInstance) {
            final SCatchEventInstance intermediateCatchEventInstance = (SCatchEventInstance) flowNodeInstance;
            // handleEventTriggerInstances(processDefinition, intermediateCatchEventInstance);
            final SFlowElementContainerDefinition processContainer = processDefinition.getProcessContainer();
            final SIntermediateCatchEventDefinition intermediateCatchEventDefinition = (SIntermediateCatchEventDefinition) processContainer
                    .getFlowNode(intermediateCatchEventInstance.getFlowNodeDefinitionId());
            try {
                eventsHandler.handleCatchEvent(processDefinition, intermediateCatchEventDefinition, intermediateCatchEventInstance);
            } catch (final SBonitaException e) {
                throw new SActivityStateExecutionException("unable to handle catch event " + flowNodeInstance, e);
            }
        } else if (flowNodeInstance instanceof SReceiveTaskInstance) {
            final SReceiveTaskInstance receiveTaskInstance = (SReceiveTaskInstance) flowNodeInstance;
            final SFlowElementContainerDefinition processContainer = processDefinition.getProcessContainer();
            final SReceiveTaskDefinition receiveTaskIDefinition = (SReceiveTaskDefinition) processContainer.getFlowNode(receiveTaskInstance
                    .getFlowNodeDefinitionId());
            try {
                eventsHandler.handleCatchMessage(processDefinition, receiveTaskIDefinition, receiveTaskInstance);
            } catch (final SBonitaException e) {
                throw new SActivityStateExecutionException("unable to handle catch event " + flowNodeInstance, e);
            }
        }
    }

    public void handleBoundaryEvent(final SProcessDefinition processDefinition, final SBoundaryEventInstance boundaryInstance)
            throws SActivityStateExecutionException {
        final long activityInstanceId = boundaryInstance.getActivityInstanceId();
        // FIXME: add activity name in SBoundaryEventInstance to avoid the getActivityInstance below
        try {
            final SActivityInstance activityInstance = activityInstanceService.getActivityInstance(activityInstanceId);

            final SActivityDefinition activityDefinition = (SActivityDefinition) processDefinition.getProcessContainer().getFlowNode(
                    activityInstance.getFlowNodeDefinitionId());
            final SBoundaryEventDefinition boundaryEventDefinition = activityDefinition.getBoundaryEventDefinition(boundaryInstance.getName());
            eventsHandler.handleCatchEvent(processDefinition, boundaryEventDefinition, boundaryInstance);
        } catch (final SBonitaException e) {
            throw new SActivityStateExecutionException("unable to handle catch event " + boundaryInstance, e);
        }
    }

    public BEntry<Integer, BEntry<SConnectorInstance, SConnectorDefinition>> getConnectorToExecuteAndFlag(final SProcessDefinition processDefinition,
            final SFlowNodeInstance flowNodeInstance) throws SActivityStateExecutionException {
        try {
            final SFlowElementContainerDefinition processContainer = processDefinition.getProcessContainer();
            final SFlowNodeDefinition flowNodeDefinition = processContainer.getFlowNode(flowNodeInstance.getFlowNodeDefinitionId());
            if (flowNodeDefinition != null) {
                boolean onEnterExecuted = false;
                final List<SConnectorDefinition> connectorsOnEnter = flowNodeDefinition.getConnectors(ConnectorEvent.ON_ENTER);
                if (connectorsOnEnter.size() > 0) {
                    final SConnectorInstance nextConnectorInstanceToExecute = getNextConnectorInstance(flowNodeInstance, ConnectorEvent.ON_ENTER);
                    if (nextConnectorInstanceToExecute != null) {
                        // Have we already executed the 'before on enter' phase?
                        if (nextConnectorInstanceToExecute.getState().equals(ConnectorState.TO_BE_EXECUTED.name())
                                && connectorsOnEnter.get(0).getName().equals(nextConnectorInstanceToExecute.getName())) {
                            // first enter connector
                            return getConnectorWithFlag(nextConnectorInstanceToExecute, connectorsOnEnter.get(0), BEFORE_ON_ENTER | DURING_ON_ENTER);
                            // Or do we have to skip the 'before on enter' phase:
                        } else {
                            // no the first, don't execute before
                            for (final SConnectorDefinition sConnectorDefinition : connectorsOnEnter) {
                                if (sConnectorDefinition.getName().equals(nextConnectorInstanceToExecute.getName())) {
                                    return getConnectorWithFlag(nextConnectorInstanceToExecute, sConnectorDefinition, DURING_ON_ENTER);
                                }
                            }
                            throw new SActivityStateExecutionException("Connector definition of " + nextConnectorInstanceToExecute + " not found on "
                                    + flowNodeInstance);
                        }
                    }
                    // All connectors ON ENTER have already been executed:
                    onEnterExecuted = true;
                }
                // no on enter connector to execute
                final List<SConnectorDefinition> connectorsOnFinish = flowNodeDefinition.getConnectors(ConnectorEvent.ON_FINISH);
                if (connectorsOnFinish.size() > 0) {
                    final SConnectorInstance nextConnectorInstanceToExecute = getNextConnectorInstance(flowNodeInstance, ConnectorEvent.ON_FINISH);
                    if (nextConnectorInstanceToExecute != null) {
                        if (nextConnectorInstanceToExecute.getState().equals(ConnectorState.TO_BE_EXECUTED.name())
                                && connectorsOnFinish.get(0).getName().equals(nextConnectorInstanceToExecute.getName())) {
                            // first finish connector
                            final SConnectorDefinition connectorDefinition = connectorsOnFinish.get(0);
                            if (onEnterExecuted) {
                                // some connectors were already executed
                                return getConnectorWithFlag(nextConnectorInstanceToExecute, connectorDefinition, BEFORE_ON_FINISH | DURING_ON_FINISH);
                            } else {
                                // on finish but the first connector
                                return getConnectorWithFlag(nextConnectorInstanceToExecute, connectorDefinition, BEFORE_ON_ENTER | BEFORE_ON_FINISH
                                        | DURING_ON_FINISH);
                            }
                        } else {
                            // no the first, don't execute before
                            for (final SConnectorDefinition sConnectorDefinition : connectorsOnFinish) {
                                if (sConnectorDefinition.getName().equals(nextConnectorInstanceToExecute.getName())) {
                                    return getConnectorWithFlag(nextConnectorInstanceToExecute, sConnectorDefinition, DURING_ON_FINISH);
                                }
                            }
                            throw new SActivityStateExecutionException("Connector definition of " + nextConnectorInstanceToExecute + " not found on "
                                    + flowNodeInstance);
                        }
                    } else {
                        // all finish connectors executed
                        return new BEntry<Integer, BEntry<SConnectorInstance, SConnectorDefinition>>(AFTER_ON_FINISH, null);
                    }
                }
                // no ON ENTER no ON FINISH active
                if (flowNodeInstance.isStateExecuting()) {
                    // there was a connector executed but no more: execute only before and after finish
                    return new BEntry<Integer, BEntry<SConnectorInstance, SConnectorDefinition>>(BEFORE_ON_FINISH | AFTER_ON_FINISH, null);
                }
            }
            // no connector and was just starting
            return getConnectorWithFlag(null, null, BEFORE_ON_ENTER | BEFORE_ON_FINISH | AFTER_ON_FINISH);
        } catch (final SConnectorInstanceReadException e) {
            throw new SActivityStateExecutionException(e);
        }
    }

    private BEntry<Integer, BEntry<SConnectorInstance, SConnectorDefinition>> getConnectorWithFlag(final SConnectorInstance nextConnectorInstance,
            final SConnectorDefinition connectorDefinition, final int flag) {
        return new BEntry<Integer, BEntry<SConnectorInstance, SConnectorDefinition>>(flag, new BEntry<SConnectorInstance, SConnectorDefinition>(
                nextConnectorInstance, connectorDefinition));
    }

    private SConnectorInstance getNextConnectorInstance(final SFlowNodeInstance flowNodeInstance, final ConnectorEvent event)
            throws SConnectorInstanceReadException {
        final SConnectorInstance connectorInstances = connectorInstanceService.getNextExecutableConnectorInstance(flowNodeInstance.getId(),
                SConnectorInstance.FLOWNODE_TYPE, event);
        return connectorInstances;
    }

    public void createData(final SProcessDefinition processDefinition, final SFlowNodeInstance flowNodeInstance) throws SActivityStateExecutionException {
        boolean childHaveData = false;
        if (flowNodeInstance instanceof SActivityInstance) {
            final String containerType = getParentContainerType(flowNodeInstance).name();
            final SExpressionContext sExpressionContext = new SExpressionContext(flowNodeInstance.getParentContainerId(), containerType,
                    processDefinition.getId());
            childHaveData = bpmInstancesCreator.createDataInstances(processDefinition, flowNodeInstance, sExpressionContext);
        }
        final SFlowNodeDefinition flowNodeDefinition = processDefinition.getProcessContainer().getFlowNode(flowNodeInstance.getFlowNodeDefinitionId());
        if (hasLocalOrInheritedData(processDefinition, childHaveData, flowNodeDefinition)) {
            bpmInstancesCreator.addChildDataContainer(flowNodeInstance);
        }
    }

    private boolean hasLocalOrInheritedData(final SProcessDefinition processDefinition, final boolean childHaveData,
            final SFlowNodeDefinition flowNodeDefinition) {
        // processDefinition.getProcessContainer() is different of flowNodeDefinition.getParentContainer() in the case of a sub process
        boolean hasLocalOrInheritedData = childHaveData || !processDefinition.getProcessContainer().getDataDefinitions().isEmpty();

        // can be null if the task has been added at runtime (sub tasks)
        if (flowNodeDefinition != null) {
            hasLocalOrInheritedData = hasLocalOrInheritedData || !flowNodeDefinition.getParentContainer().getDataDefinitions().isEmpty();
        }
        return hasLocalOrInheritedData;
    }

    public void handleCallActivity(final SProcessDefinition processDefinition, final SFlowNodeInstance flowNodeInstance)
            throws SActivityStateExecutionException {
        if (isCallActivity(flowNodeInstance)) {
            final SFlowElementContainerDefinition processContainer = processDefinition.getProcessContainer();
            try {
                final SCallActivityDefinition callActivity = (SCallActivityDefinition) processContainer.getFlowNode(flowNodeInstance.getFlowNodeDefinitionId());
                if (callActivity == null) {
                    final StringBuilder stb = new StringBuilder("unable to find call activity definition with name '");
                    stb.append(flowNodeInstance.getName());
                    stb.append("' in procecess definition '");
                    stb.append(processDefinition.getId());
                    stb.append("'");
                    throw new SActivityStateExecutionException(stb.toString());
                }

                final SExpressionContext expressionContext = new SExpressionContext(flowNodeInstance.getId(), DataInstanceContainer.ACTIVITY_INSTANCE.name(),
                        processDefinition.getId());
                final String callableElement = (String) expressionResolverService.evaluate(callActivity.getCallableElement(), expressionContext);
                String callableElementVersion = null;
                if (callActivity.getCallableElementVersion() != null) {
                    callableElementVersion = (String) expressionResolverService.evaluate(callActivity.getCallableElementVersion(), expressionContext);
                }

                final long targetProcessDefinitionId = getTargetProcessDefinitionId(callableElement, callableElementVersion);
                instantiateProcess(processDefinition, callActivity, flowNodeInstance, targetProcessDefinitionId);
                final SCallActivityInstance callActivityInstance = (SCallActivityInstance) flowNodeInstance;
                // update token count
                activityInstanceService.setTokenCount(callActivityInstance, callActivityInstance.getTokenCount() + 1);
            } catch (final SBonitaException e) {
                throw new SActivityStateExecutionException(e);
            }
        }
    }

    private long getTargetProcessDefinitionId(final String callableElement, final String callableElementVersion) throws SProcessDefinitionReadException {
        if (callableElementVersion != null) {
            return processDefinitionService.getProcessDefinitionId(callableElement, callableElementVersion);
        } else {
            return processDefinitionService.getLatestProcessDefinitionId(callableElement);
        }
    }

    private boolean isCallActivity(final SFlowNodeInstance flowNodeInstance) {
        return SFlowNodeType.CALL_ACTIVITY.equals(flowNodeInstance.getType());
    }

    private void instantiateProcess(final SProcessDefinition callerProcessDefinition, final SCallActivityDefinition callActivityDefinition,
            final SFlowNodeInstance callActivityInstance, final long targetProcessDefinitionId) throws SActivityStateExecutionException,
            SProcessDefinitionNotFoundException, SProcessDefinitionReadException, WorkRegisterException {
        final long callerProcessDefinitionId = callerProcessDefinition.getId();
        final long callerId = callActivityInstance.getId();
        final List<SOperation> operationList = callActivityDefinition.getDataInputOperations();
        final SExpressionContext context = new SExpressionContext(callerId, DataInstanceContainer.ACTIVITY_INSTANCE.name(), callerProcessDefinitionId);
        final OperationsWithContext operations = new OperationsWithContext(context, operationList);
        final SProcessDefinition targetSProcessDefinition = processDefinitionService.getProcessDefinition(targetProcessDefinitionId);
        final InstantiateProcessWork instantiateProcessWork = new InstantiateProcessWork(targetSProcessDefinition, operations, processExecutor,
                processInstanceService, activityInstanceService, null, logger, bpmInstancesCreator, transactionExecutor);
        instantiateProcessWork.setCallerId(callerId);
        workService.registerWork(instantiateProcessWork);
    }

    public void updateDisplayNameAndDescription(final SProcessDefinition processDefinition, final SFlowNodeInstance flowNodeInstance)
            throws SActivityStateExecutionException {
        try {
            final SFlowElementContainerDefinition processContainer = processDefinition.getProcessContainer();
            final SFlowNodeDefinition flowNode = processContainer.getFlowNode(flowNodeInstance.getFlowNodeDefinitionId());
            if (flowNode != null) {
                final SExpression displayNameExpression = flowNode.getDisplayName();
                final SExpression displayDescriptionExpression = flowNode.getDisplayDescription();
                final SExpressionContext sExpressionContext = new SExpressionContext(flowNodeInstance.getId(), DataInstanceContainer.ACTIVITY_INSTANCE.name(),
                        processDefinition.getId());
                final String displayName;
                if (displayNameExpression != null) {
                    displayName = (String) expressionResolverService.evaluate(displayNameExpression, sExpressionContext);
                } else {
                    displayName = flowNode.getName();
                }
                final String displayDescription;
                if (displayDescriptionExpression != null) {
                    displayDescription = (String) expressionResolverService.evaluate(displayDescriptionExpression, sExpressionContext);
                } else {
                    displayDescription = flowNode.getDescription();
                }
                activityInstanceService.updateDisplayName(flowNodeInstance, displayName);
                activityInstanceService.updateDisplayDescription(flowNodeInstance, displayDescription);
            }
        } catch (final SFlowNodeModificationException e) {
            throw new SActivityStateExecutionException("error while updating display name and description", e);
        } catch (final SExpressionTypeUnknownException e) {
            throw new SActivityStateExecutionException("error while updating display name and description", e);
        } catch (final SExpressionEvaluationException e) {
            throw new SActivityStateExecutionException("error while updating display name and description", e);
        } catch (final SExpressionDependencyMissingException e) {
            throw new SActivityStateExecutionException("error while updating display name and description", e);
        } catch (final SInvalidExpressionException e) {
            throw new SActivityStateExecutionException("error while updating display name and description", e);
        }
    }

    public void updateDisplayDescriptionAfterCompletion(final SProcessDefinition processDefinition, final SFlowNodeInstance flowNodeInstance)
            throws SActivityStateExecutionException {
        try {
            final SFlowElementContainerDefinition processContainer = processDefinition.getProcessContainer();
            final SFlowNodeDefinition flowNode = processContainer.getFlowNode(flowNodeInstance.getFlowNodeDefinitionId());
            if (flowNode != null) {
                final SExpression displayDescriptionAfterCompletionExpression = flowNode.getDisplayDescriptionAfterCompletion();
                final SExpressionContext sExpressionContext = new SExpressionContext(flowNodeInstance.getId(), DataInstanceContainer.ACTIVITY_INSTANCE.name(),
                        processDefinition.getId());
                final String displayDescriptionAfterCompletion;
                if (displayDescriptionAfterCompletionExpression != null) {
                    displayDescriptionAfterCompletion = (String) expressionResolverService.evaluate(displayDescriptionAfterCompletionExpression,
                            sExpressionContext);
                    activityInstanceService.updateDisplayDescription(flowNodeInstance, displayDescriptionAfterCompletion);
                }
            }
        } catch (final SFlowNodeModificationException e) {
            throw new SActivityStateExecutionException("error while updating display name and description", e);
        } catch (final SExpressionTypeUnknownException e) {
            throw new SActivityStateExecutionException("error while updating display name and description", e);
        } catch (final SExpressionEvaluationException e) {
            throw new SActivityStateExecutionException("error while updating display name and description", e);
        } catch (final SExpressionDependencyMissingException e) {
            throw new SActivityStateExecutionException("error while updating display name and description", e);
        } catch (final SInvalidExpressionException e) {
            throw new SActivityStateExecutionException("error while updating display name and description", e);
        }
    }

    public void executeOperations(final SProcessDefinition processDefinition, final SActivityInstance activityInstance) throws SActivityStateExecutionException {
        try {
            final SFlowElementContainerDefinition processContainer = processDefinition.getProcessContainer();
            final SFlowNodeDefinition flowNode = processContainer.getFlowNode(activityInstance.getFlowNodeDefinitionId());
            if (flowNode instanceof SActivityDefinition) {
                final SActivityDefinition activityDefinition = (SActivityDefinition) flowNode;
                final List<SOperation> sOperations = activityDefinition.getSOperations();
                final SExpressionContext sExpressionContext = new SExpressionContext(activityInstance.getId(), DataInstanceContainer.ACTIVITY_INSTANCE.name(),
                        processDefinition.getId());
                operationService.execute(sOperations, sExpressionContext);
            }
        } catch (final SBonitaException e) {
            throw new SActivityStateExecutionException(e);
        }
    }

    public void handleThrowEvent(final SProcessDefinition processDefinition, final SFlowNodeInstance flowNodeInstance) throws SActivityStateExecutionException {

        if (flowNodeInstance instanceof SThrowEventInstance) {
            final SThrowEventInstance throwEventInstance = (SThrowEventInstance) flowNodeInstance;
            final SFlowElementContainerDefinition processContainer = processDefinition.getProcessContainer();
            final SThrowEventDefinition eventDefinition = (SThrowEventDefinition) processContainer.getFlowNode(throwEventInstance.getFlowNodeDefinitionId());
            try {
                eventsHandler.handleThrowEvent(processDefinition, eventDefinition, throwEventInstance);
            } catch (final SBonitaException e) {
                throw new SActivityStateExecutionException("unable to handle throw event " + flowNodeInstance, e);
            }
        } else if (SFlowNodeType.SEND_TASK.equals(flowNodeInstance.getType())) {
            final SSendTaskInstance sendTaskInstance = (SSendTaskInstance) flowNodeInstance;
            final SFlowElementContainerDefinition processContainer = processDefinition.getProcessContainer();
            final SSendTaskDefinition sendTaskDefinition = (SSendTaskDefinition) processContainer.getFlowNode(sendTaskInstance.getFlowNodeDefinitionId());
            try {
                eventsHandler.handleThrowMessage(processDefinition, sendTaskDefinition, sendTaskInstance);
            } catch (final SBonitaException e) {
                throw new SActivityStateExecutionException("unable to handle throw event " + flowNodeInstance, e);
            }
        }
    }

    public void setProcessExecutor(final ProcessExecutor processExecutor) {
        this.processExecutor = processExecutor;
    }

    public void executeChildrenActivities(final SFlowNodeInstance flowNodeInstance) throws SActivityExecutionException {
        try {
            int i = 0;
            List<SActivityInstance> childrenOfAnActivity;
            do {
                childrenOfAnActivity = activityInstanceService.getChildrenOfAnActivity(flowNodeInstance.getId(), i, BATCH_SIZE);
                for (final SActivityInstance sActivityInstance : childrenOfAnActivity) {
                    containerRegistry.executeFlowNode(sActivityInstance.getId(), null, null, SFlowElementsContainerType.FLOWNODE.name(),
                            sActivityInstance.getLogicalGroup(instanceBuilders.getSAAutomaticTaskInstanceBuilder().getParentProcessInstanceIndex()));
                }
                i += BATCH_SIZE;
            } while (childrenOfAnActivity.size() == BATCH_SIZE);
        } catch (final SBonitaException e) {
            throw new SActivityExecutionException(e);
        }
    }

    public void interruptSubActivities(final long parentActivityInstanceId, final SStateCategory stateCategory) throws SBonitaException {
        final int numberOfResults = 100;
        long count = 0;
        List<SActivityInstance> childrenToEnd;
        final SUserTaskInstanceBuilder flowNodeKeyProvider = instanceBuilders.getUserTaskInstanceBuilder();
        do {
            final OrderByOption orderByOption = new OrderByOption(SActivityInstance.class, flowNodeKeyProvider.getNameKey(), OrderByType.ASC);
            final List<FilterOption> filters = new ArrayList<FilterOption>(3);
            filters.add(new FilterOption(SActivityInstance.class, flowNodeKeyProvider.getParentActivityInstanceKey(), parentActivityInstanceId));
            filters.add(new FilterOption(SActivityInstance.class, flowNodeKeyProvider.getTerminalKey(), false));
            filters.add(new FilterOption(SActivityInstance.class, flowNodeKeyProvider.getStateCategoryKey(), SStateCategory.NORMAL.name()));
            final QueryOptions queryOptions = new QueryOptions(0, numberOfResults, Collections.singletonList(orderByOption), filters, null);
            final QueryOptions countOptions = new QueryOptions(0, numberOfResults, null, filters, null);
            childrenToEnd = activityInstanceService.searchActivityInstances(SActivityInstance.class, queryOptions);
            count = activityInstanceService.getNumberOfActivityInstances(SActivityInstance.class, countOptions);
            for (final SActivityInstance child : childrenToEnd) {
                activityInstanceService.setStateCategory(child, stateCategory);
                if (child.isStable()) {
                    containerRegistry.executeFlowNode(child.getId(), null, null, SFlowElementsContainerType.FLOWNODE.name(),
                            child.getLogicalGroup(instanceBuilders.getSAAutomaticTaskInstanceBuilder().getParentProcessInstanceIndex()));
                }
            }

        } while (count > childrenToEnd.size());
    }

    public void executeConnectorInWork(final SProcessDefinition processDefinition, final SFlowNodeInstance flowNodeInstance,
            final SConnectorInstance connector, final SConnectorDefinition sConnectorDefinition) throws SActivityStateExecutionException {
        // TODO this work should be triggered by with the id of the user logged in?

        final SExpressionContext sExpressionContext = new SExpressionContext(flowNodeInstance.getId(), DataInstanceContainer.ACTIVITY_INSTANCE.name(),
                processDefinition.getId());
        Map<String, Object> inputParameters = null;
        try {
            inputParameters = connectorService.evaluateInputParameters(sConnectorDefinition.getInputs(), sExpressionContext, null);
        } catch (final SBonitaException sbe) {
            try {
                final ExecuteConnectorWork work = getWork(processDefinition, flowNodeInstance, connector, sConnectorDefinition, inputParameters);
                work.setErrorThrownWhenEvaluationOfInputParameters(sbe);
                workService.registerWork(work);
            } catch (final WorkRegisterException e) {
                throw new SActivityStateExecutionException("Unable to register the work that execute the connector " + connector + " on " + flowNodeInstance, e);
            }
        }
        if (inputParameters != null) {
            try {
                workService.registerWork(getWork(processDefinition, flowNodeInstance, connector, sConnectorDefinition, inputParameters));
            } catch (final WorkRegisterException e) {
                throw new SActivityStateExecutionException("Unable to register the work that execute the connector " + connector + " on " + flowNodeInstance, e);
            }
        }
    }

    private ExecuteConnectorOfActivity getWork(final SProcessDefinition processDefinition, final SFlowNodeInstance flowNodeInstance,
            final SConnectorInstance connector, final SConnectorDefinition sConnectorDefinition, final Map<String, Object> inputParameters) {
        return new ExecuteConnectorOfActivity(containerRegistry, transactionExecutor, processInstanceService, archiveService, instanceBuilders,
                dataInstanceService, dataInstanceBuilders, activityInstanceService, flowNodeStateManager, classLoaderService, connectorService,
                connectorInstanceService, processDefinition, flowNodeInstance, connector, sConnectorDefinition, inputParameters, eventsHandler,
                bpmInstancesCreator, bpmDefinitionBuilders, eventInstanceService, workService);
    }

    public void createAttachedBoundaryEvents(final SProcessDefinition processDefinition, final SActivityInstance activityInstance)
            throws SActivityStateExecutionException {
        final SActivityDefinition activityDefinition = (SActivityDefinition) processDefinition.getProcessContainer().getFlowNode(
                activityInstance.getFlowNodeDefinitionId());
        if (activityDefinition != null) {
            boolean mustAddBoundaries = true;
            // avoid to add boundary events in children of multi instance
            if (activityDefinition.getLoopCharacteristics() != null
                    && !(SFlowNodeType.MULTI_INSTANCE_ACTIVITY.equals(activityInstance.getType()) || SFlowNodeType.LOOP_ACTIVITY.equals(activityInstance
                            .getType()))) {
                mustAddBoundaries = false;
            }
            if (mustAddBoundaries) {
                final List<SBoundaryEventDefinition> boundaryEventDefinitions = activityDefinition.getBoundaryEventDefinitions();
                if (!boundaryEventDefinitions.isEmpty()) {
                    try {

                        final SBoundaryEventInstanceBuilder boundaryEventInstanceBuilder = bpmInstancesCreator.getBPMInstanceBuilders()
                                .getSBoundaryEventInstanceBuilder();
                        final long rootProcessInstanceId = activityInstance.getLogicalGroup(boundaryEventInstanceBuilder.getRootProcessInstanceIndex());
                        final long parentProcessInstanceId = activityInstance.getLogicalGroup(boundaryEventInstanceBuilder.getParentProcessInstanceIndex());

                        SFlowElementsContainerType containerType = SFlowElementsContainerType.PROCESS;
                        final long parentActivityInstanceId = activityInstance.getLogicalGroup(boundaryEventInstanceBuilder.getParentActivityInstanceIndex());
                        if (parentActivityInstanceId > 0) {
                            containerType = SFlowElementsContainerType.FLOWNODE;
                        }

                        for (final SBoundaryEventDefinition boundaryEventDefinition : boundaryEventDefinitions) {
                            final SFlowNodeInstance boundaryEventInstance = bpmInstancesCreator.createFlowNodeInstance(processDefinition,
                                    rootProcessInstanceId, activityInstance.getParentContainerId(), containerType, boundaryEventDefinition,
                                    rootProcessInstanceId, parentProcessInstanceId, false, -1, SStateCategory.NORMAL, activityInstance.getId(),
                                    activityInstance.getTokenRefId());
                            containerRegistry.executeFlowNodeInSameThread(boundaryEventInstance.getId(), null, null, containerType.name(),
                                    parentProcessInstanceId);
                        }
                    } catch (final SBonitaException e) {
                        throw new SActivityStateExecutionException("Unable to create boundary events attached to activity " + activityInstance.getName(), e);
                    }
                }
            }
        }
    }

    public void interruptAttachedBoundaryEvent(final SProcessDefinition processDefinition, final SActivityInstance activityInstance,
            final SStateCategory categoryState) throws SActivityStateExecutionException {
        final SBoundaryEventInstanceBuilder keyProvider = instanceBuilders.getSBoundaryEventInstanceBuilder();
        try {
            final List<SBoundaryEventInstance> boundaryEventInstances = eventInstanceService.getActivityBoundaryEventInstances(activityInstance.getId());
            for (final SBoundaryEventInstance boundaryEventInstance : boundaryEventInstances) {
                // don't abort boundary event that put this activity in aborting state
                if (activityInstance.getAbortedByBoundary() != boundaryEventInstance.getId()) {
                    final boolean stable = boundaryEventInstance.isStable();
                    final SCatchEventDefinition catchEventDef = processDefinition.getProcessContainer().getBoundaryEvent(boundaryEventInstance.getName());
                    interrupWaitinEvents(processDefinition, boundaryEventInstance, catchEventDef);
                    activityInstanceService.setStateCategory(boundaryEventInstance, categoryState);
                    if (stable) {
                        String containerType = SFlowElementsContainerType.PROCESS.name();
                        final long parentActivityInstanceId = boundaryEventInstance.getLogicalGroup(keyProvider.getParentActivityInstanceIndex());
                        if (parentActivityInstanceId > 0) {
                            containerType = SFlowElementsContainerType.FLOWNODE.name();
                        }
                        containerRegistry.executeFlowNode(boundaryEventInstance.getId(), null, null, containerType,
                                boundaryEventInstance.getLogicalGroup(keyProvider.getParentProcessInstanceIndex()));
                    }
                }
            }
        } catch (final SBonitaException e) {
            throw new SActivityStateExecutionException("Unable cancel boundary events attached to activity " + activityInstance.getName(), e);
        }
    }

    public void interrupWaitinEvents(final SProcessDefinition processDefinition, final SCatchEventInstance catchEventInstance,
            final SCatchEventDefinition catchEventDef) throws SBonitaException {
        interruptTimerEvent(processDefinition, catchEventInstance, catchEventDef);
        // message, signal and error
        interruptWaitingEvents(catchEventInstance.getId(), catchEventDef);
    }

    private void interruptWaitingEvents(final long instanceId, final SCatchEventDefinition catchEventDef) throws SBonitaSearchException,
            SWaitingEventModificationException {
        if (!catchEventDef.getEventTriggers().isEmpty()) {
            final SWaitingEventKeyProvider waitingEventKeyProvider = instanceBuilders.getSWaitingMessageEventBuilder();
            interruptWaitingEvents(instanceId, SWaitingEvent.class, waitingEventKeyProvider);
        }
    }

    public void interrupWaitinEvents(final SProcessDefinition processDefinition, final SReceiveTaskInstance receiveTaskInstance) throws SBonitaException {
        final SWaitingEventKeyProvider waitingEventKeyProvider = instanceBuilders.getSWaitingMessageEventBuilder();
        interruptWaitingEvents(receiveTaskInstance.getId(), SWaitingEvent.class, waitingEventKeyProvider);
    }

    private QueryOptions getWaitingEventsCountOptions(final long instanceId, final SWaitingEventKeyProvider waitingEventKeyProvider,
            final Class<? extends SWaitingEvent> waitingEventClass) {
        final List<FilterOption> filters = getFilterForWaitingEventsToInterrupt(instanceId, waitingEventKeyProvider, waitingEventClass);
        return new QueryOptions(filters, null);
    }

    private QueryOptions getWaitingEventsQueryOptions(final long instanceId, final SWaitingEventKeyProvider waitingEventKeyProvider,
            final Class<? extends SWaitingEvent> waitingEventClass) {
        final OrderByOption orderByOption = new OrderByOption(waitingEventClass, waitingEventKeyProvider.getIdKey(), OrderByType.ASC);
        final List<FilterOption> filters = getFilterForWaitingEventsToInterrupt(instanceId, waitingEventKeyProvider, waitingEventClass);
        return new QueryOptions(0, MAX_NUMBER_OF_RESULTS, Collections.singletonList(orderByOption), filters, null);
    }

    private List<FilterOption> getFilterForWaitingEventsToInterrupt(final long instanceId, final SWaitingEventKeyProvider waitingEventKeyProvider,
            final Class<? extends SWaitingEvent> waitingEventClass) {
        final List<FilterOption> filters = new ArrayList<FilterOption>(2);
        filters.add(new FilterOption(waitingEventClass, waitingEventKeyProvider.getFlowNodeInstanceIdKey(), instanceId));
        filters.add(new FilterOption(waitingEventClass, waitingEventKeyProvider.getActiveKey(), true));
        return filters;
    }

    private <T extends SWaitingEvent> void interruptWaitingEvents(final long instanceId, final Class<T> waitingEventClass,
            final SWaitingEventKeyProvider waitingEventKeyProvider) throws SBonitaSearchException, SWaitingEventModificationException {
        final QueryOptions queryOptions = getWaitingEventsQueryOptions(instanceId, waitingEventKeyProvider, waitingEventClass);
        final QueryOptions countOptions = getWaitingEventsCountOptions(instanceId, waitingEventKeyProvider, waitingEventClass);
        long count = 0;
        List<T> waitingEvents;
        do {
            waitingEvents = eventInstanceService.searchWaitingEvents(waitingEventClass, queryOptions);
            count = eventInstanceService.getNumberOfWaitingEvents(waitingEventClass, countOptions);
            deleWaitingEvents(waitingEvents);
        } while (count > waitingEvents.size());
    }

    private void deleWaitingEvents(final List<? extends SWaitingEvent> waitingEvents) throws SWaitingEventModificationException {
        for (final SWaitingEvent sWaitingEvent : waitingEvents) {
            eventInstanceService.deleteWaitingEvent(sWaitingEvent);
        }
    }

    private void interruptTimerEvent(final SProcessDefinition processDefinition, final SCatchEventInstance catchEventInstance,
            final SCatchEventDefinition catchEventDef) throws SSchedulerException {
        // FIXME to support multiple events change this code
        if (!catchEventDef.getTimerEventTriggerDefinitions().isEmpty()) {
            final String jobName = JobNameBuilder.getTimerEventJobName(processDefinition.getId(), catchEventDef, catchEventInstance);
            final boolean delete = schedulerService.delete(jobName);
            if (!delete) {
                if (logger.isLoggable(EndingIntermediateCatchEventExceptionStateImpl.class, TechnicalLogSeverity.WARNING)) {
                    logger.log(EndingIntermediateCatchEventExceptionStateImpl.class, TechnicalLogSeverity.WARNING, "No job found with name '" + jobName
                            + "' when interrupting timer catch event named '" + catchEventDef.getName() + "' and id '" + catchEventInstance.getId()
                            + "'. It was probably already triggered.");
                }
            }
        }
    }

}
