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

package com.distrimind.upnp_igd.support.shared;

import com.distrimind.upnp_igd.swing.logging.LogCategory;

import java.util.ArrayList;
import java.util.logging.Level;

/**
 * @author Christian Bauer
 */
public class CoreLogCategories extends ArrayList<LogCategory> {
    private static final long serialVersionUID = 1L;

    public CoreLogCategories() {
        super(10);

        add(new LogCategory("Network", new LogCategory.Group[]{

                new LogCategory.Group(
                        "UDP communication",
                        new LogCategory.LoggerLevel[]{
                                new LogCategory.LoggerLevel(com.distrimind.upnp_igd.transport.spi.DatagramIO.class.getName(), Level.FINE),
                                new LogCategory.LoggerLevel(com.distrimind.upnp_igd.transport.spi.MulticastReceiver.class.getName(), Level.FINE),
                        }
                ),

                new LogCategory.Group(
                        "UDP datagram processing and content",
                        new LogCategory.LoggerLevel[]{
                                new LogCategory.LoggerLevel(com.distrimind.upnp_igd.transport.spi.DatagramProcessor.class.getName(), Level.FINER)
                        }
                ),

                new LogCategory.Group(
                        "TCP communication",
                        new LogCategory.LoggerLevel[]{
                                new LogCategory.LoggerLevel(com.distrimind.upnp_igd.transport.spi.UpnpStream.class.getName(), Level.FINER),
                                new LogCategory.LoggerLevel(com.distrimind.upnp_igd.transport.spi.StreamServer.class.getName(), Level.FINE),
                                new LogCategory.LoggerLevel(com.distrimind.upnp_igd.transport.spi.StreamClient.class.getName(), Level.FINE),
                        }
                ),

                new LogCategory.Group(
                        "SOAP action message processing and content",
                        new LogCategory.LoggerLevel[]{
                                new LogCategory.LoggerLevel(com.distrimind.upnp_igd.transport.spi.SOAPActionProcessor.class.getName(), Level.FINER)
                        }
                ),

                new LogCategory.Group(
                        "GENA event message processing and content",
                        new LogCategory.LoggerLevel[]{
                                new LogCategory.LoggerLevel(com.distrimind.upnp_igd.transport.spi.GENAEventProcessor.class.getName(), Level.FINER)
                        }
                ),

                new LogCategory.Group(
                        "HTTP header processing",
                        new LogCategory.LoggerLevel[]{
                                new LogCategory.LoggerLevel(com.distrimind.upnp_igd.model.message.UpnpHeaders.class.getName(), Level.FINER)
                        }
                ),
        }));


        add(new LogCategory("UPnP Protocol", new LogCategory.Group[]{

                new LogCategory.Group(
                        "Discovery (Notification & Search)",
                        new LogCategory.LoggerLevel[]{
                                new LogCategory.LoggerLevel(com.distrimind.upnp_igd.protocol.ProtocolFactory.class.getName(), Level.FINER),
                                new LogCategory.LoggerLevel("com.distrimind.upnp_igd.protocol.async", Level.FINER)
                        }
                ),

                new LogCategory.Group(
                        "Description",
                        new LogCategory.LoggerLevel[]{
                                new LogCategory.LoggerLevel(com.distrimind.upnp_igd.protocol.ProtocolFactory.class.getName(), Level.FINER),
                                new LogCategory.LoggerLevel(com.distrimind.upnp_igd.protocol.RetrieveRemoteDescriptors.class.getName(), Level.FINE),
                                new LogCategory.LoggerLevel(com.distrimind.upnp_igd.protocol.sync.ReceivingRetrieval.class.getName(), Level.FINE),
                                new LogCategory.LoggerLevel(com.distrimind.upnp_igd.binding.xml.DeviceDescriptorBinder.class.getName(), Level.FINE),
                                new LogCategory.LoggerLevel(com.distrimind.upnp_igd.binding.xml.ServiceDescriptorBinder.class.getName(), Level.FINE),
                        }
                ),

                new LogCategory.Group(
                        "Control",
                        new LogCategory.LoggerLevel[]{
                                new LogCategory.LoggerLevel(com.distrimind.upnp_igd.protocol.ProtocolFactory.class.getName(), Level.FINER),
                                new LogCategory.LoggerLevel(com.distrimind.upnp_igd.protocol.sync.ReceivingAction.class.getName(), Level.FINER),
                                new LogCategory.LoggerLevel(com.distrimind.upnp_igd.protocol.sync.SendingAction.class.getName(), Level.FINER),
                        }
                ),

                new LogCategory.Group(
                        "GENA ",
                        new LogCategory.LoggerLevel[]{
                                new LogCategory.LoggerLevel("com.distrimind.upnp_igd.model.gena", Level.FINER),
                                new LogCategory.LoggerLevel(com.distrimind.upnp_igd.protocol.ProtocolFactory.class.getName(), Level.FINER),
                                new LogCategory.LoggerLevel(com.distrimind.upnp_igd.protocol.sync.ReceivingEvent.class.getName(), Level.FINER),
                                new LogCategory.LoggerLevel(com.distrimind.upnp_igd.protocol.sync.ReceivingSubscribe.class.getName(), Level.FINER),
                                new LogCategory.LoggerLevel(com.distrimind.upnp_igd.protocol.sync.ReceivingUnsubscribe.class.getName(), Level.FINER),
                                new LogCategory.LoggerLevel(com.distrimind.upnp_igd.protocol.sync.SendingEvent.class.getName(), Level.FINER),
                                new LogCategory.LoggerLevel(com.distrimind.upnp_igd.protocol.sync.SendingSubscribe.class.getName(), Level.FINER),
                                new LogCategory.LoggerLevel(com.distrimind.upnp_igd.protocol.sync.SendingUnsubscribe.class.getName(), Level.FINER),
                                new LogCategory.LoggerLevel(com.distrimind.upnp_igd.protocol.sync.SendingRenewal.class.getName(), Level.FINER),
                        }
                ),
        }));

        add(new LogCategory("Core", new LogCategory.Group[]{

                new LogCategory.Group(
                        "Router",
                        new LogCategory.LoggerLevel[]{
                                new LogCategory.LoggerLevel(com.distrimind.upnp_igd.transport.Router.class.getName(), Level.FINER)
                        }
                ),

                new LogCategory.Group(
                        "Registry",
                        new LogCategory.LoggerLevel[]{
                                new LogCategory.LoggerLevel(com.distrimind.upnp_igd.registry.Registry.class.getName(), Level.FINER),
                        }
                ),

                new LogCategory.Group(
                        "Local service binding & invocation",
                        new LogCategory.LoggerLevel[]{
                                new LogCategory.LoggerLevel("com.distrimind.upnp_igd.binding.annotations", Level.FINER),
                                new LogCategory.LoggerLevel(com.distrimind.upnp_igd.model.meta.LocalService.class.getName(), Level.FINER),
                                new LogCategory.LoggerLevel("com.distrimind.upnp_igd.model.action", Level.FINER),
                                new LogCategory.LoggerLevel("com.distrimind.upnp_igd.model.state", Level.FINER),
                                new LogCategory.LoggerLevel(com.distrimind.upnp_igd.model.DefaultServiceManager.class.getName(), Level.FINER)
                        }
                ),

                new LogCategory.Group(
                        "Control Point interaction",
                        new LogCategory.LoggerLevel[]{
                                new LogCategory.LoggerLevel("com.distrimind.upnp_igd.controlpoint", Level.FINER),
                        }
                ),
        }));

    }

}
