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

package com.distrimind.upnp_igd.model.meta;

import com.distrimind.upnp_igd.model.ModelUtil;
import com.distrimind.upnp_igd.model.Validatable;
import com.distrimind.upnp_igd.model.ValidationError;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Describes an action and its input/output arguments.
 *
 * @author Christian Bauer
 */
public class Action<S extends Service<?, ?, ?>> implements Validatable {

    final private static Logger log = Logger.getLogger(Action.class.getName());

    final private String name;
    final private List<ActionArgument<S>> arguments;
    final private List<ActionArgument<S>> inputArguments;
    final private List<ActionArgument<S>> outputArguments;

    // Package mutable state
    private S service;

    public Action(String name, Collection<ActionArgument<S>> arguments) {
        this.name = name;
        if (arguments != null) {

            List<ActionArgument<S>> inputList= new ArrayList<>();
            List<ActionArgument<S>> outputList = new ArrayList<>();

            for (ActionArgument<S> argument : arguments) {
                argument.setAction(this);
                if (argument.getDirection().equals(ActionArgument.Direction.IN))
                    inputList.add(argument);
                if (argument.getDirection().equals(ActionArgument.Direction.OUT))
                    outputList.add(argument);
            }

            this.arguments = new ArrayList<>(arguments);
            this.inputArguments = Collections.unmodifiableList(inputList);
            this.outputArguments = Collections.unmodifiableList(outputList);
        } else {
            this.arguments = Collections.emptyList();
            this.inputArguments = Collections.emptyList();
            this.outputArguments = Collections.emptyList();
        }
    }

    public String getName() {
        return name;
    }

    public boolean hasArguments() {
        return getArguments() != null && !getArguments().isEmpty();
    }

    public List<ActionArgument<S>> getArguments() {
        return arguments;
    }

    public S getService() {
        return service;
    }

    void setService(S service) {
        if (this.service != null)
            throw new IllegalStateException("Final value has been set already, model is immutable");
        this.service = service;
    }

    public ActionArgument<S> getFirstInputArgument() {
        if (!hasInputArguments()) throw new IllegalStateException("No input arguments: " + this);
        return getInputArguments().iterator().next();
    }

    public ActionArgument<S> getFirstOutputArgument() {
        if (!hasOutputArguments()) throw new IllegalStateException("No output arguments: " + this);
        return getOutputArguments().iterator().next();
    }

    public List<ActionArgument<S>> getInputArguments() {
        return inputArguments;
    }

    public ActionArgument<S> getInputArgument(String name) {
        for (ActionArgument<S> arg : getInputArguments()) {
            if (arg.isNameOrAlias(name)) return arg;
        }
        return null;
    }

    public List<ActionArgument<S>> getOutputArguments() {
        return outputArguments;
    }

    public ActionArgument<S> getOutputArgument(String name) {
        for (ActionArgument<S> arg : getOutputArguments()) {
            if (arg.getName().equals(name)) return arg;
        }
        return null;
    }

    public boolean hasInputArguments() {
        return getInputArguments() != null && !getInputArguments().isEmpty();
    }

    public boolean hasOutputArguments() {
        return getOutputArguments() != null && !getOutputArguments().isEmpty();
    }


    @Override
    public String toString() {
        return "(" + getClass().getSimpleName() +
                ", Arguments: " + (getArguments() != null ? getArguments().size() : "NO ARGS") +
                ") " + getName();
    }

    @Override
	public List<ValidationError> validate() {
        List<ValidationError> errors = new ArrayList<>();

        if (getName() == null || getName().isEmpty()) {
            errors.add(new ValidationError(
                    getClass(),
                    "name",
                    "Action without name of: " + getService()
            ));
        } else if (!ModelUtil.isValidUDAName(getName())) {
            if (log.isLoggable(Level.WARNING)) {
                log.warning("UPnP specification violation of: " + getService().getDevice());
                log.warning("Invalid action name: " + this);
            }
        }

        for (ActionArgument<S> actionArgument : getArguments()) {
            // Check argument relatedStateVariable in service state table

            if (getService().getStateVariable(actionArgument.getRelatedStateVariableName()) == null) {
                errors.add(new ValidationError(
                        getClass(),
                        "arguments",
                        "Action argument references an unknown state variable: " + actionArgument.getRelatedStateVariableName()
                ));
            }
        }

        ActionArgument<S> retValueArgument = null;

        for (ActionArgument<S> actionArgument : getArguments()) {
            // Check retval
            if (actionArgument.isReturnValue()) {
                if (actionArgument.getDirection() == ActionArgument.Direction.IN) {
                    if (log.isLoggable(Level.WARNING)) {
                        log.warning("UPnP specification violation of :" + getService().getDevice());
                        log.warning("Input argument can not have <retval/>");
                    }
                } else {
                    if (retValueArgument != null) {
                        if (log.isLoggable(Level.WARNING)) {
                            log.warning("UPnP specification violation of: " + getService().getDevice());
                            log.warning("Only one argument of action '" + getName() + "' can be <retval/>");
                        }
                    }
                    retValueArgument = actionArgument;
                }
            }
        }
        if (retValueArgument != null) {
            for (ActionArgument<S> a : getArguments()) {
                if (a.getDirection() == ActionArgument.Direction.OUT) {
                    if (log.isLoggable(Level.WARNING)) {
                        log.warning("UPnP specification violation of: " + getService().getDevice());
                        log.warning("Argument '" + retValueArgument.getName() + "' of action '" + getName() + "' is <retval/> but not the first OUT argument");
                    }
                }
            }
        }

        for (ActionArgument<S> argument : arguments) {
            errors.addAll(argument.validate());
        }

        return errors;
    }

    public Action<S> deepCopy() {
        List<ActionArgument<S>> actionArgumentsDupe = new ArrayList<>(getArguments().size());
        for (ActionArgument<S> arg : getArguments()) {
            actionArgumentsDupe.add(arg.deepCopy());
        }

        return new Action<>(
                getName(),
                actionArgumentsDupe
        );
    }

}
