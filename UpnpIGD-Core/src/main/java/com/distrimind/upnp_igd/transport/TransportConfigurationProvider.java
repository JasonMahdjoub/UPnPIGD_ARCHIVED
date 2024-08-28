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
package com.distrimind.upnp_igd.transport;

import com.distrimind.upnp_igd.transport.spi.StreamClientConfiguration;
import com.distrimind.upnp_igd.transport.spi.StreamServerConfiguration;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * This is the central place to switch between transport implementations.
 *
 * @author Victor Toni - inital contribution
 *
 */
public final class TransportConfigurationProvider {
	private static final Constructor<TransportConfiguration<?, ?>> constructor;

	static {
		try {
			//noinspection unchecked
			constructor = ((Class<TransportConfiguration<?, ?>>)Class.forName("com.distrimind.upnp_igd.android.transport.JettyTransportConfiguration")).getConstructor();
		} catch (NoSuchMethodException | ClassNotFoundException e) {
			throw new RuntimeException("Please add dependency com.distrimind.upnp_igd.android:UpnpIGD-Android", e);
		}
	}

	@SuppressWarnings("unchecked")
	public static <SCC extends StreamClientConfiguration, SSC extends StreamServerConfiguration> TransportConfiguration<SCC, SSC> getDefaultTransportConfiguration() {
		try {
			return (TransportConfiguration<SCC, SSC>)constructor.newInstance();
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}
}