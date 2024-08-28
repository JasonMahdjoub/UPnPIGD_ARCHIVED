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

package com.distrimind.upnp_igd.binding;

import com.distrimind.upnp_igd.model.meta.LocalService;
import com.distrimind.upnp_igd.model.types.ServiceId;
import com.distrimind.upnp_igd.model.types.ServiceType;

import java.util.Set;

/**
 * Reads {@link LocalService} metadata given a Java class.
 *
 * @author Christian Bauer
 */
public interface LocalServiceBinder {

    /**
     * @param clazz The Java class that is the source of the service metadata.
     * @return The produced metadata.
     * @throws LocalServiceBindingException If binding failed.
     */
    <T> LocalService<T> read(Class<T> clazz) throws LocalServiceBindingException;

    /**
     *
     * @param clazz The Java class that is the source of the service metadata.
     * @param id The pre-defined identifier of the service.
     * @param type The pre-defined type of the service.
     * @param supportsQueryStateVariables <code>true</code> if the service should support the
     *                                    deprecated "query any state variable value" action.
     * @param stringConvertibleTypes A list of Java classes which map directly to string-typed
     *                               UPnP state variables.
     * @return The produced metadata.
     * @throws LocalServiceBindingException If binding failed.
     */
    <T> LocalService<T> read(Class<T> clazz, ServiceId id, ServiceType type,
                              boolean supportsQueryStateVariables, Set<Class<?>> stringConvertibleTypes) throws LocalServiceBindingException;
}