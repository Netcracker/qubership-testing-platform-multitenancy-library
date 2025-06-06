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

package org.qubership.atp.multitenancy.interceptor.config;

import org.qubership.atp.auth.springbootstarter.security.permissions.PolicyEnforcement;
import org.qubership.atp.multitenancy.interceptor.http.MultiTenantHttpRequestInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@ConditionalOnProperty(value = {"atp.multi-tenancy.enabled"})
public class MultiTenantHttpRequestInterceptorConfiguration implements WebMvcConfigurer {

    /**
     * Policy Enforcement object.
     */
    private final PolicyEnforcement entityAccess;

    /**
     * Constructor.
     *
     * @param entityAccess Policy Enforcement object.
     */
    @SuppressWarnings({"SpringJavaInjectionPointsAutowiringInspection"})
    public MultiTenantHttpRequestInterceptorConfiguration(@Lazy final PolicyEnforcement entityAccess) {
        this.entityAccess = entityAccess;
    }

    /**
     * Add new MultiTenantHttpRequestInterceptor to the registry given.
     *
     * @param registry InterceptorRegistry to add Interceptor into.
     */
    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        registry.addInterceptor(new MultiTenantHttpRequestInterceptor(entityAccess));
    }
}
