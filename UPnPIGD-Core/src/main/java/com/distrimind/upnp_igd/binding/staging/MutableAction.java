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

import com.distrimind.upnp_igd.model.meta.Action;
import com.distrimind.upnp_igd.model.meta.ActionArgument;
import com.distrimind.upnp_igd.model.meta.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Christian Bauer
 */
public class MutableAction<S extends Service<?, ?, ?>> {

    public String name;
    public List<MutableActionArgument<S>> arguments = new ArrayList<>();

    public Action<S> build() {
        return new Action<>(name, createActionArgumennts());
    }

    public List<ActionArgument<S>> createActionArgumennts() {
        List<ActionArgument<S>> array = new ArrayList<>(arguments.size());
        for (MutableActionArgument<S> argument : arguments) {
            array.add(argument.build());
        }
        return array;
    }

}
