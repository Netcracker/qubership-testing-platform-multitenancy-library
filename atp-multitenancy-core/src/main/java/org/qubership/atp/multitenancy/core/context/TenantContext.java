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

    private static final String DEFAULT_TENANT = "default";
    private static final ThreadLocal<String> TENANT_INFO = new InheritableThreadLocal<>();
    private static final Set<String> tenantIds = new HashSet<>();
    private static final Map<String, String> tenantIdsPerCluster = new HashMap<>();

    private TenantContext() {
    }

    /**
     * return tenant info.
     */
    public static String getTenantInfo() {
        if (null != TENANT_INFO.get()) {
            return TENANT_INFO.get();
        }
        return DEFAULT_TENANT;
    }

    public static void setTenantInfo(String tenant) {
        TENANT_INFO.set(tenant);
    }

    public static void clear() {
        TENANT_INFO.remove();
    }

    public static void setDefaultTenantInfo() {
        TENANT_INFO.set(DEFAULT_TENANT);
    }

    /**
     * Add tenant id by clustedId.
     * @param clusterId clusterId
     * @param tenantId tenantId
     */
    public static void addTenantId(String clusterId, String tenantId) {
        tenantIds.add(tenantId);
        tenantIdsPerCluster.putIfAbsent(clusterId, tenantId);
    }

    /**
     * Return tentant ids.
     * @param oneTenantIdPerCluster specifies to return of a tenant identifier for a cluster or all tenant identifiers
     * @return list of tenant ids
     */
    public static Collection<String> getTenantIds(boolean oneTenantIdPerCluster) {
        if (oneTenantIdPerCluster) {
            return tenantIdsPerCluster.values();
        }
        return tenantIds;
    }
}
