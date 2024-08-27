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

package com.distrimind.upnp_igd.protocol.async;

import com.distrimind.upnp_igd.UpnpServiceConfiguration;
import com.distrimind.upnp_igd.protocol.ReceivingAsync;
import com.distrimind.upnp_igd.protocol.RetrieveRemoteDescriptors;
import com.distrimind.upnp_igd.transport.RouterException;
import com.distrimind.upnp_igd.UpnpService;
import com.distrimind.upnp_igd.model.ValidationError;
import com.distrimind.upnp_igd.model.ValidationException;
import com.distrimind.upnp_igd.model.message.IncomingDatagramMessage;
import com.distrimind.upnp_igd.model.message.UpnpResponse;
import com.distrimind.upnp_igd.model.message.discovery.IncomingSearchResponse;
import com.distrimind.upnp_igd.model.meta.RemoteDevice;
import com.distrimind.upnp_igd.model.meta.RemoteDeviceIdentity;
import com.distrimind.upnp_igd.model.types.UDN;

import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles reception of search response messages.
 * <p>
 * This protocol implementation is basically the same as
 * the {@link ReceivingNotification} protocol for
 * an <em>ALIVE</em> message.
 * </p>
 *
 * @author Christian Bauer
 */
public class ReceivingSearchResponse extends ReceivingAsync<IncomingSearchResponse> {

    final private static Logger log = Logger.getLogger(ReceivingSearchResponse.class.getName());

    public ReceivingSearchResponse(UpnpService upnpService, IncomingDatagramMessage<UpnpResponse> inputMessage) {
        super(upnpService, new IncomingSearchResponse(inputMessage));
    }

    @Override
	protected void execute() throws RouterException {

        if (!getInputMessage().isSearchResponseMessage()) {
			if (log.isLoggable(Level.FINE)) {
				log.fine("Ignoring invalid search response message: " + getInputMessage());
			}
			return;
        }

        UDN udn = getInputMessage().getRootDeviceUDN();
        if (udn == null) {
			if (log.isLoggable(Level.FINE)) {
				log.fine("Ignoring search response message without UDN: " + getInputMessage());
			}
			return;
        }

        RemoteDeviceIdentity rdIdentity = new RemoteDeviceIdentity(getInputMessage());
		if (log.isLoggable(Level.FINE)) {
			log.fine("Received device search response: " + rdIdentity);
		}

		if (getUpnpService().getRegistry().update(rdIdentity)) {
			if (log.isLoggable(Level.FINE)) {
				log.fine("Remote device was already known: " + udn);
			}
			return;
        }

        RemoteDevice rd;
        try {
            rd = new RemoteDevice(rdIdentity);
        } catch (ValidationException ex) {
			if (log.isLoggable(Level.WARNING)) log.warning("Validation errors of device during discovery: " + rdIdentity);
            for (ValidationError validationError : ex.getErrors()) {
				if (log.isLoggable(Level.WARNING)) log.warning(validationError.toString());
            }
            return;
        }

        if (rdIdentity.getDescriptorURL() == null) {
			if (log.isLoggable(Level.FINER)) {
				log.finer("Ignoring message without location URL header: " + getInputMessage());
			}
			return;
        }

        if (rdIdentity.getMaxAgeSeconds() == null) {
			if (log.isLoggable(Level.FINER)) {
				log.finer("Ignoring message without max-age header: " + getInputMessage());
			}
			return;
        }

		UpnpServiceConfiguration conf = getUpnpService().getConfiguration();
		if (conf != null) {
			Executor executor = conf.getAsyncProtocolExecutor();
			if (executor != null) {
				executor.execute(new RetrieveRemoteDescriptors(getUpnpService(), rd));
			}
		}

    }

}
