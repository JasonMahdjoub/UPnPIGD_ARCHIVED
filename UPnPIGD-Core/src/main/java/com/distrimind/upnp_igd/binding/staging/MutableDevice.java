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

package com.distrimind.upnp_igd.binding.staging;

import com.distrimind.upnp_igd.model.ValidationException;
import com.distrimind.upnp_igd.model.meta.Device;
import com.distrimind.upnp_igd.model.meta.DeviceDetails;
import com.distrimind.upnp_igd.model.meta.Icon;
import com.distrimind.upnp_igd.model.meta.ManufacturerDetails;
import com.distrimind.upnp_igd.model.meta.ModelDetails;
import com.distrimind.upnp_igd.model.meta.Service;
import com.distrimind.upnp_igd.model.meta.UDAVersion;
import com.distrimind.upnp_igd.model.types.DLNACaps;
import com.distrimind.upnp_igd.model.types.DLNADoc;
import com.distrimind.upnp_igd.model.types.DeviceType;
import com.distrimind.upnp_igd.model.types.UDN;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Christian Bauer
 */
public class MutableDevice<D extends Device<?, D, S>, S extends Service<?, D, S>> {

    public UDN udn;
    public MutableUDAVersion udaVersion = new MutableUDAVersion();
    public URL baseURL;
    public String deviceType;
    public String friendlyName;
    public String manufacturer;
    public URI manufacturerURI;
    public String modelName;
    public String modelDescription;
    public String modelNumber;
    public URI modelURI;
    public String serialNumber;
    public String upc;
    public URI presentationURI;
    public List<DLNADoc> dlnaDocs = new ArrayList<>();
    public DLNACaps dlnaCaps;
    public List<MutableIcon> icons = new ArrayList<>();
    public List<MutableService<D,S>> services = new ArrayList<>();
    public List<MutableDevice<D, S>> embeddedDevices = new ArrayList<>();
    public MutableDevice<D, S> parentDevice;

    public D build(D prototype) throws ValidationException {
        // Note how all embedded devices inherit the version and baseURL of the root!
        return build(prototype, createDeviceVersion(), baseURL);
    }

    public D build(D prototype, UDAVersion deviceVersion, URL baseURL) throws ValidationException {

        List<D> embeddedDevicesList = new ArrayList<>();
        for (MutableDevice<D, S> embeddedDevice : embeddedDevices) {
            embeddedDevicesList.add(embeddedDevice.build(prototype, deviceVersion, baseURL));
        }
        return prototype.newInstance(
                udn,
                deviceVersion,
                createDeviceType(),
                createDeviceDetails(baseURL),
                createIcons(),
                createServices(prototype),
                embeddedDevicesList
        );
    }

    public UDAVersion createDeviceVersion() {
        return new UDAVersion(udaVersion.major, udaVersion.minor);
    }

    public DeviceType createDeviceType() {
        return deviceType==null?null:DeviceType.valueOf(deviceType);
    }

    public DeviceDetails createDeviceDetails(URL baseURL) {
        return new DeviceDetails(
                baseURL,
                friendlyName,
                new ManufacturerDetails(manufacturer, manufacturerURI),
                new ModelDetails(modelName, modelDescription, modelNumber, modelURI),
                serialNumber, upc, presentationURI, dlnaDocs, dlnaCaps
        );
    }

    public List<Icon> createIcons() {
        List<Icon> iconArray = new ArrayList<>(icons.size());
        for (MutableIcon icon : icons) {
            iconArray.add(icon.build());
        }
        return iconArray;
    }

    public Collection<S> createServices(D prototype) throws ValidationException {
        Collection<S> services = new ArrayList<>(this.services.size());
        for (MutableService<D, S> service : this.services) {
            services.add(service.build(prototype));
        }
        return services;
    }

}
