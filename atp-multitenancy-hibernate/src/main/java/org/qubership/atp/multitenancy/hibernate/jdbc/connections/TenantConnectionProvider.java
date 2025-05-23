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

package org.qubership.atp.multitenancy.hibernate.jdbc.connections;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import javax.sql.DataSource;

import org.hibernate.MultiTenancyStrategy;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;

public class TenantConnectionProvider implements MultiTenantConnectionProvider, HibernatePropertiesCustomizer {

    /**
     * DataSource field.
     */
    private final DataSource dataSource;

    /**
     * Constructor.
     *
     * @param dataSource DataSource object.
     */
    public TenantConnectionProvider(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Get connection to the dataSource.
     *
     * @return a connection to the dataSource
     * @throws SQLException if a database error occurs.
     */
    @Override
    public Connection getAnyConnection() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * Close given connection.
     *
     * @param connection Connection object to be closed
     * @throws SQLException if a database error occurs.
     */
    @Override
    public void releaseAnyConnection(final Connection connection) throws SQLException {
        connection.close();
    }

    /**
     * Get connection to the schema + dataSource;
     * Actually, schema is ignored, So, connection is to the default schema of the dataSource.
     *
     * @param schema String schema name
     * @return a connection to the dataSource
     * @throws SQLException if a database error occurs.
     */
    @Override
    public Connection getConnection(final String schema) throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * Release the connection.
     *
     * @param s Tenant identifier
     * @param connection Connection to be closed
     * @throws SQLException if a database error occurs.
     */
    @Override
    public void releaseConnection(final String s, final Connection connection) throws SQLException {
        connection.close();
    }

    /**
     * Get flag if the provider supports aggressive release of connections (true) or not.
     *
     * @return false (= doesn't support aggressive release).
     */
    @Override
    public boolean supportsAggressiveRelease() {
        return false;
    }

    /**
     * Is it unwrappable as unwrapType given or not.
     *
     * @param unwrapType Class type of unwrap
     * @return boolean if it's unwrappable as unwrapType given or not; always false.
     */
    @Override
    public boolean isUnwrappableAs(final Class unwrapType) {
        return false;
    }

    /**
     * Unwrap to unwrapType.
     *
     * @param unwrapType Class type of unwrap
     * @param <T> Class
     * @return unwrapped object; always returns null.
     */
    @Override
    public <T> T unwrap(final Class<T> unwrapType) {
        return null;
    }

    /**
     * Set provider Hibernate properties.
     *
     * @param hibernateProperties Map of Hibernate Configuration properties.
     */
    @Override
    public void customize(final Map<String, Object> hibernateProperties) {
        hibernateProperties.put(AvailableSettings.MULTI_TENANT, MultiTenancyStrategy.DATABASE);
        hibernateProperties.put(AvailableSettings.MULTI_TENANT_CONNECTION_PROVIDER, this);
    }
}
