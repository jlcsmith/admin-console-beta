/**
 * Copyright (c) Codice Foundation
 * <p>
 * This is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or any later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details. A copy of the GNU Lesser General Public License
 * is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 **/
package org.codice.ddf.admin.common.fields.base;

import static org.codice.ddf.admin.api.fields.Field.FieldBaseType.OBJECT;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.codice.ddf.admin.api.action.Message;
import org.codice.ddf.admin.api.fields.Field;
import org.codice.ddf.admin.api.fields.ObjectField;

public abstract class BaseObjectField extends BaseField<Map<String, Object>>
        implements ObjectField {

    protected BaseObjectField(String fieldName, String fieldTypeName, String description,
            FieldBaseType baseType) {
        super(fieldName, fieldTypeName, description, baseType);
        initializeFields();
        getFields().forEach(field -> field.addToPath(fieldName()));
    }

    public BaseObjectField(String fieldName, String fieldTypeName, String description) {
        this(fieldName, fieldTypeName, description, OBJECT);
    }

    @Override
    public Map<String, Object> getValue() {
        Map<String, Object> value = new HashMap<>();
        getFields().forEach(field -> value.put(field.fieldName(), field.getValue()));
        return value;
    }

    @Override
    public void setValue(Map<String, Object> values) {
        getFields().stream()
                .filter(field -> values.containsKey(field.fieldName()))
                .forEach(field -> field.setValue(values.get(field.fieldName())));
    }

    @Override
    public List<Message> validate() {
        List<Message> validationErrors = super.validate();

        if (!validationErrors.isEmpty()) {
            return validationErrors;
        }

        validationErrors.addAll(getFields().stream()
                .filter(Field::isRequired)
                .map(field -> (List<Message>) field.validate())
                .flatMap(Collection::stream)
                .collect(Collectors.toList()));

        validationErrors.addAll(getFields().stream()
                .filter(field -> !field.isRequired() && field.getValue() != null)
                .map(field -> (List<Message>) field.validate())
                .flatMap(Collection::stream)
                .collect(Collectors.toList()));

        validationErrors.forEach(msg -> msg.addSubpath(fieldName()));
        return validationErrors;
    }

    @Override
    public BaseObjectField allFieldsRequired(boolean required) {
        if (required) {
            isRequired(required);
        }

        getFields().stream()
                .map(field -> field.isRequired(required))
                .filter(field -> field instanceof ObjectField)
                .map(ObjectField.class::cast)
                .forEach(objField -> objField.allFieldsRequired(required));
        return this;
    }

    @Override
    public BaseObjectField innerFieldRequired(boolean required, String fieldName) {
        if (required) {
            isRequired(true);
        }

        getFields().stream()
                .filter(field -> field.fieldName()
                        .equals(fieldName))
                .forEach(field -> field.isRequired(required));

        getFields().stream()
                .filter(field -> field instanceof ObjectField)
                .map(ObjectField.class::cast)
                .forEach(objField -> objField.innerFieldRequired(required, fieldName));
        return this;
    }

    /**
     * Initializes all the {@link org.codice.ddf.admin.api.fields.Field} of an {@code ObjectField}. Any {@link org.codice.ddf.admin.api.fields.Field} that gets initialized
     * in this method, and is also returned by the {@link ObjectField#getFields()}, will have the {@code ObjectField} added to its path.
     */
    public abstract void initializeFields(boolean useDefaultRequiredFields);

    @Override
    public void addToPath(String fieldName) {
        super.addToPath(fieldName);
        getFields().forEach(child -> child.addToPath(fieldName));
    }
}
