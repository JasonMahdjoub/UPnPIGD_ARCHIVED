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

package com.distrimind.upnp_igd.binding.staging;

import com.distrimind.upnp_igd.model.meta.ActionArgument;
import com.distrimind.upnp_igd.model.meta.Service;

/**
 * @author Christian Bauer
 */
public class MutableActionArgument<S extends Service<?, ?, ?>> {

    public String name;
    public String relatedStateVariable;
    public ActionArgument.Direction direction;
    public boolean retval;

    public ActionArgument<S> build() {
        return new ActionArgument<>(name, relatedStateVariable, direction, retval);
    }

}
