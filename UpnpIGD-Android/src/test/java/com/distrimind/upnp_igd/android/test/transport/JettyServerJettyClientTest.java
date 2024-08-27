/*
 * Copyright (C) 2011-2024 4th Line GmbH, Switzerland and others
 *
 * The contents of this file are subject to the terms of the
 * Common Development and Distribution License Version 1 or later
 * ("CDDL") (collectively, the "License"). You may not use this file
 * except in compliance with the License. See LICENSE.txt for more
 * information.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * SPDX-License-Identifier: CDDL-1.0
 */
package com.distrimind.upnp_igd.android.test.transport;

import com.distrimind.upnp_igd.UpnpServiceConfiguration;
import com.distrimind.upnp_igd.android.transport.JettyTransportConfiguration;
import com.distrimind.upnp_igd.android.transport.impl.AsyncServletStreamServerConfigurationImpl;
import com.distrimind.upnp_igd.android.transport.impl.jetty.StreamClientConfigurationImpl;
import com.distrimind.upnp_igd.transport.TransportConfiguration;
import com.distrimind.upnp_igd.transport.spi.StreamClient;
import com.distrimind.upnp_igd.transport.spi.StreamClientConfiguration;
import com.distrimind.upnp_igd.transport.spi.StreamServer;
import org.testng.annotations.BeforeClass;

/**
 * @author Christian Bauer - initial contribution
 * @author Victor Toni - adapted to JUPnP
 */
class JettyServerJettyClientTest extends StreamServerClientTest {

    private static final TransportConfiguration<StreamClientConfigurationImpl, AsyncServletStreamServerConfigurationImpl> jettyTransportConfiguration = JettyTransportConfiguration.INSTANCE;
    private static final StreamClientConfiguration sccConfiguration = new StreamClientConfigurationImpl(null, 3, 0, 0,
            0);

    @BeforeClass
    @Override
    public void start() throws Exception {
        start(this::createStreamServer, this::createStreamClient);
    }

    @Override
    public StreamServer<?> createStreamServer(final int port) {
        return jettyTransportConfiguration.createStreamServer(port);
    }


    @Override
    public StreamClient<?> createStreamClient(UpnpServiceConfiguration configuration) {
        return jettyTransportConfiguration.createStreamClient(configuration.getSyncProtocolExecutorService(),
                sccConfiguration);
    }
}

