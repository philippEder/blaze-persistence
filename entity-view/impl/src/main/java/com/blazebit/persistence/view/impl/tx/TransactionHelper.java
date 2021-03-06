/*
 * Copyright 2014 - 2020 Blazebit.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.blazebit.persistence.view.impl.tx;

import com.blazebit.persistence.view.spi.TransactionAccess;
import com.blazebit.persistence.view.spi.TransactionAccessFactory;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class TransactionHelper {

    private static final Logger LOG = Logger.getLogger(TransactionHelper.class.getName());
    private static volatile TransactionAccessFactory factory;

    private TransactionHelper() {
    }

    public static TransactionAccess getTransactionAccess(EntityManager em) {
        SynchronizationRegistry registry = SynchronizationRegistry.getRegistry();
        if (registry == null) {
            TransactionAccess transactionAccess = getTransactionAccessInternal(em);
            if (!transactionAccess.isActive()) {
                return transactionAccess;
            }
            registry = new SynchronizationRegistry(transactionAccess);
        }
        return registry;
    }

    private static TransactionAccess getTransactionAccessInternal(EntityManager em) {
        TransactionAccessFactory factory = TransactionHelper.factory;
        if (factory != null) {
            return factory.createTransactionAccess(em);
        }

        InitialContext context = null;

        try {
            context = new InitialContext();
        } catch (NamingException e) {
            // Maybe in Java SE environment
        }

        if (context != null) {
            JtaResources jtaResources = JtaResources.getInstance();
            if (jtaResources != null) {
                TransactionHelper.factory = jtaResources;
                return jtaResources.createTransactionAccess(em);
            }
        }

        for (TransactionAccessFactory transactionAccessFactory : ServiceLoader.load(TransactionAccessFactory.class)) {
            try {
                TransactionAccess transactionAccess = transactionAccessFactory.createTransactionAccess(em);
                if (transactionAccess != null) {
                    TransactionHelper.factory = transactionAccessFactory;
                    return transactionAccess;
                }
            } catch (Exception ex) {
                LOG.log(Level.WARNING, "Error during creation of transaction access!", ex);
            }
        }

        try {
            String version = Class.forName("org.hibernate.Session")
                    .getPackage().getImplementationVersion();
            String[] versionParts = version.split("\\.");
            int major = Integer.parseInt(versionParts[0]);

            if (major >= 5) {
                Object jtaPlatform = Hibernate5JtaPlatformTransactionAccessFactory.getHibernate5JtaPlatformPresent(em);
                if (jtaPlatform == null || jtaPlatform.getClass() == Class.forName("org.hibernate.engine.transaction.jta.platform.internal.NoJtaPlatform")) {
                    TransactionHelper.factory = new Hibernate5EntityTransactionAccessFactory();
                    return new Hibernate5EntityTransactionSynchronizationStrategy(em);
                } else {
                    TransactionHelper.factory = new Hibernate5JtaPlatformTransactionAccessFactory();
                    return new Hibernate5JtaPlatformTransactionSynchronizationStrategy(jtaPlatform);
                }
            } else {
                TransactionHelper.factory = new Hibernate4TransactionAccessFactory();
                return new Hibernate4TransactionSynchronizationStrategy(em);
            }
        } catch (ClassNotFoundException ex) {
            throw new IllegalArgumentException("Unsupported jpa provider!", ex);
        }
    }
}
