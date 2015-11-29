/*
 * Copyright 2014 Blazebit.
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
package com.blazebit.persistence.view.basic;

import static org.junit.Assert.assertEquals;

import java.util.List;

import javax.persistence.EntityTransaction;

import org.junit.Before;
import org.junit.Test;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.view.AbstractEntityViewTest;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.EntityViews;
import com.blazebit.persistence.view.basic.model.NamedView;
import com.blazebit.persistence.view.basic.model.TestEntityView;
import com.blazebit.persistence.view.entity.TestEntity;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public class PolymorphicTest extends AbstractEntityViewTest {

    @Override
	protected Class<?>[] getEntityClasses() {
		return new Class<?>[] {
				TestEntity.class
		};
	}

	@Before
    public void setUp() {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            
            TestEntity test = new TestEntity("test1", "test");
            em.persist(test);

            em.flush();
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testApplyParent() {
        EntityViewConfiguration cfg = EntityViews.createDefaultConfiguration();
        cfg.addEntityView(NamedView.class);
        cfg.addEntityView(TestEntityView.class);
        EntityViewManager evm = cfg.createEntityViewManager();

        // Base setting
        EntityViewSetting<NamedView, CriteriaBuilder<NamedView>> setting = EntityViewSetting.create(NamedView.class);

        // Query
        CriteriaBuilder<TestEntity> cb = cbf.create(em, TestEntity.class);
        List<NamedView> result = evm.applySetting(setting, cb).getResultList();

        assertEquals(1, result.size());
        assertEquals("test1", result.get(0).getName());
    }
}