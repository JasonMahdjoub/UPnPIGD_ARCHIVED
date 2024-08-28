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

module com.distrimind.upnp_igd {


	requires java.logging;
	requires jdk.httpserver;
	requires org.jsoup;
	requires static java.desktop;
	requires static jakarta.cdi;


	exports com.distrimind.upnp_igd;
	exports com.distrimind.upnp_igd.binding;
	exports com.distrimind.upnp_igd.binding.xml;
	exports com.distrimind.upnp_igd.binding.annotations;
	exports com.distrimind.upnp_igd.binding.staging;
	exports com.distrimind.upnp_igd.controlpoint;
	exports com.distrimind.upnp_igd.controlpoint.event;
	exports com.distrimind.upnp_igd.http;
	exports com.distrimind.upnp_igd.mock;
	exports com.distrimind.upnp_igd.model;
	exports com.distrimind.upnp_igd.model.action;
	exports com.distrimind.upnp_igd.model.gena;
	exports com.distrimind.upnp_igd.model.message;
	exports com.distrimind.upnp_igd.model.message.gena;
	exports com.distrimind.upnp_igd.model.message.header;
	exports com.distrimind.upnp_igd.model.message.control;
	exports com.distrimind.upnp_igd.model.message.discovery;
	exports com.distrimind.upnp_igd.model.meta;
	exports com.distrimind.upnp_igd.model.profile;
	exports com.distrimind.upnp_igd.model.resource;
	exports com.distrimind.upnp_igd.model.state;
	exports com.distrimind.upnp_igd.model.types;
	exports com.distrimind.upnp_igd.model.types.csv;
	exports com.distrimind.upnp_igd.protocol;
	exports com.distrimind.upnp_igd.protocol.async;
	exports com.distrimind.upnp_igd.protocol.sync;
	exports com.distrimind.upnp_igd.registry;
	exports com.distrimind.upnp_igd.registry.event;
	exports com.distrimind.upnp_igd.statemachine;
	exports com.distrimind.upnp_igd.support.model;
	exports com.distrimind.upnp_igd.support.avtransport;
	exports com.distrimind.upnp_igd.support.avtransport.lastchange;
	exports com.distrimind.upnp_igd.support.avtransport.impl;
	exports com.distrimind.upnp_igd.support.avtransport.impl.state;
	exports com.distrimind.upnp_igd.support.avtransport.callback;
	exports com.distrimind.upnp_igd.support.contentdirectory;
	exports com.distrimind.upnp_igd.support.contentdirectory.callback;
	exports com.distrimind.upnp_igd.support.contentdirectory.ui;
	exports com.distrimind.upnp_igd.support.igd;
	exports com.distrimind.upnp_igd.support.igd.callback;
	exports com.distrimind.upnp_igd.support.lastchange;
	exports com.distrimind.upnp_igd.support.model.container;
	exports com.distrimind.upnp_igd.support.model.dlna;
	exports com.distrimind.upnp_igd.support.model.item;
	exports com.distrimind.upnp_igd.support.shared;
	exports com.distrimind.upnp_igd.support.shared.log;
	exports com.distrimind.upnp_igd.swing;
	exports com.distrimind.upnp_igd.swing.logging;
	exports com.distrimind.upnp_igd.transport;
	exports com.distrimind.upnp_igd.transport.impl;
	exports com.distrimind.upnp_igd.transport.spi;
	exports com.distrimind.upnp_igd.util;
	exports com.distrimind.upnp_igd.util.io;
	exports com.distrimind.upnp_igd.util.logging;
	exports com.distrimind.upnp_igd.xml;
}

