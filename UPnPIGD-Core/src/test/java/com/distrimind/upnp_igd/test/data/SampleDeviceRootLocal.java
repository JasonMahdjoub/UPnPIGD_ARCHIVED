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

import com.distrimind.upnp_igd.model.meta.Service;
import com.distrimind.upnp_igd.model.resource.DeviceDescriptorResource;
import com.distrimind.upnp_igd.model.resource.IconResource;
import com.distrimind.upnp_igd.model.resource.ServiceControlResource;
import com.distrimind.upnp_igd.model.resource.ServiceDescriptorResource;
import com.distrimind.upnp_igd.model.resource.ServiceEventSubscriptionResource;
import com.distrimind.upnp_igd.model.resource.Resource;
import com.distrimind.upnp_igd.model.meta.Device;
import com.distrimind.upnp_igd.model.meta.DeviceIdentity;

import java.net.URI;
import java.util.Collection;
import java.util.Objects;

import static org.testng.Assert.assertEquals;

/**
 * @author Christian Bauer
 */
public class SampleDeviceRootLocal<D extends Device<?, D, S>, S extends Service<?, D, S>> extends SampleDeviceRoot<D,S> {

    public SampleDeviceRootLocal(DeviceIdentity identity, S service, D embeddedDevice) {
        super(identity, service, embeddedDevice);
    }

    public static void assertLocalResourcesMatch(Collection<Resource<?>> resources){
        assertEquals(
                Objects.requireNonNull(getLocalResource(resources, URI.create("/dev/MY-DEVICE-123/desc"))).getClass(),
                DeviceDescriptorResource.class
        );
        assertEquals(
                Objects.requireNonNull(getLocalResource(resources, URI.create("/dev/MY-DEVICE-123/icon.png"))).getClass(),
                IconResource.class
        );
        assertEquals(
                Objects.requireNonNull(getLocalResource(resources, URI.create("/dev/MY-DEVICE-123/icon2.png"))).getClass(),
                IconResource.class
        );
        assertEquals(
                Objects.requireNonNull(getLocalResource(resources, URI.create("/dev/MY-DEVICE-123/svc/upnp-org/MY-SERVICE-123/desc"))).getClass(),
                ServiceDescriptorResource.class
        );
        assertEquals(
                Objects.requireNonNull(getLocalResource(resources, URI.create("/dev/MY-DEVICE-123/svc/upnp-org/MY-SERVICE-123/action"))).getClass(),
                ServiceControlResource.class
        );
        assertEquals(
                Objects.requireNonNull(getLocalResource(resources, URI.create("/dev/MY-DEVICE-123/svc/upnp-org/MY-SERVICE-123/event"))).getClass(),
                ServiceEventSubscriptionResource.class
        );
        assertEquals(
                Objects.requireNonNull(getLocalResource(resources, URI.create("/dev/MY-DEVICE-456/icon3.png"))).getClass(),
                IconResource.class
        );
        assertEquals(
                Objects.requireNonNull(getLocalResource(resources, URI.create("/dev/MY-DEVICE-456/svc/upnp-org/MY-SERVICE-456/desc"))).getClass(),
                ServiceDescriptorResource.class
        );
        assertEquals(
                Objects.requireNonNull(getLocalResource(resources, URI.create("/dev/MY-DEVICE-456/svc/upnp-org/MY-SERVICE-456/action"))).getClass(),
                ServiceControlResource.class
        );
        assertEquals(
                Objects.requireNonNull(getLocalResource(resources, URI.create("/dev/MY-DEVICE-456/svc/upnp-org/MY-SERVICE-456/event"))).getClass(),
                ServiceEventSubscriptionResource.class
        );
        assertEquals(
                Objects.requireNonNull(getLocalResource(resources, URI.create("/dev/MY-DEVICE-789/svc/upnp-org/MY-SERVICE-789/desc"))).getClass(),
                ServiceDescriptorResource.class
        );
        assertEquals(
                Objects.requireNonNull(getLocalResource(resources, URI.create("/dev/MY-DEVICE-789/svc/upnp-org/MY-SERVICE-789/action"))).getClass(),
                ServiceControlResource.class
        );
        assertEquals(
                Objects.requireNonNull(getLocalResource(resources, URI.create("/dev/MY-DEVICE-789/svc/upnp-org/MY-SERVICE-789/event"))).getClass(),
                ServiceEventSubscriptionResource.class
        );

    }

}
