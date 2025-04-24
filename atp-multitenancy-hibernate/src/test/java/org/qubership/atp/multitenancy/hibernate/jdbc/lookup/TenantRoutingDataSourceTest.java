/*
 * # Copyright 2024-2025 NetCracker Technology Corporation
 * #
 * # Licensed under the Apache License, Version 2.0 (the "License");
 * # you may not use this file except in compliance with the License.
 * # You may obtain a copy of the License at
 * #
 * #      http://www.apache.org/licenses/LICENSE-2.0
 * #
 * # Unless required by applicable law or agreed to in writing, software
 * # distributed under the License is distributed on an "AS IS" BASIS,
 * # WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * # See the License for the specific language governing permissions and
 * # limitations under the License.
 */

package org.qubership.atp.multitenancy.hibernate.jdbc.lookup;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.qubership.atp.multitenancy.core.context.TenantContext;
import org.qubership.atp.multitenancy.hibernate.config.HikariConfiguration;
import org.qubership.atp.multitenancy.hibernate.jdbc.pojo.AdditionalPostgresCluster;
import org.qubership.atp.multitenancy.hibernate.jdbc.pojo.AdditionalPostgresClusters;
import org.qubership.atp.multitenancy.hibernate.jdbc.pojo.DefaultPostgresCluster;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import liquibase.exception.LiquibaseException;
import liquibase.integration.spring.SpringLiquibase;

@RunWith(SpringJUnit4ClassRunner.class)
@ConfigurationPropertiesScan
@TestPropertySource(locations = "classpath:test.properties")
@ContextConfiguration(classes = {AdditionalPostgresClusters.class,
        DefaultPostgresCluster.class, HikariConfiguration.class})
public class TenantRoutingDataSourceTest {

    @Autowired
    AdditionalPostgresClusters additionalPostgresClusters;
    @Autowired
    DefaultPostgresCluster defaultPgCluster;
    @Autowired
    HikariConfig hikariConfig;
    TenantRoutingDataSource tenantRoutingDataSource;
    Map<Object, Object> dataSources = new HashMap<>();
    @Mock
    private TenantIdentifierResolver tenantIdentifierResolverMock;
    @Mock
    private SpringLiquibase springLiquibaseMock;
    @Mock
    private ResourceLoader resourceLoaderMock;
    @Mock
    private LiquibaseProperties liquibasePropertiesMock;

    @Before
    public void setUp() throws Exception {
        tenantRoutingDataSource = new TenantRoutingDataSource(tenantIdentifierResolverMock, springLiquibaseMock,
                resourceLoaderMock, additionalPostgresClusters, defaultPgCluster, hikariConfig, liquibasePropertiesMock);
    }

    @Test
    public void testReadAdditionalClustersParamsFromProperties_shouldReturnTrueIfProjectUuidContainsInClusterOneProjects_whenClustersParamsReadFromApplicationProperties() {
        Iterator<AdditionalPostgresCluster> clusterIterator = additionalPostgresClusters.getClusters().iterator();
        List<String> cluster1Projects = new ArrayList<>();
        while (clusterIterator.hasNext()) {
            AdditionalPostgresCluster cluster = clusterIterator.next();
            if (cluster.getUrl() != null && cluster.getUrl().contains("cluster1")) {
                cluster1Projects.addAll(cluster.getProjectsAsList());
            }
        }
        assertTrue(cluster1Projects.contains("3d6a138d-057b-4e35-8348-17aee2f2b0f8"));
    }

    @Test
    public void testCreateDefaultDataSource_shouldReturnDefaultDataSourceJdbcUrl_whenDefaultDataSourceCreated()
            throws LiquibaseException {
        DataSource defaultDataSource = tenantRoutingDataSource.createDefaultDataSource(defaultPgCluster);
        assertEquals("jdbc:postgresql://localhost:5432/default",
                ((HikariDataSource) defaultDataSource).getJdbcUrl());
    }

    @Test
    public void testCreateTargetDataSources_shouldReturnDataSourceJdbcUrlForTwoProjectIdFromDataSourcesMap_whenTargetDataSourcesCreated()
            throws LiquibaseException {
        tenantRoutingDataSource.createTargetDataSources(additionalPostgresClusters, dataSources);
        assertEquals("jdbc:postgresql://localhost:5432/cluster0", ((HikariDataSource) dataSources.get("ab70725d-318c-4d06-976a-e2c843d999e6")).getJdbcUrl());
    }

    @Test
    public void testGetTenantIdsWithOneTenantIdPerClusterParamIsTrue_shouldReturnCollectionOfOneTenantIdPerCluster_whenTargetDataSourcesCreated()
            throws LiquibaseException {
        tenantRoutingDataSource.createTargetDataSources(additionalPostgresClusters, dataSources);
        Collection<String> tenantIdPerCluster = TenantContext.getTenantIds(true);
        assertEquals("[ab70725d-318c-4d06-976a-e2c843d999e6, 3d6a138d-057b-4e35-8348-17aee2f2b0f8]",
                tenantIdPerCluster.toString());
    }

    @Test
    public void testGetTenantIdsWithOneTenantIdPerClusterParamIsFalse_shouldReturnCollectionOfAllTenantIds_whenTargetDataSourcesCreated()
            throws LiquibaseException {
        tenantRoutingDataSource.createTargetDataSources(additionalPostgresClusters, dataSources);
        Collection<String> tenantIds = TenantContext.getTenantIds(false);
        assertEquals("[cc4055cc-48e6-4375-97a8-307fbf71c2b1, ab70725d-318c-4d06-976a-e2c843d999e6, "
                + "2d5e2d09-25e7-4801-b559-86da63a0bdcf, 46709694-b495-446b-a372-65cfc86a7b5d, "
                + "3d6a138d-057b-4e35-8348-17aee2f2b0f8]", tenantIds.toString());
    }

    @Test
    public void testCreateTargetDataSources_shouldNotCreateDataSourceWithEmptyJdbcUrl_whenTargetDataSourcesCreating()
            throws LiquibaseException {
        tenantRoutingDataSource.createTargetDataSources(additionalPostgresClusters, dataSources);
        assertEquals(2, TenantContext.getTenantIds(true).size());
    }

    @Test
    public void testReadHikariConfigs_shouldReturnHikariIdleTimeOutEqualsFiftyFiveThousand_whenHikariPropertiesReadFromApplicationPropertiesFileAndDataSourceWasCreated() throws LiquibaseException {
        DataSource defaultDataSource = tenantRoutingDataSource.createDefaultDataSource(defaultPgCluster);
        assertEquals(55000,
                ((HikariDataSource) defaultDataSource).getIdleTimeout());
    }
}