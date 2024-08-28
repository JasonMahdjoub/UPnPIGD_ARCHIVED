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

package com.distrimind.upnp_igd.model.action;

import com.distrimind.upnp_igd.model.VariableValue;
import com.distrimind.upnp_igd.model.meta.Service;
import com.distrimind.upnp_igd.model.meta.ActionArgument;
import com.distrimind.upnp_igd.model.types.InvalidValueException;

/**
 * Represents the value of an action input or output argument.
 *
 * @author Christian Bauer
 */
public class ActionArgumentValue<S extends Service<?, ?, ?>> extends VariableValue {

    final private ActionArgument<S> argument;

    public ActionArgumentValue(ActionArgument<S> argument, Object value) throws InvalidValueException {
        super(argument.getDatatype(), value != null && value.getClass().isEnum() ? value.toString() : value);
        this.argument = argument;
    }

    public ActionArgument<S> getArgument() {
        return argument;
    }

}