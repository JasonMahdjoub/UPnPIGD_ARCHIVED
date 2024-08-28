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

package com.distrimind.upnp_igd.support.model;

import com.distrimind.upnp_igd.model.ModelUtil;
import com.distrimind.upnp_igd.model.action.ActionArgumentValue;

import java.util.List;
import java.util.Map;

/**
 * @author Christian Bauer
 */
public class DeviceCapabilities {

    private final List<StorageMedium> playMedia;
    private List<StorageMedium> recMedia = List.of(StorageMedium.NOT_IMPLEMENTED);
    private List<RecordQualityMode> recQualityModes = List.of(RecordQualityMode.NOT_IMPLEMENTED);

    public DeviceCapabilities(Map<String, ? extends ActionArgumentValue<?>> args) {
        this(
                StorageMedium.valueOfCommaSeparatedList((String) args.get("PlayMedia").getValue()),
                StorageMedium.valueOfCommaSeparatedList((String) args.get("RecMedia").getValue()),
                RecordQualityMode.valueOfCommaSeparatedList((String) args.get("RecQualityModes").getValue())
        );
    }

    public DeviceCapabilities(List<StorageMedium> playMedia) {
        this.playMedia = playMedia;
    }

    public DeviceCapabilities(List<StorageMedium> playMedia, List<StorageMedium> recMedia, List<RecordQualityMode> recQualityModes) {
        this.playMedia = playMedia;
        this.recMedia = recMedia;
        this.recQualityModes = recQualityModes;
    }

    public List<StorageMedium> getPlayMedia() {
        return playMedia;
    }

    public List<StorageMedium> getRecMedia() {
        return recMedia;
    }

    public List<RecordQualityMode> getRecQualityModes() {
        return recQualityModes;
    }

    public String getPlayMediaString() {
        return ModelUtil.toCommaSeparatedList(playMedia);
    }

    public String getRecMediaString() {
        return ModelUtil.toCommaSeparatedList(recMedia);
    }

    public String getRecQualityModesString() {
        return ModelUtil.toCommaSeparatedList(recQualityModes);
    }
}
