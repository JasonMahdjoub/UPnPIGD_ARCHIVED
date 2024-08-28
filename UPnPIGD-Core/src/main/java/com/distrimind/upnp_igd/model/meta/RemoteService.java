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

package com.distrimind.upnp_igd.model.meta;

import com.distrimind.upnp_igd.model.ValidationError;
import com.distrimind.upnp_igd.model.ValidationException;
import com.distrimind.upnp_igd.model.types.ServiceId;
import com.distrimind.upnp_igd.model.types.ServiceType;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * The metadata of a service discovered on a remote device.
 * <p>
 * Includes the URI's for getting the service's descriptor, calling its
 * actions, and subscribing to events.
 * </p>
 * 
 * @author Christian Bauer
 */
public class RemoteService extends Service<RemoteDeviceIdentity, RemoteDevice, RemoteService> {

    final private URI descriptorURI;
    final private URI controlURI;
    final private URI eventSubscriptionURI;

    public RemoteService(ServiceType serviceType, ServiceId serviceId,
						 URI descriptorURI, URI controlURI, URI eventSubscriptionURI) throws ValidationException {
        this(serviceType, serviceId, descriptorURI, controlURI, eventSubscriptionURI, null, null);
    }

    public RemoteService(ServiceType serviceType, ServiceId serviceId,
                         URI descriptorURI, URI controlURI, URI eventSubscriptionURI,
                         Collection<Action<RemoteService>> actions, Collection<StateVariable<RemoteService>> stateVariables) throws ValidationException {
        super(serviceType, serviceId, actions, stateVariables);

        this.descriptorURI = descriptorURI;
        this.controlURI = controlURI;
        this.eventSubscriptionURI = eventSubscriptionURI;

        List<ValidationError> errors = validateThis();
        if (!errors.isEmpty()) {
            throw new ValidationException("Validation of device graph failed, call getErrors() on exception", errors);
        }
    }

    @Override
    public Action<RemoteService> getQueryStateVariableAction() {
        return new QueryStateVariableAction<>(this);
    }

    public URI getDescriptorURI() {
        return descriptorURI;
    }

    public URI getControlURI() {
        return controlURI;
    }

    public URI getEventSubscriptionURI() {
        return eventSubscriptionURI;
    }

    public List<ValidationError> validateThis() {
        List<ValidationError> errors = new ArrayList<>();

        if (getDescriptorURI() == null) {
            errors.add(new ValidationError(
                    getClass(),
                    "descriptorURI",
                    "Descriptor location (SCPDURL) is required"
            ));
        }

        if (getControlURI() == null) {
            errors.add(new ValidationError(
                    getClass(),
                    "controlURI",
                    "Control URL is required"
            ));
        }

        if (getEventSubscriptionURI() == null) {
            errors.add(new ValidationError(
                    getClass(),
                    "eventSubscriptionURI",
                    "Event subscription URL is required"
            ));
        }

        return errors;
    }

    @Override
    public String toString() {
        return "(" + getClass().getSimpleName() + ") Descriptor: " + getDescriptorURI();
    }

}