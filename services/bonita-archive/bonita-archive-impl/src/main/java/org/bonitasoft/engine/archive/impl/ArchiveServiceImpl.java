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
package org.bonitasoft.engine.archive.impl;

import org.bonitasoft.engine.archive.ArchiveInsertRecord;
import org.bonitasoft.engine.archive.ArchiveService;
import org.bonitasoft.engine.archive.ArchivingStrategy;
import org.bonitasoft.engine.archive.SArchiveDescriptor;
import org.bonitasoft.engine.archive.SDefinitiveArchiveNotFound;
import org.bonitasoft.engine.commons.ClassReflector;
import org.bonitasoft.engine.commons.LogUtil;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.ArchivedPersistentObject;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLog;
import org.bonitasoft.engine.recorder.SRecorderException;
import org.bonitasoft.engine.recorder.model.DeleteRecord;
import org.bonitasoft.engine.services.PersistenceService;
import org.bonitasoft.engine.services.SPersistenceException;
import org.bonitasoft.engine.transaction.STransactionNotFoundException;
import org.bonitasoft.engine.transaction.TransactionService;

/**
 * @author Matthieu Chaffotte
 * @author Hongwen Zang
 */
public class ArchiveServiceImpl implements ArchiveService {

    private final SArchiveDescriptor definitiveArchiveDescriptor;

    private final ThreadLocal<BatchArchiveSynchronization> synchronizations = new ThreadLocal<BatchArchiveSynchronization>();

    private final TransactionService transactionService;

    private final PersistenceService definitiveArchivePersistenceService;

    private final TechnicalLoggerService logger;

    private final ArchivingStrategy archivingStrategy;

    public ArchiveServiceImpl(final SArchiveDescriptor definitiveArchiveDescriptor, final PersistenceService definitiveArchivePersistenceService,
            final TechnicalLoggerService logger, final ArchivingStrategy archivingStrategy, final TransactionService transactionService) {
        super();
        this.definitiveArchiveDescriptor = definitiveArchiveDescriptor;
        this.definitiveArchivePersistenceService = definitiveArchivePersistenceService;
        this.archivingStrategy = archivingStrategy;
        this.logger = logger;
        this.transactionService = transactionService;
    }

    @Override
    public void recordInsert(final long time, final ArchiveInsertRecord record, final SQueriableLog queriableLog) throws SDefinitiveArchiveNotFound,
            SRecorderException {
        if (isArchivable(record.getEntity().getPersistentObjectInterface())) {
            if (this.logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                this.logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "recordInsert"));
            }
            final ArchivedPersistentObject entity = record.getEntity();
            setArchiveDate(entity, time);

            BatchArchiveSynchronization synchro;
            try {
                synchro = getBatchArchiveSynchronization();
                synchro.addArchivedObject(entity);
            } catch (final STransactionNotFoundException e) {
                this.logger.log(this.getClass(), TechnicalLogSeverity.ERROR, "Unable to register synchronization for the archives : transaction not found");
            }

            if (this.logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                this.logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "recordInsert"));
            }
        }
    }

    @Override
    public void recordInserts(final long time, final ArchiveInsertRecord... records) throws SRecorderException {
        if (this.logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            this.logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "recordInsert"));
        }
        BatchArchiveSynchronization synchro;
        try {
            synchro = getBatchArchiveSynchronization();
            for (final ArchiveInsertRecord record : records) {
                final ArchivedPersistentObject entity = record.getEntity();
                setArchiveDate(entity, time);
                synchro.addArchivedObject(entity);
            }
        } catch (final STransactionNotFoundException e) {
            this.logger.log(this.getClass(), TechnicalLogSeverity.ERROR, "Unable to register synchronization to log queriable logs: transaction not found");
        }

        if (this.logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            this.logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "recordInsert"));
        }
    }

    /**
     * get or create and return the batch synchronization registered on the current transaction
     * 
     * @return
     * @throws STransactionNotFoundException
     */
    private synchronized BatchArchiveSynchronization getBatchArchiveSynchronization() throws STransactionNotFoundException {
        BatchArchiveSynchronization synchro = this.synchronizations.get();
        if (synchro == null) {
            synchro = new BatchArchiveSynchronization(this.definitiveArchivePersistenceService);
            this.transactionService.registerBonitaSynchronization(synchro);
        }
        return synchro;
    }

    private void setArchiveDate(final ArchivedPersistentObject entity, final long time) throws SRecorderException {
        if (entity.getArchiveDate() <= 0) {
            try {
                ClassReflector.invokeSetter(entity, "setArchiveDate", long.class, time);
            } catch (final Exception e) {
                throw new SRecorderException(e);
            }
        }
    }

    @Override
    public void recordDelete(final DeleteRecord record, final SQueriableLog queriableLog) throws SRecorderException {
        try {
            if (this.logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                this.logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "recordDelete"));
            }
            this.definitiveArchivePersistenceService.delete(record.getEntity());
            if (this.logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                this.logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "recordDelete"));
            }
        } catch (final SPersistenceException e) {
            if (this.logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                this.logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "recordDelete", e));
            }
            throw new SRecorderException(e);
        }
    }

    @Override
    public SArchiveDescriptor getDefinitiveArchiveDescriptor() {
        return this.definitiveArchiveDescriptor;
    }

    @Override
    public boolean isArchivable(final Class<? extends PersistentObject> sourceObjectClass) {
        return this.archivingStrategy.isArchivable(sourceObjectClass);
    }

    @Override
    public ReadPersistenceService getDefinitiveArchiveReadPersistenceService() {
        return this.definitiveArchivePersistenceService;
    }

}
