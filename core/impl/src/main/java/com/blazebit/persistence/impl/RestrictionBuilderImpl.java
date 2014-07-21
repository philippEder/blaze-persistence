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
package com.blazebit.persistence.impl;

import com.blazebit.persistence.QuantifiableBinaryPredicateBuilder;
import com.blazebit.persistence.RestrictionBuilder;
import com.blazebit.persistence.impl.expression.Expression;
import com.blazebit.persistence.impl.expression.Expressions;
import com.blazebit.persistence.impl.expression.ParameterExpression;
import com.blazebit.persistence.impl.predicate.BetweenPredicate;
import com.blazebit.persistence.impl.predicate.BuilderEndedListener;
import com.blazebit.persistence.impl.predicate.EqPredicate;
import com.blazebit.persistence.impl.predicate.GePredicate;
import com.blazebit.persistence.impl.predicate.GtPredicate;
import com.blazebit.persistence.impl.predicate.InPredicate;
import com.blazebit.persistence.impl.predicate.IsEmptyPredicate;
import com.blazebit.persistence.impl.predicate.IsMemberOfPredicate;
import com.blazebit.persistence.impl.predicate.IsNullPredicate;
import com.blazebit.persistence.impl.predicate.LePredicate;
import com.blazebit.persistence.impl.predicate.LikePredicate;
import com.blazebit.persistence.impl.predicate.LtPredicate;
import com.blazebit.persistence.impl.predicate.NotPredicate;
import com.blazebit.persistence.impl.predicate.Predicate;
import com.blazebit.persistence.impl.predicate.PredicateBuilder;
import java.util.List;

/**
 *
 * @author cpbec
 */
public class RestrictionBuilderImpl<T> extends AbstractBuilderEndedListener implements RestrictionBuilder<T>, PredicateBuilder {

    private final T result;
    private final BuilderEndedListener listener;
    private final Expression leftExpression;
    private Predicate predicate;
    
    public RestrictionBuilderImpl(T result, BuilderEndedListener listener, Expression leftExpression) {
        this.leftExpression = leftExpression;
        this.listener = listener;
        this.result = result;
    }
    
    private T chain(Predicate predicate) {
        verifyBuilderEnded();
        this.predicate = predicate;
        listener.onBuilderEnded(this);
        return result;
    }
    
    @Override
    public void onBuilderEnded(PredicateBuilder builder) {
        super.onBuilderEnded(builder);
        predicate = builder.getPredicate();
        listener.onBuilderEnded(this);
    }

    @Override
    public Predicate getPredicate() {
        return predicate;
    }

    @Override
    public T between(Object start, Object end) {
        if(start == null || end == null) throw new NullPointerException();
        return chain(new BetweenPredicate(leftExpression, new ParameterExpression(start), new ParameterExpression(end)));
    }

    @Override
    public T notBetween(Object start, Object end) {
        if(start == null || end == null) throw new NullPointerException();
        return chain(new NotPredicate(new BetweenPredicate(leftExpression, new ParameterExpression(start), new ParameterExpression(end))));
    }

    @Override
    public QuantifiableBinaryPredicateBuilder<T> eq() {
        return startBuilder(new EqPredicate.EqPredicateBuilder<T>(result, this, leftExpression, false));
    }

    @Override
    public T eq(Object value) {
        if(value == null) throw new NullPointerException();
        return chain(new EqPredicate(leftExpression, new ParameterExpression(value)));
    }

    @Override
    public T eqExpression(String expression) {
        return chain(new EqPredicate(leftExpression, Expressions.createSimpleExpression(expression)));
    }

    @Override
    public QuantifiableBinaryPredicateBuilder<T> notEq() {
        return startBuilder(new EqPredicate.EqPredicateBuilder<T>(result, this, leftExpression, true));
    }

    @Override
    public T notEq(Object value) {
        if(value == null) throw new NullPointerException();
        return chain(new NotPredicate(new EqPredicate(leftExpression, new ParameterExpression(value))));
    }

    @Override
    public T notEqExpression(String expression) {
        return chain(new NotPredicate(new EqPredicate(leftExpression, Expressions.createSimpleExpression(expression))));
    }

    @Override
    public QuantifiableBinaryPredicateBuilder<T> gt() {
        return startBuilder(new GtPredicate.GtPredicateBuilder<T>(result, this, leftExpression));
    }

    @Override
    public T gt(Object value) {
        if(value == null) throw new NullPointerException();
        return chain(new GtPredicate(leftExpression, new ParameterExpression(value)));
    }

    @Override
    public T gtExpression(String expression) {
        return chain(new GtPredicate(leftExpression, Expressions.createSimpleExpression(expression)));
    }

    @Override
    public QuantifiableBinaryPredicateBuilder<T> ge() {
        return startBuilder(new GePredicate.GePredicateBuilder<T>(result, this, leftExpression));
    }

    @Override
    public T ge(Object value) {
        if(value == null) throw new NullPointerException();
        return chain(new GePredicate(leftExpression, new ParameterExpression(value)));
    }

    @Override
    public T geExpression(String expression) {
        return chain(new GePredicate(leftExpression, Expressions.createSimpleExpression(expression)));
    }

    @Override
    public QuantifiableBinaryPredicateBuilder<T> lt() {
        return startBuilder(new LtPredicate.LtPredicateBuilder<T>(result, this, leftExpression));
    }

    @Override
    public T lt(Object value) {
        if(value == null) throw new NullPointerException();
        return chain(new LtPredicate(leftExpression, new ParameterExpression(value)));
    }

    @Override
    public T ltExpression(String expression) {
        return chain(new LtPredicate(leftExpression, Expressions.createSimpleExpression(expression)));
    }

    @Override
    public QuantifiableBinaryPredicateBuilder<T> le() {
        return startBuilder(new LePredicate.LePredicateBuilder<T>(result, this, leftExpression));
    }

    @Override
    public T le(Object value) {
        if(value == null) throw new NullPointerException();
        return chain(new LePredicate(leftExpression, new ParameterExpression(value)));
    }

    @Override
    public T leExpression(String expression) {
        return chain(new LePredicate(leftExpression, Expressions.createSimpleExpression(expression)));
    }

    @Override
    public T in(List<?> values) {
        if(values == null) throw new NullPointerException();
        return chain(new InPredicate(leftExpression, new ParameterExpression(values)));
    }

    @Override
    public T notIn(List<?> values) {
        if(values == null) throw new NullPointerException();
        return chain(new NotPredicate(new InPredicate(leftExpression, new ParameterExpression(values))));
    }
    
    @Override
    public T isNull() {
        return chain(new IsNullPredicate(leftExpression));
    }

    @Override
    public T isNotNull() {
        return chain(new NotPredicate(new IsNullPredicate(leftExpression)));
    }

    @Override
    public T isEmpty() {
        return chain(new IsEmptyPredicate(leftExpression));
    }

    @Override
    public T isNotEmpty() {
        return chain(new NotPredicate(new IsEmptyPredicate(leftExpression)));
    }

    @Override
    public T isMemberOf(String expression) {
        return chain(new IsMemberOfPredicate(leftExpression, Expressions.createSimpleExpression(expression)));
    }

    @Override
    public T isNotMemberOf(String expression) {
        return chain(new NotPredicate(new IsMemberOfPredicate(leftExpression, Expressions.createSimpleExpression(expression))));
    }
    
    @Override
    public T like(String value) {
        if(value == null) throw new NullPointerException();
        return chain(new LikePredicate(leftExpression, new ParameterExpression((Object)value), true, null));
    }

    @Override
    public T like(String value, boolean caseSensitive) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public T like(String value, boolean caseSensitive, Character escapeCharacter) {
        if(value == null) throw new NullPointerException();
        return chain(new LikePredicate(leftExpression, new ParameterExpression((Object)value), caseSensitive, escapeCharacter));
    }
    
    @Override
    public T likeExpression(String expression) {
        return chain(new LikePredicate(leftExpression, Expressions.createSimpleExpression(expression), true, null));
    }

    @Override
    public T likeExpression(String value, boolean caseSensitive) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public T likeExpression(String expression, boolean caseSensitive, Character escapeCharacter) {
        return chain(new LikePredicate(leftExpression, Expressions.createSimpleExpression(expression), caseSensitive, escapeCharacter));
    }
    
    //TODO: notLike overload with 2 args

    @Override
    public T notLike(String value) {
        if(value == null) throw new NullPointerException();
        return chain(new NotPredicate(new LikePredicate(leftExpression, new ParameterExpression((Object)value), true, null)));
    }

    @Override
    public T notLike(String value, boolean caseSensitive, Character escapeCharacter) {
        if(value == null) throw new NullPointerException();
        return chain(new NotPredicate(new LikePredicate(leftExpression, new ParameterExpression((Object)value), caseSensitive, escapeCharacter)));
    }

    @Override
    public T notLikeExpression(String expression) {
        return chain(new NotPredicate(new LikePredicate(leftExpression, Expressions.createSimpleExpression(expression), true, null)));
    }

    @Override
    public T notLikeExpression(String expression, boolean caseSensitive, Character escapeCharacter) {
        return chain(new NotPredicate(new LikePredicate(leftExpression, Expressions.createSimpleExpression(expression), caseSensitive, escapeCharacter)));
    }

}