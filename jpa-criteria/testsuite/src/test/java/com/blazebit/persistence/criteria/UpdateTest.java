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

package com.blazebit.persistence.criteria;


import com.blazebit.persistence.criteria.impl.BlazeCriteria;
import com.blazebit.persistence.testsuite.AbstractCoreTest;
import com.blazebit.persistence.testsuite.base.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Document_;
import com.blazebit.persistence.testsuite.entity.Person;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Calendar;

import static org.junit.Assert.assertEquals;

public class UpdateTest extends AbstractCoreTest {

    @Test
    public void simpleSet() {
        BlazeCriteriaBuilder cb = BlazeCriteria.get(em, cbf);
        BlazeCriteriaUpdate<Document> query = cb.createCriteriaUpdate(Document.class, "d");
        BlazeRoot<Document> root = query.getRoot();

        query.set(Document_.name, "asd");
        query.set(Document_.age, cb.literal(1L));
        query.set("idx", 1);
        query.set(root.get(Document_.lastModified), cb.currentDate());
        query.set(root.get(Document_.creationDate), cb.currentTimestamp().as(Calendar.class));
        query.where(cb.equal(root.get(Document_.name), "abc"));

        assertEquals("UPDATE Document d SET name = :param_0,age = :param_1,idx = :param_2,lastModified = CURRENT_DATE,creationDate = CURRENT_TIMESTAMP WHERE d.name = :generated_param_0", query.getQueryString());
        query.getQuery();
    }

    @Test
    // TODO: https://github.com/datanucleus/datanucleus-core/issues/119
    @Category(NoDatanucleus.class)
    public void setSubquery() {
        BlazeCriteriaBuilder cb = BlazeCriteria.get(em, cbf);
        BlazeCriteriaUpdate<Document> query = cb.createCriteriaUpdate(Document.class, "d");
        BlazeRoot<Document> root = query.getRoot();
        BlazeSubquery<String> subquery = query.subquery(String.class);
        BlazeRoot<Document> subqueryRoot = subquery.from(Document.class, "subDoc");

        subquery.select(cb.greatest(subqueryRoot.get(Document_.name)));
        subquery.where(cb.equal(subqueryRoot, root));

        query.set(Document_.name, subquery);

        assertEquals("UPDATE Document d SET name = (SELECT MAX(subDoc.name) FROM Document subDoc WHERE subDoc = d)", query.getQueryString());
        query.getQuery();
    }

    @Test
    // TODO: https://github.com/datanucleus/datanucleus-core/issues/119
    @Category(NoDatanucleus.class)
    public void setCorrelatedSubqueryExpression() {
        BlazeCriteriaBuilder cb = BlazeCriteria.get(em, cbf);
        BlazeCriteriaUpdate<Document> query = cb.createCriteriaUpdate(Document.class, "d");
        BlazeRoot<Document> root = query.getRoot();
        BlazeSubquery<Integer> subquery = query.subquery(Integer.class);
        BlazeRoot<Document> subqueryRoot = subquery.correlate(root);
        BlazeJoin<Document, Person> owner = subqueryRoot.join(Document_.owner, "owner");

        subquery.select(cb.count(owner).as(Integer.class));

        query.set(Document_.idx, cb.sum(subquery, 1));

        assertEquals("UPDATE Document d SET idx = (SELECT " + function("CAST_INTEGER", "COUNT(owner)") + " FROM d.owner owner) + 1", query.getQueryString());
        query.getQuery();
    }
}