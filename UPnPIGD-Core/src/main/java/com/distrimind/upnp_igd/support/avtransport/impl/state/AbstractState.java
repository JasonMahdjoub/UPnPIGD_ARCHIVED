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

import com.distrimind.upnp_igd.support.model.AVTransport;
import com.distrimind.upnp_igd.support.model.TransportAction;

import java.util.List;

/**
 *
 */
public abstract class AbstractState<T extends AVTransport> {

    private final T transport;

    public AbstractState(T transport) {
        this.transport = transport;
    }

    public T getTransport() {
        return transport;
    }

    public abstract List<TransportAction> getCurrentTransportActions();

}
