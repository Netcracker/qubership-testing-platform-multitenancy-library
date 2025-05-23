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

package org.qubership.atp.multitenancy.core.context;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class TenantContext {

    /**
     * Default Tenant Name.
     */
    private static final String DEFAULT_TENANT = "default";

    /**
     * Tenant Info ThreadLocal String (inheritable).
     */
    private static final ThreadLocal<String> TENANT_INFO = new InheritableThreadLocal<>();

    /**
     * Set of Tenant ID Strings.
     */
    private static final Set<String> TENANT_IDS = new HashSet<>();

    /**
     * Map of ClusterId - TenantId (where TenantId resides).
     */
    private static final Map<String, String> TENANT_IDS_PER_CLUSTER = new HashMap<>();

    /**
     * Constructor.
     */
    private TenantContext() {
    }

    /**
     * Get Tenant Info.
     *
     * @return The current Tenant Info String.
     */
    public static String getTenantInfo() {
        if (null != TENANT_INFO.get()) {
            return TENANT_INFO.get();
        }
        return DEFAULT_TENANT;
    }

    /**
     * Set Tenant Info.
     *
     * @param tenant String tenant id.
     */
    public static void setTenantInfo(final String tenant) {
        TENANT_INFO.set(tenant);
    }

    /**
     * Clear Tenant Info.
     */
    public static void clear() {
        TENANT_INFO.remove();
    }

    /**
     * Set Default Tenant Info.
     */
    public static void setDefaultTenantInfo() {
        TENANT_INFO.set(DEFAULT_TENANT);
    }

    /**
     * Add tenantId under clusterId.
     *
     * @param clusterId String clusterId
     * @param tenantId String tenantId
     */
    public static void addTenantId(final String clusterId, final String tenantId) {
        TENANT_IDS.add(tenantId);
        TENANT_IDS_PER_CLUSTER.putIfAbsent(clusterId, tenantId);
    }

    /**
     * Return List of String Tenant IDs.
     *
     * @param oneTenantIdPerCluster true to return one of tenant identifiers for a cluster,
     *                             or false to return all tenant identifiers
     * @return list of tenant IDs.
     */
    public static Collection<String> getTenantIds(final boolean oneTenantIdPerCluster) {
        if (oneTenantIdPerCluster) {
            return TENANT_IDS_PER_CLUSTER.values();
        }
        return TENANT_IDS;
    }
}
