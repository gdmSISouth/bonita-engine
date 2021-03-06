/**
 * Copyright (C) 2012 BonitaSoft S.A.
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
package org.bonitasoft.engine.external.permission;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import org.bonitasoft.engine.actor.mapping.ActorMappingService;
import org.bonitasoft.engine.actor.mapping.model.SActor;
import org.bonitasoft.engine.api.impl.transaction.actor.GetActor;
import org.bonitasoft.engine.api.impl.transaction.process.GetProcessDefinition;
import org.bonitasoft.engine.command.SCommandExecutionException;
import org.bonitasoft.engine.command.SCommandParameterizationException;
import org.bonitasoft.engine.command.TenantCommand;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionExecutor;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.model.SActorDefinition;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.service.TenantServiceAccessor;

/**
 * Specific Command to access the actor Id list for a specific Process Definition and a specific actor ids.
 * The mandatory keys to set as parameters are "PROCESS_DEFINITION_ID_KEY" and "ACTOR_IDS_KEY".
 * 
 * @author Zhao Na
 */
public class IsAllowedToStartProcess extends TenantCommand {

    private TenantServiceAccessor serviceAccessor;
    
    private static final String PROCESS_DEFINITION_ID_KEY = "PROCESS_DEFINITION_ID_KEY";

    private static final String ACTOR_IDS_KEY = "ACTOR_IDS_KEY";


    /**
     * @return true if the given Set contains the actor that is allowed to start the given process.
     */
    @Override
    public Serializable execute(final Map<String, Serializable> parameters, final TenantServiceAccessor serviceAccessor)
            throws SCommandParameterizationException, SCommandExecutionException {
        this.serviceAccessor = serviceAccessor;
        ActorMappingService actorMappingService = this.serviceAccessor.getActorMappingService();
        final TransactionExecutor transactionExecutor = this.serviceAccessor.getTransactionExecutor();
        final ProcessDefinitionService processDefinitionService = this.serviceAccessor.getProcessDefinitionService();
        
        Set<Long> actorIds = (Set<Long>) parameters.get(ACTOR_IDS_KEY);
        if(actorIds==null){
            throw new SCommandParameterizationException("Mandatory parameter " + ACTOR_IDS_KEY + " is missing or not convertible to Set<Long>.");
        }
        long processDefinitionId = (Long) parameters.get(PROCESS_DEFINITION_ID_KEY);
        if(processDefinitionId==0){
            throw new SCommandParameterizationException("Mandatory parameter " + PROCESS_DEFINITION_ID_KEY + " is missing or not convertible to Long.");
        }
        final GetProcessDefinition getProcessDefinition = new GetProcessDefinition(processDefinitionId, processDefinitionService);
        boolean isAllowedToStartProcess = false;
        try{
            transactionExecutor.execute(getProcessDefinition);
            final SProcessDefinition definition = getProcessDefinition.getResult();
            SActorDefinition sActorDefinition = definition.getActorInitiator();

            final String name = sActorDefinition.getName();
            final GetActor getActor = new GetActor(actorMappingService, name, processDefinitionId);
            transactionExecutor.execute(getActor);
            final SActor sActor = getActor.getResult();
            if(sActor!=null){
                for(long actorId : actorIds){
                    if(sActor.getId()==actorId){
                        isAllowedToStartProcess = true;
                        break;
                    }
                }
            }            
        }catch(final SBonitaException e){
            throw new SCommandExecutionException("Can't get actors For Initiator."+e);
        }
        return isAllowedToStartProcess;
    }
}
