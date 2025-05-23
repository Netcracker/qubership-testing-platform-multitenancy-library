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

package org.qubership.atp.multitenancy.hibernate.config;

import javax.sql.DataSource;

import org.qubership.atp.multitenancy.hibernate.jdbc.connections.TenantConnectionProvider;
import org.qubership.atp.multitenancy.hibernate.jdbc.lookup.TenantIdentifierResolver;
import org.qubership.atp.multitenancy.hibernate.jdbc.lookup.TenantRoutingDataSource;
import org.qubership.atp.multitenancy.hibernate.jdbc.pojo.AdditionalPostgresClusters;
import org.qubership.atp.multitenancy.hibernate.jdbc.pojo.DefaultPostgresCluster;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.io.ResourceLoader;

import com.zaxxer.hikari.HikariConfig;
import liquibase.exception.LiquibaseException;
import liquibase.integration.spring.SpringLiquibase;

@Configuration
@ConditionalOnProperty(value = {"atp.multi-tenancy.enabled"})
public class MultiTenantDataSourceConfiguration {

    /**
     * Create defaultPostgresCluster bean.
     *
     * @return new DefaultPostgresCluster object.
     */
    @Bean
    public DefaultPostgresCluster defaultPostgresCluster() {
        return new DefaultPostgresCluster();
    }

    /**
     * Create additionalPostgresClusters bean.
     *
     * @return new AdditionalPostgresClusters object.
     */
    @Bean
    public AdditionalPostgresClusters additionalPostgresClusters() {
        return new AdditionalPostgresClusters();
    }

    /**
     * Create tenantIdentifierResolver bean.
     *
     * @return new TenantIdentifierResolver object.
     */
    @Bean
    public TenantIdentifierResolver tenantIdentifierResolver() {
        return new TenantIdentifierResolver();
    }

    /**
     * Create liquibaseProperties bean.
     *
     * @return new LiquibaseProperties object.
     */
    @Bean
    @ConditionalOnMissingBean(name = "liquibaseProperties")
    public LiquibaseProperties liquibaseProperties() {
        return new LiquibaseProperties();
    }

    /**
     * Create springLiquibase bean.
     *
     * @return new SpringLiquibase object.
     */
    @Bean
    @ConditionalOnMissingBean(name = "springLiquibase")
    public SpringLiquibase springLiquibase() {
        return new SpringLiquibase();
    }

    /**
     * Create tenantRoutingDataSource bean.
     *
     * @param tenantIdentifierResolver TenantIdentifierResolver bean
     * @param defaultPostgresCluster DefaultPostgresCluster bean
     * @param resourceLoader ResourceLoader bean
     * @param additionalPostgresClusters AdditionalPostgresClusters bean
     * @param hikariConfig HikariConfig bean
     * @param springLiquibase SpringLiquibase bean
     * @param liquibaseProperties LiquibaseProperties bean
     * @return new TenantRoutingDataSource object created and configured.
     * @throws LiquibaseException in case data source initialization errors occurred.
     */
    @Bean
    @DependsOn(value = "springLiquibase")
    public TenantRoutingDataSource tenantRoutingDataSource(
            final TenantIdentifierResolver tenantIdentifierResolver,
            final DefaultPostgresCluster defaultPostgresCluster,
            final ResourceLoader resourceLoader,
            final AdditionalPostgresClusters additionalPostgresClusters,
            @Qualifier(value = "hikariConfig") final HikariConfig hikariConfig,
            final SpringLiquibase springLiquibase,
            final LiquibaseProperties liquibaseProperties) throws LiquibaseException {
        return new TenantRoutingDataSource(tenantIdentifierResolver, springLiquibase, resourceLoader,
                additionalPostgresClusters, defaultPostgresCluster, hikariConfig, liquibaseProperties);
    }

    /**
     * Create tenantConnectionProvider bean for dataSource provided.
     *
     * @param dataSource DataSource object
     * @return new TenantConnectionProvider object configured with dataSource.
     */
    @Bean
    public TenantConnectionProvider tenantConnectionProvider(final DataSource dataSource) {
        return new TenantConnectionProvider(dataSource);
    }
}
