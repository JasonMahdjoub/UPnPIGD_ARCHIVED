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

package com.distrimind.upnp_igd.test.resources;

import com.distrimind.upnp_igd.binding.xml.ServiceDescriptorBinder;
import com.distrimind.upnp_igd.binding.xml.UDA10ServiceDescriptorBinderImpl;
import com.distrimind.upnp_igd.binding.xml.UDA10ServiceDescriptorBinderSAXImpl;
import com.distrimind.upnp_igd.model.meta.RemoteDevice;
import com.distrimind.upnp_igd.model.meta.RemoteService;
import com.distrimind.upnp_igd.transport.impl.NetworkAddressFactoryImpl;
import com.distrimind.upnp_igd.test.data.SampleData;
import com.distrimind.upnp_igd.test.data.SampleServiceOne;
import com.distrimind.upnp_igd.util.io.IO;
import org.testng.annotations.Test;


public class UDA10ServiceDescriptorParsingTest {

    @Test
    public void readUDA10DescriptorDOM() throws Exception {

        ServiceDescriptorBinder binder = new UDA10ServiceDescriptorBinderImpl(new NetworkAddressFactoryImpl());

        RemoteService service = SampleData.createUndescribedRemoteService();

        service = binder.describe(service, IO.readLines(getClass().getResourceAsStream("/descriptors/service/uda10.xml")));

        SampleServiceOne.assertMatch(service, SampleData.getFirstService(SampleData.createRemoteDevice()));
    }

    @Test
    public void readUDA10DescriptorSAX() throws Exception {

        ServiceDescriptorBinder binder = new UDA10ServiceDescriptorBinderSAXImpl(new NetworkAddressFactoryImpl());

        RemoteService service = SampleData.createUndescribedRemoteService();

        service = binder.describe(service, IO.readLines(getClass().getResourceAsStream("/descriptors/service/uda10.xml")));

        SampleServiceOne.assertMatch(service, SampleData.getFirstService(SampleData.createRemoteDevice()));
    }

    @Test
    public void writeUDA10Descriptor() throws Exception {

        ServiceDescriptorBinder binder = new UDA10ServiceDescriptorBinderImpl(new NetworkAddressFactoryImpl());

        RemoteDevice rd = SampleData.createRemoteDevice();
        String descriptorXml = binder.generate(SampleData.getFirstService(rd));

/*
        System.out.println("#######################################################################################");
        System.out.println(descriptorXml);
        System.out.println("#######################################################################################");

*/

        RemoteService service = SampleData.createUndescribedRemoteService();
        service = binder.describe(service, descriptorXml);
        SampleServiceOne.assertMatch(service, SampleData.getFirstService(rd));
    }

}
