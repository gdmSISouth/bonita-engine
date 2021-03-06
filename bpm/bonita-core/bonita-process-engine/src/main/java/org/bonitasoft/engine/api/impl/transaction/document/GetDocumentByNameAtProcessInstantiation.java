/**
 * Copyright (C) 2011 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.engine.api.impl.transaction.document;

import java.util.Date;

import org.bonitasoft.engine.api.impl.transaction.process.GetArchivedProcessInstanceList;
import org.bonitasoft.engine.bpm.process.ArchivedProcessInstance;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;
import org.bonitasoft.engine.core.process.document.api.ProcessDocumentService;
import org.bonitasoft.engine.core.process.document.model.SProcessDocument;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.model.archive.builder.SAProcessInstanceBuilder;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.search.descriptor.SearchEntitiesDescriptor;

/**
 * @author Baptiste Mesta
 * @author Celine Souchet
 */
public class GetDocumentByNameAtProcessInstantiation implements TransactionContentWithResult<SProcessDocument> {

    private final ProcessDocumentService processDocumentService;

    private final ProcessInstanceService processInstanceService;

    private final ReadPersistenceService readPersistenceService;

    private final SAProcessInstanceBuilder saProcessInstanceBuilder;

    private final SearchEntitiesDescriptor searchEntitiesDescriptor;

    private final long processInstanceId;

    private SProcessDocument result;

    private final String documentName;

    public GetDocumentByNameAtProcessInstantiation(final ProcessDocumentService processDocumentService, final ReadPersistenceService readPersistenceService,
            final ProcessInstanceService processInstanceService, final SAProcessInstanceBuilder saProcessInstanceBuilder,
            final SearchEntitiesDescriptor searchEntitiesDescriptor, final long processInstanceId, final String documentName) {
        this.processDocumentService = processDocumentService;
        this.processInstanceId = processInstanceId;
        this.documentName = documentName;
        this.readPersistenceService = readPersistenceService;
        this.processInstanceService = processInstanceService;
        this.saProcessInstanceBuilder = saProcessInstanceBuilder;
        this.searchEntitiesDescriptor = searchEntitiesDescriptor;
    }

    @Override
    public void execute() throws SBonitaException {
        final GetArchivedProcessInstanceList getArchivedProcessInstanceList = new GetArchivedProcessInstanceList(processInstanceService,
                searchEntitiesDescriptor, processInstanceId, 0, 1, saProcessInstanceBuilder.getIdKey(), OrderByType.ASC);
        getArchivedProcessInstanceList.execute();
        final ArchivedProcessInstance saProcessInstance = getArchivedProcessInstanceList.getResult().get(0);
        final Date startDate = saProcessInstance.getStartDate();
        final long startTime = startDate != null ? startDate.getTime() : 0;
        result = processDocumentService.getDocument(processInstanceId, documentName, startTime, readPersistenceService);
    }

    @Override
    public SProcessDocument getResult() {
        return result;
    }

}
