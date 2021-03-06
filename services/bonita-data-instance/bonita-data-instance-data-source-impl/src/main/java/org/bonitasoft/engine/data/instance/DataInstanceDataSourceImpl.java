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
package org.bonitasoft.engine.data.instance;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.commons.CollectionUtil;
import org.bonitasoft.engine.commons.NullCheckingUtil;
import org.bonitasoft.engine.data.DataSourceConfiguration;
import org.bonitasoft.engine.data.instance.exception.SCreateDataInstanceException;
import org.bonitasoft.engine.data.instance.exception.SDataInstanceException;
import org.bonitasoft.engine.data.instance.exception.SDataInstanceNotFoundException;
import org.bonitasoft.engine.data.instance.exception.SDeleteDataInstanceException;
import org.bonitasoft.engine.data.instance.exception.SUpdateDataInstanceException;
import org.bonitasoft.engine.data.instance.model.SDataInstance;
import org.bonitasoft.engine.data.instance.model.builder.SDataInstanceBuilder;
import org.bonitasoft.engine.data.instance.model.builder.SDataInstanceBuilders;
import org.bonitasoft.engine.data.instance.model.builder.SDataInstanceLogBuilder;
import org.bonitasoft.engine.events.model.SDeleteEvent;
import org.bonitasoft.engine.events.model.SInsertEvent;
import org.bonitasoft.engine.events.model.SUpdateEvent;
import org.bonitasoft.engine.events.model.builders.SEventBuilders;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectByIdDescriptor;
import org.bonitasoft.engine.persistence.SelectListDescriptor;
import org.bonitasoft.engine.persistence.SelectOneDescriptor;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLog;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLogSeverity;
import org.bonitasoft.engine.queriablelogger.model.builder.HasCRUDEAction;
import org.bonitasoft.engine.queriablelogger.model.builder.HasCRUDEAction.ActionType;
import org.bonitasoft.engine.queriablelogger.model.builder.SLogBuilder;
import org.bonitasoft.engine.queriablelogger.model.builder.SPersistenceLogBuilder;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.recorder.SRecorderException;
import org.bonitasoft.engine.recorder.model.DeleteRecord;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.recorder.model.InsertRecord;
import org.bonitasoft.engine.recorder.model.UpdateRecord;
import org.bonitasoft.engine.services.QueriableLoggerService;

/**
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 */
public class DataInstanceDataSourceImpl implements DataInstanceDataSource {

    private SDataInstanceBuilders dataInstanceBuilders;

    private Recorder recorder;

    private ReadPersistenceService persistenceRead;

    private SEventBuilders eventBuilders;

    private QueriableLoggerService queriableLoggerService;

    private <T extends SLogBuilder> void initializeLogBuilder(final T logBuilder, final String message) {
        logBuilder.createNewInstance().actionStatus(SQueriableLog.STATUS_FAIL).severity(SQueriableLogSeverity.INTERNAL).rawMessage(message);
    }

    private <T extends HasCRUDEAction> void updateLog(final ActionType actionType, final T logBuilder) {
        logBuilder.setActionType(actionType);
    }

    private SDataInstanceLogBuilder getDataInstanceLogBuilder(final ActionType actionType, final String message) {
        final SDataInstanceLogBuilder logBuilder = dataInstanceBuilders.getDataInstanceLogBuilder();
        initializeLogBuilder(logBuilder, message);
        updateLog(actionType, logBuilder);
        return logBuilder;
    }

    private SInsertEvent getInsertEvent(final Object obj) {
        return (SInsertEvent) eventBuilders.getEventBuilder().createInsertEvent(DATA_INSTANCE).setObject(obj).done();
    }

    private SUpdateEvent getUpdateEvent(final Object obj) {
        return (SUpdateEvent) eventBuilders.getEventBuilder().createUpdateEvent(DATA_INSTANCE).setObject(obj).done();
    }

    private SDeleteEvent getDeleteEvent(final Object obj) {
        return (SDeleteEvent) eventBuilders.getEventBuilder().createDeleteEvent(DATA_INSTANCE).setObject(obj).done();
    }

    @Override
    public void setParameters(final Map<String, String> dataSourceParameters) {
        // TODO Auto-generated method stub

    }

    @Override
    public void createDataInstance(final SDataInstance dataInstance) throws SDataInstanceException {
        try {
            final InsertRecord insertRecord = new InsertRecord(dataInstance);
            final SInsertEvent insertEvent = getInsertEvent(dataInstance);
            recorder.recordInsert(insertRecord, insertEvent);
        } catch (final SRecorderException e) {
            throw new SCreateDataInstanceException("Imposible to create data intance.", e);
        }
    }

    @Override
    public void updateDataInstance(final SDataInstance dataInstance, final EntityUpdateDescriptor descriptor) throws SDataInstanceException {
        NullCheckingUtil.checkArgsNotNull(dataInstance);
        final SDataInstanceLogBuilder logBuilder = getDataInstanceLogBuilder(ActionType.UPDATED, "Updating a data instance " + dataInstance.getName());
        final UpdateRecord updateRecord = UpdateRecord.buildSetFields(dataInstance, descriptor);
        final SUpdateEvent updateEvent = getUpdateEvent(dataInstance);
        try {
            recorder.recordUpdate(updateRecord, updateEvent);
            initiateLogBuilder(dataInstance.getId(), SQueriableLog.STATUS_OK, logBuilder, "updateDataInstance");
        } catch (final SRecorderException e) {
            initiateLogBuilder(dataInstance.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "updateDataInstance");
            throw new SUpdateDataInstanceException("Impossible to update data instance '" + dataInstance.getName() + "': " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteDataInstance(final SDataInstance dataInstance) throws SDataInstanceException {
        NullCheckingUtil.checkArgsNotNull(dataInstance);
        final SDataInstanceLogBuilder logBuilder = getDataInstanceLogBuilder(ActionType.DELETED, "Deleting a data instance");
        final DeleteRecord deleteRecord = new DeleteRecord(dataInstance);
        final SDeleteEvent deleteEvent = getDeleteEvent(dataInstance);
        try {
            recorder.recordDelete(deleteRecord, deleteEvent);
            initiateLogBuilder(dataInstance.getId(), SQueriableLog.STATUS_OK, logBuilder, "deleteDataInstance");
        } catch (final SRecorderException e) {
            initiateLogBuilder(dataInstance.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "deleteDataInstance");
            throw new SDeleteDataInstanceException("Impossible to delete data instance", e);
        }
    }

    @Override
    public SDataInstance getDataInstance(final long dataInstanceId) throws SDataInstanceException {
        NullCheckingUtil.checkArgsNotNull(dataInstanceId);
        try {
            final SelectByIdDescriptor<SDataInstance> selectDescriptor = new SelectByIdDescriptor<SDataInstance>("getDataInstanceById", SDataInstance.class,
                    dataInstanceId);
            final SDataInstance dataInstance = persistenceRead.selectById(selectDescriptor);
            if (dataInstance == null) {
                throw new SDataInstanceNotFoundException("Cannot get the data instance with id " + dataInstanceId);
            }
            return dataInstance;
        } catch (final SBonitaReadException e) {
            throw new SDataInstanceException("Cannot get the data instance with id " + dataInstanceId, e);
        }
    }

    @Override
    public boolean configurationMatches(final DataSourceConfiguration datasourceConfiguration) {
        return datasourceConfiguration instanceof PersistentDataInstanceDataSourceConfiguration;
    }

    @Override
    public void configure(final DataSourceConfiguration dataSourceConfiguration) {
        final Map<String, Object> resources = dataSourceConfiguration.getResources();
        persistenceRead = getResource(resources, ReadPersistenceService.class, PersistentDataInstanceDataSourceConfiguration.PERSISTENCE_READ_KEY);
        recorder = getResource(resources, Recorder.class, PersistentDataInstanceDataSourceConfiguration.RECORDER_KEY);
        dataInstanceBuilders = getResource(resources, SDataInstanceBuilders.class, PersistentDataInstanceDataSourceConfiguration.DATA_INSTANCE_BUILDERS_KEY);
        eventBuilders = getResource(resources, SEventBuilders.class, PersistentDataInstanceDataSourceConfiguration.EVENT_BUILDERS_KEY);
        queriableLoggerService = getResource(resources, QueriableLoggerService.class, PersistentDataInstanceDataSourceConfiguration.QUERIABLE_LOGGER_SERVICE);
    }

    private <T> T getResource(final Map<String, Object> resources, final Class<T> clazz, final String key) {
        final Object resource = resources.get(key);

        if (resource != null && clazz.isInstance(resource)) {
            return clazz.cast(resource);
        }

        // FIXME: throws an exception if resource not found

        return null;
    }

    @Override
    public SDataInstance getDataInstance(final String dataName, final long containerId, final String containerType) throws SDataInstanceException {
        NullCheckingUtil.checkArgsNotNull(dataName, containerType);

        final SDataInstanceBuilder dataInstanceBuilder = dataInstanceBuilders.getDataInstanceBuilder();
        final Map<String, Object> paraMap = CollectionUtil.buildSimpleMap(dataInstanceBuilder.getNameKey(), dataName);
        paraMap.put(dataInstanceBuilder.getContainerIdKey(), containerId);
        paraMap.put(dataInstanceBuilder.getContainerTypeKey(), containerType);

        try {
            final SDataInstance dataInstance = persistenceRead.selectOne(new SelectOneDescriptor<SDataInstance>("getDataInstancesByNameAndContainer", paraMap,
                    SDataInstance.class, SDataInstance.class)); // conditions :and not or
            if (dataInstance == null) {
                throw new SDataInstanceException("No data instance found");
            }
            return dataInstance;
        } catch (final SBonitaReadException e) {
            throw new SDataInstanceException("Unable to check if a data instance already exists: " + e.getMessage(), e);
        }
    }

    @Override
    public List<SDataInstance> getDataInstances(final long containerId, final String containerType, final int fromIndex, final int numberOfResults)
            throws SDataInstanceException {
        NullCheckingUtil.checkArgsNotNull(containerType);

        final SDataInstanceBuilder dataInstanceBuilder = dataInstanceBuilders.getDataInstanceBuilder();
        final Map<String, Object> paraMap = CollectionUtil.buildSimpleMap(dataInstanceBuilder.getContainerIdKey(), containerId);
        paraMap.put(dataInstanceBuilder.getContainerTypeKey(), containerType);

        try {
            return persistenceRead.selectList(new SelectListDescriptor<SDataInstance>("getDataInstancesByContainer", paraMap, SDataInstance.class,
                    SDataInstance.class, new QueryOptions(fromIndex, numberOfResults)));
        } catch (final SBonitaReadException e) {
            throw new SDataInstanceException("Unable to check if a data instance already exists for the data container of type " + containerType + " with id "
                    + containerId + " for reason: " + e.getMessage(), e);
        }
    }

    @Override
    public List<SDataInstance> getDataInstances(final List<Long> dataInstanceIds) throws SDataInstanceException {
        NullCheckingUtil.checkArgsNotNull(dataInstanceIds);
        if (dataInstanceIds.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            final Map<String, Object> parameters = CollectionUtil.buildSimpleMap("ids", dataInstanceIds);
            final SelectListDescriptor<SDataInstance> selectDescriptor = new SelectListDescriptor<SDataInstance>("getDataInstanceByIds", parameters,
                    SDataInstance.class, new QueryOptions(0, dataInstanceIds.size()));
            final List<SDataInstance> dataInstances = persistenceRead.selectList(selectDescriptor);
            if (dataInstances == null) {
                throw new SDataInstanceNotFoundException("Cannot get the data instance with id " + dataInstanceIds);
            }
            return dataInstances;
        } catch (final SBonitaReadException e) {
            throw new SDataInstanceException("Cannot get the data instance with id " + dataInstanceIds, e);
        }
    }

    private void initiateLogBuilder(final long objectId, final int sQueriableLogStatus, final SPersistenceLogBuilder logBuilder, final String callerMethodName) {
        logBuilder.actionScope(String.valueOf(objectId));
        logBuilder.actionStatus(sQueriableLogStatus);
        logBuilder.objectId(objectId);
        final SQueriableLog log = logBuilder.done();
        if (queriableLoggerService.isLoggable(log.getActionType(), log.getSeverity())) {
            queriableLoggerService.log(this.getClass().getName(), "recordInsert", log);
        }
    }

}
