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

package com.distrimind.upnp_igd.binding.xml;

import com.distrimind.upnp_igd.model.Namespace;
import com.distrimind.upnp_igd.model.ValidationException;
import com.distrimind.upnp_igd.model.meta.Device;
import com.distrimind.upnp_igd.model.meta.Service;
import com.distrimind.upnp_igd.model.profile.RemoteClientInfo;
import org.w3c.dom.Document;

/**
 * Reads and generates device descriptor XML metadata.
 *
 * @author Christian Bauer
 */
public interface DeviceDescriptorBinder {

    <D extends Device<?, D, S>, S extends Service<?, D, S>> D describe(D undescribedDevice, String descriptorXml)
            throws DescriptorBindingException, ValidationException;

    <D extends Device<?, D, S>, S extends Service<?, D, S>> D describe(D undescribedDevice, Document dom)
            throws DescriptorBindingException, ValidationException;

    String generate(Device<?, ?, ?> device, RemoteClientInfo info, Namespace namespace) throws DescriptorBindingException;

    Document buildDOM(Device<?, ?, ?> device, RemoteClientInfo info, Namespace namespace) throws DescriptorBindingException;

}
