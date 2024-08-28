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

package com.distrimind.upnp_igd.test.data;

import com.distrimind.upnp_igd.model.meta.Action;
import com.distrimind.upnp_igd.model.meta.Service;
import com.distrimind.upnp_igd.model.meta.StateVariable;
import com.distrimind.upnp_igd.model.types.ServiceId;
import com.distrimind.upnp_igd.model.types.ServiceType;

import java.lang.reflect.Constructor;
import java.net.URI;
import java.util.Collection;

/**
 * @author Christian Bauer
 */
public abstract class SampleService {

    public abstract ServiceType getServiceType();
    public abstract ServiceId getServiceId();
    public abstract URI getDescriptorURI();
    public abstract URI getControlURI();
    public abstract URI getEventSubscriptionURI();
    public abstract Collection<Action<?>> getActions();
    public abstract Collection<StateVariable<?>> getStateVariables();

    public <S extends Service<?, ?, ?>> S newInstanceLocal(Constructor<S> ctor) {
        try {
            return ctor.newInstance(
                    getServiceType(), getServiceId(),
                    getActions(), getStateVariables()
            );
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public <S extends Service<?, ?, ?>> S newInstanceRemote(Constructor<S> ctor) {
        try {
            return ctor.newInstance(
                    getServiceType(), getServiceId(),
                    getDescriptorURI(), getControlURI(), getEventSubscriptionURI(),
                    getActions(), getStateVariables()
            );
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

}
