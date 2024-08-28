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

package com.distrimind.upnp_igd.test.model;

import com.distrimind.upnp_igd.model.meta.DeviceDetails;
import com.distrimind.upnp_igd.model.meta.Icon;
import com.distrimind.upnp_igd.model.meta.RemoteDevice;
import com.distrimind.upnp_igd.model.types.UDADeviceType;
import com.distrimind.upnp_igd.test.data.SampleData;
import org.testng.annotations.Test;

import java.net.URI;
import java.util.Collections;
import java.util.List;

import static org.testng.Assert.*;

/**
 * @author Christian Bauer
 */
public class IconTest {

    public static final String FOO = "Foo";

    @Test
    public void validIcons() throws Exception {
        RemoteDevice rd = new RemoteDevice(
            SampleData.createRemoteDeviceIdentity(),
            new UDADeviceType(FOO, 1),
            new DeviceDetails(FOO),
            List.of(
                new Icon(null, 0, 0, 0, URI.create("foo")),
                new Icon("foo/bar", 0, 0, 0, URI.create("foo")),
                new Icon("foo/bar", 123, 456, 0, URI.create("foo"))
            ),
                Collections.emptyList()
        );
        assertEquals(rd.findIcons().size(), 3);
    }

    @Test
    public void invalidIcons() throws Exception {
        RemoteDevice rd = new RemoteDevice(
            SampleData.createRemoteDeviceIdentity(),
            new UDADeviceType(FOO, 1),
            new DeviceDetails(FOO),
            List.of(
                new Icon("image/png", 123, 123, 8, URI.create("urn:not_a_URL"))
                    ),
            Collections.emptyList()
        );
        assertEquals(rd.findIcons().size(), 0);
    }
}
