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
package com.distrimind.upnp_igd.test.model.profile;

import com.distrimind.upnp_igd.model.meta.DeviceDetails;
import com.distrimind.upnp_igd.model.profile.RemoteClientInfo;
import com.distrimind.upnp_igd.model.profile.HeaderDeviceDetailsProvider;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Mario Franco
 */
public class DeviceDetailsProviderTest {

    public static final String USER_AGENT = "User-Agent";

    @Test
    public void headerRegexMatch() {

        RemoteClientInfo clientInfo = new RemoteClientInfo();

        DeviceDetails dd1 = new DeviceDetails("My Testdevice 1");
        DeviceDetails dd2 = new DeviceDetails("My Testdevice 2");

        Map<HeaderDeviceDetailsProvider.Key, DeviceDetails> headerDetails = new HashMap<>();

        headerDetails.put(new HeaderDeviceDetailsProvider.Key(USER_AGENT, "Xbox.*"), dd1);
        headerDetails.put(new HeaderDeviceDetailsProvider.Key("X-AV-Client-Info", ".*PLAYSTATION 3.*"), dd2);

        HeaderDeviceDetailsProvider provider = new HeaderDeviceDetailsProvider(dd1, headerDetails);

        // No match, test default behavior
        clientInfo.getRequestHeaders().clear();
        clientInfo.getRequestHeaders().add(
                USER_AGENT,
                "Microsoft-Windows/6.1 UPnP/1.0 Windows-Media-Player-DMS/12.0.7600.16385 DLNADOC/1.50"
        );
        Assert.assertEquals(provider.provide(clientInfo), dd1);

        clientInfo.getRequestHeaders().clear();
        clientInfo.getRequestHeaders().add(
                USER_AGENT,
                "UPnP/1.0"
        );
        clientInfo.getRequestHeaders().add(
                "X-AV-Client-Info",
                "av=5.0; cn=\"Sony Computer Entertainment Inc.\"; mn=\"PLAYSTATION 3\"; mv=\"1.0\";"
        );
        Assert.assertEquals(provider.provide(clientInfo), dd2);

        clientInfo.getRequestHeaders().clear();
        clientInfo.getRequestHeaders().add(
                USER_AGENT,
                "Xbox/2.0.4548.0 UPnP/1.0 Xbox/2.0.4548.0"
        );
        Assert.assertEquals(provider.provide(clientInfo), dd1);
    }
}
