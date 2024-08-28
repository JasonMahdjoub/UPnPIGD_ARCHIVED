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
package com.distrimind.upnp_igd.support.model.dlna.message.header;

import java.util.ArrayList;
import java.util.List;
import com.distrimind.upnp_igd.model.message.header.InvalidHeaderException;
import com.distrimind.upnp_igd.model.types.PragmaType;

/**
 * DLNA Pragma tokens:
 *  - getIfoFileURI.dlna.org
 *  - ifoFileURI.dlna.org
 * 
 * @author Mario Franco
 */
public class PragmaHeader extends DLNAHeader<List<PragmaType>> {
    
    public PragmaHeader() {
        setValue(new ArrayList<>());
    }
    
    @Override
    public void setString(String _s) throws InvalidHeaderException {
        String s=_s;
        if (!s.isEmpty()) {
            if (s.endsWith(";")) {
                s = s.substring(0, s.length() - 1);
            }
            String[] list = s.split("\\s*;\\s*");
            List<PragmaType> value = new ArrayList<>();
            for (String pragma : list) {
                value.add(PragmaType.valueOf(pragma));
            }
            setValue(value);
            return;
        }
        throw new InvalidHeaderException("Invalid Pragma header value: " + s);
    }
    
    @Override
    public String getString() {
        List<PragmaType> v = getValue();
        StringBuilder r = new StringBuilder();
        for (PragmaType pragma : v) {
            r.append((r.length() == 0) ? "" : ",").append(pragma.getString());
        }
        return r.toString();
    }
}
