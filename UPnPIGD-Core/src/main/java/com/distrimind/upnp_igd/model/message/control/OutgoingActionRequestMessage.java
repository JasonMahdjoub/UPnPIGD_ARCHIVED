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

package com.distrimind.upnp_igd.model.message.control;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.distrimind.upnp_igd.model.message.StreamRequestMessage;
import com.distrimind.upnp_igd.model.message.UpnpRequest;
import com.distrimind.upnp_igd.model.action.ActionInvocation;
import com.distrimind.upnp_igd.model.action.RemoteActionInvocation;
import com.distrimind.upnp_igd.model.meta.Action;
import com.distrimind.upnp_igd.model.meta.QueryStateVariableAction;
import com.distrimind.upnp_igd.model.message.header.ContentTypeHeader;
import com.distrimind.upnp_igd.model.message.header.SoapActionHeader;
import com.distrimind.upnp_igd.model.message.header.UpnpHeader;
import com.distrimind.upnp_igd.model.message.header.UserAgentHeader;
import com.distrimind.upnp_igd.model.types.SoapActionType;

import java.net.URL;

/**
 * @author Christian Bauer
 */
public class OutgoingActionRequestMessage extends StreamRequestMessage implements ActionRequestMessage {

    private static final Logger log = Logger.getLogger(OutgoingActionRequestMessage.class.getName());

    final private String actionNamespace;

    public OutgoingActionRequestMessage(ActionInvocation<?> actionInvocation, URL controlURL) {
        this(actionInvocation.getAction(), new UpnpRequest(UpnpRequest.Method.POST, controlURL));

        // For proxy remote invocations, pass through the user agent header
        if (actionInvocation instanceof RemoteActionInvocation) {
            RemoteActionInvocation<?> remoteActionInvocation = (RemoteActionInvocation<?>) actionInvocation;
            if (remoteActionInvocation.getRemoteClientInfo() != null
                && remoteActionInvocation.getRemoteClientInfo().getRequestUserAgent() != null) {
                getHeaders().add(
                    UpnpHeader.Type.USER_AGENT,
                    new UserAgentHeader(remoteActionInvocation.getRemoteClientInfo().getRequestUserAgent())
                );
            }
        } else if (actionInvocation.getClientInfo() != null) {
            getHeaders().putAll(actionInvocation.getClientInfo().getRequestHeaders());
        }
    }

    public OutgoingActionRequestMessage(Action<?> action, UpnpRequest operation) {
        super(operation);

        getHeaders().add(
                UpnpHeader.Type.CONTENT_TYPE,
                new ContentTypeHeader(ContentTypeHeader.DEFAULT_CONTENT_TYPE_UTF8)
        );

        SoapActionHeader soapActionHeader;
        if (action instanceof QueryStateVariableAction) {
            log.fine("Adding magic control SOAP action header for state variable query action");
            soapActionHeader = new SoapActionHeader(
                    new SoapActionType(
                            SoapActionType.MAGIC_CONTROL_NS, SoapActionType.MAGIC_CONTROL_TYPE, null, action.getName()
                    )
            );
        } else {
            soapActionHeader = new SoapActionHeader(
                    new SoapActionType(
                            action.getService().getServiceType(),
                            action.getName()
                    )
            );
        }

        // We need to keep it for later, convenience for writing the SOAP body XML
        actionNamespace = soapActionHeader.getValue().getTypeString();

        if (getOperation().getMethod().equals(UpnpRequest.Method.POST)) {

            getHeaders().add(UpnpHeader.Type.SOAPACTION, soapActionHeader);
			if (log.isLoggable(Level.FINE)) {
				log.fine("Added SOAP action header: " + soapActionHeader);
			}

        /* TODO: Finish the M-POST crap (or not)
        } else if (getOperation().getMethod().equals(UpnpRequest.Method.MPOST)) {

            getHeaders().add(UpnpHeader.Type.MAN, new MANHeader(Constants.SOAP_NS_ENVELOPE, "01"));

            getHeaders().add(UpnpHeader.Type.SOAPACTION, soapActionHeader);
            getHeaders().setPrefix(UpnpHeader.Type.SOAPACTION, "01");
            log.fine("Added SOAP action header with prefix '01': " + getHeaders().getFirstHeader(UpnpHeader.Type.SOAPACTION).getString());
            */

        } else {
            throw new IllegalArgumentException("Can't send action with request method: " + getOperation().getMethod());
        }
    }

    @Override
	public String getActionNamespace() {
        return actionNamespace;
    }

}
