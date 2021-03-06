package org.bonitasoft.engine.bpm.event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.bpm.BPMServicesBuilder;
import org.bonitasoft.engine.bpm.CommonBPMServicesTest;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.STimerType;
import org.bonitasoft.engine.core.process.instance.api.event.EventInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.SEventInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.model.SActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.builder.BPMInstanceBuilders;
import org.bonitasoft.engine.core.process.instance.model.builder.event.SBoundaryEventInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.event.SEndEventInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.event.SEventInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.event.SIntermediateCatchEventInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.event.SIntermediateThrowEventInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.event.SStartEventInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.event.handling.SWaitingErrorEventBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.event.handling.SWaitingMessageEventBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.event.handling.SWaitingSignalEventBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.event.trigger.SEventTriggerInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.event.trigger.SThrowErrorEventTriggerInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.event.trigger.SThrowMessageEventTriggerInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.event.trigger.SThrowSignalEventTriggerInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.event.trigger.STimerEventTriggerInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.event.SBoundaryEventInstance;
import org.bonitasoft.engine.core.process.instance.model.event.SEndEventInstance;
import org.bonitasoft.engine.core.process.instance.model.event.SEventInstance;
import org.bonitasoft.engine.core.process.instance.model.event.SIntermediateCatchEventInstance;
import org.bonitasoft.engine.core.process.instance.model.event.SIntermediateThrowEventInstance;
import org.bonitasoft.engine.core.process.instance.model.event.SStartEventInstance;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SWaitingEvent;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SWaitingMessageEvent;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SWaitingSignalEvent;
import org.bonitasoft.engine.core.process.instance.model.event.trigger.SEventTriggerInstance;
import org.bonitasoft.engine.core.process.instance.model.event.trigger.SThrowErrorEventTriggerInstance;
import org.bonitasoft.engine.core.process.instance.model.event.trigger.SThrowMessageEventTriggerInstance;
import org.bonitasoft.engine.core.process.instance.model.event.trigger.SThrowSignalEventTriggerInstance;
import org.bonitasoft.engine.core.process.instance.model.event.trigger.STimerEventTriggerInstance;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.transaction.TransactionService;
import org.junit.Test;

/**
 * @author Elias Ricken de Medeiros
 */
public class EventInstanceServiceTest extends CommonBPMServicesTest {

    private final TransactionService transactionService;

    private final EventInstanceService eventInstanceService;

    private final BPMInstanceBuilders bpmInstanceBuilders;

    private final BPMServicesBuilder servicesBuilder;

    public EventInstanceServiceTest() {
        servicesBuilder = getServicesBuilder();
        transactionService = servicesBuilder.getTransactionService();
        eventInstanceService = servicesBuilder.getEventInstanceService();
        bpmInstanceBuilders = servicesBuilder.getBPMInstanceBuilders();
    }

    private void checkStartEventInstance(final SEventInstance expectedEventInstance, final SEventInstance actualEventInstance) {
        assertTrue(actualEventInstance instanceof SStartEventInstance);
        checkEventInstance(expectedEventInstance, actualEventInstance);
    }

    private void checkEndEventInstance(final SEventInstance expectedEventInstance, final SEventInstance actualEventInstance) {
        assertTrue(actualEventInstance instanceof SEndEventInstance);
        checkEventInstance(expectedEventInstance, actualEventInstance);
    }

    private void checkIntermediateCatchEventInstance(final SEventInstance expectedEventInstance, final SEventInstance actualEventInstance) {
        assertTrue(actualEventInstance instanceof SIntermediateCatchEventInstance);
        checkEventInstance(expectedEventInstance, actualEventInstance);
    }

    private void checkBoundaryEventInstance(final SEventInstance expectedEventInstance, final SEventInstance actualEventInstance) {
        assertTrue(actualEventInstance instanceof SBoundaryEventInstance);
        final SBoundaryEventInstance expectedBoundary = (SBoundaryEventInstance) expectedEventInstance;
        final SBoundaryEventInstance actualBoundary = (SBoundaryEventInstance) actualEventInstance;
        assertEquals(expectedBoundary.getActivityInstanceId(), actualBoundary.getActivityInstanceId());
        checkEventInstance(expectedEventInstance, actualEventInstance);
    }

    private void checkIntermediateThrowEventInstance(final SEventInstance expectedEventInstance, final SEventInstance actualEventInstance) {
        assertTrue(actualEventInstance instanceof SIntermediateThrowEventInstance);
        checkEventInstance(expectedEventInstance, actualEventInstance);
    }

    private void checkEventInstance(final SEventInstance expectedEventInstance, final SEventInstance actualEventInstance) {
        final SEndEventInstanceBuilder eventInstanceBuilder = bpmInstanceBuilders.getSEndEventInstanceBuilder();
        final int processDefinitionIndex = eventInstanceBuilder.getProcessDefinitionIndex();
        final int processInstanceIndex = eventInstanceBuilder.getRootProcessInstanceIndex();

        assertEquals(expectedEventInstance.getId(), actualEventInstance.getId());
        assertEquals(expectedEventInstance.getName(), actualEventInstance.getName());
        assertEquals(expectedEventInstance.getParentContainerId(), actualEventInstance.getParentContainerId());
        assertEquals(expectedEventInstance.getStateId(), actualEventInstance.getStateId());
        assertEquals(expectedEventInstance.getLogicalGroup(processDefinitionIndex), actualEventInstance.getLogicalGroup(processDefinitionIndex));
        assertEquals(expectedEventInstance.getLogicalGroup(processInstanceIndex), actualEventInstance.getLogicalGroup(processInstanceIndex));
    }

    private List<SEventInstance> getEventInstances(final SEventInstanceBuilder startEventInstanceBuilder, final long processInstanceId, final int fromIndex,
            final int maxResult) throws SBonitaException {
        return getEventInstances(processInstanceId, fromIndex, maxResult, startEventInstanceBuilder.getNameKey(), OrderByType.ASC);
    }

    private List<SEventInstance> getEventInstances(final long processInstanceId, final int fromIndex, final int maxResult, final String fieldName,
            final OrderByType orderByType) throws SBonitaException {
        transactionService.begin();
        final List<SEventInstance> eventInstances = eventInstanceService.getEventInstances(processInstanceId, fromIndex, maxResult, fieldName, orderByType);
        transactionService.complete();
        return eventInstances;
    }

    private SEventInstance getEventInstance(final long eventId) throws SBonitaException {
        transactionService.begin();
        final SEventInstance eventInstance = eventInstanceService.getEventInstance(eventId);
        transactionService.complete();
        return eventInstance;
    }

    private List<SBoundaryEventInstance> getActiviyBoundaryEventInstances(final long activityId) throws SBonitaException {
        transactionService.begin();
        final List<SBoundaryEventInstance> boundaryEvents = eventInstanceService.getActivityBoundaryEventInstances(activityId);
        transactionService.complete();
        return boundaryEvents;
    }

    private void checkTimerEventTriggerInstance(final STimerEventTriggerInstance expectedTriggerInstance,
            final SEventTriggerInstance retrievedEventTriggerInstance) {
        assertTrue(retrievedEventTriggerInstance instanceof STimerEventTriggerInstance);
        final STimerEventTriggerInstance retrievedTimer = (STimerEventTriggerInstance) retrievedEventTriggerInstance;
        assertEquals(expectedTriggerInstance.getTimerType(), retrievedTimer.getTimerType());
        assertEquals(expectedTriggerInstance.getTimerValue(), retrievedTimer.getTimerValue());
        checkEventTriggerInstance(expectedTriggerInstance, retrievedEventTriggerInstance);
    }

    private void checkEventTriggerInstance(final SEventTriggerInstance expectedTriggerInstance, final SEventTriggerInstance retrievedEventTriggerInstance) {
        assertEquals(expectedTriggerInstance.getId(), retrievedEventTriggerInstance.getId());
        assertEquals(expectedTriggerInstance.getEventInstanceId(), retrievedEventTriggerInstance.getEventInstanceId());
    }

    private STimerEventTriggerInstance createTimerEventTriggerInstance(final STimerEventTriggerInstanceBuilder timerEventTriggerInstanceBuilder,
            final long eventInstanceId, final STimerType timerType, final long timerValue) throws SBonitaException {
        final STimerEventTriggerInstance triggerInstance = timerEventTriggerInstanceBuilder.createNewTimerEventTriggerInstance(eventInstanceId, timerType,
                timerValue).done();
        createEventTriggerInstance(triggerInstance);
        return triggerInstance;
    }

    private SThrowMessageEventTriggerInstance createThrowMessageEventTriggerInstance(final SThrowMessageEventTriggerInstanceBuilder messageTriggerBuilder,
            final long eventInstanceId, final String messageName, final String targetProcess, final String targetFlowNode) throws SBonitaException {
        final SThrowMessageEventTriggerInstance messageTrigger = messageTriggerBuilder.createNewInstance(eventInstanceId, messageName, targetProcess,
                targetFlowNode).done();
        createEventTriggerInstance(messageTrigger);
        return messageTrigger;
    }

    private SThrowSignalEventTriggerInstance createThrowSignalEventTriggerInstance(final SThrowSignalEventTriggerInstanceBuilder signalTriggerBuilder,
            final long eventInstanceId, final String signalName) throws SBonitaException {
        final SThrowSignalEventTriggerInstance signalTrigger = signalTriggerBuilder.createNewInstance(eventInstanceId, signalName).done();
        createEventTriggerInstance(signalTrigger);
        return signalTrigger;
    }

    private SThrowErrorEventTriggerInstance createThrowErrorEventTriggerInstance(final SThrowErrorEventTriggerInstanceBuilder errorTriggerBuilder,
            final long eventInstanceId, final String errorCode) throws SBonitaException {
        final SThrowErrorEventTriggerInstance errorTriggerInstance = errorTriggerBuilder.createNewInstance(eventInstanceId, errorCode).done();
        createEventTriggerInstance(errorTriggerInstance);
        return errorTriggerInstance;
    }

    private void createEventTriggerInstance(final SEventTriggerInstance triggerInstance) throws SBonitaException {
        transactionService.begin();
        eventInstanceService.createEventTriggerInstance(triggerInstance);
        transactionService.complete();
    }

    private void createWaitingEvent(final SWaitingEvent waitingEvent) throws SBonitaException {
        transactionService.begin();
        eventInstanceService.createWaitingEvent(waitingEvent);
        transactionService.complete();
    }

    private SEventInstance createBoundaryEventInstance(final SBoundaryEventInstanceBuilder eventInstanceBuilder, final String eventName,
            final long flowNodeDefinitionId, final long rootProcessInstanceId, final long processDefinitionId, final long parentProcessInstanceId,
            final long activityInstanceId, boolean isInterrupting) throws SBonitaException {
        final SEventInstance eventInstance = eventInstanceBuilder.createNewBoundaryEventInstance(eventName, isInterrupting, flowNodeDefinitionId,
                rootProcessInstanceId, parentProcessInstanceId, processDefinitionId, rootProcessInstanceId, parentProcessInstanceId, activityInstanceId).done();
        createSEventInstance(eventInstance);
        return eventInstance;
    }

    @Test
    public void testCreateAndRetrieveStartEventInstanceFromRootContainer() throws Exception {
        final SStartEventInstanceBuilder startEventInstanceBuilder = bpmInstanceBuilders.getSStartEventInstanceBuilder();
        final SProcessInstance processInstance = createSProcessInstance();

        List<SEventInstance> eventInstances = getEventInstances(startEventInstanceBuilder, processInstance.getId(), 0, 5);
        assertTrue(eventInstances.isEmpty());

        final SEventInstance startEventInstance = createSStartEventInstance(startEventInstanceBuilder, "startEvent", 1, processInstance.getId(), 5,
                processInstance.getId());
        eventInstances = getEventInstances(startEventInstanceBuilder, processInstance.getId(), 0, 5);

        assertEquals(1, eventInstances.size());
        checkStartEventInstance(startEventInstance, eventInstances.get(0));

        deleteSProcessInstance(processInstance);

        eventInstances = getEventInstances(startEventInstanceBuilder, processInstance.getId(), 0, 5);
        assertTrue(eventInstances.isEmpty());
    }

    @Test
    public void testCreateAndRetrieveEndEventInstance() throws Exception {
        final SEndEventInstanceBuilder eventInstanceBuilder = bpmInstanceBuilders.getSEndEventInstanceBuilder();
        final SProcessInstance processInstance = createSProcessInstance();

        List<SEventInstance> eventInstances = getEventInstances(eventInstanceBuilder, processInstance.getId(), 0, 5);
        assertTrue(eventInstances.isEmpty());

        final SEventInstance eventInstance = createSEndEventInstance(eventInstanceBuilder, "EndEvent", 1, processInstance.getId(), 5, processInstance.getId());
        eventInstances = getEventInstances(eventInstanceBuilder, processInstance.getId(), 0, 5);

        assertEquals(1, eventInstances.size());
        checkEndEventInstance(eventInstance, eventInstances.get(0));

        deleteSProcessInstance(processInstance);

        eventInstances = getEventInstances(eventInstanceBuilder, processInstance.getId(), 0, 5);
        assertTrue(eventInstances.isEmpty());
    }

    @Test
    public void testCreateAndRetrieveIntermediateCatchEventInstance() throws Exception {
        final SIntermediateCatchEventInstanceBuilder eventInstanceBuilder = bpmInstanceBuilders.getSIntermediateCatchEventInstanceBuilder();
        final SProcessInstance processInstance = createSProcessInstance();

        List<SEventInstance> eventInstances = getEventInstances(eventInstanceBuilder, processInstance.getId(), 0, 5);
        assertTrue(eventInstances.isEmpty());

        final SEventInstance eventInstance = createSIntermediateCatchEventInstance(eventInstanceBuilder, "IntermediateCatchEvent", 1, processInstance.getId(),
                5,
                processInstance.getId());
        eventInstances = getEventInstances(eventInstanceBuilder, processInstance.getId(), 0, 5);

        assertEquals(1, eventInstances.size());
        checkIntermediateCatchEventInstance(eventInstance, eventInstances.get(0));

        deleteSProcessInstance(processInstance);

        eventInstances = getEventInstances(eventInstanceBuilder, processInstance.getId(), 0, 5);
        assertTrue(eventInstances.isEmpty());
    }

    @Test
    public void testCreateAndRetrieveBoundaryEventInstance() throws Exception {
        final SBoundaryEventInstanceBuilder eventInstanceBuilder = bpmInstanceBuilders.getSBoundaryEventInstanceBuilder();
        final SProcessInstance processInstance = createSProcessInstance();

        List<SEventInstance> eventInstances = getEventInstances(eventInstanceBuilder, processInstance.getId(), 0, 5);
        assertTrue(eventInstances.isEmpty());

        final int activityInstanceId = 10;
        final SEventInstance eventInstance = createBoundaryEventInstance(eventInstanceBuilder, "BoundaryEvent", 1, processInstance.getId(), 5,
                processInstance.getId(), activityInstanceId, true);
        eventInstances = getEventInstances(eventInstanceBuilder, processInstance.getId(), 0, 5);

        assertEquals(1, eventInstances.size());
        checkBoundaryEventInstance(eventInstance, eventInstances.get(0));

        deleteSProcessInstance(processInstance);

        eventInstances = getEventInstances(eventInstanceBuilder, processInstance.getId(), 0, 5);
        assertTrue(eventInstances.isEmpty());
    }

    @Test
    public void testGetActivityBoundaryEventInstances() throws Exception {
        final SBoundaryEventInstanceBuilder eventInstanceBuilder = bpmInstanceBuilders.getSBoundaryEventInstanceBuilder();
        final SProcessInstance processInstance = createSProcessInstance();
        final long processDefinitionId = 5;
        final SActivityInstance automaticTaskInstance = createSAutomaticTaskInstance(bpmInstanceBuilders.getSAutomaticTaskInstanceBuilder(), "auto1",
                1, processInstance.getId(), processDefinitionId, processInstance.getId());
        final long activityInstanceId = automaticTaskInstance.getId();

        List<SBoundaryEventInstance> boundaryEventInstances = getActiviyBoundaryEventInstances(activityInstanceId);
        assertTrue(boundaryEventInstances.isEmpty());

        final SEventInstance eventInstance1 = createBoundaryEventInstance(eventInstanceBuilder, "BoundaryEvent1", 2, processInstance.getId(),
                processDefinitionId,
                processInstance.getId(), activityInstanceId, true);
        final SEventInstance eventInstance2 = createBoundaryEventInstance(eventInstanceBuilder, "BoundaryEvent2", 3, processInstance.getId(),
                processDefinitionId,
                processInstance.getId(), activityInstanceId, true);

        boundaryEventInstances = getActiviyBoundaryEventInstances(activityInstanceId);
        assertEquals(2, boundaryEventInstances.size());
        checkBoundaryEventInstance(eventInstance1, boundaryEventInstances.get(0));
        checkBoundaryEventInstance(eventInstance2, boundaryEventInstances.get(1));

        deleteSProcessInstance(processInstance);

        boundaryEventInstances = getActiviyBoundaryEventInstances(activityInstanceId);
        assertTrue(boundaryEventInstances.isEmpty());
    }

    @Test
    public void testCreateAndRetrieveIntermediateThrowEventInstance() throws Exception {
        final SIntermediateThrowEventInstanceBuilder eventInstanceBuilder = bpmInstanceBuilders.getSIntermediateThrowEventInstanceBuilder();
        final SProcessInstance processInstance = createSProcessInstance();

        List<SEventInstance> eventInstances = getEventInstances(eventInstanceBuilder, processInstance.getId(), 0, 5);
        assertTrue(eventInstances.isEmpty());

        final SEventInstance eventInstance = createSIntermediateThrowEventInstance(eventInstanceBuilder, "IntermediateThrowEvent", 1, processInstance.getId(),
                5,
                processInstance.getId());
        eventInstances = getEventInstances(eventInstanceBuilder, processInstance.getId(), 0, 5);

        assertEquals(1, eventInstances.size());
        checkIntermediateThrowEventInstance(eventInstance, eventInstances.get(0));

        deleteSProcessInstance(processInstance);

        eventInstances = getEventInstances(eventInstanceBuilder, processInstance.getId(), 0, 5);
        assertTrue(eventInstances.isEmpty());
    }

    @Test
    public void testGetEventInstanceById() throws SBonitaException {
        final SStartEventInstanceBuilder startEventInstanceBuilder = bpmInstanceBuilders.getSStartEventInstanceBuilder();
        final SProcessInstance processInstance = createSProcessInstance();
        final SEventInstance startEventInstance = createSStartEventInstance(startEventInstanceBuilder, "startEvent", 1, processInstance.getId(), 5,
                processInstance.getId());

        final SEventInstance retrievedEventInstance = getEventInstance(startEventInstance.getId());

        checkStartEventInstance(startEventInstance, retrievedEventInstance);

        deleteSProcessInstance(processInstance);
    }

    @Test(expected = SEventInstanceNotFoundException.class)
    public void testCannotRetrieveEventUsingInvalidId() throws SBonitaException {
        getEventInstance(100000L);
    }

    @Test
    public void testGetEventInstancesOrderByNameAsc() throws SBonitaException {
        final SEndEventInstanceBuilder eventInstanceBuilder = bpmInstanceBuilders.getSEndEventInstanceBuilder();
        final SProcessInstance processInstance = createSProcessInstance();

        final SEventInstance eventInstance1 = createSEndEventInstance(eventInstanceBuilder, "EndEvent1", 1, processInstance.getId(), 5, processInstance.getId());
        final SEventInstance eventInstance2 = createSEndEventInstance(eventInstanceBuilder, "EndEvent2", 1, processInstance.getId(), 5, processInstance.getId());
        final List<SEventInstance> eventInstances = getEventInstances(processInstance.getId(), 0, 5, eventInstanceBuilder.getNameKey(), OrderByType.ASC);

        assertEquals(2, eventInstances.size());
        checkEndEventInstance(eventInstance1, eventInstances.get(0));
        checkEndEventInstance(eventInstance2, eventInstances.get(1));

        deleteSProcessInstance(processInstance);
    }

    @Test
    public void testGetEventInstancesOrderByNameDesc() throws SBonitaException {
        final SEndEventInstanceBuilder eventInstanceBuilder = bpmInstanceBuilders.getSEndEventInstanceBuilder();
        final SProcessInstance processInstance = createSProcessInstance();

        final SEventInstance eventInstance1 = createSEndEventInstance(eventInstanceBuilder, "EndEvent1", 1, processInstance.getId(), 5, processInstance.getId());
        final SEventInstance eventInstance2 = createSEndEventInstance(eventInstanceBuilder, "EndEvent2", 1, processInstance.getId(), 5, processInstance.getId());
        final List<SEventInstance> eventInstances = getEventInstances(processInstance.getId(), 0, 5, eventInstanceBuilder.getNameKey(), OrderByType.DESC);

        assertEquals(2, eventInstances.size());
        checkEndEventInstance(eventInstance2, eventInstances.get(0));
        checkEndEventInstance(eventInstance1, eventInstances.get(1));

        deleteSProcessInstance(processInstance);
    }

    @Test(expected = SEventInstanceNotFoundException.class)
    public void testDeleteProcessInstanceAlsoDeleteEventInstance() throws SBonitaException {
        final SStartEventInstanceBuilder startEventInstanceBuilder = bpmInstanceBuilders.getSStartEventInstanceBuilder();
        final SProcessInstance processInstance = createSProcessInstance();
        final SEventInstance startEventInstance = createSStartEventInstance(startEventInstanceBuilder, "startEvent", 1, processInstance.getId(), 5,
                processInstance.getId());

        final SEventInstance retrievedEventInstance = getEventInstance(startEventInstance.getId());

        checkStartEventInstance(startEventInstance, retrievedEventInstance);

        deleteSProcessInstance(processInstance);

        getEventInstance(startEventInstance.getId());
    }

    @Test
    public void testCreateAndRetrieveEventTriggerInstance() throws SBonitaException {
        final SStartEventInstanceBuilder startEventInstanceBuilder = bpmInstanceBuilders.getSStartEventInstanceBuilder();
        final SProcessInstance processInstance = createSProcessInstance();
        final SEventInstance startEventInstance = createSStartEventInstance(startEventInstanceBuilder, "startEvent", 1, processInstance.getId(), 5,
                processInstance.getId());

        final STimerEventTriggerInstanceBuilder timerEventTriggerInstanceBuilder = bpmInstanceBuilders.getSTimerEventTriggerInstanceBuilder();

        List<SEventTriggerInstance> triggerInstances = getEventTriggerInstances(startEventInstance.getId(), 0, 5, timerEventTriggerInstanceBuilder);
        assertTrue(triggerInstances.isEmpty());

        final STimerEventTriggerInstance triggerInstance = createTimerEventTriggerInstance(timerEventTriggerInstanceBuilder, startEventInstance.getId(),
                STimerType.DURATION, 1000);
        triggerInstances = getEventTriggerInstances(startEventInstance.getId(), 0, 5, timerEventTriggerInstanceBuilder);
        assertEquals(1, triggerInstances.size());
        checkTimerEventTriggerInstance(triggerInstance, triggerInstances.get(0));

        deleteSProcessInstance(processInstance);

    }

    private List<SEventTriggerInstance> getEventTriggerInstances(final long eventInstanceId, final int fromIndex, final int maxResults,
            final SEventTriggerInstanceBuilder eventTriggerInstanceBuilder) throws SBonitaException {
        transactionService.begin();
        final List<SEventTriggerInstance> eventTriggerInstances = eventInstanceService.getEventTriggerInstances(eventInstanceId, fromIndex, maxResults,
                eventTriggerInstanceBuilder.getIdKey(), OrderByType.ASC);
        transactionService.complete();
        return eventTriggerInstances;
    }

    private <T extends SWaitingEvent> List<T> searchWaitingEvents(final Class<T> clazz, final QueryOptions searchOptions) throws SBonitaException {
        transactionService.begin();
        final List<T> waitingEvents = eventInstanceService.searchWaitingEvents(clazz, searchOptions);
        transactionService.complete();
        return waitingEvents;
    }

    private long getNumberOfWaitingEvents(final Class<? extends SWaitingEvent> clazz, final QueryOptions countOptions) throws SBonitaException {
        transactionService.begin();
        final long nbOfwaitingEvents = eventInstanceService.getNumberOfWaitingEvents(clazz, countOptions);
        transactionService.complete();
        return nbOfwaitingEvents;
    }

    private <T extends SEventTriggerInstance> List<T> searchEventTrigger(final Class<T> clazz, final QueryOptions searchOptions) throws SBonitaException {
        transactionService.begin();
        final List<T> eventTriggerInstances = eventInstanceService.searchEventTriggerInstances(clazz, searchOptions);
        transactionService.complete();
        return eventTriggerInstances;
    }

    private long getNumberOfEventTriggerInstances(final Class<? extends SEventTriggerInstance> clazz, final QueryOptions countOptions) throws SBonitaException {
        transactionService.begin();
        final long nbOfEventTriggerInstances = eventInstanceService.getNumberOfEventTriggerInstances(clazz, countOptions);
        transactionService.complete();
        return nbOfEventTriggerInstances;
    }

    @Test
    public void testRetrieveEventTriggerInstanceById() throws SBonitaException {
        final SStartEventInstanceBuilder startEventInstanceBuilder = bpmInstanceBuilders.getSStartEventInstanceBuilder();
        final SProcessInstance processInstance = createSProcessInstance();
        final SEventInstance startEventInstance = createSStartEventInstance(startEventInstanceBuilder, "startEvent", 1, processInstance.getId(), 5,
                processInstance.getId());

        final STimerEventTriggerInstanceBuilder timerEventTriggerInstanceBuilder = bpmInstanceBuilders.getSTimerEventTriggerInstanceBuilder();

        final STimerEventTriggerInstance triggerInstance = createTimerEventTriggerInstance(timerEventTriggerInstanceBuilder, startEventInstance.getId(),
                STimerType.DURATION, 1000);
        final SEventTriggerInstance retrievedEventTrigger = getEventTrigger(triggerInstance.getId());
        checkTimerEventTriggerInstance(triggerInstance, retrievedEventTrigger);

        deleteSProcessInstance(processInstance);
    }

    private SEventTriggerInstance getEventTrigger(final long triggerEventInstanceId) throws SBonitaException {
        transactionService.begin();
        final SEventTriggerInstance eventTriggerInstance = eventInstanceService.getEventTriggerInstance(triggerEventInstanceId);
        transactionService.complete();
        return eventTriggerInstance;
    }

    private List<SEventTriggerInstance> getEventTriggers(final long eventInstanceId, final int fromIndex, final int maxResults, final String fieldName,
            final OrderByType orderByType) throws SBonitaException {
        transactionService.begin();
        final List<SEventTriggerInstance> eventTriggerInstances = eventInstanceService.getEventTriggerInstances(eventInstanceId, fromIndex, maxResults,
                fieldName, orderByType);
        transactionService.complete();
        return eventTriggerInstances;
    }

    public void testDeleteEventInstanceAlsoDeleteEventTriggerInstance() throws SBonitaException {
        final SStartEventInstanceBuilder startEventInstanceBuilder = bpmInstanceBuilders.getSStartEventInstanceBuilder();
        final SProcessInstance processInstance = createSProcessInstance();
        final SEventInstance startEventInstance = createSStartEventInstance(startEventInstanceBuilder, "startEvent", 1, processInstance.getId(), 5,
                processInstance.getId());

        final STimerEventTriggerInstanceBuilder timerEventTriggerInstanceBuilder = bpmInstanceBuilders.getSTimerEventTriggerInstanceBuilder();

        createTimerEventTriggerInstance(timerEventTriggerInstanceBuilder, startEventInstance.getId(), STimerType.DURATION, 1000);

        final SEventTriggerInstanceBuilder eventTriggerKeyProvider = bpmInstanceBuilders.getSThrowMessageEventTriggerInstanceBuilder();

        List<SEventTriggerInstance> eventTriggers = getEventTriggers(startEventInstance.getId(), 0, 10, eventTriggerKeyProvider.getEventInstanceIdKey(),
                OrderByType.ASC);
        assertEquals(1, eventTriggers.size());

        deleteSProcessInstance(processInstance);

        eventTriggers = getEventTriggers(startEventInstance.getId(), 0, 10, eventTriggerKeyProvider.getEventInstanceIdKey(), OrderByType.ASC);
        assertEquals(0, eventTriggers.size());
    }

    @Test
    public void testSearchWaitingEvents() throws SBonitaException {
        final SProcessInstance processInstance = createSProcessInstance();
        final SWaitingErrorEventBuilder waitingErrorEventBuilder = bpmInstanceBuilders.getSWaitingErrorEventBuilder();

        final SEventInstance eventInstance = createSIntermediateCatchEventInstance(bpmInstanceBuilders.getSIntermediateCatchEventInstanceBuilder(),
                "itermediate", 1, processInstance.getId(), 5, processInstance.getId());

        final Class<SWaitingEvent> waitingEventClass = SWaitingEvent.class;
        final String processDefinitionIdKey = waitingErrorEventBuilder.getProcessDefinitionIdKey();
        final String flowNodeInstanceIdKey = waitingErrorEventBuilder.getFlowNodeInstanceIdKey();
        final long eventInstanceId = eventInstance.getId();
        checkWaitingEvents(0, waitingEventClass, processDefinitionIdKey, flowNodeInstanceIdKey, eventInstanceId);

        final SWaitingMessageEventBuilder waitingMessageBuilder = bpmInstanceBuilders.getSWaitingMessageEventBuilder();
        final SWaitingMessageEvent messageWaitingEvent = waitingMessageBuilder.createNewWaitingMessageIntermediateEventInstance(5, processInstance.getId(),
                eventInstanceId, "m1", processInstance.getName(), eventInstance.getFlowNodeDefinitionId(), eventInstance.getName()).done();
        createWaitingEvent(messageWaitingEvent);

        final SWaitingSignalEventBuilder waitingSignalEventBuilder = bpmInstanceBuilders.getSWaitingSignalEventBuilder();
        final SWaitingSignalEvent waitingSignalEvent = waitingSignalEventBuilder.createNewWaitingSignalIntermediateEventInstance(5, processInstance.getId(),
                eventInstanceId, "go", processInstance.getName(), eventInstance.getFlowNodeDefinitionId(), eventInstance.getName()).done();
        createWaitingEvent(waitingSignalEvent);

        // search with SWaitingEvent
        checkWaitingEvents(2, waitingEventClass, processDefinitionIdKey, flowNodeInstanceIdKey, eventInstanceId);

        // search with SWaitingMessageEvent, SWaitingSignalEvent
        checkWaitingEvents(1, SWaitingMessageEvent.class, processDefinitionIdKey, flowNodeInstanceIdKey, eventInstanceId);
        checkWaitingEvents(1, SWaitingSignalEvent.class, processDefinitionIdKey, flowNodeInstanceIdKey, eventInstanceId);

        deleteSProcessInstance(processInstance);

        // checkWaitingEvents(0, waitingEventClass, processDefinitionIdKey, flowNodeInstanceIdKey, eventInstanceId);
    }

    private void checkWaitingEvents(final int expectedNbOfWaitingEvents, final Class<? extends SWaitingEvent> clazz, final String processDefinitionIdKey,
            final String flowNodeInstanceIdKey, final long eventInstanceId) throws SBonitaException {
        final int maxResults = Math.max(expectedNbOfWaitingEvents + 1, 10);
        final QueryOptions queryOptions = getQueryOptions(clazz, 0, maxResults, processDefinitionIdKey, OrderByType.ASC, flowNodeInstanceIdKey, eventInstanceId);
        final QueryOptions countOptions = getCountOptions(clazz, flowNodeInstanceIdKey, eventInstanceId);
        final List<? extends SWaitingEvent> waitingErrorEvents = searchWaitingEvents(clazz, queryOptions);
        final long numberOfWaitingErrorEvents = getNumberOfWaitingEvents(clazz, countOptions);
        assertEquals(expectedNbOfWaitingEvents, numberOfWaitingErrorEvents);
        assertEquals(expectedNbOfWaitingEvents, waitingErrorEvents.size());
    }

    private QueryOptions getQueryOptions(final Class<? extends PersistentObject> clazz, final int fromIndex, final int maxResult, final String orderByField,
            final OrderByType orderByType, final String filterKey, final Object filterValue) {
        final OrderByOption orderByOption = new OrderByOption(clazz, orderByField, orderByType);
        final FilterOption filterOption = new FilterOption(clazz, filterKey, filterValue);
        final QueryOptions boundaryQueryOptions = new QueryOptions(fromIndex, maxResult, Collections.singletonList(orderByOption),
                Collections.singletonList(filterOption), null);
        return boundaryQueryOptions;
    }

    private QueryOptions getCountOptions(final Class<? extends PersistentObject> clazz, final String filterKey, final Object filterValue) {
        final FilterOption filterOption = new FilterOption(clazz, filterKey, filterValue);
        final List<OrderByOption> emptyOrderByOptions = Collections.emptyList();
        final QueryOptions countOptions = new QueryOptions(0, 1, emptyOrderByOptions, Collections.singletonList(filterOption), null);
        return countOptions;
    }

    private void checkEventTriggerInstances(final int exptectedNbOfTrigger, final Class<? extends SEventTriggerInstance> clazz,
            final String eventInstanceIdKey, final long eventInstanceId) throws SBonitaException {
        final int maxResults = Math.max(10, exptectedNbOfTrigger + 1);
        final QueryOptions queryOptions = getQueryOptions(clazz, 0, maxResults, eventInstanceIdKey, OrderByType.ASC, eventInstanceIdKey, eventInstanceId);
        final QueryOptions countOptions = getCountOptions(clazz, eventInstanceIdKey, eventInstanceId);
        final List<? extends SEventTriggerInstance> triggers = searchEventTrigger(clazz, queryOptions);
        final long nbOfTriggers = getNumberOfEventTriggerInstances(clazz, countOptions);
        assertEquals(exptectedNbOfTrigger, nbOfTriggers);
        assertEquals(exptectedNbOfTrigger, triggers.size());
    }

    @Test
    public void testSearchEventTriggerInstances() throws SBonitaException {
        final SProcessInstance processInstance = createSProcessInstance();
        final STimerEventTriggerInstanceBuilder timerTriggerBuilder = bpmInstanceBuilders.getSTimerEventTriggerInstanceBuilder();

        final SEventInstance eventInstance = createSEndEventInstance(bpmInstanceBuilders.getSEndEventInstanceBuilder(), "end", 1, processInstance.getId(), 5,
                processInstance.getId());
        final long eventInstanceId = eventInstance.getId();

        final Class<SEventTriggerInstance> triggerInstanceClass = SEventTriggerInstance.class;
        final String eventInstanceIdKey = timerTriggerBuilder.getEventInstanceIdKey();
        checkEventTriggerInstances(0, triggerInstanceClass, eventInstanceIdKey, eventInstanceId);

        createTimerEventTriggerInstance(timerTriggerBuilder, eventInstanceId, STimerType.DURATION, 1000);
        createThrowMessageEventTriggerInstance(bpmInstanceBuilders.getSThrowMessageEventTriggerInstanceBuilder(), eventInstanceId, "m1", "p2", "start1");
        createThrowSignalEventTriggerInstance(bpmInstanceBuilders.getSThrowSignalEventTriggerInstanceBuilder(), eventInstanceId, "s1");
        createThrowErrorEventTriggerInstance(bpmInstanceBuilders.getSThrowErrorEventTriggerInstanceBuilder(), eventInstanceId, "e1");

        // search with STriggerEventInstance
        checkEventTriggerInstances(4, triggerInstanceClass, eventInstanceIdKey, eventInstanceId);

        // search with STimerEventTriggerInstance, SThrowMessageEventTriggerInstance, SThrowSignalEventTriggerInstance, SThrowErrorEventTriggerInstance
        checkEventTriggerInstances(1, STimerEventTriggerInstance.class, eventInstanceIdKey, eventInstanceId);
        checkEventTriggerInstances(1, SThrowMessageEventTriggerInstance.class, eventInstanceIdKey, eventInstanceId);
        checkEventTriggerInstances(1, SThrowSignalEventTriggerInstance.class, eventInstanceIdKey, eventInstanceId);
        checkEventTriggerInstances(1, SThrowErrorEventTriggerInstance.class, eventInstanceIdKey, eventInstanceId);

        deleteSProcessInstance(processInstance);
        checkEventTriggerInstances(0, triggerInstanceClass, eventInstanceIdKey, eventInstanceId);

    }

}
