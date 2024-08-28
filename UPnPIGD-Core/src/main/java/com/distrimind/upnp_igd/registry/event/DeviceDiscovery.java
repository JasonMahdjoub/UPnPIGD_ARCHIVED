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

package com.distrimind.upnp_igd.registry.event;

import com.distrimind.upnp_igd.model.meta.Device;

/**
 * An observable event for CDI containers.
 *
 * @author Christian Bauer
 */
public class DeviceDiscovery<D extends Device<?, D, ?>> {

    protected D device;

    public DeviceDiscovery(D device) {
        this.device = device;
    }

    public D getDevice() {
        return device;
    }
}
