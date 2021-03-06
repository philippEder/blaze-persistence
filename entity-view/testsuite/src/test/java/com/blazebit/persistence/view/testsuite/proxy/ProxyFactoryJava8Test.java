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

package com.blazebit.persistence.view.testsuite.proxy;

import com.blazebit.persistence.spi.PackageOpener;
import com.blazebit.persistence.view.impl.metamodel.ManagedViewTypeImplementor;
import com.blazebit.persistence.view.impl.proxy.ProxyFactory;
import com.blazebit.persistence.view.metamodel.ViewMetamodel;
import com.blazebit.persistence.view.metamodel.ViewType;
import com.blazebit.persistence.view.testsuite.AbstractEntityViewTest;
import com.blazebit.persistence.view.testsuite.proxy.model.DocumentJava8View;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class ProxyFactoryJava8Test extends AbstractEntityViewTest {

    private final ProxyFactory proxyFactory = new ProxyFactory(false, false, PackageOpener.NOOP);

    private ViewMetamodel getViewMetamodel() {
        return build(
                DocumentJava8View.class
        );
    }

    @Test
    public void testProxyCreateInitialization() throws Exception {
        ViewType<DocumentJava8View> viewType = getViewMetamodel().view(DocumentJava8View.class);
        Class<? extends DocumentJava8View> proxyClass = proxyFactory.getProxy(evm, (ManagedViewTypeImplementor<DocumentJava8View>) viewType, null);

        DocumentJava8View instance = proxyClass.getConstructor().newInstance();

        assertEquals("INIT", instance.getName());
    }
}
