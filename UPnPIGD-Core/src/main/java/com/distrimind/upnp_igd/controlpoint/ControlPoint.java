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

package com.distrimind.upnp_igd.controlpoint;

import com.distrimind.upnp_igd.model.message.header.UpnpHeader;
import com.distrimind.upnp_igd.protocol.ProtocolFactory;
import com.distrimind.upnp_igd.UpnpServiceConfiguration;
import com.distrimind.upnp_igd.registry.Registry;

import java.util.concurrent.Future;

/**
 * Unified API for the asynchronous execution of network searches, actions, event subscriptions.
 *
 * @author Christian Bauer
 */
public interface ControlPoint {

    UpnpServiceConfiguration getConfiguration();
    ProtocolFactory getProtocolFactory();
    Registry getRegistry();

    void search();
    void search(UpnpHeader<?> searchType);
    void search(int mxSeconds);
    void search(UpnpHeader<?> searchType, int mxSeconds);
    Future<?> execute(ActionCallback callback);
    void execute(SubscriptionCallback callback);

}
