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

import java.util.Map;

import org.hibernate.cfg.AvailableSettings;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.qubership.atp.multitenancy.core.context.TenantContext;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;

public class TenantIdentifierResolver implements CurrentTenantIdentifierResolver, HibernatePropertiesCustomizer {

    /**
     * Get current Tenant Identifier.
     *
     * @return String Tenant Info.
     */
    @Override
    public String resolveCurrentTenantIdentifier() {
        return TenantContext.getTenantInfo();
    }

    /**
     * Validate Existing Current Sessions;
     * Actually, no validation is performed. Method simply returns false.
     *
     * @return boolean (currently returns false).
     */
    @Override
    public boolean validateExistingCurrentSessions() {
        return false;
    }

    /**
     * Set Hibernate properties.
     *
     * @param hibernateProperties Map of Hibernate Configuration properties.
     */
    @Override
    public void customize(final Map<String, Object> hibernateProperties) {
        hibernateProperties.put(AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER, this);
    }
}
