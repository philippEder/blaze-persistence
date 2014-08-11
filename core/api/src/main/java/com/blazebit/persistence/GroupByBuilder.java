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

package com.blazebit.persistence;

/**
 * An interface for builders that support groupy by.
 * This is related to the fact, that a query builder supports group by clauses.
 *
 * @param <T> The result type
 * @param <X> The concrete builder type
 * @author Christian Beikov
 * @since 1.0
 */
public interface GroupByBuilder<T, X extends GroupByBuilder<T, X>> {
    
    /**
     * Adds a multiple group by clause with the given expressions to the query.
     *
     * @param expressions The expressions for the group by clauses
     * @return The query builder for chaining calls
     */
    public X groupBy(String... expressions);

    /**
     * Adds a group by clause with the given expression to the query.
     *
     * @param expression The expression for the group by clause
     * @return The query builder for chaining calls
     */
    public X groupBy(String expression);
}