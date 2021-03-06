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

package com.blazebit.persistence.view.impl.mapper;

import com.blazebit.persistence.view.ConvertOperationBuilder;
import com.blazebit.persistence.view.ConvertOption;
import com.blazebit.persistence.view.impl.EntityViewManagerImpl;
import com.blazebit.persistence.view.impl.metamodel.AbstractMethodAttribute;
import com.blazebit.persistence.view.impl.metamodel.ManagedViewTypeImplementor;

import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;

/**
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
public class ConvertOperationBuilderImpl<T> implements ConvertOperationBuilder<T> {

    private final EntityViewManagerImpl entityViewManager;
    private final ViewMapper.Key<Object, T> key;
    private final Map<String, ViewMapper.Key<Object, Object>> subMappers = new HashMap<>();
    private final Object source;

    public ConvertOperationBuilderImpl(EntityViewManagerImpl entityViewManager, ViewMapper.Key<Object, T> key, Object source) {
        this.entityViewManager = entityViewManager;
        this.key = key;
        this.source = source;
    }

    @Override
    public T convert() {
        return entityViewManager.getViewMapper(key, subMappers).map(source);
    }

    @Override
    public ConvertOperationBuilder<T> excludeAttribute(String attributePath) {
        if (!key.getSourceType().getRecursiveAttributes().containsKey(attributePath) && !key.getSourceType().getRecursiveSubviewAttributes().containsKey(attributePath)) {
            throw new IllegalArgumentException("Attribute '" + attributePath + "' could not be found on type: " + key.getSourceType().getJavaType().getName());
        }
        subMappers.put(attributePath, ViewMapper.Key.EXCLUDE_MARKER);
        return this;
    }

    @Override
    public ConvertOperationBuilder<T> excludeAttributes(String... attributePaths) {
        ManagedViewTypeImplementor<Object> sourceType = key.getSourceType();
        NavigableMap<String, AbstractMethodAttribute<? super Object, ?>> recursiveAttributes = sourceType.getRecursiveAttributes();
        NavigableMap<String, AbstractMethodAttribute<? super Object, ?>> recursiveSubviewAttributes = sourceType.getRecursiveSubviewAttributes();
        for (String attributePath : attributePaths) {
            if (!recursiveAttributes.containsKey(attributePath) && !recursiveSubviewAttributes.containsKey(attributePath)) {
                throw new IllegalArgumentException("Attribute '" + attributePath + "' could not be found on type: " + sourceType.getJavaType().getName());
            }
            subMappers.put(attributePath, ViewMapper.Key.EXCLUDE_MARKER);
        }

        return this;
    }

    @Override
    public ConvertOperationBuilder<T> convertAttribute(String attributePath, Class<?> attributeViewClass, ConvertOption... convertOptions) {
        AbstractMethodAttribute<? super Object, ?> sourceAttribute = key.getSourceType().getRecursiveSubviewAttributes().get(attributePath);
        if (sourceAttribute == null) {
            throw new IllegalArgumentException("Attribute '" + attributePath + "' could not be found on type: " + key.getSourceType().getJavaType().getName());
        }
        subMappers.put(attributePath, (ViewMapper.Key<Object, Object>) ViewMapper.Key.create(entityViewManager.getMetamodel(), sourceAttribute.getConvertedJavaType(), attributeViewClass, convertOptions));
        return this;
    }
}
