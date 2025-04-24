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

package org.qubership.atp.multitenancy.interceptor.http;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.qubership.atp.auth.springbootstarter.entities.Operation;
import org.qubership.atp.auth.springbootstarter.security.permissions.PolicyEnforcement;
import org.qubership.atp.multitenancy.core.context.TenantContext;
import org.qubership.atp.multitenancy.core.header.CustomHeader;
import org.springframework.lang.Nullable;
import org.springframework.security.web.util.TextEscapeUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

public class MultiTenantHttpRequestInterceptor implements HandlerInterceptor {

    private final PolicyEnforcement policyEnforcement;

    public MultiTenantHttpRequestInterceptor(PolicyEnforcement policyEnforcement) {
        this.policyEnforcement = policyEnforcement;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        String tenantId = request.getHeader(CustomHeader.X_PROJECT_ID);
        if (tenantId == null) {
            TenantContext.setDefaultTenantInfo();
            return true;
        }
        boolean permitted = policyEnforcement.checkAccess(tenantId, Operation.READ.toString());
        if (!permitted) {
            response.getWriter().write(String.format("Access denied to %s", TextEscapeUtils.escapeEntities(tenantId)));
            response.setStatus(403);
            return false;
        }
        TenantContext.setTenantInfo(tenantId);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
            @Nullable ModelAndView modelAndView) {
        TenantContext.clear();
    }
}
