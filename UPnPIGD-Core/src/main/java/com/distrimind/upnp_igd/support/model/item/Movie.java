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

import com.distrimind.upnp_igd.support.model.Res;
import com.distrimind.upnp_igd.support.model.StorageMedium;
import com.distrimind.upnp_igd.support.model.container.Container;

import java.util.List;

/**
 * @author Christian Bauer
 */
public class Movie extends VideoItem {

    public static final Class CLASS = new Class("object.item.videoItem.movie");

    public Movie() {
        setClazz(CLASS);
    }

    public Movie(Item other) {
        super(other);
    }

    public Movie(String id, Container parent, String title, String creator, Res... resource) {
        this(id, parent.getId(), title, creator, resource);
    }

    public Movie(String id, String parentID, String title, String creator, Res... resource) {
        super(id, parentID, title, creator, resource);
        setClazz(CLASS);
    }

    public StorageMedium getStorageMedium() {
        return getFirstPropertyValue(Property.UPNP.STORAGE_MEDIUM.class);
    }

    public Movie setStorageMedium(StorageMedium storageMedium) {
        replaceFirstProperty(new Property.UPNP.STORAGE_MEDIUM(storageMedium));
        return this;
    }

    public Integer getDVDRegionCode() {
        return getFirstPropertyValue(Property.UPNP.DVD_REGION_CODE.class);
    }

    public Movie setDVDRegionCode(Integer DVDRegionCode) {
        replaceFirstProperty(new Property.UPNP.DVD_REGION_CODE(DVDRegionCode));
        return this;
    }

    public String getChannelName() {
        return getFirstPropertyValue(Property.UPNP.CHANNEL_NAME.class);
    }

    public Movie setChannelName(String channelName) {
        replaceFirstProperty(new Property.UPNP.CHANNEL_NAME(channelName));
        return this;
    }

    public String getFirstScheduledStartTime() {
        return getFirstPropertyValue(Property.UPNP.SCHEDULED_START_TIME.class);
    }

    public List<String> getScheduledStartTimes() {
        return getPropertyValues(Property.UPNP.SCHEDULED_START_TIME.class);
    }

    public Movie setScheduledStartTimes(String[] strings) {
        removeProperties(Property.UPNP.SCHEDULED_START_TIME.class);
        for (String s : strings) {
            addProperty(new Property.UPNP.SCHEDULED_START_TIME(s));
        }
        return this;
    }

    public String getFirstScheduledEndTime() {
        return getFirstPropertyValue(Property.UPNP.SCHEDULED_END_TIME.class);
    }

    public List<String> getScheduledEndTimes() {
        return getPropertyValues(Property.UPNP.SCHEDULED_END_TIME.class);
    }

    public Movie setScheduledEndTimes(String[] strings) {
        removeProperties(Property.UPNP.SCHEDULED_END_TIME.class);
        for (String s : strings) {
            addProperty(new Property.UPNP.SCHEDULED_END_TIME(s));
        }
        return this;
    }

}
