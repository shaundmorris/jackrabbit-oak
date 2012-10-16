/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jackrabbit.oak.jcr;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javax.jcr.GuestCredentials;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

import org.apache.jackrabbit.mk.api.MicroKernel;
import org.apache.jackrabbit.mk.core.MicroKernelImpl;
import org.apache.jackrabbit.oak.security.SecurityProviderImpl;
import org.apache.jackrabbit.oak.spi.security.SecurityProvider;
import org.junit.After;

/**
 * Abstract base class for repository tests providing methods for accessing
 * the repository, a session and nodes and properties from that session.
 *
 * Users of this class must call clear to close the session associated with
 * this instance and clean up the repository when done.
 */
public abstract class AbstractRepositoryTest {

    protected ScheduledExecutorService executor = null;

    private Repository repository = null;
    private Session adminSession = null;

    @After
    public void logout() throws RepositoryException {
        // release session field
        if (adminSession != null) {
            adminSession.logout();
            adminSession = null;
        }
        // release repository field
        repository = null;

        if (executor != null) {
            executor.shutdown();
            executor = null;
        }
    }

    protected Repository getRepository() throws RepositoryException {
        if (repository == null) {
            MicroKernel mk = new MicroKernelImpl();
            ScheduledExecutorService executorService = getExecutor();
            SecurityProvider securityProvider = new SecurityProviderImpl();
            repository  = new RepositoryImpl(mk, executorService, securityProvider);
        }
        return repository;
    }

    private ScheduledExecutorService getExecutor() {
        return (executor == null) ? Executors.newScheduledThreadPool(0) : executor;
    }

    protected Session getAdminSession() throws RepositoryException {
        if (adminSession == null) {
            adminSession = createAdminSession();
        }
        return adminSession;
    }

    protected Session createAnonymousSession() throws RepositoryException {
        return getRepository().login(new GuestCredentials());
    }

    protected Session createAdminSession() throws RepositoryException {
        return getRepository().login(new SimpleCredentials("admin", "admin".toCharArray()));
    }

}
