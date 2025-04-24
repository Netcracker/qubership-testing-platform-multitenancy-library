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

    @Bean
    public DefaultPostgresCluster defaultPostgresCluster() {
        return new DefaultPostgresCluster();
    }

    @Bean
    public AdditionalPostgresClusters additionalPostgresClusters() {
        return new AdditionalPostgresClusters();
    }

    @Bean
    public TenantIdentifierResolver tenantIdentifierResolver() {
        return new TenantIdentifierResolver();
    }

    @Bean
    @ConditionalOnMissingBean(name = "liquibaseProperties")
    public LiquibaseProperties liquibaseProperties() {
        return new LiquibaseProperties();
    }

    @Bean
    @ConditionalOnMissingBean(name = "springLiquibase")
    public SpringLiquibase springLiquibase() {
        return new SpringLiquibase();
    }

    @Bean
    @DependsOn(value = "springLiquibase")
    public TenantRoutingDataSource tenantRoutingDataSource(TenantIdentifierResolver tenantIdentifierResolver,
                                                           DefaultPostgresCluster defaultPostgresCluster,
                                                           ResourceLoader resourceLoader,
                                                           AdditionalPostgresClusters additionalPostgresClusters,
                                                           @Qualifier(value = "hikariConfig") HikariConfig hikariConfig,
                                                           SpringLiquibase springLiquibase,
                                                           LiquibaseProperties liquibaseProperties)
            throws LiquibaseException {
        return new TenantRoutingDataSource(tenantIdentifierResolver, springLiquibase, resourceLoader,
                additionalPostgresClusters, defaultPostgresCluster, hikariConfig, liquibaseProperties);
    }

    @Bean
    public TenantConnectionProvider tenantConnectionProvider(DataSource dataSource) {
        return new TenantConnectionProvider(dataSource);
    }
}
