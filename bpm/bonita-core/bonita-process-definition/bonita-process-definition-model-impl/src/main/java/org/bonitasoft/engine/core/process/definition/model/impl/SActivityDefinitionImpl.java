/**
 * Copyright (C) 2012 BonitaSoft S.A.
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
package org.bonitasoft.engine.core.process.definition.model.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.bpm.data.DataDefinition;
import org.bonitasoft.engine.bpm.flownode.ActivityDefinition;
import org.bonitasoft.engine.bpm.flownode.BoundaryEventDefinition;
import org.bonitasoft.engine.bpm.flownode.LoopCharacteristics;
import org.bonitasoft.engine.bpm.flownode.impl.MultiInstanceLoopCharacteristics;
import org.bonitasoft.engine.bpm.flownode.impl.StandardLoopCharacteristics;
import org.bonitasoft.engine.core.operation.model.SOperation;
import org.bonitasoft.engine.core.operation.model.builder.SOperationBuilders;
import org.bonitasoft.engine.core.process.definition.model.SActivityDefinition;
import org.bonitasoft.engine.core.process.definition.model.SBoundaryEventNotFoundException;
import org.bonitasoft.engine.core.process.definition.model.SFlowElementContainerDefinition;
import org.bonitasoft.engine.core.process.definition.model.SLoopCharacteristics;
import org.bonitasoft.engine.core.process.definition.model.STransitionDefinition;
import org.bonitasoft.engine.core.process.definition.model.builder.ServerModelConvertor;
import org.bonitasoft.engine.core.process.definition.model.event.SBoundaryEventDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.impl.SBoundaryEventDefinitionImpl;
import org.bonitasoft.engine.data.definition.model.SDataDefinition;
import org.bonitasoft.engine.data.definition.model.builder.SDataDefinitionBuilders;
import org.bonitasoft.engine.expression.model.builder.SExpressionBuilders;
import org.bonitasoft.engine.operation.Operation;

/**
 * @author Matthieu Chaffotte
 * @author Frederic Bouquet
 * @author Celine Souchet
 */
public abstract class SActivityDefinitionImpl extends SFlowNodeDefinitionImpl implements SActivityDefinition {

    private static final long serialVersionUID = 8767258220640127769L;

    protected List<SDataDefinition> sDataDefinitions = new ArrayList<SDataDefinition>();

    protected List<SOperation> sOperations = new ArrayList<SOperation>();

    protected SLoopCharacteristics loopCharacteristics;

    private final List<SBoundaryEventDefinition> sBoundaryEventDefinitions = new ArrayList<SBoundaryEventDefinition>();

    public SActivityDefinitionImpl(final long id, final String name) {
        super(id, name);
    }

    public SActivityDefinitionImpl(final SFlowElementContainerDefinition parentContainer, final ActivityDefinition activityDefinition,
            final SExpressionBuilders sExpressionBuilders, final Map<String, STransitionDefinition> transitionsMap,
            final SDataDefinitionBuilders sDataDefinitionBuilders, final SOperationBuilders sOperationBuilders) {
        super(parentContainer, activityDefinition, sExpressionBuilders, transitionsMap, sDataDefinitionBuilders, sOperationBuilders);

        final List<DataDefinition> dataDefinitions = activityDefinition.getDataDefinitions();
        for (final DataDefinition dataDefinition : dataDefinitions) {
            sDataDefinitions.add(ServerModelConvertor.convertDataDefinition(dataDefinition, sDataDefinitionBuilders, sExpressionBuilders));
        }
        final List<Operation> operations = activityDefinition.getOperations();
        for (final Operation operation : operations) {
            sOperations.add(ServerModelConvertor.convertOperation(sOperationBuilders, sExpressionBuilders, operation));
        }
        final LoopCharacteristics loop = activityDefinition.getLoopCharacteristics();
        if (loop != null) {
            if (loop instanceof StandardLoopCharacteristics) {
                loopCharacteristics = new SStandardLoopCharacteristicsImpl((StandardLoopCharacteristics) loop, sExpressionBuilders);
            } else {
                loopCharacteristics = new SMultiInstanceLoopCharacteristicsImpl((MultiInstanceLoopCharacteristics) loop, sExpressionBuilders);
            }
        }

        addBoundaryEvents(activityDefinition, sExpressionBuilders, transitionsMap, sDataDefinitionBuilders, sOperationBuilders);
    }

    private void addBoundaryEvents(final ActivityDefinition activityDefinition, final SExpressionBuilders sExpressionBuilders,
            final Map<String, STransitionDefinition> transitionsMap, final SDataDefinitionBuilders sDataDefinitionBuilders,
            final SOperationBuilders sOperationBuilders) {
        final List<BoundaryEventDefinition> boundaryEventDefinitions = activityDefinition.getBoundaryEventDefinitions();
        for (final BoundaryEventDefinition boundaryEventDefinition : boundaryEventDefinitions) {
            addBoundaryEventDefinition(new SBoundaryEventDefinitionImpl(getParentContainer(), boundaryEventDefinition, sExpressionBuilders, transitionsMap,
                    sDataDefinitionBuilders, sOperationBuilders));
        }
    }

    @Override
    public List<SOperation> getSOperations() {
        return sOperations;
    }

    public void addSOperation(final SOperation operation) {
        sOperations.add(operation);
    }

    @Override
    public List<SDataDefinition> getSDataDefinitions() {
        return sDataDefinitions;
    }

    public void addSDataDefinition(final SDataDefinition sDataDefinition) {
        sDataDefinitions.add(sDataDefinition);
    }

    @Override
    public List<SBoundaryEventDefinition> getBoundaryEventDefinitions() {
        return Collections.unmodifiableList(sBoundaryEventDefinitions);
    }

    @Override
    public SBoundaryEventDefinition getBoundaryEventDefinition(final String name) throws SBoundaryEventNotFoundException {
        boolean found = false;
        SBoundaryEventDefinition boundary = null;
        final Iterator<SBoundaryEventDefinition> iterator = sBoundaryEventDefinitions.iterator();
        while (iterator.hasNext() && !found) {
            final SBoundaryEventDefinition currentBoundary = iterator.next();
            if (currentBoundary.getName().equals(name)) {
                boundary = currentBoundary;
                found = true;
            }
        }
        if (boundary == null) {
            throw new SBoundaryEventNotFoundException(name, getName());
        }
        return boundary;
    }

    public void addBoundaryEventDefinition(final SBoundaryEventDefinition boundaryEventDefinition) {
        sBoundaryEventDefinitions.add(boundaryEventDefinition);
    }

    @Override
    public SLoopCharacteristics getLoopCharacteristics() {
        return loopCharacteristics;
    }

    public void setLoopCharacteristics(final SLoopCharacteristics loopCharacteristics) {
        this.loopCharacteristics = loopCharacteristics;
    }

}
