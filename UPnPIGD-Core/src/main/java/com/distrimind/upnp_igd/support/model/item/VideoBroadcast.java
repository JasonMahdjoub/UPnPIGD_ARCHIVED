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

package com.distrimind.upnp_igd.support.model.item;

import com.distrimind.upnp_igd.support.model.DIDLObject;
import com.distrimind.upnp_igd.support.model.Res;
import com.distrimind.upnp_igd.support.model.container.Container;

import java.net.URI;

/**
 * @author Christian Bauer
 */
public class VideoBroadcast extends VideoItem {

    public static final Class CLASS = new Class("object.item.videoItem.videoBroadcast");

    public VideoBroadcast() {
        setClazz(CLASS);
    }

    public VideoBroadcast(Item other) {
        super(other);
    }

    public VideoBroadcast(String id, Container parent, String title, String creator, Res... resource) {
        this(id, parent.getId(), title, creator, resource);
    }

    public VideoBroadcast(String id, String parentID, String title, String creator, Res... resource) {
        super(id, parentID, title, creator, resource);
        setClazz(CLASS);
    }

    public URI getIcon() {
        return getFirstPropertyValue(DIDLObject.Property.UPNP.ICON.class);
    }

    public VideoBroadcast setIcon(URI icon) {
        replaceFirstProperty(new DIDLObject.Property.UPNP.ICON(icon));
        return this;
    }

    public String getRegion() {
        return getFirstPropertyValue(DIDLObject.Property.UPNP.REGION.class);
    }

    public VideoBroadcast setRegion(String region) {
        replaceFirstProperty(new DIDLObject.Property.UPNP.REGION(region));
        return this;
    }

    public Integer getChannelNr() {
        return getFirstPropertyValue(DIDLObject.Property.UPNP.CHANNEL_NR.class);
    }

    public VideoBroadcast setChannelNr(Integer channelNr) {
        replaceFirstProperty(new DIDLObject.Property.UPNP.CHANNEL_NR(channelNr));
        return this;
    }
}
