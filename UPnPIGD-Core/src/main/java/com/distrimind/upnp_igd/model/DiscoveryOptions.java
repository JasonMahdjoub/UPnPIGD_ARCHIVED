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

package com.distrimind.upnp_igd.model;

import com.distrimind.upnp_igd.registry.Registry;

/**
 * Options for discovery processing by the {@link Registry}.
 *
 * @author Christian Bauer
 */
public class DiscoveryOptions {

    protected boolean advertised;
    protected boolean byeByeBeforeFirstAlive;

    /**
     * @param advertised If <code>false</code>, no alive notifications will be announced for
     *                   this device, and it will not appear in search responses.
     */
    public DiscoveryOptions(boolean advertised) {
        this.advertised = advertised;
    }

    /**
     *
     * @param advertised If <code>false</code>, no alive notifications will be announced for
     *                   this device, and it will not appear in search responses.
     * @param byeByeBeforeFirstAlive If <code>true</code>, a byebye NOTIFY message will be sent before the
     *                               first alive NOTIFY message.
     */
    public DiscoveryOptions(boolean advertised, boolean byeByeBeforeFirstAlive) {
        this.advertised = advertised;
        this.byeByeBeforeFirstAlive = byeByeBeforeFirstAlive;
    }

    /**
     * @return true for regular advertisement with alive
     *         messages and in search responses.
     */
    public boolean isAdvertised() {
        return advertised;
    }

    /**
     * @return true if a byebye NOTIFY message will be sent before the
     *         first alive NOTIFY message.
     */
    public boolean isByeByeBeforeFirstAlive() {
        return byeByeBeforeFirstAlive;
    }

    // Performance optimization on Android
    private static final String simpleName = DiscoveryOptions.class.getSimpleName();
	@Override
    public String toString() {
        return "(" + simpleName + ")" + " advertised: " + isAdvertised() + " byebyeBeforeFirstAlive: " + isByeByeBeforeFirstAlive();
    }
}
