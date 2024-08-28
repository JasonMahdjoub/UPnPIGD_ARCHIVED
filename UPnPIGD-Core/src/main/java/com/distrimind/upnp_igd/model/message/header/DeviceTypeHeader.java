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

package com.distrimind.upnp_igd.model.message.header;

import com.distrimind.upnp_igd.model.types.DeviceType;

import java.net.URI;

/**
 * @author Christian Bauer
 */
public class DeviceTypeHeader extends UpnpHeader<DeviceType> {

    public DeviceTypeHeader() {
    }

    public DeviceTypeHeader(URI uri) {
        setString(uri.toString());
    }

    public DeviceTypeHeader(DeviceType value) {
        setValue(value);
    }

    @Override
	public void setString(String s) throws InvalidHeaderException {
        try {
            setValue(DeviceType.valueOf(s));
        } catch (RuntimeException ex) {
            throw new InvalidHeaderException("Invalid device type header value, " + ex.getMessage());
        }
    }

    @Override
	public String getString() {
        return getValue().toString();
    }
}
