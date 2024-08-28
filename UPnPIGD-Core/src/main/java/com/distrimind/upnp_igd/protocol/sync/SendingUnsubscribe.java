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

package com.distrimind.upnp_igd.protocol.sync;

import com.distrimind.upnp_igd.model.message.UpnpResponse;
import com.distrimind.upnp_igd.protocol.SendingSync;
import com.distrimind.upnp_igd.transport.RouterException;
import com.distrimind.upnp_igd.UpnpService;
import com.distrimind.upnp_igd.model.gena.CancelReason;
import com.distrimind.upnp_igd.model.gena.RemoteGENASubscription;
import com.distrimind.upnp_igd.model.message.StreamResponseMessage;
import com.distrimind.upnp_igd.model.message.gena.OutgoingUnsubscribeRequestMessage;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Disconnecting a GENA event subscription with a remote host.
 * <p>
 * Calls the {@link RemoteGENASubscription#end(CancelReason, UpnpResponse)}
 * method if the subscription request was responded to correctly. No {@link CancelReason}
 * will be provided if the unsubscribe procedure completed as expected, otherwise <code>UNSUBSCRIBE_FAILED</code>
 * is used. The response might be <code>null</code> if no response was received from the remote host.
 * </p>
 *
 * @author Christian Bauer
 */
public class SendingUnsubscribe extends SendingSync<OutgoingUnsubscribeRequestMessage, StreamResponseMessage> {

    final private static Logger log = Logger.getLogger(SendingUnsubscribe.class.getName());

    final protected RemoteGENASubscription subscription;

    public SendingUnsubscribe(UpnpService upnpService, RemoteGENASubscription subscription) {
        super(
            upnpService,
            new OutgoingUnsubscribeRequestMessage(
                subscription,
                upnpService.getConfiguration().getEventSubscriptionHeaders(subscription.getService())
            )
        );
        this.subscription = subscription;
    }

    @Override
	protected StreamResponseMessage executeSync() throws RouterException {

		if (log.isLoggable(Level.FINE)) {
			log.fine("Sending unsubscribe request: " + getInputMessage());
		}

		StreamResponseMessage response = null;
        try {
            response = getUpnpService().getRouter().send(getInputMessage());
            return response;
        } finally {
            onUnsubscribe(response);
        }
    }

    protected void onUnsubscribe(final StreamResponseMessage response) {
        // Always remove from the registry and end the subscription properly - even if it's failed
        getUpnpService().getRegistry().removeRemoteSubscription(subscription);

        getUpnpService().getConfiguration().getRegistryListenerExecutor().execute(
				() -> {
					if (response == null) {
						log.fine("Unsubscribe failed, no response received");
						subscription.end(CancelReason.UNSUBSCRIBE_FAILED, null);
					} else if (response.getOperation().isFailed()) {
						if (log.isLoggable(Level.FINE)) {
							log.fine("Unsubscribe failed, response was: " + response);
						}
						subscription.end(CancelReason.UNSUBSCRIBE_FAILED, response.getOperation());
					} else {
						if (log.isLoggable(Level.FINE)) {
							log.fine("Unsubscribe successful, response was: " + response);
						}
						subscription.end(null, response.getOperation());
					}
				}
		);
    }
}