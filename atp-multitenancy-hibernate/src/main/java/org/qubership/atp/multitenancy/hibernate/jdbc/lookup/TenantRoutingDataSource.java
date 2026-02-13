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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.sql.DataSource;

import org.qubership.atp.multitenancy.core.context.TenantContext;
import org.qubership.atp.multitenancy.hibernate.jdbc.pojo.AdditionalPostgresCluster;
import org.qubership.atp.multitenancy.hibernate.jdbc.pojo.AdditionalPostgresClusters;
import org.qubership.atp.multitenancy.hibernate.jdbc.pojo.DefaultPostgresCluster;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.util.Assert;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import liquibase.exception.LiquibaseException;
import liquibase.integration.spring.SpringLiquibase;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TenantRoutingDataSource extends AbstractRoutingDataSource {

    /**
     * TenantIdentifierResolver object link.
     */
    private final TenantIdentifierResolver tenantIdentifierResolver;

    /**
     * DataSourceBuilder object link.
     */
    private final DataSourceBuilder<? extends DataSource> dataSourceBuilder;

    /**
     * ResourceLoader object link.
     */
    private final ResourceLoader resourceLoader;

    /**
     * SpringLiquibase object link.
     */
    private final SpringLiquibase springLiquibase;

    /**
     * HikariConfig object link.
     */
    private final HikariConfig hikariConfig;

    /**
     * LiquibaseProperties object link.
     */
    private final LiquibaseProperties liquibaseProperties;

    /**
     * Constructor.
     *
     * @param tenantIdentifierResolver TenantIdentifierResolver bean
     * @param springLiquibase SpringLiquibase bean
     * @param resourceLoader ResourceLoader bean
     * @param additionalPostgresClusters AdditionalPostgresClusters bean
     * @param defaultPostgresCluster DefaultPostgresCluster bean
     * @param hikariConfig HikariConfig bean
     * @param liquibaseProperties LiquibaseProperties bean
     * @throws LiquibaseException in case Liquibase errors occurred.
     */
    public TenantRoutingDataSource(final TenantIdentifierResolver tenantIdentifierResolver,
                                   final SpringLiquibase springLiquibase,
                                   final ResourceLoader resourceLoader,
                                   final AdditionalPostgresClusters additionalPostgresClusters,
                                   final DefaultPostgresCluster defaultPostgresCluster,
                                   final HikariConfig hikariConfig,
                                   final LiquibaseProperties liquibaseProperties) throws LiquibaseException {
        this.tenantIdentifierResolver = tenantIdentifierResolver;
        this.dataSourceBuilder = DataSourceBuilder.create();
        this.springLiquibase = springLiquibase;
        this.hikariConfig = hikariConfig;
        this.resourceLoader = resourceLoader;
        this.liquibaseProperties = liquibaseProperties;
        DataSource defaultDataSource = createDefaultDataSource(defaultPostgresCluster);
        setDefaultTargetDataSource(defaultDataSource);
        Map<Object, Object> targetDataSources = new HashMap<>();
        createTargetDataSources(additionalPostgresClusters, targetDataSources);
        setTargetDataSources(targetDataSources);
    }

    /**
     * Determine Current Tenant Identifier.
     *
     * @return String Current Tenant Identifier.
     */
    @Override
    protected String determineCurrentLookupKey() {
        return tenantIdentifierResolver.resolveCurrentTenantIdentifier();
    }

    private DataSource createDataSource(final String url,
                                        final String user,
                                        final String password,
                                        final String driverClassName) throws LiquibaseException {
        dataSourceBuilder.driverClassName(driverClassName);
        dataSourceBuilder.username(user);
        dataSourceBuilder.password(password);
        dataSourceBuilder.url(url);
        DataSource dataSource = dataSourceBuilder.build();
        if (dataSource instanceof HikariDataSource) {
            setHikariProperties((HikariDataSource) dataSource);
        }
        migrateDataSource(dataSource);
        return dataSource;
    }

    private void migrateDataSource(final DataSource dataSource) throws LiquibaseException {
        springLiquibase.setResourceLoader(resourceLoader);
        springLiquibase.setDataSource(dataSource);
        springLiquibase.setChangeLog(liquibaseProperties.getChangeLog());
        springLiquibase.setContexts(liquibaseProperties.getContexts());
        springLiquibase.setDefaultSchema(liquibaseProperties.getDefaultSchema());
        springLiquibase.setDropFirst(liquibaseProperties.isDropFirst());
        springLiquibase.setShouldRun(liquibaseProperties.isEnabled());
        springLiquibase.setLabels("");
        springLiquibase.setChangeLogParameters(liquibaseProperties.getParameters());
        springLiquibase.setRollbackFile(liquibaseProperties.getRollbackFile());
        springLiquibase.afterPropertiesSet();
    }

    /**
     * Create TargetDataSources, migrate (Liquibase) and put them into targetDataSources map.
     * DataSource will be created only if cluster.url is not empty or cluster.url doesn't equal "".
     *
     * @param clusters          {@link AdditionalPostgresClusters} objects that have {@link AdditionalPostgresCluster}.
     * @param targetDataSources map, key - projectUuid, value - data source.
     * @throws LiquibaseException when migration is failed.
     */
    public void createTargetDataSources(final AdditionalPostgresClusters clusters,
                                        final Map<Object, Object> targetDataSources) throws LiquibaseException {
        for (AdditionalPostgresCluster cluster : clusters.getClusters()) {
            if (Objects.isNull(cluster.getUrl()) || cluster.getUrl().isEmpty() || cluster.getUrl().equals("\"\"")) {
                return;
            }
            DataSource dataSource = createDataSource(cluster.getUrl(), cluster.getUsername(), cluster.getPassword(),
                    cluster.getDriverClassName());
            mapProjectsToDataSource(cluster.getProjectsAsList(), dataSource, targetDataSources, cluster);
        }
    }

    private void mapProjectsToDataSource(final List<String> projectUuids,
                                         final DataSource dataSource,
                                         final Map<Object, Object> targetDataSources,
                                         final AdditionalPostgresCluster cluster) {
        Assert.notNull(projectUuids,
                String.format("projectUuids property is null for additional cluster (url): %s", cluster.getUrl()));
        Assert.notEmpty(projectUuids,
                String.format("projectUuids property is empty for additional cluster (url): %s", cluster.getUrl()));
        for (String projectUuid : projectUuids) {
            targetDataSources.put(projectUuid, dataSource);
            TenantContext.addTenantId(cluster.getUrl(), projectUuid);
        }
    }

    /**
     * Creating default Data Source, migrate (Liquibase) it.
     *
     * @param defaultPostgresCluster
     * {@link AdditionalPostgresClusters} objects that have {@link AdditionalPostgresCluster}.
     * @return {@link DataSource} default data source.
     * @throws LiquibaseException when migration (Liquibase) is failed.
     */
    public DataSource createDefaultDataSource(final DefaultPostgresCluster defaultPostgresCluster)
            throws LiquibaseException {
        return createDataSource(defaultPostgresCluster.getUrl(), defaultPostgresCluster.getUsername(),
                defaultPostgresCluster.getPassword(),
                defaultPostgresCluster.getDriverClassName());
    }

    private void setHikariProperties(final HikariDataSource dataSource) {
        dataSource.setMinimumIdle(hikariConfig.getMinimumIdle());
        dataSource.setMaximumPoolSize(hikariConfig.getMaximumPoolSize());
        dataSource.setIdleTimeout(hikariConfig.getIdleTimeout());
        dataSource.setMaxLifetime(hikariConfig.getMaxLifetime());
    }
}

