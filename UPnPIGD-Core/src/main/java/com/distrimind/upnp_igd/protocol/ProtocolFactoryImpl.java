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

import com.distrimind.upnp_igd.transport.RouterException;
import com.distrimind.upnp_igd.UpnpService;
import com.distrimind.upnp_igd.model.Namespace;
import com.distrimind.upnp_igd.model.NetworkAddress;
import com.distrimind.upnp_igd.model.action.ActionInvocation;
import com.distrimind.upnp_igd.model.gena.LocalGENASubscription;
import com.distrimind.upnp_igd.model.gena.RemoteGENASubscription;
import com.distrimind.upnp_igd.model.message.IncomingDatagramMessage;
import com.distrimind.upnp_igd.model.message.StreamRequestMessage;
import com.distrimind.upnp_igd.model.message.UpnpRequest;
import com.distrimind.upnp_igd.model.message.UpnpResponse;
import com.distrimind.upnp_igd.model.message.header.UpnpHeader;
import com.distrimind.upnp_igd.model.meta.LocalDevice;
import com.distrimind.upnp_igd.model.types.InvalidValueException;
import com.distrimind.upnp_igd.model.types.NamedServiceType;
import com.distrimind.upnp_igd.model.types.NotificationSubtype;
import com.distrimind.upnp_igd.model.types.ServiceType;
import com.distrimind.upnp_igd.protocol.async.ReceivingNotification;
import com.distrimind.upnp_igd.protocol.async.ReceivingSearch;
import com.distrimind.upnp_igd.protocol.async.ReceivingSearchResponse;
import com.distrimind.upnp_igd.protocol.async.SendingNotificationAlive;
import com.distrimind.upnp_igd.protocol.async.SendingNotificationByebye;
import com.distrimind.upnp_igd.protocol.async.SendingSearch;
import com.distrimind.upnp_igd.protocol.sync.ReceivingAction;
import com.distrimind.upnp_igd.protocol.sync.ReceivingEvent;
import com.distrimind.upnp_igd.protocol.sync.ReceivingRetrieval;
import com.distrimind.upnp_igd.protocol.sync.ReceivingSubscribe;
import com.distrimind.upnp_igd.protocol.sync.ReceivingUnsubscribe;
import com.distrimind.upnp_igd.protocol.sync.SendingAction;
import com.distrimind.upnp_igd.protocol.sync.SendingEvent;
import com.distrimind.upnp_igd.protocol.sync.SendingRenewal;
import com.distrimind.upnp_igd.protocol.sync.SendingSubscribe;
import com.distrimind.upnp_igd.protocol.sync.SendingUnsubscribe;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Default implementation, directly instantiates the appropriate protocols.
 *
 * @author Christian Bauer
 */
@ApplicationScoped
public class ProtocolFactoryImpl implements ProtocolFactory {

    final private static Logger log = Logger.getLogger(ProtocolFactory.class.getName());

    protected final UpnpService upnpService;

    protected ProtocolFactoryImpl() {
        upnpService = null;
    }

    @Inject
    public ProtocolFactoryImpl(UpnpService upnpService) {
		if (log.isLoggable(Level.FINE)) {
			log.fine("Creating ProtocolFactory: " + getClass().getName());
		}
		this.upnpService = upnpService;
    }

    @Override
	public UpnpService getUpnpService() {
        return upnpService;
    }

    @Override
	@SuppressWarnings("unchecked")
	public ReceivingAsync<?> createReceivingAsync(IncomingDatagramMessage<?> message) throws ProtocolCreationException {
        if (log.isLoggable(Level.FINE)) {
            log.fine("Creating protocol for incoming asynchronous: " + message);
        }

        if (message.getOperation() instanceof UpnpRequest) {
            IncomingDatagramMessage<UpnpRequest> incomingRequest = (IncomingDatagramMessage<UpnpRequest>)message;

            switch (incomingRequest.getOperation().getMethod()) {
                case NOTIFY:
                    return isByeBye(incomingRequest) || isSupportedServiceAdvertisement(incomingRequest)
                        ? createReceivingNotification(incomingRequest) : null;
                case MSEARCH:
                    return createReceivingSearch(incomingRequest);
            }

        } else if (message.getOperation() instanceof UpnpResponse) {
            IncomingDatagramMessage<UpnpResponse> incomingResponse = (IncomingDatagramMessage<UpnpResponse>)message;

            return isSupportedServiceAdvertisement(incomingResponse)
                ? createReceivingSearchResponse(incomingResponse) : null;
        }

        throw new ProtocolCreationException("Protocol for incoming datagram message not found: " + message);
    }

    protected ReceivingAsync<?> createReceivingNotification(IncomingDatagramMessage<UpnpRequest> incomingRequest) {
        return new ReceivingNotification(getUpnpService(), incomingRequest);
    }

    protected ReceivingAsync<?> createReceivingSearch(IncomingDatagramMessage<UpnpRequest> incomingRequest) {
        return new ReceivingSearch(getUpnpService(), incomingRequest);
    }

    protected ReceivingAsync<?> createReceivingSearchResponse(IncomingDatagramMessage<UpnpResponse> incomingResponse) {
        return new ReceivingSearchResponse(getUpnpService(), incomingResponse);
    }

    // DO NOT USE THE PARSED/TYPED MSG HEADERS! THIS WOULD DEFEAT THE PURPOSE OF THIS OPTIMIZATION!

    protected boolean isByeBye(IncomingDatagramMessage<?> message) {
        String ntsHeader = message.getHeaders().getFirstHeader(UpnpHeader.Type.NTS.getHttpName());
        return ntsHeader != null && ntsHeader.equals(NotificationSubtype.BYEBYE.getHeaderString());
    }

    protected boolean isSupportedServiceAdvertisement(IncomingDatagramMessage<?> message) {
        ServiceType[] exclusiveServiceTypes = getUpnpService().getConfiguration().getExclusiveServiceTypes();
        if (exclusiveServiceTypes == null) return false; // Discovery is disabled
        if (exclusiveServiceTypes.length == 0) return true; // Any advertisement is fine

        String usnHeader = message.getHeaders().getFirstHeader(UpnpHeader.Type.USN.getHttpName());
        if (usnHeader == null) return false; // Not a service advertisement, drop it

        try {
            NamedServiceType nst = NamedServiceType.valueOf(usnHeader);
            for (ServiceType exclusiveServiceType : exclusiveServiceTypes) {
                if (nst.getServiceType().implementsVersion(exclusiveServiceType))
                    return true;
            }
        } catch (InvalidValueException ex) {
			if (log.isLoggable(Level.FINEST)) {
				log.finest("Not a named service type header value: " + usnHeader);
			}
		}
		if (log.isLoggable(Level.FINE)) {
			log.fine("Service advertisement not supported, dropping it: " + usnHeader);
		}
		return false;
    }

    @Override
	public ReceivingSync<?, ?> createReceivingSync(StreamRequestMessage message) throws ProtocolCreationException {
		if (log.isLoggable(Level.FINE)) {
			log.fine("Creating protocol for incoming synchronous: " + message);
		}

		if (message.getOperation().getMethod().equals(UpnpRequest.Method.GET)) {

            return createReceivingRetrieval(message);

        } else if (getUpnpService().getConfiguration().getNamespace().isControlPath(message.getUri())) {

            if (message.getOperation().getMethod().equals(UpnpRequest.Method.POST))
                return createReceivingAction(message);

        } else if (getUpnpService().getConfiguration().getNamespace().isEventSubscriptionPath(message.getUri())) {

            if (message.getOperation().getMethod().equals(UpnpRequest.Method.SUBSCRIBE)) {
                return createReceivingSubscribe(message);
            } else if (message.getOperation().getMethod().equals(UpnpRequest.Method.UNSUBSCRIBE)) {
                return createReceivingUnsubscribe(message);
            }

        } else if (getUpnpService().getConfiguration().getNamespace().isEventCallbackPath(message.getUri())) {

            if (message.getOperation().getMethod().equals(UpnpRequest.Method.NOTIFY))
                return createReceivingEvent(message);

        } else {

            // TODO: UPNP VIOLATION: Onkyo devices send event messages with trailing garbage characters
            // /dev/9bb022aa-e922-aab9-682b-aa09e9b9e059/svc/upnp-org/RenderingControl/event/cb192%2e168%2e10%2e38
            // TODO: UPNP VIOLATION: Yamaha does the same
            // /dev/9ab0c000-f668-11de-9976-00a0de870fd4/svc/upnp-org/RenderingControl/event/cb><http://10.189.150.197:42082/dev/9ab0c000-f668-11de-9976-00a0de870fd4/svc/upnp-org/RenderingControl/event/cb
            if (message.getUri().getPath().contains(Namespace.EVENTS + Namespace.CALLBACK_FILE)) {
                if (log.isLoggable(Level.WARNING)) log.warning("Fixing trailing garbage in event message path: " + message.getUri().getPath());
                String invalid = message.getUri().toString();
                message.setUri(
                    URI.create(invalid.substring(
                        0, invalid.indexOf(Namespace.CALLBACK_FILE) + Namespace.CALLBACK_FILE.length()
                    ))
                );
                if (getUpnpService().getConfiguration().getNamespace().isEventCallbackPath(message.getUri())
                    && message.getOperation().getMethod().equals(UpnpRequest.Method.NOTIFY))
                    return createReceivingEvent(message);
            }

        }

        throw new ProtocolCreationException("Protocol for message type not found: " + message);
    }

    @Override
	public <T> SendingNotificationAlive createSendingNotificationAlive(LocalDevice<T> localDevice) {
        return new SendingNotificationAlive(getUpnpService(), localDevice);
    }

    @Override
	public <T> SendingNotificationByebye createSendingNotificationByebye(LocalDevice<T> localDevice) {
        return new SendingNotificationByebye(getUpnpService(), localDevice);
    }

    @Override
	public SendingSearch createSendingSearch(UpnpHeader<?> searchTarget, int mxSeconds) {
        return new SendingSearch(getUpnpService(), searchTarget, mxSeconds);
    }

    @Override
	public SendingAction createSendingAction(ActionInvocation<?> actionInvocation, URL controlURL) {
        return new SendingAction(getUpnpService(), actionInvocation, controlURL);
    }

    @Override
	public SendingSubscribe createSendingSubscribe(RemoteGENASubscription subscription) throws ProtocolCreationException {
        try {
            List<NetworkAddress> activeStreamServers =
                getUpnpService().getRouter().getActiveStreamServers(
                    subscription.getService().getDevice().getIdentity().getDiscoveredOnLocalAddress()
                );
            return new SendingSubscribe(getUpnpService(), subscription, activeStreamServers);
        } catch (RouterException ex) {
            throw new ProtocolCreationException(
                "Failed to obtain local stream servers (for event callback URL creation) from router",
                ex
            );
        }
    }

    @Override
	public SendingRenewal createSendingRenewal(RemoteGENASubscription subscription) {
        return new SendingRenewal(getUpnpService(), subscription);
    }

    @Override
	public SendingUnsubscribe createSendingUnsubscribe(RemoteGENASubscription subscription) {
        return new SendingUnsubscribe(getUpnpService(), subscription);
    }

    @Override
	public SendingEvent createSendingEvent(LocalGENASubscription<?> subscription) {
        return new SendingEvent(getUpnpService(), subscription);
    }

    protected ReceivingRetrieval createReceivingRetrieval(StreamRequestMessage message) {
        return new ReceivingRetrieval(getUpnpService(), message);
    }

    protected ReceivingAction createReceivingAction(StreamRequestMessage message) {
        return new ReceivingAction(getUpnpService(), message);
    }

    protected ReceivingSubscribe createReceivingSubscribe(StreamRequestMessage message) {
        return new ReceivingSubscribe(getUpnpService(), message);
    }

    protected ReceivingUnsubscribe createReceivingUnsubscribe(StreamRequestMessage message) {
        return new ReceivingUnsubscribe(getUpnpService(), message);
    }

    protected ReceivingEvent createReceivingEvent(StreamRequestMessage message) {
        return new ReceivingEvent(getUpnpService(), message);
    }
}
