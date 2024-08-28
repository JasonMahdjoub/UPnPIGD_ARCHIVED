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

import com.distrimind.upnp_igd.model.message.IncomingDatagramMessage;
import com.distrimind.upnp_igd.transport.Router;
import com.distrimind.upnp_igd.model.message.OutgoingDatagramMessage;

import java.net.InetAddress;
import java.net.DatagramPacket;

/**
 * Service for receiving (unicast only) and sending UDP datagrams, one per bound IP address.
 * <p>
 * This service typically listens on a socket for UDP unicast datagrams, with
 * an ephemeral port.
 * </p>
 * <p>
 * This listening loop is started with the <code>run()</code> method,
 * this service is <code>Runnable</code>. Any received datagram is then converted into an
 * {@link IncomingDatagramMessage} and
 * handled by the
 * {@link Router#received(IncomingDatagramMessage)}
 * method. This conversion is the job of the {@link DatagramProcessor}.
 * </p>
 * <p>
 * Clients of this service use it to send UDP datagrams, either to a unicast
 * or multicast destination. Any {@link OutgoingDatagramMessage} can
 * be converted and written into a datagram with the {@link DatagramProcessor}.
 * </p>
 * <p>
 * An implementation has to be thread-safe.
 * </p>
 *
 * @param <C> The type of the service's configuration.
 *
 * @author Christian Bauer
 */
public interface DatagramIO<C extends DatagramIOConfiguration> extends Runnable {

    /**
     * Configures the service and starts any listening sockets.
     *
     * @param bindAddress The address to bind any sockets on.
     * @param router The router which handles received {@link IncomingDatagramMessage}s.
     * @param datagramProcessor Reads and writes datagrams.
     * @throws InitializationException If the service could not be initialized or started.
     */
	void init(NetworkAddressFactory networkAddressFactory, InetAddress bindAddress, Router router, DatagramProcessor datagramProcessor) throws InitializationException;

    /**
     * Stops the service, closes any listening sockets.
     */
	void stop();

    /**
     * @return This service's configuration.
     */
	C getConfiguration();

    /**
     * Sends a datagram after conversion with {@link DatagramProcessor#write(OutgoingDatagramMessage)}.
     *
     * @param message The message to send.
     */
	void send(OutgoingDatagramMessage<?> message);

    /**
     * The actual sending of a UDP datagram.
     * <p>
     * Recoverable errors should be logged, if appropriate only with debug level. Any
     * non-recoverable errors should be thrown as <code>RuntimeException</code>s.
   
     *
     * @param datagram The UDP datagram to send.
     */
	void send(DatagramPacket datagram);
}
