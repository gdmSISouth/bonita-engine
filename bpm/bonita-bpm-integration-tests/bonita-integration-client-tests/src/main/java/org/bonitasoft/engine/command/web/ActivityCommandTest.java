package org.bonitasoft.engine.command.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.bonitasoft.engine.BPMRemoteTests;
import org.bonitasoft.engine.CommonAPITest;
import org.bonitasoft.engine.api.CommandAPI;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.bar.BarResource;
import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.bpm.data.DataInstance;
import org.bonitasoft.engine.bpm.flownode.ActivityInstance;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.UserTaskDefinitionBuilder;
import org.bonitasoft.engine.command.CommandExecutionException;
import org.bonitasoft.engine.command.CommandNotFoundException;
import org.bonitasoft.engine.command.CommandParameterizationException;
import org.bonitasoft.engine.connectors.TestConnectorWithOutput;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.expression.InvalidExpressionException;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.io.IOUtil;
import org.bonitasoft.engine.operation.Operation;
import org.bonitasoft.engine.operation.OperationBuilder;
import org.bonitasoft.engine.operation.OperatorType;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ActivityCommandTest extends CommonAPITest {

    private static final String CONNECTOR_WITH_OUTPUT_ID = "org.bonitasoft.connector.testConnectorWithOutput";

    private static final String COMMAND_EXECUTE_OPERATIONS_AND_TERMINATE = "executeActionsAndTerminate";

    private User businessUser;

    @Before
    public void before() throws Exception {
        login();
        businessUser = createUser(USERNAME, PASSWORD);
        logout();
        loginWith(USERNAME, PASSWORD);
    }

    @After
    public void after() throws BonitaException, BonitaHomeNotSetException {
        deleteUser(businessUser.getId());
        logout();
    }

    @Cover(classes = { ProcessAPI.class, ActivityInstance.class, DataInstance.class }, concept = BPMNConcept.DATA, keywords = { "Data", "Transient", "Update",
            "Connector", "Retrieve value", "Human task", "Command", "Activity" }, jira = "ENGINE-1260")
    @Test
    public void executeActionsAndTerminateAndUpdateDataTransientOnActivityInstanceWithConnectorOnFinish() throws Exception {
        final String updatedValue = "afterUpdate";

        final BusinessArchive businessArchive = buildBusinessArchiveWithDataTransientAndConnector();
        final ProcessDefinition processDefinition = deployAndEnableWithActor(businessArchive, ACTOR_NAME, businessUser);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final long activityInstanceId = waitForUserTaskAndAssigneIt("step1", processInstance.getId(), getSession().getUserId()).getId();

        // Execute it with operation using the command to update data instance
        final Map<String, Serializable> fieldValues = new HashMap<String, Serializable>();
        fieldValues.put("field_fieldId1", updatedValue);
        final Expression rightOperand = new ExpressionBuilder().createInputExpression("field_fieldId1", String.class.getName());
        executeActionsAndTerminate("dataName", activityInstanceId, fieldValues, rightOperand);

        // Get value of updated data in connector
        waitForUserTask("step2", processInstance).getId();
        assertEquals(updatedValue + "a", getProcessAPI().getProcessDataInstance("application", processInstance.getId()).getValue());

        // Clean
        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = CommandAPI.class, concept = BPMNConcept.ACTIVITIES, keywords = { "Command", "Activity", "Action" }, story = "Execute actions and terminate with custom jar.", jira = "ENGINE-928")
    @Test
    public void executeActionsAndTerminateWithCustomJarInOperation() throws Exception {
        final ProcessDefinition processDefinition = deployAndEnableWithActor(buildBusinessArchiveWithoutConnector(), ACTOR_NAME, businessUser);
        final long processInstanceID = getProcessAPI().startProcess(processDefinition.getId()).getId();
        // process is deployed here with a custom jar
        // wait for first task and assign it
        final long activityInstanceId = waitForUserTaskAndAssigneIt("step1", processInstanceID, getSession().getUserId()).getId();

        // execute it with operation using the command
        final Map<String, Serializable> fieldValues = new HashMap<String, Serializable>();
        // the operation execute a groovy script that depend in a class in the jar
        final Expression rightOperand = new ExpressionBuilder().createGroovyScriptExpression("myScript",
                "new org.bonitasoft.engine.test.TheClassOfMyLibrary().aPublicMethod()", String.class.getName());
        executeActionsAndTerminate("application", activityInstanceId, fieldValues, rightOperand);

        // Clean
        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = CommandAPI.class, concept = BPMNConcept.ACTIVITIES, keywords = { "Command", "Activity", "Action" }, story = "Execute actions and terminate.")
    @Test
    public void executeActionsAndTerminate() throws Exception {
        final ProcessDefinition processDefinition = deployAndEnableWithActor(buildBusinessArchiveWithoutConnector(), ACTOR_NAME, businessUser);
        final long processInstanceID = getProcessAPI().startProcess(processDefinition.getId()).getId();
        // wait for first task and assign it
        final long activityInstanceId = waitForUserTaskAndAssigneIt("step1", processInstanceID, getSession().getUserId()).getId();

        // execute it with operation using the command
        final Map<String, Serializable> fieldValues = new HashMap<String, Serializable>();
        fieldValues.put("field_fieldId1", "Excel");
        final Expression rightOperand = new ExpressionBuilder().createInputExpression("field_fieldId1", String.class.getName());
        executeActionsAndTerminate("application", activityInstanceId, fieldValues, rightOperand);

        // check we have the other task ready and the operation was executed
        waitForUserTask("step2", processInstanceID);
        final DataInstance dataInstance = getProcessAPI().getProcessDataInstance("application", processInstanceID);
        Assert.assertEquals("Excel", dataInstance.getValue().toString());

        // Clean
        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = CommandAPI.class, concept = BPMNConcept.ACTIVITIES, keywords = { "Command", "Activity", "Wrong parameter" }, story = "Execute activity command with wrong parameter", jira = "ENGINE-586")
    @Test(expected = CommandParameterizationException.class)
    public void executeActionsAndTerminateCommandWithWrongParameter() throws Exception {
        final Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put("BAD_KEY", "bad_value");

        getCommandAPI().execute(COMMAND_EXECUTE_OPERATIONS_AND_TERMINATE, parameters);
    }

    private BusinessArchive buildBusinessArchiveWithDataTransientAndConnector() throws Exception {
        final DesignProcessDefinition designProcessDefinition = buildProcessDefinitionWithActorAnd2HumanTasksAndLongTextDataNotTransient(true).done();
        final BusinessArchiveBuilder businessArchiveBuilder = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(
                designProcessDefinition);
        businessArchiveBuilder.addConnectorImplementation(getResource("/org/bonitasoft/engine/connectors/TestConnectorWithOutput.impl",
                "TestConnectorWithOutput.impl"));
        businessArchiveBuilder.addClasspathResource(new BarResource("TestConnectorWithOutput.jar", IOUtil
                .generateJar(TestConnectorWithOutput.class)));

        return businessArchiveBuilder.done();
    }

    private BusinessArchive buildBusinessArchiveWithoutConnector() throws Exception {
        final DesignProcessDefinition designProcessDefinition = buildProcessDefinitionWithActorAnd2HumanTasksAndLongTextDataNotTransient(false).done();
        final BusinessArchiveBuilder builder = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition);
        final InputStream stream = BPMRemoteTests.class.getResourceAsStream("/mylibrary-jar.bak");
        assertNotNull(stream);
        final byte[] byteArray = IOUtils.toByteArray(stream);
        builder.addClasspathResource(new BarResource("mylibrary.jar", byteArray));
        return builder.done();
    }

    private ProcessDefinitionBuilder buildProcessDefinitionWithActorAnd2HumanTasksAndLongTextDataNotTransient(final boolean withConnector)
            throws InvalidExpressionException {
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        processDefinitionBuilder.addActor(ACTOR_NAME);
        processDefinitionBuilder.addLongTextData("application", new ExpressionBuilder().createConstantStringExpression("Word"));

        final UserTaskDefinitionBuilder userTaskBuilder = processDefinitionBuilder.addUserTask("step1", ACTOR_NAME);
        if (withConnector) {
            userTaskBuilder.addLongTextData("dataName", null).isTransient();
            userTaskBuilder.addConnector("myConnector", CONNECTOR_WITH_OUTPUT_ID, "1.0", ConnectorEvent.ON_FINISH)
                    .addInput("input1", new ExpressionBuilder().createGroovyScriptExpression("concat", "dataName+\"a\"", String.class.getName(),
                            new ExpressionBuilder().createDataExpression("dataName", String.class.getName())))
                    .addOutput(new OperationBuilder().createSetDataOperation("application",
                            new ExpressionBuilder().createInputExpression("output1", String.class.getName())));
        }

        processDefinitionBuilder.addUserTask("step2", ACTOR_NAME);
        processDefinitionBuilder.addTransition("step1", "step2");
        return processDefinitionBuilder;
    }

    private void executeActionsAndTerminate(final String dataName, final long taskId, final Map<String, Serializable> fieldValues, final Expression rightOperand)
            throws CommandNotFoundException, CommandParameterizationException, CommandExecutionException {
        final Operation operation = buildOperation(dataName, OperatorType.ASSIGNMENT, "=", rightOperand);
        final List<Operation> operations = new ArrayList<Operation>();
        operations.add(operation);
        final HashMap<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put("ACTIVITY_INSTANCE_ID_KEY", taskId);
        parameters.put("OPERATIONS_LIST_KEY", (Serializable) operations);
        parameters.put("OPERATIONS_INPUT_KEY", (Serializable) fieldValues);
        getCommandAPI().execute(COMMAND_EXECUTE_OPERATIONS_AND_TERMINATE, parameters);
    }

}
