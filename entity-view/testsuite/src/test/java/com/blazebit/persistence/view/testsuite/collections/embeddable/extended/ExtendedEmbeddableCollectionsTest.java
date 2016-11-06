/*
 * Copyright 2014 - 2016 Blazebit.
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

package com.blazebit.persistence.view.testsuite.collections.embeddable.extended;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.persistence.EntityTransaction;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.base.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.category.NoHibernate;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.EntityViews;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.AbstractEntityViewTest;
import com.blazebit.persistence.view.testsuite.collections.embeddable.extended.model.ExtendedEmbeddableDocumentListMapSetView;
import com.blazebit.persistence.view.testsuite.collections.embeddable.extended.model.ExtendedEmbeddableDocumentListSetMapView;
import com.blazebit.persistence.view.testsuite.collections.embeddable.extended.model.ExtendedEmbeddableDocumentMapListSetView;
import com.blazebit.persistence.view.testsuite.collections.embeddable.extended.model.ExtendedEmbeddableDocumentMapSetListView;
import com.blazebit.persistence.view.testsuite.collections.embeddable.extended.model.ExtendedEmbeddableDocumentSetListMapView;
import com.blazebit.persistence.view.testsuite.collections.embeddable.extended.model.ExtendedEmbeddableDocumentSetMapListView;
import com.blazebit.persistence.view.testsuite.collections.embeddable.simple.model.EmbeddableDocumentCollectionsView;
import com.blazebit.persistence.view.testsuite.collections.entity.extended.ExtendedDocumentForElementCollections;
import com.blazebit.persistence.view.testsuite.collections.entity.extended.ExtendedPersonForElementCollections;

/**
 *
 * @author Christian Beikov
 * @since 1.0.6
 */
@RunWith(Parameterized.class)
public class ExtendedEmbeddableCollectionsTest<T extends EmbeddableDocumentCollectionsView> extends AbstractEntityViewTest {

    private final Class<T> viewType;

    private ExtendedDocumentForElementCollections doc1;
    private ExtendedDocumentForElementCollections doc2;
    private ExtendedDocumentForElementCollections doc0;

    public ExtendedEmbeddableCollectionsTest(Class<T> viewType) {
        this.viewType = viewType;
    }

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[] {
            ExtendedDocumentForElementCollections.class,
            ExtendedPersonForElementCollections.class
        };
    }

    @Before
    public void setUp() {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            doc0 = new ExtendedDocumentForElementCollections("doc0");
            doc1 = new ExtendedDocumentForElementCollections("doc1");
            doc2 = new ExtendedDocumentForElementCollections("doc2");

            ExtendedPersonForElementCollections o1 = new ExtendedPersonForElementCollections("pers1");
            ExtendedPersonForElementCollections o2 = new ExtendedPersonForElementCollections("pers2");
            ExtendedPersonForElementCollections o3 = new ExtendedPersonForElementCollections("pers3");
            ExtendedPersonForElementCollections o4 = new ExtendedPersonForElementCollections("pers4");
            o1.setPartnerDocument(doc0);
            o2.setPartnerDocument(doc0);
            o3.setPartnerDocument(doc0);
            o4.setPartnerDocument(doc0);

            doc1.setOwner(o1);
            doc2.setOwner(o2);

            doc1.getContacts().put(1, o1);
            doc2.getContacts().put(1, o2);
            doc1.getContacts().put(2, o3);
            doc2.getContacts().put(2, o4);

            doc1.getPartners().add(o1);
            doc1.getPartners().add(o3);
            doc2.getPartners().add(o2);
            doc2.getPartners().add(o4);

            doc1.getPersonList().add(o1);
            doc1.getPersonList().add(o2);
            doc2.getPersonList().add(o3);
            doc2.getPersonList().add(o4);

            em.persist(doc0);
            em.persist(doc1);
            em.persist(doc2);

            em.flush();
            tx.commit();
            em.clear();

            doc0 = em.find(ExtendedDocumentForElementCollections.class, doc0.getId());
            doc1 = em.find(ExtendedDocumentForElementCollections.class, doc1.getId());
            doc2 = em.find(ExtendedDocumentForElementCollections.class, doc2.getId());
        } catch (Exception e) {
            tx.rollback();
            throw new RuntimeException(e);
        }
    }

    @Parameterized.Parameters
    public static Collection<?> entityViewCombinations() {
        return Arrays.asList(new Object[][]{
            { ExtendedEmbeddableDocumentListMapSetView.class },
            { ExtendedEmbeddableDocumentListSetMapView.class },
            { ExtendedEmbeddableDocumentMapListSetView.class },
            { ExtendedEmbeddableDocumentMapSetListView.class },
            { ExtendedEmbeddableDocumentSetListMapView.class },
            { ExtendedEmbeddableDocumentSetMapListView.class }
        });
    }

    @Test
    @Category({NoHibernate.class, NoDatanucleus.class})
    public void testCollections() {
        EntityViewConfiguration cfg = EntityViews.createDefaultConfiguration();
        cfg.addEntityView(viewType);
        EntityViewManager evm = cfg.createEntityViewManager(cbf, em.getEntityManagerFactory());

        CriteriaBuilder<ExtendedDocumentForElementCollections> criteria = cbf.create(em, ExtendedDocumentForElementCollections.class, "d")
            .orderByAsc("id").where("id").gt(doc0.getId());
        CriteriaBuilder<T> cb = evm.applySetting(EntityViewSetting.create(viewType), criteria);
        List<T> results = cb.getResultList();

        assertEquals(2, results.size());
        // Doc1
        assertEquals(doc1.getName(), results.get(0).getName());
        assertEquals(doc1.getContacts(), results.get(0).getContacts());
        assertEquals(doc1.getPartners(), results.get(0).getPartners());
        assertEquals(doc1.getPersonList(), results.get(0).getPersonList());

        // Doc2
        assertEquals(doc2.getName(), results.get(1).getName());
        assertEquals(doc2.getContacts(), results.get(1).getContacts());
        assertEquals(doc2.getPartners(), results.get(1).getPartners());
        assertEquals(doc2.getPersonList(), results.get(1).getPersonList());
    }
}