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
package com.distrimind.upnp_igd.android.test.transport;

import com.distrimind.upnp_igd.UpnpServiceConfiguration;
import com.distrimind.upnp_igd.android.transport.impl.AsyncServletStreamServerConfigurationImpl;
import com.distrimind.upnp_igd.android.transport.impl.AsyncServletStreamServerImpl;
import com.distrimind.upnp_igd.android.transport.impl.jetty.JettyServletContainer;
import com.distrimind.upnp_igd.android.transport.impl.jetty.StreamClientConfigurationImpl;
import com.distrimind.upnp_igd.android.transport.impl.jetty.JettyStreamClientImpl;
import com.distrimind.upnp_igd.transport.spi.StreamClient;
import com.distrimind.upnp_igd.transport.spi.StreamServer;
import jakarta.servlet.http.HttpServletRequest;


/**
 * @author Christian Bauer
 */
public class JettyServerJettyClientTest extends StreamServerClientTest {

    @Override
    public StreamServer<?> createStreamServer(int port) {
        AsyncServletStreamServerConfigurationImpl configuration =
            new AsyncServletStreamServerConfigurationImpl(
                JettyServletContainer.INSTANCE,
                port
            );

        return new AsyncServletStreamServerImpl(
            configuration
        ) {
            @Override
            protected boolean isConnectionOpen(HttpServletRequest request) {
                return JettyServletContainer.isConnectionOpen(request);
            }
        };
    }

    @Override
    public StreamClient<?> createStreamClient(UpnpServiceConfiguration configuration) {
        return new JettyStreamClientImpl(
            new StreamClientConfigurationImpl(
                configuration.getSyncProtocolExecutorService(),
                3
            )
        );
    }
}
