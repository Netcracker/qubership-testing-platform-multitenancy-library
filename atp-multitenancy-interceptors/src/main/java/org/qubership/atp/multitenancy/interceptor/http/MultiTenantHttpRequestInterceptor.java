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

    /**
     * Policy Enforcement object.
     */
    private final PolicyEnforcement policyEnforcement;

    /**
     * Constructor.
     *
     * @param policyEnforcement Policy Enforcement object.
     */
    public MultiTenantHttpRequestInterceptor(final PolicyEnforcement policyEnforcement) {
        this.policyEnforcement = policyEnforcement;
    }

    /**
     * Process request and response.
     *
     * @param request HttpServletRequest received
     * @param response HttpServletResponse to be sent
     * @param handler Handler object
     * @return false in case access is denied (and make "Access denied" response with 403 code); otherwise true.
     * @throws IOException in case IO errors occurred.
     */
    @Override
    public boolean preHandle(final HttpServletRequest request,
                             final HttpServletResponse response,
                             final Object handler)
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

    /**
     * Process request and response; actually, it just clears TenantContext.
     *
     * @param request HttpServletRequest received
     * @param response HttpServletResponse to be sent
     * @param handler Handler object
     * @param modelAndView ModelAndView object.
     */
    @Override
    public void postHandle(final HttpServletRequest request,
                           final HttpServletResponse response,
                           final Object handler,
                           @Nullable final ModelAndView modelAndView) {
        TenantContext.clear();
    }
}
