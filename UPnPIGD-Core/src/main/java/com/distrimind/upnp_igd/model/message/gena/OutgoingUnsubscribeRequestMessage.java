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

package com.distrimind.upnp_igd.model.message.gena;

import com.distrimind.upnp_igd.model.message.IUpnpHeaders;
import com.distrimind.upnp_igd.model.message.StreamRequestMessage;
import com.distrimind.upnp_igd.model.message.UpnpRequest;
import com.distrimind.upnp_igd.model.message.header.SubscriptionIdHeader;
import com.distrimind.upnp_igd.model.message.header.UpnpHeader;
import com.distrimind.upnp_igd.model.gena.RemoteGENASubscription;

/**
 * @author Christian Bauer
 */
public class OutgoingUnsubscribeRequestMessage extends StreamRequestMessage {

    public OutgoingUnsubscribeRequestMessage(RemoteGENASubscription subscription,
                                             IUpnpHeaders extraHeaders) {

        super(UpnpRequest.Method.UNSUBSCRIBE, subscription.getEventSubscriptionURL());

        getHeaders().add(
                UpnpHeader.Type.SID,
                new SubscriptionIdHeader(subscription.getSubscriptionId())
        );

        if (extraHeaders != null)
            getHeaders().putAll(extraHeaders);

    }
}