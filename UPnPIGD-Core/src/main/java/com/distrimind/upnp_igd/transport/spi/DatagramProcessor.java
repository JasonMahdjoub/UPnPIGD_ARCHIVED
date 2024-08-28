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


import com.distrimind.upnp_igd.model.message.UpnpRequest;
import com.distrimind.upnp_igd.model.message.UpnpResponse;
import com.distrimind.upnp_igd.model.UnsupportedDataException;
import com.distrimind.upnp_igd.model.message.IncomingDatagramMessage;
import com.distrimind.upnp_igd.model.message.OutgoingDatagramMessage;

import java.net.DatagramPacket;
import java.net.InetAddress;

/**
 * Reads and creates UDP datagrams from and into UPnP messages.
 * <p>
 * An implementation of this interface has to be thread-safe.
 * </p>
 *
 * @author Christian Bauer
 */
public interface DatagramProcessor {

    /**
     * Reads the datagram and instantiates a message.
     * <p>
     * The message is either a {@link UpnpRequest} or
     * a {@link UpnpResponse} operation type.
   
     *
     * @param receivedOnAddress The address of the socket on which this datagram was received.
     * @param datagram The received UDP datagram.
     * @return The populated instance.
     * @throws UnsupportedDataException If the datagram could not be read, or didn't contain required data.
     */
	IncomingDatagramMessage<?> read(InetAddress receivedOnAddress, DatagramPacket datagram) throws UnsupportedDataException;

    /**
     * Creates a UDP datagram with the content of a message.
     * <p>
     * The outgoing message might be a {@link UpnpRequest} or a
     * {@link UpnpResponse}.
   
     *
     * @param message The outgoing datagram message.
     * @return An actual UDP datagram.
     * @throws UnsupportedDataException If the datagram could not be created.
     */
	DatagramPacket write(OutgoingDatagramMessage<?> message) throws UnsupportedDataException;

}


