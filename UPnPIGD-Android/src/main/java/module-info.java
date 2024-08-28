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

module com.distrimind.upnp_igd.android {

	requires static jakarta.annotation;
	requires static jakarta.cdi;
	requires static jakarta.inject;


	requires static org.eclipse.jetty.client;
	requires static org.eclipse.jetty.http;
	requires static org.eclipse.jetty.server;
	requires static org.eclipse.jetty.ee10.servlet;
	requires static org.eclipse.jetty.util;

	requires static android;

	requires com.distrimind.upnp_igd;
	requires java.logging;




	exports com.distrimind.upnp_igd.android;
	exports com.distrimind.upnp_igd.android.transport.spi;
	exports com.distrimind.upnp_igd.android.transport.impl;
}

