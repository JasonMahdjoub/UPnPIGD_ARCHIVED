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

package com.distrimind.upnp_igd.support.avtransport.impl.state;

import com.distrimind.upnp_igd.support.avtransport.lastchange.AVTransportVariable;
import com.distrimind.upnp_igd.support.model.AVTransport;
import com.distrimind.upnp_igd.support.model.SeekMode;
import com.distrimind.upnp_igd.support.model.TransportAction;
import com.distrimind.upnp_igd.support.model.TransportInfo;
import com.distrimind.upnp_igd.support.model.TransportState;

import java.net.URI;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author Christian Bauer
 */
public abstract class Playing<T extends AVTransport> extends AbstractState<T> {

    final private static Logger log = Logger.getLogger(Playing.class.getName());

    public Playing(T transport) {
        super(transport);
    }

    public void onEntry() {
        log.fine("Setting transport state to PLAYING");
        getTransport().setTransportInfo(
                new TransportInfo(
                        TransportState.PLAYING,
                        getTransport().getTransportInfo().getCurrentTransportStatus(),
                        getTransport().getTransportInfo().getCurrentSpeed()
                )
        );
        getTransport().getLastChange().setEventedValue(
                getTransport().getInstanceId(),
                new AVTransportVariable.TransportState(TransportState.PLAYING),
                new AVTransportVariable.CurrentTransportActions(getCurrentTransportActions())
        );
    }

    public abstract Class<? extends AbstractState<?>> setTransportURI(URI uri, String metaData);
    public abstract Class<? extends AbstractState<?>> stop();
    public abstract Class<? extends AbstractState<?>> play(String speed);
    public abstract Class<? extends AbstractState<?>> pause();
    public abstract Class<? extends AbstractState<?>> next();
    public abstract Class<? extends AbstractState<?>> previous();
    public abstract Class<? extends AbstractState<?>> seek(SeekMode unit, String target);

    @Override
	public List<TransportAction> getCurrentTransportActions() {
        return List.of(
                TransportAction.Stop,
                TransportAction.Play,
                TransportAction.Pause,
                TransportAction.Next,
                TransportAction.Previous,
                TransportAction.Seek
        );
    }
}
