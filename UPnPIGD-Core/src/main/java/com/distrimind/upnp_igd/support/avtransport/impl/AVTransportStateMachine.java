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

package com.distrimind.upnp_igd.support.avtransport.impl;

import com.distrimind.upnp_igd.support.avtransport.impl.state.AbstractState;
import com.distrimind.upnp_igd.support.model.SeekMode;
import com.distrimind.upnp_igd.statemachine.StateMachine;

import java.net.URI;

public interface AVTransportStateMachine extends StateMachine<AbstractState<?>> {

    void setTransportURI(URI uri, String uriMetaData);
    void setNextTransportURI(URI uri, String uriMetaData);
    void stop();
    void play(String speed);
    void pause();
    void record();
    void seek(SeekMode unit, String target);
    void next();
    void previous();

}
