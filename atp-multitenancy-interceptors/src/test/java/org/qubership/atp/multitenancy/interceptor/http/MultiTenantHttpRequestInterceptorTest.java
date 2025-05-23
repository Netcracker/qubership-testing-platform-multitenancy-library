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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.qubership.atp.auth.springbootstarter.security.permissions.PolicyEnforcement;
import org.qubership.atp.multitenancy.core.context.TenantContext;
import org.qubership.atp.multitenancy.core.header.CustomHeader;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
public class MultiTenantHttpRequestInterceptorTest {

    private MultiTenantHttpRequestInterceptor multiTenantHttpRequestInterceptor;
    private String TENANT_ID = "3d6a138d-057b-4e35-8348-17aee2f2b0f8";
    @Mock
    private HttpServletRequest requestMock;
    @Mock
    private HttpServletResponse responseMock;
    @Mock
    private Object handlerMock;
    @Mock
    private PolicyEnforcement policyEnforcementMock;
    @Mock
    private PrintWriter printWriterMock;

    /**
     * Init multiTenantHttpRequestInterceptor and response mock.
     *
     * @throws Exception in case errors.
     */
    @Before
    public void setUp() throws Exception {
        multiTenantHttpRequestInterceptor = new MultiTenantHttpRequestInterceptor(policyEnforcementMock);
        when(responseMock.getWriter()).thenReturn(printWriterMock);
    }

    /**
     * Test that Tenant Info equals to Tenant ID got from X-Project-Id header.
     *
     * @throws IOException in case IO errors occurred.
     */
    @Test
    public void testPreHandleShouldReturnEqualTenantIdFromTenantContextAndXProjectIdHeaderWhenXProjectIdHeaderHasAndUserHasAccessToProject() throws IOException {
        when(requestMock.getHeader(CustomHeader.X_PROJECT_ID)).thenReturn(TENANT_ID);
        when(policyEnforcementMock.checkAccess((String) any(), any())).thenReturn(true);
        multiTenantHttpRequestInterceptor.preHandle(requestMock, responseMock, handlerMock);
        assertEquals(TENANT_ID, TenantContext.getTenantInfo());
    }

    /**
     * Test that pre-Handle returns false in case there is no access to project identified by X-Project-Id header.
     *
     * @throws IOException in case IO errors occurred.
     */
    @Test
    public void testPreHandleShouldReturnFalseWhenXProjectIdHeaderHasButNoAccessToProject() throws IOException {
        when(requestMock.getHeader(CustomHeader.X_PROJECT_ID)).thenReturn(TENANT_ID);
        when(policyEnforcementMock.checkAccess((String) any(), any())).thenReturn(false);
        assertFalse(multiTenantHttpRequestInterceptor.preHandle(requestMock, responseMock, handlerMock));
    }

    /**
     * Test that pre-Handle returns true in case there is no X-Project-Id header.
     *
     * @throws IOException in case IO errors occurred.
     */
    @Test
    public void testPreHandleShouldReturnTrueWhenNoXProjectIdHeader() throws IOException {
        when(requestMock.getHeader(CustomHeader.X_PROJECT_ID)).thenReturn(null);
        assertTrue(multiTenantHttpRequestInterceptor.preHandle(requestMock, responseMock, handlerMock));
    }

}
