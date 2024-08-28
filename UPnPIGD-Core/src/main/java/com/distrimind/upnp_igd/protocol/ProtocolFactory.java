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

package com.distrimind.upnp_igd.protocol;

import com.distrimind.upnp_igd.controlpoint.ControlPoint;
import com.distrimind.upnp_igd.model.gena.GENASubscription;
import com.distrimind.upnp_igd.model.message.UpnpRequest;
import com.distrimind.upnp_igd.model.message.UpnpResponse;
import com.distrimind.upnp_igd.protocol.async.*;
import com.distrimind.upnp_igd.protocol.sync.*;
import com.distrimind.upnp_igd.registry.Registry;
import com.distrimind.upnp_igd.UpnpService;
import com.distrimind.upnp_igd.model.action.ActionInvocation;
import com.distrimind.upnp_igd.model.meta.LocalDevice;
import com.distrimind.upnp_igd.model.gena.LocalGENASubscription;
import com.distrimind.upnp_igd.model.gena.RemoteGENASubscription;
import com.distrimind.upnp_igd.model.message.IncomingDatagramMessage;
import com.distrimind.upnp_igd.model.message.StreamRequestMessage;
import com.distrimind.upnp_igd.model.message.header.UpnpHeader;

import java.net.URL;

/**
 * Factory for UPnP protocols, the core implementation of the UPnP specification.
 * <p>
 * This factory creates an executable protocol either based on the received UPnP messsage, or
 * on local device/search/service metadata). A protocol is an aspect of the UPnP specification,
 * you can override individual protocols to customize the behavior of the UPnP stack.
 * </p>
 * <p>
 * An implementation has to be thread-safe.
 * </p>
 * 
 * @author Christian Bauer
 */
public interface ProtocolFactory {

    UpnpService getUpnpService();

    /**
     * Creates a {@link ReceivingNotification},
     * {@link ReceivingSearch},
     * or {@link ReceivingSearchResponse} protocol.
     *
     * @param message The incoming message, either {@link UpnpRequest} or
     *                {@link UpnpResponse}.
     * @return        The appropriate protocol that handles the messages or <code>null</code> if the message should be dropped.
     * @throws ProtocolCreationException If no protocol could be found for the message.
     */
	ReceivingAsync<?> createReceivingAsync(IncomingDatagramMessage<?> message) throws ProtocolCreationException;

    /**
     * Creates a {@link ReceivingRetrieval},
     * {@link ReceivingAction},
     * {@link ReceivingSubscribe},
     * {@link ReceivingUnsubscribe}, or
     * {@link ReceivingEvent} protocol.
     *
     * @param requestMessage The incoming message, examime {@link UpnpRequest.Method}
     *                       to determine the protocol.
     * @return        The appropriate protocol that handles the messages.
     * @throws ProtocolCreationException If no protocol could be found for the message.
     */
	ReceivingSync<?, ?> createReceivingSync(StreamRequestMessage requestMessage) throws ProtocolCreationException;

    /**
     * Called by the {@link Registry}, creates a protocol for announcing local devices.
     */
	<T> SendingNotificationAlive createSendingNotificationAlive(LocalDevice<T> localDevice);

    /**
     * Called by the {@link Registry}, creates a protocol for announcing local devices.
     */
	<T> SendingNotificationByebye createSendingNotificationByebye(LocalDevice<T> localDevice);

    /**
     * Called by the {@link ControlPoint}, creates a protocol for a multicast search.
     */
	SendingSearch createSendingSearch(UpnpHeader<?> searchTarget, int mxSeconds);

    /**
     * Called by the {@link ControlPoint}, creates a protocol for executing an action.
     */
	SendingAction createSendingAction(ActionInvocation<?> actionInvocation, URL controlURL);

    /**
     * Called by the {@link ControlPoint}, creates a protocol for GENA subscription.
     */
	SendingSubscribe createSendingSubscribe(RemoteGENASubscription subscription) throws ProtocolCreationException;

    /**
     * Called by the {@link ControlPoint}, creates a protocol for GENA renewal.
     */
	SendingRenewal createSendingRenewal(RemoteGENASubscription subscription);

    /**
     * Called by the {@link ControlPoint}, creates a protocol for GENA unsubscription.
     */
	SendingUnsubscribe createSendingUnsubscribe(RemoteGENASubscription subscription);

    /**
     * Called by the {@link GENASubscription}, creates a protocol for sending GENA events.
     */
	SendingEvent createSendingEvent(LocalGENASubscription<?> subscription);
}
