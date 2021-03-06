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

package org.bonitasoft.engine.data;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.data.DataServiceImpl;
import org.bonitasoft.engine.data.DataSourceConfiguration;
import org.bonitasoft.engine.data.SDataException;
import org.bonitasoft.engine.data.SDataSourceNotFoundException;
import org.bonitasoft.engine.data.SDataSourceParameterNotFoundException;
import org.bonitasoft.engine.data.model.SDataSource;
import org.bonitasoft.engine.data.model.SDataSourceParameter;
import org.bonitasoft.engine.data.model.builder.SDataSourceModelBuilder;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectByIdDescriptor;
import org.bonitasoft.engine.persistence.SelectListDescriptor;
import org.bonitasoft.engine.persistence.SelectOneDescriptor;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.services.QueriableLoggerService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Matchers.any;

/**
 * @author Celine Souchet
 * 
 */
public class DataServiceImplTest {

    private SDataSourceModelBuilder logModelBuilder;

    private ClassLoaderService classLoaderService;

    private List<DataSourceConfiguration> dataSourceConfigurations;

    private Recorder recorder;

    private ReadPersistenceService persistence;

    private EventService eventService;

    private TechnicalLoggerService logger;

    private QueriableLoggerService queriableLoggerService;

    private DataServiceImpl dataServiceImpl;

    @Before
    public void setUp() throws Exception {
        logModelBuilder = mock(SDataSourceModelBuilder.class);
        classLoaderService = mock(ClassLoaderService.class);
        dataSourceConfigurations = new ArrayList<DataSourceConfiguration>();
        recorder = mock(Recorder.class);
        persistence = mock(ReadPersistenceService.class);
        eventService = mock(EventService.class);
        logger = mock(TechnicalLoggerService.class);
        queriableLoggerService = mock(QueriableLoggerService.class);
        dataServiceImpl = new DataServiceImpl(logModelBuilder, recorder, persistence, classLoaderService, eventService, dataSourceConfigurations, logger,
                queriableLoggerService);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.data.DataServiceImpl#getDataSource(long)}.
     * 
     * @throws SDataSourceNotFoundException
     * @throws SBonitaReadException
     */
    @Test
    public final void getDataSourceById() throws SDataSourceNotFoundException, SBonitaReadException {
        final SDataSource sDataSource = mock(SDataSource.class);
        when(persistence.selectById(any(SelectByIdDescriptor.class))).thenReturn(sDataSource);

        Assert.assertEquals(sDataSource, dataServiceImpl.getDataSource(456L));
    }

    @Test(expected = SDataSourceNotFoundException.class)
    public final void getDataSourceByIdNotExists() throws SBonitaReadException, SDataSourceNotFoundException {
        when(persistence.selectById(any(SelectByIdDescriptor.class))).thenReturn(null);

        dataServiceImpl.getDataSource(456L);
    }

    @Test(expected = SDataSourceNotFoundException.class)
    public final void getByIdThrowException() throws SBonitaReadException, SDataSourceNotFoundException {
        when(persistence.selectById(any(SelectByIdDescriptor.class))).thenThrow(new SBonitaReadException(""));

        dataServiceImpl.getDataSource(456L);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.data.DataServiceImpl#getDataSource(java.lang.String, java.lang.String)}.
     * 
     * @throws SBonitaReadException
     * @throws SDataSourceNotFoundException
     */
    @Test
    public final void getDataSourceByNameAndVersion() throws SBonitaReadException, SDataSourceNotFoundException {
        final SDataSource sDataSource = mock(SDataSource.class);
        when(persistence.selectOne(any(SelectOneDescriptor.class))).thenReturn(sDataSource);

        Assert.assertEquals(sDataSource, dataServiceImpl.getDataSource("name", "version"));
    }

    @Test(expected = SDataSourceNotFoundException.class)
    public final void getDataSourceByNameAndVersionNotExists() throws SBonitaReadException, SDataSourceNotFoundException {
        when(persistence.selectOne(any(SelectOneDescriptor.class))).thenReturn(null);

        dataServiceImpl.getDataSource("name", "version");
    }

    @Test(expected = SDataSourceNotFoundException.class)
    public final void getDataSourceByNameAndVersionThrowException() throws SBonitaReadException, SDataSourceNotFoundException {
        when(persistence.selectOne(any(SelectOneDescriptor.class))).thenThrow(new SBonitaReadException(""));

        dataServiceImpl.getDataSource("name", "version");
    }

    /**
     * Test method for {@link org.bonitasoft.engine.data.DataServiceImpl#getDataSourceParameter(long)}.
     * 
     * @throws SBonitaReadException
     * @throws SDataSourceParameterNotFoundException
     */
    @Test
    public final void getDataSourceParameterById() throws SBonitaReadException, SDataSourceParameterNotFoundException {
        final SDataSourceParameter sDataSourceParameter = mock(SDataSourceParameter.class);
        when(persistence.selectById(any(SelectByIdDescriptor.class))).thenReturn(sDataSourceParameter);

        Assert.assertEquals(sDataSourceParameter, dataServiceImpl.getDataSourceParameter(456L));
    }

    @Test(expected = SDataSourceParameterNotFoundException.class)
    public final void getDataSourceParameterByIdNotExists() throws SBonitaReadException, SDataSourceParameterNotFoundException {
        when(persistence.selectById(any(SelectByIdDescriptor.class))).thenReturn(null);

        dataServiceImpl.getDataSourceParameter(456L);
    }

    @Test(expected = SDataSourceParameterNotFoundException.class)
    public final void getDataSourceParameterByIdThrowException() throws SBonitaReadException, SDataSourceParameterNotFoundException {
        when(persistence.selectById(any(SelectByIdDescriptor.class))).thenThrow(new SBonitaReadException(""));

        dataServiceImpl.getDataSourceParameter(456L);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.data.DataServiceImpl#getDataSourceParameter(java.lang.String, long)}.
     * 
     * @throws SDataSourceParameterNotFoundException
     * @throws SBonitaReadException
     */
    @Test
    public final void getDataSourceParameterByNameAndDataSourceId() throws SDataSourceParameterNotFoundException, SBonitaReadException {
        final SDataSourceParameter sDataSourceParameter = mock(SDataSourceParameter.class);
        when(persistence.selectOne(any(SelectOneDescriptor.class))).thenReturn(sDataSourceParameter);

        Assert.assertEquals(sDataSourceParameter, dataServiceImpl.getDataSourceParameter("name", 546L));
    }

    @Test(expected = SDataSourceParameterNotFoundException.class)
    public final void getDataSourceParameterByNameAndDataSourceIdNotExists() throws SDataSourceParameterNotFoundException, SBonitaReadException {
        when(persistence.selectOne(any(SelectOneDescriptor.class))).thenReturn(null);

        dataServiceImpl.getDataSourceParameter("name", 546L);
    }

    @Test(expected = SDataSourceParameterNotFoundException.class)
    public final void getDataSourceParameterByNameAndDataSourceIdThrowException() throws SDataSourceParameterNotFoundException, SBonitaReadException {
        when(persistence.selectOne(any(SelectOneDescriptor.class))).thenThrow(new SBonitaReadException(""));

        dataServiceImpl.getDataSourceParameter("name", 546L);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.data.DataServiceImpl#getDataSourceParameters(long, org.bonitasoft.engine.persistence.QueryOptions)}.
     * 
     * @throws SDataException
     * @throws SBonitaReadException
     */
    @Test
    public final void getDataSourceParameters() throws SDataException, SBonitaReadException {
        final List<SDataSourceParameter> sDataSourceParameters = new ArrayList<SDataSourceParameter>();
        when(persistence.selectList(any(SelectListDescriptor.class))).thenReturn(sDataSourceParameters);

        final QueryOptions option = mock(QueryOptions.class);
        Assert.assertEquals(sDataSourceParameters, dataServiceImpl.getDataSourceParameters(546L, option));
    }

    @Test(expected = SDataException.class)
    public final void getDataSourceParametersThrowException() throws SDataException, SBonitaReadException {
        when(persistence.selectList(any(SelectListDescriptor.class))).thenThrow(new SBonitaReadException(""));

        final QueryOptions option = mock(QueryOptions.class);
        dataServiceImpl.getDataSourceParameters(546L, option);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.data.DataServiceImpl#getDataSources(org.bonitasoft.engine.persistence.QueryOptions)}.
     * 
     * @throws SDataException
     * @throws SBonitaReadException
     */
    @Test
    public final void getDataSources() throws SDataException, SBonitaReadException {
        final List<SDataSource> sDataSources = new ArrayList<SDataSource>();
        when(persistence.selectList(any(SelectListDescriptor.class))).thenReturn(sDataSources);

        final QueryOptions option = mock(QueryOptions.class);
        Assert.assertEquals(sDataSources, dataServiceImpl.getDataSources(option));
    }

    @Test(expected = SDataException.class)
    public final void getDataSourcesThrowException() throws SDataException, SBonitaReadException {
        when(persistence.selectList(any(SelectListDescriptor.class))).thenThrow(new SBonitaReadException(""));

        final QueryOptions option = mock(QueryOptions.class);
        dataServiceImpl.getDataSources(option);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.data.DataServiceImpl#getDataSourceImplementation(java.lang.Class, long)}.
     */
    @Test
    public final void getDataSourceImplementation() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.data.DataServiceImpl#createDataSource(org.bonitasoft.engine.data.model.SDataSource)}.
     */
    @Test
    public final void createDataSource() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.data.DataServiceImpl#createDataSourceParameter(org.bonitasoft.engine.data.model.SDataSourceParameter)}.
     */
    @Test
    public final void createDataSourceParameter() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.data.DataServiceImpl#createDataSourceParameters(java.util.Collection)}.
     */
    @Test
    public final void createDataSourceParameters() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.data.DataServiceImpl#removeDataSource(org.bonitasoft.engine.data.model.SDataSource)}.
     */
    @Test
    public final void removeDataSourceByObject() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.data.DataServiceImpl#removeDataSource(long)}.
     */
    @Test
    public final void removeDataSourceById() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.data.DataServiceImpl#removeDataSourceParameter(long)}.
     */
    @Test
    public final void removeDataSourceParameterById() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.data.DataServiceImpl#removeDataSourceParameter(org.bonitasoft.engine.data.model.SDataSourceParameter)}.
     */
    @Test
    public final void removeDataSourceParameterByObject() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.data.DataServiceImpl#removeDataSourceParameters(long)}.
     */
    @Test
    public final void removeDataSourceParametersByDataSourceId() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.data.DataServiceImpl#removeDataSourceParameters(java.util.Collection)}.
     */
    @Test
    public final void removeDataSourceParametersByIds() {
        // TODO : Not yet implemented
    }

}
