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
import com.distrimind.upnp_igd.model.types.InvalidValueException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Christian Bauer
 */
public class ProtocolInfos extends ArrayList<ProtocolInfo> {
    private static final long serialVersionUID = 1L;

    public ProtocolInfos(ProtocolInfo... info) {
		this.addAll(Arrays.asList(info));
    }

    public ProtocolInfos(String s) throws InvalidValueException {
        String[] infos = ModelUtil.fromCommaSeparatedList(s);
        if (infos != null)
            for (String info : infos)
                add(new ProtocolInfo(info));
    }

    @Override
    public String toString() {
        return ModelUtil.toCommaSeparatedList(List.of(new ProtocolInfo[size()]));
    }
}
