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
import com.distrimind.upnp_igd.model.meta.ManufacturerDetails;
import com.distrimind.upnp_igd.model.meta.ModelDetails;
import com.distrimind.upnp_igd.model.meta.RemoteDevice;
import com.distrimind.upnp_igd.model.meta.RemoteService;
import com.distrimind.upnp_igd.model.meta.Service;
import com.distrimind.upnp_igd.model.profile.DeviceDetailsProvider;
import com.distrimind.upnp_igd.model.resource.Resource;
import com.distrimind.upnp_igd.model.resource.ServiceEventCallbackResource;
import com.distrimind.upnp_igd.model.types.DLNACaps;
import com.distrimind.upnp_igd.model.types.DLNADoc;
import com.distrimind.upnp_igd.model.types.DeviceType;
import com.distrimind.upnp_igd.model.types.UDADeviceType;
import com.distrimind.upnp_igd.model.types.UDN;
import com.distrimind.upnp_igd.util.URIUtil;

import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author Christian Bauer
 */
public class SampleDeviceRoot<D extends Device<?, D, S>, S extends Service<?, D, S>> extends SampleDevice<D, S> {

    public SampleDeviceRoot(DeviceIdentity identity, S service, D embeddedDevice) {
        super(identity, service, embeddedDevice);
    }

    @Override
    public DeviceType getDeviceType() {
        return new UDADeviceType("MY-DEVICE-TYPE", 1);
    }

    @Override
    public DeviceDetails getDeviceDetails() {
        return new DeviceDetails(
                "My Testdevice",
                new ManufacturerDetails("4th Line", "http://www.4thline.org/"),
                new ModelDetails("MYMODEL", "TEST Device", "ONE", "http://www.4thline.org/foo"),
                "000da201238c",
                "100000000001",
                "http://www.4thline.org/some_user_interface/",
                List.of(
                        new DLNADoc("DMS", DLNADoc.Version.V1_5),
                        new DLNADoc("M-DMS", DLNADoc.Version.V1_5)
                ),
                new DLNACaps(List.of(
                        "av-upload", "image-upload", "audio-upload"
                ))
        );
    }

    @Override
	public DeviceDetailsProvider getDeviceDetailsProvider() {
        return info -> getDeviceDetails();
    }

    @Override
    public List<Icon> getIcons() {
        return List.of(
                new Icon("image/png", 32, 32, 8, URI.create("icon.png")),
                new Icon("image/png", 32, 32, 8, URI.create("icon2.png"))
        );
    }

    public static UDN getRootUDN() {
        return new UDN("MY-DEVICE-123");
    }

    public static URI getDeviceDescriptorURI() {
        return URI.create("/dev/MY-DEVICE-123/desc");
    }

    public static URL getDeviceDescriptorURL() {
        return URIUtil.createAbsoluteURL(SampleData.getLocalBaseURL(), getDeviceDescriptorURI());
    }

    public static void assertMatch(Device<?, ?, ?> a, Device<?, ?, ?> b) {
        assertMatch(a, b, true);
    }
    
    public static void assertMatch(Device<?, ?, ?> a, Device<?, ?, ?> b, boolean checkType) {
        assertTrue(a.isRoot());
        assertDeviceMatch(a, b, checkType);
    }

    public static void assertLocalResourcesMatch(Collection<Resource<?>> resources){
        assertEquals(
                Objects.requireNonNull(getLocalResource(resources, URI.create("/dev/MY-DEVICE-123/svc/upnp-org/MY-SERVICE-123/event/cb"))).getClass(),
                ServiceEventCallbackResource.class
        );

        assertEquals(
                Objects.requireNonNull(getLocalResource(resources, URI.create("/dev/MY-DEVICE-456/svc/upnp-org/MY-SERVICE-456/event/cb"))).getClass(),
                ServiceEventCallbackResource.class
        );
        assertEquals(
                Objects.requireNonNull(getLocalResource(resources, URI.create("/dev/MY-DEVICE-789/svc/upnp-org/MY-SERVICE-789/event/cb"))).getClass(),
                ServiceEventCallbackResource.class
        );
    }

    protected static Resource<?> getLocalResource(Collection<Resource<?>> resources, URI localPathQuery) {
        for (Resource<?> localResource : resources) {
            if (localResource.matches(localPathQuery))
                return localResource;
        }
        return null;
    }

    protected static void assertDeviceMatch(Device<?, ?, ?> a, Device<?, ?, ?> b) {
        assertDeviceMatch(a,b,true);
    }
    protected static void assertDeviceMatch(Device<?, ?, ?> a, Device<?, ?, ?> b, boolean checkType) {

        assert (a.validate().isEmpty());
        assert (b.validate().isEmpty());

        if (checkType)
            assertEquals(a, b); // Checking equals() method
        assertEquals(a.getIdentity().getUdn(), b.getIdentity().getUdn());
        assertEquals(a.getVersion().getMajor(), b.getVersion().getMajor());
        assertEquals(a.getVersion().getMinor(), b.getVersion().getMinor());
        assertEquals(a.getType(), b.getType());
        assertEquals(a.getDetails().getFriendlyName(), b.getDetails().getFriendlyName());
        assertEquals(a.getDetails().getManufacturerDetails().getManufacturer(), b.getDetails().getManufacturerDetails().getManufacturer());
        assertEquals(a.getDetails().getManufacturerDetails().getManufacturerURI(), b.getDetails().getManufacturerDetails().getManufacturerURI());
        assertEquals(a.getDetails().getModelDetails().getModelDescription(), b.getDetails().getModelDetails().getModelDescription());
        assertEquals(a.getDetails().getModelDetails().getModelName(), b.getDetails().getModelDetails().getModelName());
        assertEquals(a.getDetails().getModelDetails().getModelNumber(), b.getDetails().getModelDetails().getModelNumber());
        assertEquals(a.getDetails().getModelDetails().getModelURI(), b.getDetails().getModelDetails().getModelURI());
        assertEquals(a.getDetails().getSerialNumber(), b.getDetails().getSerialNumber());
        assertEquals(a.getDetails().getUpc(), b.getDetails().getUpc());
        assertEquals(a.getDetails().getPresentationURI(), b.getDetails().getPresentationURI());

        assertEquals(a.getDetails().getDlnaDocs().size(), b.getDetails().getDlnaDocs().size());
        for (int i = 0; i < a.getDetails().getDlnaDocs().size(); i++) {
            DLNADoc aDoc = a.getDetails().getDlnaDocs().get(i);
            DLNADoc bDoc = b.getDetails().getDlnaDocs().get(i);
            assertEquals(aDoc, bDoc);
        }
        assertEquals(a.getDetails().getDlnaCaps(), b.getDetails().getDlnaCaps());

        assertEquals(a.getIcons() != null, b.getIcons() != null);
        if (a.getIcons() != null) {
            assertEquals(a.getIcons().size(), b.getIcons().size());
            for (int i = 0; i < a.getIcons().size(); i++) {
                assertEquals(a.getIcons().get(i).getDevice(), a);
                assertEquals(b.getIcons().get(i).getDevice(), b);
                assertEquals(a.getIcons().get(i).getUri(), b.getIcons().get(i).getUri());
                assertEquals(a.getIcons().get(i).getMimeType(), b.getIcons().get(i).getMimeType());
                assertEquals(a.getIcons().get(i).getWidth(), b.getIcons().get(i).getWidth());
                assertEquals(a.getIcons().get(i).getHeight(), b.getIcons().get(i).getHeight());
                assertEquals(a.getIcons().get(i).getDepth(), b.getIcons().get(i).getDepth());
            }
        }

        assertEquals(a.hasServices(), b.hasServices());
        if (a.getServices() != null) {
            assertEquals(a.getServices().size(), b.getServices().size());
            for (int i = 0; i < a.getServices().size(); i++) {
                Service<?, ?, ?> service = a.getServices().get(i);
                assertEquals(service.getServiceType(), b.getServices().get(i).getServiceType());
                assertEquals(service.getServiceId(), b.getServices().get(i).getServiceId());
                if (a instanceof RemoteDevice && b instanceof RemoteDevice) {
                    RemoteService remoteServiceA = (RemoteService) service;
                    RemoteService remoteServiceB = (RemoteService) b.getServices().get(i);

                    assertEquals(
                            remoteServiceA.getEventSubscriptionURI(),
                            remoteServiceB.getEventSubscriptionURI()
                    );
                    assertEquals(
                            remoteServiceA.getControlURI(),
                            remoteServiceB.getControlURI()
                    );
                    assertEquals(
                            remoteServiceA.getDescriptorURI(),
                            remoteServiceB.getDescriptorURI()
                    );
                }
            }
        }

        assertEquals(a.hasEmbeddedDevices(), b.hasEmbeddedDevices());
        if (a.getEmbeddedDevices() != null) {
            assertEquals(a.getEmbeddedDevices().size(), b.getEmbeddedDevices().size());
            for (int i = 0; i < a.getEmbeddedDevices().size(); i++) {
                Device<?, ?, ?> aEmbedded = a.getEmbeddedDevices().get(i);
                assertDeviceMatch(aEmbedded, b.getEmbeddedDevices().get(i),checkType);

            }
        }

    }


}
