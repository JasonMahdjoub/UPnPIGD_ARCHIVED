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
package com.distrimind.upnp_igd.support.model.dlna;

import com.distrimind.upnp_igd.model.types.InvalidValueException;
import com.distrimind.upnp_igd.support.avtransport.lastchange.AVTransportVariable.TransportPlaySpeed;

/**
 * @author Mario Franco
 */
public class DLNAPlaySpeedAttribute extends DLNAAttribute<TransportPlaySpeed[]> {
    
    public DLNAPlaySpeedAttribute() {
        setValue(new TransportPlaySpeed[] {});
    }

    public DLNAPlaySpeedAttribute(TransportPlaySpeed[] speeds) {
        setValue(speeds);
    }
    
    public DLNAPlaySpeedAttribute(String[] speeds) {
        TransportPlaySpeed[] sp = new TransportPlaySpeed[speeds.length]; 
        try {
            for (int i = 0; i < speeds.length; i++) {
                sp[i] = new TransportPlaySpeed(speeds[i]);
            }
        } catch (InvalidValueException invalidValueException) {
            throw new InvalidDLNAProtocolAttributeException("Can't parse DLNA play speeds.");
        }
        setValue(sp);
    }

    @Override
	public void setString(String s, String cf) throws InvalidDLNAProtocolAttributeException {
        TransportPlaySpeed[] value = null;
        if (s != null && !s.isEmpty()) {
            String[] speeds = s.split(",");
            try {
                value = new TransportPlaySpeed[speeds.length]; 
                for (int i = 0; i < speeds.length; i++) {
                    value[i] = new TransportPlaySpeed(speeds[i]);
                }
            } catch (InvalidValueException invalidValueException) {
                value = null;
            }
        }
        if (value == null) {
            throw new InvalidDLNAProtocolAttributeException("Can't parse DLNA play speeds from: " + s);
        }
        setValue(value);
    }

    @Override
	public String getString() {
        StringBuilder s = new StringBuilder();
        for (TransportPlaySpeed speed : getValue()) {
            if ("1".equals(speed.getValue()))
                continue;
            s.append(s.length() == 0 ? "" : ",").append(speed);
        }
        return s.toString();
    }
    
}
