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

package com.distrimind.upnp_igd.model.types;

import com.distrimind.upnp_igd.model.ModelUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An arbitrary list of comma-separated elements, representing DLNA capabilities (whatever that is).
 *
 * @author Christian Bauer
 */
public class DLNACaps {

    final List<String> caps;

    public DLNACaps(List<String> caps) {
        this.caps = caps;
    }

    public List<String> getCaps() {
        return caps;
    }

    static public DLNACaps valueOf(String s) throws InvalidValueException {
        if (s == null || s.isEmpty()) return new DLNACaps(Collections.emptyList());
        String[] caps = s.split(",");
        List<String> trimmed = new ArrayList<>(caps.length);
		for (String cap : caps) {
			trimmed.add(cap.trim());
		}
        return new DLNACaps(trimmed);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DLNACaps dlnaCaps = (DLNACaps) o;

		return caps.equals(dlnaCaps.caps);
	}

    @Override
    public int hashCode() {
        return caps.size();
    }

    @Override
    public String toString() {
        return ModelUtil.toCommaSeparatedList(getCaps());
    }
}
