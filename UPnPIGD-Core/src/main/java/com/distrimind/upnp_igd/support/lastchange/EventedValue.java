/*
 * Copyright (C) 2013 4th Line GmbH, Switzerland
 *
 * The contents of this file are subject to the terms of either the GNU
 * Lesser General Public License Version 2 or later ("LGPL") or the
 * Common Development and Distribution License Version 1 or later
 * ("CDDL") (collectively, the "License"). You may not use this file
 * except in compliance with the License. See LICENSE.txt for more
 * information.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package com.distrimind.upnp_igd.support.lastchange;

import com.distrimind.upnp_igd.model.types.Datatype;
import com.distrimind.upnp_igd.model.types.InvalidValueException;
import com.distrimind.upnp_igd.support.shared.AbstractMap;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public abstract class EventedValue<V> {

    final protected V value;

    public EventedValue(V value) {
        this.value = value;
    }

    public EventedValue(Collection<Map.Entry<String,String>> attributes) {
        try {
            this.value = valueOf(attributes);
        } catch (InvalidValueException ex) {
            throw new RuntimeException(ex);
        }
    }

    public String getName() {
        return getClass().getSimpleName();
    }

    public V getValue() {
        return value;
    }

    public List<Map.Entry<String, String>> getAttributes() {
        return List.of(
            new AbstractMap.SimpleEntry<>("val", toString())
        );
    }

    protected V valueOf(Collection<Map.Entry<String,String>> attributes) throws InvalidValueException {
        V v = null;
        for (Map.Entry<String, String> attribute : attributes) {
            if ("val".equals(attribute.getKey())) v = valueOf(attribute.getValue());
        }
        return v;
    }

	protected V valueOf(String s) throws InvalidValueException {
        return getDatatype().valueOf(s);
    }

	@Override
    public String toString() {
        return getDatatype().getString(getValue());
    }

    abstract protected Datatype<V> getDatatype();
}
