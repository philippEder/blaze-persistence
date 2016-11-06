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

import java.util.List;

import javax.persistence.criteria.Expression;

import com.blazebit.persistence.criteria.impl.BlazeCriteriaBuilderImpl;
import com.blazebit.persistence.criteria.impl.RenderContext;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class FunctionFunction<X> extends FunctionExpressionImpl<X> {

    private static final long serialVersionUID = 1L;
    
    private final String name;
    
    public FunctionFunction(BlazeCriteriaBuilderImpl criteriaBuilder, Class<X> javaType, String functionName, Expression<?>... argumentExpressions) {
        super(criteriaBuilder, javaType, "FUNCTION", argumentExpressions);
        this.name = functionName;
    }

    @Override
    public void render(RenderContext context) {
        final StringBuilder buffer = context.getBuffer();
        List<Expression<?>> args = getArgumentExpressions();
        buffer.append("FUNCTION('").append(name).append('\'');
        for (int i = 0; i < args.size(); i++) {
            buffer.append(',');
            context.apply(args.get(i));
        }
        buffer.append(')');
    }
}