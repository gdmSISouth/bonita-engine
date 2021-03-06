package org.bonitasoft.engine.actor.mapping.impl;

/**
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.bonitasoft.engine.actor.mapping.SActorNotFoundException;
import org.bonitasoft.engine.actor.mapping.model.SActor;
import org.bonitasoft.engine.actor.mapping.model.SActorMember;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectByIdDescriptor;
import org.bonitasoft.engine.persistence.SelectListDescriptor;
import org.bonitasoft.engine.persistence.SelectOneDescriptor;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.services.QueriableLoggerService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Celine Souchet
 * 
 */
@RunWith(MockitoJUnitRunner.class)
public class ActorMappingServiceImplTest {

    @Mock
    private Recorder recorder;

    @Mock
    private ReadPersistenceService persistenceService;

    @Mock
    private EventService eventService;

    @Mock
    private QueriableLoggerService queriableLoggerService;

    @Mock
    private IdentityService identityService;

    @InjectMocks
    private ActorMappingServiceImpl actorMappingServiceImpl;

    /**
     * Test method for {@link org.bonitasoft.engine.actor.mapping.impl.ActorMappingServiceImpl#getActor(long)}.
     * 
     * @throws SBonitaReadException
     * @throws SActorNotFoundException
     */
    @Test
    public final void getActorById() throws SActorNotFoundException, SBonitaReadException {
        final SActor actor = mock(SActor.class);
        when(persistenceService.selectById(any(SelectByIdDescriptor.class))).thenReturn(actor);

        Assert.assertEquals(actor, actorMappingServiceImpl.getActor(456L));
    }

    @Test(expected = SActorNotFoundException.class)
    public final void getActorByIdNotExists() throws SBonitaReadException, SActorNotFoundException {
        when(persistenceService.selectById(any(SelectByIdDescriptor.class))).thenReturn(null);

        actorMappingServiceImpl.getActor(456L);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.actor.mapping.impl.ActorMappingServiceImpl#getNumberOfActorMembers(long)}.
     * 
     * @throws SBonitaReadException
     */
    @Test
    public final void getNumberOfActorMembers() throws SBonitaReadException {
        final long actorId = 456L;
        final long numberOfActorMemebers = 1L;
        when(persistenceService.selectOne(any(SelectOneDescriptor.class))).thenReturn(numberOfActorMemebers);

        Assert.assertEquals(numberOfActorMemebers, actorMappingServiceImpl.getNumberOfActorMembers(actorId));
    }

    /**
     * Test method for {@link org.bonitasoft.engine.actor.mapping.impl.ActorMappingServiceImpl#getNumberOfUsersOfActor(long)}.
     * 
     * @throws SBonitaReadException
     */
    @Test
    public final void getNumberOfUsersOfActor() throws SBonitaReadException {
        final long numberOfUsersOfActor = 155L;
        when(persistenceService.selectOne(any(SelectOneDescriptor.class))).thenReturn(numberOfUsersOfActor);

        Assert.assertEquals(numberOfUsersOfActor, actorMappingServiceImpl.getNumberOfUsersOfActor(456L));
    }

    @Test(expected = RuntimeException.class)
    public final void getNumberOfUsersOfActorThrowException() throws SBonitaReadException {
        when(persistenceService.selectOne(any(SelectOneDescriptor.class))).thenThrow(new SBonitaReadException(""));

        actorMappingServiceImpl.getNumberOfUsersOfActor(456L);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.actor.mapping.impl.ActorMappingServiceImpl#getNumberOfRolesOfActor(long)}.
     * 
     * @throws SBonitaReadException
     */
    @Test
    public final void getNumberOfRolesOfActor() throws SBonitaReadException {
        final long numberOfRolesOfActor = 155L;
        when(persistenceService.selectOne(any(SelectOneDescriptor.class))).thenReturn(numberOfRolesOfActor);

        Assert.assertEquals(numberOfRolesOfActor, actorMappingServiceImpl.getNumberOfRolesOfActor(456L));
    }

    @Test(expected = RuntimeException.class)
    public final void getNumberOfRolesOfActorThrowException() throws SBonitaReadException {
        when(persistenceService.selectOne(any(SelectOneDescriptor.class))).thenThrow(new SBonitaReadException(""));

        actorMappingServiceImpl.getNumberOfRolesOfActor(456L);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.actor.mapping.impl.ActorMappingServiceImpl#getNumberOfGroupsOfActor(long)}.
     * 
     * @throws SBonitaReadException
     */
    @Test
    public final void getNumberOfGroupsOfActor() throws SBonitaReadException {
        final long numberOfGroupsOfActor = 155L;
        when(persistenceService.selectOne(any(SelectOneDescriptor.class))).thenReturn(numberOfGroupsOfActor);

        Assert.assertEquals(numberOfGroupsOfActor, actorMappingServiceImpl.getNumberOfGroupsOfActor(456L));
    }

    @Test(expected = RuntimeException.class)
    public final void getNumberOfGroupsOfActorThrowException() throws SBonitaReadException {
        when(persistenceService.selectOne(any(SelectOneDescriptor.class))).thenThrow(new SBonitaReadException(""));

        actorMappingServiceImpl.getNumberOfGroupsOfActor(456L);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.actor.mapping.impl.ActorMappingServiceImpl#getNumberOfMembershipsOfActor(long)}.
     * 
     * @throws SBonitaReadException
     */
    @Test
    public final void getNumberOfMembershipsOfActor() throws SBonitaReadException {
        final long numberOfGroupsOfActor = 155L;
        when(persistenceService.selectOne(any(SelectOneDescriptor.class))).thenReturn(numberOfGroupsOfActor);

        Assert.assertEquals(numberOfGroupsOfActor, actorMappingServiceImpl.getNumberOfMembershipsOfActor(456L));
    }

    @Test(expected = RuntimeException.class)
    public final void getNumberOfMembershipsOfActorThrowException() throws SBonitaReadException {
        when(persistenceService.selectOne(any(SelectOneDescriptor.class))).thenThrow(new SBonitaReadException(""));

        actorMappingServiceImpl.getNumberOfMembershipsOfActor(456L);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.actor.mapping.impl.ActorMappingServiceImpl#getActor(java.lang.String, long)}.
     * 
     * @throws SBonitaReadException
     * @throws SActorNotFoundException
     */
    @Test
    public final void getActorByNameAndScopeId() throws SActorNotFoundException, SBonitaReadException {
        final SActor actor = mock(SActor.class);
        when(persistenceService.selectOne(any(SelectOneDescriptor.class))).thenReturn(actor);

        Assert.assertEquals(actor, actorMappingServiceImpl.getActor("actorName", 69L));
    }

    @Test(expected = SActorNotFoundException.class)
    public final void getActorByNameAndScopeIdNotExists() throws SActorNotFoundException, SBonitaReadException {
        when(persistenceService.selectOne(any(SelectOneDescriptor.class))).thenReturn(null);

        actorMappingServiceImpl.getActor("actorName", 69L);
    }

    @Test(expected = SActorNotFoundException.class)
    public final void getActorByNameAndScopeIdThrowException() throws SActorNotFoundException, SBonitaReadException {
        when(persistenceService.selectOne(any(SelectOneDescriptor.class))).thenThrow(new SBonitaReadException(""));

        actorMappingServiceImpl.getActor("actorName", 69L);
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.actor.mapping.impl.ActorMappingServiceImpl#getActorMembers(int, int, java.lang.String, org.bonitasoft.engine.persistence.OrderByType)}
     * .
     * 
     * @throws SBonitaReadException
     */
    @Test
    public final void getActorMembersIntIntStringOrderByType() throws SBonitaReadException {
        final List<SActorMember> actors = new ArrayList<SActorMember>();
        when(persistenceService.selectList(any(SelectListDescriptor.class))).thenReturn(actors);

        Assert.assertEquals(actors, actorMappingServiceImpl.getActorMembers(0, 1, "id", OrderByType.ASC));
    }

    /**
     * Test method for {@link org.bonitasoft.engine.actor.mapping.impl.ActorMappingServiceImpl#getActorMembers(long, int, int)}.
     * 
     * @throws SBonitaReadException
     */
    @Test
    public final void getActorMembersLongIntInt() throws SBonitaReadException {
        final List<SActorMember> actors = new ArrayList<SActorMember>();
        when(persistenceService.selectList(any(SelectListDescriptor.class))).thenReturn(actors);

        Assert.assertEquals(actors, actorMappingServiceImpl.getActorMembers(4115L, 0, 1));
    }

    /**
     * Test method for {@link org.bonitasoft.engine.actor.mapping.impl.ActorMappingServiceImpl#getActorMembersOfGroup(long)}.
     * 
     * @throws SBonitaReadException
     */
    @Test
    public final void getActorMembersOfGroup() throws SBonitaReadException {
        final List<SActorMember> actors = new ArrayList<SActorMember>(6);
        when(persistenceService.selectList(any(SelectListDescriptor.class))).thenReturn(actors);

        Assert.assertEquals(actors, actorMappingServiceImpl.getActorMembersOfGroup(41L));
    }

    /**
     * Test method for {@link org.bonitasoft.engine.actor.mapping.impl.ActorMappingServiceImpl#getActorMembersOfRole(long)}.
     * 
     * @throws SBonitaReadException
     */
    @Test
    public final void getActorMembersOfRole() throws SBonitaReadException {
        final List<SActorMember> actors = new ArrayList<SActorMember>(3);
        when(persistenceService.selectList(any(SelectListDescriptor.class))).thenReturn(actors);

        Assert.assertEquals(actors, actorMappingServiceImpl.getActorMembersOfRole(41L));
    }

    /**
     * Test method for {@link org.bonitasoft.engine.actor.mapping.impl.ActorMappingServiceImpl#getActorMembersOfUser(long)}.
     * 
     * @throws SBonitaReadException
     */
    @Test
    public final void getActorMembersOfUser() throws SBonitaReadException {
        final List<SActorMember> actors = new ArrayList<SActorMember>(3);
        when(persistenceService.selectList(any(SelectListDescriptor.class))).thenReturn(actors);

        Assert.assertEquals(actors, actorMappingServiceImpl.getActorMembersOfUser(41L));
    }

    /**
     * Test method for {@link org.bonitasoft.engine.actor.mapping.impl.ActorMappingServiceImpl#getActors(java.util.List)}.
     * 
     * @throws SBonitaReadException
     */
    @Test
    public final void getActorsByListOfIds() throws SBonitaReadException {
        final List<SActor> actors = new ArrayList<SActor>(3);
        when(persistenceService.selectList(any(SelectListDescriptor.class))).thenReturn(actors);

        final List<Long> actorIds = new ArrayList<Long>(1);
        actorIds.add(589L);
        Assert.assertEquals(actors, actorMappingServiceImpl.getActors(actorIds));
    }

    @Test
    public final void getActorsByListOfIdsWithEmptyList() throws SBonitaReadException {
        Assert.assertEquals(Collections.emptyList(), actorMappingServiceImpl.getActors(new ArrayList<Long>(0)));
    }

    @Test
    public final void getActorsByListOfIdsWithNullList() throws SBonitaReadException {
        Assert.assertEquals(Collections.emptyList(), actorMappingServiceImpl.getActors(null));
    }

    /**
     * Test method for {@link org.bonitasoft.engine.actor.mapping.impl.ActorMappingServiceImpl#getActors(long)}.
     * 
     * @throws SBonitaReadException
     */
    @Test
    public final void getActorsByScopeId() throws SBonitaReadException {
        final List<SActor> actors = new ArrayList<SActor>(3);
        when(persistenceService.selectList(any(SelectListDescriptor.class))).thenReturn(actors);

        Assert.assertEquals(actors, actorMappingServiceImpl.getActors(1654L));
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.actor.mapping.impl.ActorMappingServiceImpl#getActors(long, int, int, java.lang.String, org.bonitasoft.engine.persistence.OrderByType)}
     * .
     * 
     * @throws SBonitaReadException
     */
    @Test
    public final void getActorsLongIntIntStringOrderByType() throws SBonitaReadException {
        final List<SActor> actors = new ArrayList<SActor>(3);
        when(persistenceService.selectList(any(SelectListDescriptor.class))).thenReturn(actors);

        Assert.assertEquals(actors, actorMappingServiceImpl.getActors(41564L, 0, 1, "id", OrderByType.ASC));
    }

    /**
     * Test method for {@link org.bonitasoft.engine.actor.mapping.impl.ActorMappingServiceImpl#getActorsOfUserCanStartProcessDefinition(long, long)}.
     * 
     * @throws SBonitaReadException
     */
    @Test
    public final void getActorsOfUserCanStartProcessDefinition() throws SBonitaReadException {
        final List<SActor> actors = new ArrayList<SActor>(3);
        when(persistenceService.selectList(any(SelectListDescriptor.class))).thenReturn(actors);

        Assert.assertEquals(actors, actorMappingServiceImpl.getActorsOfUserCanStartProcessDefinition(315L, 5484L));
    }

    /**
     * Test method for {@link org.bonitasoft.engine.actor.mapping.impl.ActorMappingServiceImpl#getActors(java.util.Set, java.lang.Long)}.
     * 
     * @throws SBonitaReadException
     */
    @Test
    public final void getActorsByScopeIdsAndUserId() throws SBonitaReadException {
        final List<SActor> actors = new ArrayList<SActor>(3);
        when(persistenceService.selectList(any(SelectListDescriptor.class))).thenReturn(actors);

        Assert.assertEquals(actors, actorMappingServiceImpl.getActors(new HashSet<Long>(), 5484L));
    }

    /**
     * Test method for {@link org.bonitasoft.engine.actor.mapping.impl.ActorMappingServiceImpl#addActors(java.util.Set)}.
     */
    @Test
    public final void addActors() {
        // TODO : "Not yet implemented"
    }

    /**
     * Test method for {@link org.bonitasoft.engine.actor.mapping.impl.ActorMappingServiceImpl#addActor(org.bonitasoft.engine.actor.mapping.model.SActor)}.
     */
    @Test
    public final void addActor() {
        // TODO : "Not yet implemented"
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.actor.mapping.impl.ActorMappingServiceImpl#updateActor(long, org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor)}.
     */
    @Test
    public final void updateActor() {
        // TODO : "Not yet implemented"
    }

    /**
     * Test method for {@link org.bonitasoft.engine.actor.mapping.impl.ActorMappingServiceImpl#deleteActors(long)}.
     */
    @Test
    public final void deleteActors() {
        // TODO : "Not yet implemented"
    }

    /**
     * Test method for {@link org.bonitasoft.engine.actor.mapping.impl.ActorMappingServiceImpl#addUserToActor(long, long)}.
     */
    @Test
    public final void addUserToActor() {
        // TODO : "Not yet implemented"
    }

    /**
     * Test method for {@link org.bonitasoft.engine.actor.mapping.impl.ActorMappingServiceImpl#addGroupToActor(long, long)}.
     */
    @Test
    public final void addGroupToActor() {
        // TODO : "Not yet implemented"
    }

    /**
     * Test method for {@link org.bonitasoft.engine.actor.mapping.impl.ActorMappingServiceImpl#addRoleToActor(long, long)}.
     */
    @Test
    public final void addRoleToActor() {
        // TODO : "Not yet implemented"
    }

    /**
     * Test method for {@link org.bonitasoft.engine.actor.mapping.impl.ActorMappingServiceImpl#addRoleAndGroupToActor(long, long, long)}.
     */
    @Test
    public final void addRoleAndGroupToActor() {
        // TODO : "Not yet implemented"
    }

    /**
     * Test method for {@link org.bonitasoft.engine.actor.mapping.impl.ActorMappingServiceImpl#removeActorMember(long)}.
     */
    @Test
    public final void removeActorMemberLong() {
        // TODO : "Not yet implemented"
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.actor.mapping.impl.ActorMappingServiceImpl#removeActorMember(org.bonitasoft.engine.actor.mapping.model.SActorMember)}.
     */
    @Test
    public final void removeActorMemberSActorMember() {
        // TODO : "Not yet implemented"
    }

    /**
     * Test method for {@link org.bonitasoft.engine.actor.mapping.impl.ActorMappingServiceImpl#deleteAllActorMembers()}.
     */
    @Test
    public final void deleteAllActorMembers() {
        // TODO : "Not yet implemented"
    }

}
