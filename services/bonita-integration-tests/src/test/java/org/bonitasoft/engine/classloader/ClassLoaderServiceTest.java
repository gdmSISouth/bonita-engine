package org.bonitasoft.engine.classloader;

import java.io.InputStream;
import java.net.URL;

import org.bonitasoft.engine.CommonServiceTest;
import org.bonitasoft.engine.commons.IOUtil;
import org.bonitasoft.engine.dependency.DependencyService;
import org.bonitasoft.engine.dependency.SDependencyException;
import org.bonitasoft.engine.dependency.model.SDependency;
import org.bonitasoft.engine.dependency.model.SDependencyMapping;
import org.bonitasoft.engine.dependency.model.builder.DependencyBuilder;
import org.bonitasoft.engine.dependency.model.builder.DependencyBuilderAccessor;
import org.bonitasoft.engine.dependency.model.builder.DependencyMappingBuilder;
import org.bonitasoft.engine.test.util.TestUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Elias Ricken de Medeiros, Charles Souillard, Baptiste Mesta
 */
public class ClassLoaderServiceTest extends CommonServiceTest {

    private static DependencyMappingBuilder dependencyMappingModelBuilder;

    private static DependencyBuilder dependencyModelBuilder;

    private static DependencyBuilderAccessor platformDependencyBuilderAccessor;

    private DependencyService dependencyService;

    private DependencyService platformDependencyService;

    private ClassLoaderService classLoaderService;

    static {
        dependencyMappingModelBuilder = getServicesBuilder().buildDependencyMappingModelBuilder();
        dependencyModelBuilder = getServicesBuilder().buildDependencyModelBuilder();
        platformDependencyBuilderAccessor = getServicesBuilder().buildPlatformDependencyBuilderAccessor();
    }

    private static final String TYPE1 = "type1";

    private static final String TYPE2 = "type2";

    private static final long ID1 = 1;

    private static final long ID2 = 2;

    @Override
    @After
    public void tearDown() throws Exception {
        classLoaderService.removeAllLocalClassLoaders(TYPE1);
        classLoaderService.removeAllLocalClassLoaders(TYPE2);

        TestUtil.closeTransactionIfOpen(getTransactionService());

        getTransactionService().begin();
        dependencyService.deleteAllDependencyMappings();
        dependencyService.deleteAllDependencies();
        platformDependencyService.deleteAllDependencyMappings();
        platformDependencyService.deleteAllDependencies();
        getTransactionService().complete();
        classLoaderService = null;
        dependencyService = null;
        platformDependencyService = null;
    }

    @Before
    public void setUp() throws Exception {
        classLoaderService = getServicesBuilder().buildClassLoaderService();
        dependencyService = getServicesBuilder().buildDependencyService();
        platformDependencyService = getServicesBuilder().buildPlatformDependencyService();
    }

    private void initializeClassLoaderService() throws Exception {
        getTransactionService().begin();
        final long globalResourceId = createPlatformDependency("globalResource", "1.0", "globalResource.jar",
                IOUtil.generateJar(GlobalClass1.class, GlobalClass2.class, SharedClass1.class));

        createPlatformDependencyMapping(globalResourceId, classLoaderService.getGlobalClassLoaderType(), classLoaderService.getGlobalClassLoaderId());

        final long localResource1Id = createDependency("LocalResource1", "1.0", "LocalResource1.jar", IOUtil.generateJar(LocalClass1.class, LocalClass2.class));
        final long localResource2Id = createDependency("LocalResource2", "1.0", "LocalResource2.jar",
                IOUtil.generateJar(LocalClass3.class, LocalClass4.class, SharedClass1.class));

        createDependencyMapping(localResource1Id, TYPE1, ID1);
        createDependencyMapping(localResource2Id, TYPE1, ID1);

        createDependencyMapping(localResource1Id, TYPE1, ID2);
        getTransactionService().complete();
    }

    private void addNotInPathDependencies() throws Exception {
        getTransactionService().begin();
        final URL globalFile = ClassLoaderServiceTest.class.getResource("NotInPathGlobal.jar");
        final byte[] globalFileContent = IOUtil.getAllContentFrom(globalFile);
        final long globalFileId = createPlatformDependency("NotInPathGlobal", "1.0", "NotInPathGlobal.jar", globalFileContent);

        final URL sharedFile = ClassLoaderServiceTest.class.getResource("NotInPathShared.jar");
        final byte[] sharedFileContent = IOUtil.getAllContentFrom(sharedFile);
        final long sharedFileId = createPlatformDependency("NotInPathShared", "1.0", "NotInPathShared.jar", sharedFileContent);

        final URL localFile = ClassLoaderServiceTest.class.getResource("NotInPathLocal.jar");
        final byte[] localFileContent = IOUtil.getAllContentFrom(localFile);
        final long localFileId = createDependency("NotInPathLocal", "1.0", "NotInPathLocal.jar", localFileContent);

        createPlatformDependencyMapping(globalFileId, classLoaderService.getGlobalClassLoaderType(), classLoaderService.getGlobalClassLoaderId());
        createPlatformDependencyMapping(sharedFileId, classLoaderService.getGlobalClassLoaderType(), classLoaderService.getGlobalClassLoaderId());

        createDependencyMapping(localFileId, TYPE1, ID1);
        // createDependencyMapping(sharedFileId, TYPE1, ID1);
        getTransactionService().complete();
    }

    private long createDependency(final String name, final String version, final String fileName, final byte[] value) throws SDependencyException {
        final DependencyBuilder builder = dependencyModelBuilder.createNewInstance(name, version, fileName, value);
        final SDependency dependency = builder.done();
        dependencyService.createDependency(dependency);
        return dependency.getId();
    }

    private long createPlatformDependency(final String name, final String version, final String fileName, final byte[] value) throws SDependencyException {
        final DependencyBuilder dependencyBuilder = platformDependencyBuilderAccessor.getDependencyBuilder();
        final DependencyBuilder builder = dependencyBuilder.createNewInstance(name, version, fileName, value);
        final SDependency dependency = builder.done();
        platformDependencyService.createDependency(dependency);
        return dependency.getId();
    }

    private void initializeClassLoaderServiceWithTwoApplications() throws Exception {
        getTransactionService().begin();
        final long globalResourceId = createPlatformDependency("globalResource", "1.0", "globalResource.jar",
                IOUtil.generateJar(GlobalClass1.class, SharedClass1.class));
        createPlatformDependencyMapping(globalResourceId, classLoaderService.getGlobalClassLoaderType(), classLoaderService.getGlobalClassLoaderId());

        final long localResource1Id = createDependency("LocalResource11", "1.0", "LocalResource1.jar", IOUtil.generateJar(LocalClass1.class));
        final long localResource2Id = createDependency("LocalResource12", "1.0", "LocalResource2.jar", IOUtil.generateJar(LocalClass3.class));

        createDependencyMapping(localResource1Id, TYPE1, ID1);
        createDependencyMapping(localResource2Id, TYPE1, ID1);

        createDependencyMapping(localResource1Id, TYPE1, ID2);

        final long localResource1Id2 = createDependency("LocalResource21", "1.0", "LocalResource1.jar", IOUtil.generateJar(LocalClass2.class));
        final long localResource2Id2 = createDependency("LocalResource22", "1.0", "LocalResource2.jar", IOUtil.generateJar(LocalClass4.class));

        createDependencyMapping(localResource1Id2, TYPE2, ID1);
        createDependencyMapping(localResource2Id2, TYPE2, ID1);

        createDependencyMapping(localResource1Id2, TYPE2, ID2);
        getTransactionService().complete();
    }

    private long createPlatformDependencyMapping(final long dependencyId, final String artifactType, final long artifactId) throws SDependencyException {
        final DependencyMappingBuilder dependencyMappingBuilder = platformDependencyBuilderAccessor.getDependencyMappingBuilder();
        final DependencyMappingBuilder builder = dependencyMappingBuilder.createNewInstance(dependencyId, artifactId, artifactType);
        final SDependencyMapping dependencyMapping = builder.done();
        platformDependencyService.createDependencyMapping(dependencyMapping);
        return dependencyMapping.getId();
    }

    private long createDependencyMapping(final long dependencyId, final String artifactType, final long artifactId) throws SDependencyException {
        final DependencyMappingBuilder builder = dependencyMappingModelBuilder.createNewInstance(dependencyId, artifactId, artifactType);
        final SDependencyMapping dependencyMapping = builder.done();
        dependencyService.createDependencyMapping(dependencyMapping);
        return dependencyMapping.getId();
    }

    @Test
    public void testLoadGlobalClassUsingGlobalClassLoader() throws Exception {
        initializeClassLoaderService();
        // getTransactionService().begin();
        final ClassLoader globalClassLoader = classLoaderService.getGlobalClassLoader();
        final Class<?> clazz = globalClassLoader.loadClass("org.bonitasoft.engine.classloader.GlobalClass1");
        final ClassLoader classLoader = clazz.getClassLoader();

        // getTransactionService().complete();
        assertTrue(isClassLoaderGlobal(classLoader));
        assertSameClassloader(globalClassLoader, classLoader);
    }

    @Test
    public void testLoadLocalClassUsingLocalClassLoader() throws Exception {
        initializeClassLoaderService();
        // getTransactionService().begin();
        final ClassLoader localClassLoader = classLoaderService.getLocalClassLoader(TYPE1, ID1);
        final Class<?> clazz = localClassLoader.loadClass("org.bonitasoft.engine.classloader.LocalClass1");
        final ClassLoader classLoader = clazz.getClassLoader();
        assertTrue(isClassLoaderLocal(classLoader));

        assertSameClassloader(localClassLoader, classLoader);
        // getTransactionService().complete();
    }

    @Test
    public void testLoadGlobalClassUsingLocalClassLoader() throws Exception {
        initializeClassLoaderService();
        // getTransactionService().begin();
        final ClassLoader localClassLoader = classLoaderService.getLocalClassLoader(TYPE1, ID1);
        final Class<?> clazz = localClassLoader.loadClass("org.bonitasoft.engine.classloader.GlobalClass1");
        final ClassLoader classLoader = clazz.getClassLoader();
        assertTrue(isClassLoaderGlobal(classLoader));

        assertNotSameClassloader(localClassLoader, classLoader);
        // getTransactionService().complete();
    }

    @Test
    public void testLoadSharedClassUsingLocalClassLoader() throws Exception {
        initializeClassLoaderService();
        // getTransactionService().begin();
        final ClassLoader localClassLoader = classLoaderService.getLocalClassLoader(TYPE1, ID1);
        final Class<?> clazz = localClassLoader.loadClass("org.bonitasoft.engine.classloader.SharedClass1");
        final ClassLoader classLoader = clazz.getClassLoader();
        assertTrue(isClassLoaderLocal(classLoader));

        assertSameClassloader(localClassLoader, classLoader);
        // getTransactionService().complete();
    }

    @Test
    public void testLoadSharedClassUsingGlobalClassLoader() throws Exception {
        initializeClassLoaderService();
        // getTransactionService().begin();
        final ClassLoader globalClassLoader = classLoaderService.getGlobalClassLoader();
        final Class<?> clazz = globalClassLoader.loadClass("org.bonitasoft.engine.classloader.SharedClass1");
        final ClassLoader classLoader = clazz.getClassLoader();
        assertTrue(isClassLoaderGlobal(classLoader));

        assertSameClassloader(globalClassLoader, classLoader);
        // getTransactionService().complete();
    }

    @Test
    public void testLoadOnlyInPathClassUsingGlobalClassLoader() throws Exception {
        initializeClassLoaderService();
        // getTransactionService().begin();
        final ClassLoader classLoader = classLoaderService.getGlobalClassLoader();
        final Class<?> clazz = classLoader.loadClass("org.bonitasoft.engine.classloader.OnlyInPathClass1");
        assertFalse(isBonitaClassLoader(clazz.getClassLoader()));
        // getTransactionService().complete();
    }

    @Test
    public void testLoadOnlyInPathClassUsingLocalClassLoader() throws Exception {
        initializeClassLoaderService();
        // getTransactionService().begin();
        final ClassLoader classLoader = classLoaderService.getLocalClassLoader(TYPE1, ID1);
        final Class<?> clazz = classLoader.loadClass("org.bonitasoft.engine.classloader.OnlyInPathClass1");
        assertFalse(isBonitaClassLoader(clazz.getClassLoader()));
        // getTransactionService().complete();
    }

    @Test
    public void testLoadLocalClassUsingGlobalClassLoader() throws Exception {
        initializeClassLoaderService();
        // getTransactionService().begin();
        final ClassLoader virtualGlobalClassLoader = classLoaderService.getGlobalClassLoader();

        final Class<?> clazz = virtualGlobalClassLoader.loadClass("org.bonitasoft.engine.classloader.LocalClass1");
        assertFalse(isBonitaClassLoader(clazz.getClassLoader()));
        // getTransactionService().complete();
    }

    @Test
    public void testGlobalClassLoaderIsSingleForTwoLocalClassLoaders() throws Exception {
        initializeClassLoaderService();
        // getTransactionService().begin();
        final ClassLoader localClassLoaderP1 = classLoaderService.getLocalClassLoader(TYPE1, ID1);
        final Class<?> clazzP1 = localClassLoaderP1.loadClass("org.bonitasoft.engine.classloader.GlobalClass1");
        final ClassLoader classLoader = clazzP1.getClassLoader();
        assertTrue(isClassLoaderGlobal(classLoader));

        assertFalse(localClassLoaderP1 == classLoader);

        final ClassLoader localClassLoaderP2 = classLoaderService.getLocalClassLoader(TYPE1, ID2);
        final Class<?> clazzP2 = localClassLoaderP2.loadClass("org.bonitasoft.engine.classloader.GlobalClass1");
        final ClassLoader classLoader2 = clazzP2.getClassLoader();
        assertTrue(isClassLoaderGlobal(classLoader2));

        assertFalse(localClassLoaderP2 == classLoader2);

        // verify if they are the same object (same reference)
        assertSame(classLoader, classLoader2);
        // getTransactionService().complete();
    }

    @Test
    public void testLoadLocalClassUsingUsingBadLocalClassLoader() throws Exception {
        initializeClassLoaderService();
        // getTransactionService().begin();
        final ClassLoader classLoader = classLoaderService.getLocalClassLoader(TYPE1, ID2);
        final Class<?> clazz = classLoader.loadClass("org.bonitasoft.engine.classloader.LocalClass3");
        assertFalse(isBonitaClassLoader(clazz.getClassLoader()));
        // getTransactionService().complete();
    }

    @Test
    public void testLoadSharedClassUsingUsingBadLocalClassLoader() throws Exception {
        initializeClassLoaderService();
        // getTransactionService().begin();
        final ClassLoader classLoader = classLoaderService.getLocalClassLoader(TYPE1, ID2);
        final Class<?> clazz = classLoader.loadClass("org.bonitasoft.engine.classloader.SharedClass1");
        final ClassLoader classLoader2 = clazz.getClassLoader();
        assertTrue(isClassLoaderGlobal(classLoader2));

        assertNotSame(classLoader, classLoader2);
        // getTransactionService().complete();
    }

    @Test
    public void testRemoveLocalClassLoader() throws Exception {
        initializeClassLoaderService();
        // getTransactionService().begin();
        final ClassLoader localClassLoader1 = classLoaderService.getLocalClassLoader(TYPE1, ID1);
        final ClassLoader localClassLoader2 = classLoaderService.getLocalClassLoader(TYPE1, ID2);

        classLoaderService.removeLocalClassLoader(TYPE1, ID1);

        assertNotSameClassloader(localClassLoader1, classLoaderService.getLocalClassLoader(TYPE1, ID1));

        classLoaderService.removeLocalClassLoader(TYPE1, ID2);

        assertNotSameClassloader(localClassLoader2, classLoaderService.getLocalClassLoader(TYPE1, ID2));
        // getTransactionService().complete();
    }

    @Test
    public void testRemoveAllLocalClassLoader() throws Exception {
        initializeClassLoaderService();
        // getTransactionService().begin();
        final ClassLoader localClassLoader1 = classLoaderService.getLocalClassLoader(TYPE1, ID1);
        final ClassLoader localClassLoader2 = classLoaderService.getLocalClassLoader(TYPE1, ID2);

        classLoaderService.removeAllLocalClassLoaders(TYPE1);

        assertNotSameClassloader(localClassLoader1, classLoaderService.getLocalClassLoader(TYPE1, ID1));
        assertNotSameClassloader(localClassLoader2, classLoaderService.getLocalClassLoader(TYPE1, ID2));
        // getTransactionService().complete();
    }

    @Test
    public void testAddResourcesToGlobalClassLoader() throws Exception {
        initializeClassLoaderService();
        getTransactionService().begin();
        final ClassLoader globalClassLoader = classLoaderService.getGlobalClassLoader();
        Class<?> clazz = globalClassLoader.loadClass("org.bonitasoft.engine.classloader.GlobalClass3");
        assertFalse(isBonitaClassLoader(clazz.getClassLoader()));

        final long dependencyId = createPlatformDependency("newlib", "1.0", "newlib.jar", IOUtil.generateJar(GlobalClass3.class));
        createPlatformDependencyMapping(dependencyId, classLoaderService.getGlobalClassLoaderType(), classLoaderService.getGlobalClassLoaderId());
        Thread.sleep(10); // to be sure classlaoder refresh does NOT occur.
        clazz = globalClassLoader.loadClass("org.bonitasoft.engine.classloader.GlobalClass3");
        final ClassLoader classLoader2 = clazz.getClassLoader();
        assertTrue(isClassLoaderGlobal(classLoader2));

        assertSameClassloader(globalClassLoader, classLoader2);
        getTransactionService().complete();
    }

    @Test
    public void testAddResourcesToLocalClassLoader() throws Exception {
        initializeClassLoaderService();
        getTransactionService().begin();
        final ClassLoader localClassLoader = classLoaderService.getLocalClassLoader(TYPE1, ID1);
        assertTrue(isClassLoaderGlobal(localClassLoader.loadClass("org.bonitasoft.engine.classloader.GlobalClass2").getClassLoader()));

        final long dependencyId = createDependency("newlib", "1.0", "newlib.jar", IOUtil.generateJar(GlobalClass2.class));
        createDependencyMapping(dependencyId, TYPE1, ID1);

        // check the refresh has been done using the service
        assertTrue(isClassLoaderLocal(classLoaderService.getLocalClassLoader(TYPE1, ID1).loadClass("org.bonitasoft.engine.classloader.GlobalClass2")
                .getClassLoader()));

        // check the refresh has been done using the old reference
        assertTrue(isClassLoaderLocal(localClassLoader.loadClass("org.bonitasoft.engine.classloader.GlobalClass2").getClassLoader()));

        assertSameClassloader(localClassLoader, classLoaderService.getLocalClassLoader(TYPE1, ID1));
        getTransactionService().complete();
    }

    @Test
    @Ignore("ENGINE-1366")
    public void testResetGlobalClassLoader() throws Exception {
        initializeClassLoaderService();

        getTransactionService().begin();
        final long dependencyId = createPlatformDependency("newlib", "1.0", "newlib.jar", IOUtil.generateJar(GlobalClass3.class));
        final long mappingId = createPlatformDependencyMapping(dependencyId, classLoaderService.getGlobalClassLoaderType(),
                classLoaderService.getGlobalClassLoaderId());

        ClassLoader globalClassLoader = classLoaderService.getGlobalClassLoader();
        Class<?> clazz = globalClassLoader.loadClass("org.bonitasoft.engine.classloader.GlobalClass3");
        final ClassLoader classLoader = clazz.getClassLoader();
        assertTrue(isClassLoaderGlobal(classLoader));
        assertSameClassloader(globalClassLoader, classLoader);

        platformDependencyService.deleteDependencyMapping(mappingId);

        globalClassLoader = classLoaderService.getGlobalClassLoader();
        clazz = globalClassLoader.loadClass("org.bonitasoft.engine.classloader.GlobalClass3");
        assertFalse(isBonitaClassLoader(clazz.getClassLoader()));
        getTransactionService().complete();
    }

    @Test
    public void testLoadNotInPathGlobalClassUsingGlobalClassLoader() throws Exception {
        initializeClassLoaderService();
        addNotInPathDependencies();
        // getTransactionService().begin();

        final ClassLoader globalClassLoader = classLoaderService.getGlobalClassLoader();
        final Class<?> clazz = globalClassLoader.loadClass("org.bonitasoft.classloader.test.NotInPathGlobalClass1");
        final ClassLoader classLoader = clazz.getClassLoader();
        assertTrue(isClassLoaderGlobal(classLoader));

        assertSameClassloader(globalClassLoader, classLoader);
        // getTransactionService().complete();
    }

    @Test
    public void testLoadNotInPathGlobalClassUsingLocalClassLoader() throws Exception {
        initializeClassLoaderService();
        addNotInPathDependencies();
        // getTransactionService().begin();

        final ClassLoader localClassLoader = classLoaderService.getLocalClassLoader(TYPE1, ID1);
        final Class<?> clazz = localClassLoader.loadClass("org.bonitasoft.classloader.test.NotInPathGlobalClass1");
        assertTrue(isClassLoaderGlobal(clazz.getClassLoader()));
        assertNotSameClassloader(localClassLoader, clazz.getClassLoader());
        assertSameClassloader(classLoaderService.getGlobalClassLoader(), clazz.getClassLoader());
        // getTransactionService().complete();
    }

    @Test
    public void testLoadNotInPathLocalClassUsingLocalClassLoader() throws Exception {
        initializeClassLoaderService();
        addNotInPathDependencies();
        // getTransactionService().begin();

        final ClassLoader localClassLoader = classLoaderService.getLocalClassLoader(TYPE1, ID1);
        final Class<?> clazz = localClassLoader.loadClass("org.bonitasoft.classloader.test.NotInPathLocalClass1");
        final ClassLoader classLoader = clazz.getClassLoader();
        assertTrue(isClassLoaderLocal(classLoader));

        assertSameClassloader(localClassLoader, classLoader);
        // getTransactionService().complete();
    }

    @Test(expected = ClassNotFoundException.class)
    public void testLoadNotInPathLocalClassUsingWrongLocalClassLoader() throws Exception {
        initializeClassLoaderService();
        addNotInPathDependencies();
        // getTransactionService().begin();

        final ClassLoader classLoader = classLoaderService.getLocalClassLoader(TYPE1, ID2);
        // getTransactionService().complete();
        classLoader.loadClass("org.bonitasoft.classloader.test.NotInPathLocalClass1");
        fail("load class with wrong classloader");
    }

    @Test
    public void testLoadNotInPathSharedClassUsingGlobalClassLoader() throws Exception {
        initializeClassLoaderService();
        addNotInPathDependencies();
        // getTransactionService().begin();

        final ClassLoader globalClassLoader = classLoaderService.getGlobalClassLoader();
        final Class<?> clazz = globalClassLoader.loadClass("org.bonitasoft.classloader.test.NotInPathSharedClass1");
        final ClassLoader classLoader = clazz.getClassLoader();
        assertTrue(isClassLoaderGlobal(classLoader));

        assertSameClassloader(globalClassLoader, classLoader);
        // getTransactionService().complete();
    }

    @Test
    public void testLoadNotInPathSharedClassUsingLocalClassLoader() throws Exception {
        initializeClassLoaderService();
        addNotInPathDependencies();
        // getTransactionService().begin();

        final ClassLoader localClassLoader = classLoaderService.getLocalClassLoader(TYPE1, ID1);
        final Class<?> clazz = localClassLoader.loadClass("org.bonitasoft.classloader.test.NotInPathSharedClass1");
        final ClassLoader classLoader = clazz.getClassLoader();
        assertTrue(isClassLoaderGlobal(classLoader));

        // getTransactionService().complete();
    }

    @Test
    public void testLoadResource() throws Exception {
        initializeClassLoaderService();
        getTransactionService().begin();
        final URL resourceFile = ClassLoaderServiceTest.class.getResource("resource.txt");
        final byte[] resourceFileContent = IOUtil.getAllContentFrom(resourceFile);

        final long resourceId = createPlatformDependency("resource", "1.0", "resource.txt", resourceFileContent);
        createPlatformDependencyMapping(resourceId, classLoaderService.getGlobalClassLoaderType(), classLoaderService.getGlobalClassLoaderId());
        getTransactionService().complete();

        // getTransactionService().begin();
        final ClassLoader virtualGlobalClassLoader = classLoaderService.getGlobalClassLoader();
        final InputStream resourceStream = virtualGlobalClassLoader.getResourceAsStream("resource.txt");
        assertEquals(resourceFileContent.length, IOUtil.getAllContentFrom(resourceStream).length);
        // getTransactionService().complete();
    }

    @Test
    public void testDifferentsApplicationHaveDifferentGlobalClassLoader() throws Exception {
        initializeClassLoaderServiceWithTwoApplications();
        // getTransactionService().begin();
        final ClassLoader type1ClassLoader = classLoaderService.getLocalClassLoader(TYPE1, ID1);
        final ClassLoader type2ClassLoader = classLoaderService.getLocalClassLoader(TYPE2, ID1);

        final Class<?> bpmClazz = type1ClassLoader.loadClass("org.bonitasoft.engine.classloader.SharedClass1");
        final Class<?> casesClazz = type2ClassLoader.loadClass("org.bonitasoft.engine.classloader.SharedClass1");
        final ClassLoader type1ClassLoader2 = bpmClazz.getClassLoader();
        assertTrue(isClassLoaderGlobal(type1ClassLoader2));
        assertSameClassloader(classLoaderService.getGlobalClassLoader(), type1ClassLoader2);

        final ClassLoader type2ClassLoader2 = casesClazz.getClassLoader();
        assertTrue(isClassLoaderGlobal(type2ClassLoader2));
        assertSameClassloader(classLoaderService.getGlobalClassLoader(), type2ClassLoader2);
        // getTransactionService().complete();
    }

    private boolean isClassLoaderGlobal(final ClassLoader classLoader) {
        return classLoader instanceof BonitaClassLoader && ((BonitaClassLoader) classLoader).getType().equals(ClassLoaderServiceImpl.GLOBAL_TYPE);
    }

    private boolean isClassLoaderLocal(final ClassLoader classLoader) {
        return classLoader instanceof BonitaClassLoader && !((BonitaClassLoader) classLoader).getType().equals(ClassLoaderServiceImpl.GLOBAL_TYPE);
    }

    private boolean isBonitaClassLoader(final ClassLoader classLoader) {
        return classLoader instanceof BonitaClassLoader;
    }

    private void assertSameClassloader(final ClassLoader classLoader1, final ClassLoader classLoader2) throws ClassLoaderException {
        assertNotNull(classLoader1);
        assertNotNull(classLoader2);
        final ClassLoader c1 = classLoader1 instanceof VirtualClassLoader ? ((VirtualClassLoader) classLoader1).getClassLoader() : classLoader1;
        final ClassLoader c2 = classLoader2 instanceof VirtualClassLoader ? ((VirtualClassLoader) classLoader2).getClassLoader() : classLoader2;
        assertEquals(c1, c2);
    }

    private void assertNotSameClassloader(final ClassLoader classLoader1, final ClassLoader classLoader2) throws ClassLoaderException {
        assertNotNull(classLoader1);
        assertNotNull(classLoader2);
        final ClassLoader c1 = classLoader1 instanceof VirtualClassLoader ? ((VirtualClassLoader) classLoader1).getClassLoader() : classLoader1;
        final ClassLoader c2 = classLoader2 instanceof VirtualClassLoader ? ((VirtualClassLoader) classLoader2).getClassLoader() : classLoader2;
        assertNotSame(c1, c2);
    }

}
