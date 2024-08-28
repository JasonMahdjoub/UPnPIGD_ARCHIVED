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

package com.distrimind.upnp_igd.transport.spi;

import com.distrimind.upnp_igd.model.message.StreamRequestMessage;
import com.distrimind.upnp_igd.model.message.StreamResponseMessage;
import com.distrimind.upnp_igd.model.UnsupportedDataException;
import com.distrimind.upnp_igd.model.action.ActionInvocation;
import com.distrimind.upnp_igd.model.message.control.ActionRequestMessage;
import com.distrimind.upnp_igd.model.message.control.ActionResponseMessage;
import com.distrimind.upnp_igd.model.meta.Service;

/**
 * Converts UPnP SOAP messages from/to action invocations.
 * <p>
 * The UPnP protocol layer processes local and remote {@link ActionInvocation}
 * instances. The UPnP transport layer accepts and returns {@link StreamRequestMessage}s
 * and {@link StreamResponseMessage}s. This processor is an adapter between the
 * two layers, reading and writing SOAP content.
 * </p>
 *
 * @author Christian Bauer
 */
public interface SOAPActionProcessor {

    /**
     * Converts the given invocation input into SOAP XML content, setting on the given request message.
     *
     * @param requestMessage The request message on which the SOAP content is set.
     * @param actionInvocation The action invocation from which input argument values are read.
     * @throws UnsupportedDataException if a problem occurs
     */
    <S extends Service<?, ?, ?>> void writeBody(ActionRequestMessage requestMessage, ActionInvocation<S> actionInvocation) throws UnsupportedDataException;

    /**
     * Converts the given invocation output into SOAP XML content, setting on the given response message.
     *
     * @param responseMessage The response message on which the SOAP content is set.
     * @param actionInvocation The action invocation from which output argument values are read.
     * @throws UnsupportedDataException if a problem occurs
     */
    <S extends Service<?, ?, ?>> void writeBody(ActionResponseMessage responseMessage, ActionInvocation<S> actionInvocation) throws UnsupportedDataException;

    /**
     * Converts SOAP XML content of the request message and sets input argument values on the given invocation.
     *
     * @param requestMessage The request message from which SOAP content is read.
     * @param actionInvocation The action invocation on which input argument values are set.
     * @throws UnsupportedDataException if a problem occurs
     */
    <S extends Service<?, ?, ?>> void readBody(ActionRequestMessage requestMessage, ActionInvocation<S> actionInvocation) throws UnsupportedDataException;

    /**
     * Converts SOAP XML content of the response message and sets output argument values on the given invocation.
     *
     * @param responseMsg The response message from which SOAP content is read.
     * @param actionInvocation The action invocation on which output argument values are set.
     * @throws UnsupportedDataException if a problem occurs
     */
    <S extends Service<?, ?, ?>> void readBody(ActionResponseMessage responseMsg, ActionInvocation<S> actionInvocation) throws UnsupportedDataException;

}
