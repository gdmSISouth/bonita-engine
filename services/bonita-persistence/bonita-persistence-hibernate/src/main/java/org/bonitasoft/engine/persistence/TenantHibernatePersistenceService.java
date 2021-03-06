/**
 * Copyright (C) 2011 BonitaSoft S.A.
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
package org.bonitasoft.engine.persistence;

import java.util.List;

import javax.sql.DataSource;

import org.bonitasoft.engine.commons.ClassReflector;
import org.bonitasoft.engine.commons.ReflectException;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.sequence.SequenceManager;
import org.bonitasoft.engine.services.SPersistenceException;
import org.bonitasoft.engine.sessionaccessor.ReadSessionAccessor;
import org.bonitasoft.engine.sessionaccessor.TenantIdNotSetException;
import org.hibernate.AssertionFailure;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.StaleStateException;
import org.hibernate.exception.LockAcquisitionException;

/**
 * @author Baptiste Mesta
 * @author Nicolas Chabanoles
 * @author Yanyan Liu
 * @author Matthieu Chaffotte
 */
public class TenantHibernatePersistenceService extends AbstractHibernatePersistenceService {

    private static final String TENANT_FILTER = "tenantFilter";

    private final ReadSessionAccessor sessionAccessor;

    public TenantHibernatePersistenceService(final String name, final ReadSessionAccessor sessionAccessor,
            final HibernateConfigurationProvider hbmConfigurationProvider, final DBConfigurationsProvider tenantConfigurationsProvider,
            final String statementDelimiter, final String likeEscapeCharacter, final TechnicalLoggerService logger, final SequenceManager sequenceManager,
            final DataSource datasource) throws SPersistenceException {
        super(name, hbmConfigurationProvider, tenantConfigurationsProvider, statementDelimiter, likeEscapeCharacter, logger, sequenceManager, datasource);
        this.sessionAccessor = sessionAccessor;
    }

    protected void updateTenantFilter(final Session session, final boolean useTenant) throws SPersistenceException {
        if (useTenant) {
            try {
                session.enableFilter(TENANT_FILTER).setParameter("tenantId", getTenantId());
            } catch (final TenantIdNotSetException e) {
                throw new SPersistenceException(e);
            }
        } else {
            session.disableFilter(TENANT_FILTER);
        }
    }

    protected void setTenant(final PersistentObject entity) throws SPersistenceException {
        if (entity == null) {
            return;
        }
        // if this entity has no id, set it
        Long tenantId = null;
        try {
            tenantId = ClassReflector.invokeGetter(entity, "getTenantId");
        } catch (final Exception e) {
            // this is a new object to save
        }
        if (tenantId == null || tenantId == -1 || tenantId == 0) {
            try {
                tenantId = getTenantId();
                ClassReflector.invokeSetter(entity, "setTenantId", long.class, tenantId);
            } catch (final ReflectException e) {
                throw new SPersistenceException("Can't set tenant on entity " + entity, e);
            } catch (final TenantIdNotSetException e) {
                throw new SPersistenceException("Can't set tenant on entity " + entity, e);
            }
        }
    }

    @Override
    protected Session getSession(final boolean useTenant) throws SPersistenceException {
        final Session session = super.getSession(useTenant);
        updateTenantFilter(session, useTenant);
        return session;
    }

    @Override
    public void flushStatements() throws SPersistenceException {
        super.flushStatements(true);
    }

    @Override
    public void delete(final PersistentObject entity) throws SPersistenceException {
        try {
            logger.log(this.getClass(), TechnicalLogSeverity.DEBUG, "[Tenant] Deleting instance of class " + entity.getClass().getSimpleName() + " with id="
                    + entity.getId());
            final Class<? extends PersistentObject> mappedClass = getMappedClass(entity.getClass());
            final Session session = getSession(true);
            final Object pe = session.get(mappedClass, new PersistentObjectId(entity.getId(), getTenantId()));
            session.delete(pe);
            // Object o2 = session.get(entity.getClass(), new PersistentObjectId(entity.getId(), getTenantId()));
            // assert(o2 == null);
        } catch (final TenantIdNotSetException e) {
            throw new SPersistenceException(e);
        } catch (final AssertionFailure af) {
            throw new SRetryableException(af);
        } catch (final LockAcquisitionException lae) {
            throw new SRetryableException(lae);
        } catch (final StaleStateException sse) {
            throw new SRetryableException(sse);
        } catch (final HibernateException he) {
            throw new SPersistenceException(he);
        }
    }

    @Override
    public void insert(final PersistentObject entity) throws SPersistenceException {
        setTenant(entity);
        super.insert(entity);
    }

    @Override
    public void insertInBatch(final List<PersistentObject> entities) throws SPersistenceException {
        for (final PersistentObject entity : entities) {
            setTenant(entity);
        }
        super.insertInBatch(entities);
    }

    @Override
    protected long getTenantId() throws TenantIdNotSetException {
        return sessionAccessor.getTenantId();
    }

    @SuppressWarnings("unchecked")
    @Override
    <T extends PersistentObject> T selectById(final Session session, final SelectByIdDescriptor<T> selectDescriptor) throws SBonitaReadException {
        try {
            final PersistentObjectId id = new PersistentObjectId(selectDescriptor.getId(), getTenantId());
            Class<? extends PersistentObject> mappedClass = null;
            try {
                mappedClass = getMappedClass(selectDescriptor.getEntityType());
            } catch (final SPersistenceException e) {
                throw new SBonitaReadException(e);
            }
            /*
             * if (mappedClass.getName().equals("org.bonitasoft.engine.core.process.instance.model.event.impl.SEventInstanceImpl")) {
             * return (T) session.get("SEventInstanceImpl", id);
             * }
             */
            return (T) session.get(mappedClass, id);
        } catch (final TenantIdNotSetException e) {
            return super.selectById(session, selectDescriptor);
        } catch (final AssertionFailure af) {
            throw new SRetryableException(af);
        } catch (final LockAcquisitionException lae) {
            throw new SRetryableException(lae);
        } catch (final StaleStateException sse) {
            throw new SRetryableException(sse);
        } catch (final HibernateException he) {
            throw new SBonitaReadException(he);
        }
    }

}
