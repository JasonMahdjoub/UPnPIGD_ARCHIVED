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

package com.distrimind.upnp_igd.model.message.header;

import com.distrimind.upnp_igd.model.types.UnsignedIntegerFourBytes;

/**
 * @author Christian Bauer
 */
public class EventSequenceHeader extends UpnpHeader<UnsignedIntegerFourBytes> {

    public EventSequenceHeader() {
    }

    public EventSequenceHeader(long value) {
        setValue(new UnsignedIntegerFourBytes(value));
    }

    @Override
	public void setString(String _s) throws InvalidHeaderException {

        // Cut off leading zeros
        String s=_s;
        if (!"0".equals(s)) {
            while(s.startsWith("0")) {
                s = s.substring(1);
            }
        }

        try {
            setValue(new UnsignedIntegerFourBytes(s));
        } catch (NumberFormatException ex) {
            throw new InvalidHeaderException("Invalid event sequence, " + ex.getMessage());
        }

    }

    @Override
	public String getString() {
        return getValue().toString();
    }
}
