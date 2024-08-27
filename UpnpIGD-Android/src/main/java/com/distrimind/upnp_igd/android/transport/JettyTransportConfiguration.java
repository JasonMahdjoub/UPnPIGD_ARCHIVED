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
package com.distrimind.upnp_igd.android.transport;

import java.util.concurrent.ExecutorService;

import com.distrimind.upnp_igd.android.transport.impl.AsyncServletStreamServerConfigurationImpl;
import com.distrimind.upnp_igd.android.transport.impl.AsyncServletStreamServerImpl;
import com.distrimind.upnp_igd.android.transport.impl.jetty.JettyServletContainer;
import com.distrimind.upnp_igd.android.transport.impl.jetty.JettyStreamClientImpl;
import com.distrimind.upnp_igd.android.transport.impl.jetty.StreamClientConfigurationImpl;
import com.distrimind.upnp_igd.transport.TransportConfiguration;
import com.distrimind.upnp_igd.transport.spi.StreamClient;
import com.distrimind.upnp_igd.transport.spi.StreamClientConfiguration;
import com.distrimind.upnp_igd.transport.spi.StreamServer;


/**
 * Implementation of {@link com.distrimind.upnp_igd.transport.TransportConfiguration} for Jetty HTTP components.
 *
 * @author Victor Toni - initial contribution
 */
public class JettyTransportConfiguration implements TransportConfiguration<StreamClientConfigurationImpl, AsyncServletStreamServerConfigurationImpl> {

	public static final TransportConfiguration<StreamClientConfigurationImpl, AsyncServletStreamServerConfigurationImpl> INSTANCE = new JettyTransportConfiguration();

	@Override
	public StreamClient<StreamClientConfigurationImpl> createStreamClient(final ExecutorService executorService,
										   final StreamClientConfiguration configuration) {
		StreamClientConfigurationImpl clientConfiguration = new StreamClientConfigurationImpl(executorService,
				configuration.getTimeoutSeconds(), configuration.getLogWarningSeconds(),
				configuration.getRetryAfterSeconds(), configuration.getRetryIterations());

		return new JettyStreamClientImpl(clientConfiguration);
	}

	@Override
	public StreamServer<AsyncServletStreamServerConfigurationImpl> createStreamServer(final int listenerPort) {
		return new AsyncServletStreamServerImpl(
				new AsyncServletStreamServerConfigurationImpl(JettyServletContainer.INSTANCE, listenerPort));
	}
}
