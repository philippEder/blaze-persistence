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

package com.blazebit.persistence.criteria.impl.expression.function;

import java.sql.Timestamp;
import java.util.Calendar;

import com.blazebit.persistence.criteria.impl.BlazeCriteriaBuilderImpl;
import com.blazebit.persistence.criteria.impl.RenderContext;

import javax.persistence.criteria.Expression;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class CurrentTimestampFunction extends AbstractFunctionExpression<Timestamp> {

    public static final String NAME = "CURRENT_TIMESTAMP";

    private static final long serialVersionUID = 1L;

    public CurrentTimestampFunction(BlazeCriteriaBuilderImpl criteriaBuilder) {
        super(criteriaBuilder, Timestamp.class, NAME);
    }

    @Override
    public void render(RenderContext context) {
        context.getBuffer().append(getFunctionName());
    }

    @Override
    @SuppressWarnings({ "unchecked" })
    public <X> Expression<X> as(Class<X> type) {
        if (java.util.Date.class.isAssignableFrom(type) || Calendar.class.isAssignableFrom(type)) {
            return (Expression<X>) this;
        }
        return super.as(type);
    }
}