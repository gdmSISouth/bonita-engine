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

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.persistence.ArchivedPersistentObject;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.services.PersistenceService;
import org.bonitasoft.engine.services.SPersistenceException;
import org.bonitasoft.engine.transaction.BonitaTransactionSynchronization;
import org.bonitasoft.engine.transaction.TransactionState;

/**
 * Transaction synchronization to insert archives in batch mode at the end of the transaction.
 * 
 * @author Emmanuel Duchastenier
 * @author Matthieu Chaffotte
 */
public class BatchArchiveSynchronization implements BonitaTransactionSynchronization {

    private final PersistenceService persistenceService;

    private final List<ArchivedPersistentObject> archivedObjects;

    public BatchArchiveSynchronization(final PersistenceService persistenceService) {
        super();
        this.persistenceService = persistenceService;
        this.archivedObjects = new ArrayList<ArchivedPersistentObject>();
    }

    @Override
    public void afterCompletion(final TransactionState status) {
        // NOTHING
    }

    @Override
    public void beforeCommit() {
        if (this.archivedObjects != null && !this.archivedObjects.isEmpty()) {
            try {
                this.persistenceService.insertInBatch(new ArrayList<PersistentObject>(this.archivedObjects));
                this.persistenceService.flushStatements();
            } catch (final SPersistenceException spe) {
                throw new RuntimeException(spe);
            } finally {
                this.archivedObjects.clear();
            }
        }
    }

    public void addArchivedObject(final ArchivedPersistentObject archivedPersistentObject) {
        // no synchronized block required as we are working on a threadLocal:
        this.archivedObjects.add(archivedPersistentObject);
    }

}
