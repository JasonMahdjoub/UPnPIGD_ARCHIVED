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

import java.util.Collection;
import java.util.Map;

/**
 * @author Christian Bauer
 */
public abstract class EventedValueEnum<E extends Enum<?>> extends EventedValue<E> {

    public EventedValueEnum(E e) {
        super(e);
    }

    public EventedValueEnum(Collection<Map.Entry<String, String>> attributes) {
        super(attributes);
    }

    @Override
    protected E valueOf(String s) throws InvalidValueException {
        return enumValueOf(s);
    }

    protected abstract E enumValueOf(String s);

    @Override
    public String toString() {
        return getValue().name();
    }

    @Override
    protected Datatype<E> getDatatype() {
        return null;
    }
}
