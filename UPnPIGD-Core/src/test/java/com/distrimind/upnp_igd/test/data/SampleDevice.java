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

import com.distrimind.upnp_igd.model.meta.Device;
import com.distrimind.upnp_igd.model.meta.DeviceDetails;
import com.distrimind.upnp_igd.model.meta.DeviceIdentity;
import com.distrimind.upnp_igd.model.meta.Icon;
import com.distrimind.upnp_igd.model.meta.Service;
import com.distrimind.upnp_igd.model.profile.DeviceDetailsProvider;
import com.distrimind.upnp_igd.model.types.DeviceType;

import java.lang.reflect.Constructor;
import java.util.List;

/**
 * @author Christian Bauer
 */
public abstract class SampleDevice<D extends Device<?, D, S>, S extends Service<?, D, S>> {

    public DeviceIdentity identity;
    public S service;
    public D embeddedDevice;

    protected SampleDevice(DeviceIdentity identity, S service, D embeddedDevice) {
        this.identity = identity;
        this.service = service;
        this.embeddedDevice = embeddedDevice;
    }

    public DeviceIdentity getIdentity() {
        return identity;
    }

    public S getService() {
        return service;
    }

    public D getEmbeddedDevice() {
        return embeddedDevice;
    }

    public abstract DeviceType getDeviceType();
    public abstract DeviceDetails getDeviceDetails();
    public abstract DeviceDetailsProvider getDeviceDetailsProvider();
    public abstract List<Icon> getIcons();

    public D newInstance(Constructor<D> deviceConstructor) {
        return newInstance(deviceConstructor, false);
    }

    @SuppressWarnings("unchecked")
	public D newInstance(Constructor<?> deviceConstructor, boolean useProvider) {
        try {
            if (useProvider) {
                return (D)deviceConstructor.newInstance(
                        getIdentity(), getDeviceType(), getDeviceDetailsProvider(),
                        getIcons(), getService(), getEmbeddedDevice()
                );
            }
            return (D)deviceConstructor.newInstance(
                    getIdentity(), getDeviceType(), getDeviceDetails(),
                    getIcons(), getService(), getEmbeddedDevice()
            );
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
