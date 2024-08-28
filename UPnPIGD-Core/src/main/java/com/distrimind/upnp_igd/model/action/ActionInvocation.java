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

import com.distrimind.upnp_igd.model.profile.ClientInfo;
import com.distrimind.upnp_igd.model.meta.Action;
import com.distrimind.upnp_igd.model.meta.ActionArgument;
import com.distrimind.upnp_igd.model.meta.Service;
import com.distrimind.upnp_igd.model.types.InvalidValueException;

import java.util.*;

/**
 * The input, output, and failure values of an action invocation.
 *
 * @author Christian Bauer
 */
public class ActionInvocation<S extends Service<?, ?, ?>> {

    final protected Action<S> action;
    final protected ClientInfo clientInfo;

    // We don't necessarily have to preserve insertion order, but it's nicer if the arrays returned
    // by the getters are reliable
    protected Map<String, ActionArgumentValue<S>> input = new LinkedHashMap<>();
    protected Map<String, ActionArgumentValue<S>> output = new LinkedHashMap<>();

    protected ActionException failure = null;
    @SuppressWarnings("unchecked")
	public ActionInvocation(Action<? extends Service<?, ?, ?>> action) {
        this((Action<S>)action, null, null, null);
    }

    public ActionInvocation(Action<S> action,
                            ClientInfo clientInfo) {
        this(action, null, null, clientInfo);
    }

    public ActionInvocation(Action<S> action,
                            List<ActionArgumentValue<S>> input) {
        this(action, input, null, null);
    }

    public ActionInvocation(Action<S> action,
                            List<ActionArgumentValue<S>> input,
                            ClientInfo clientInfo) {
        this(action, input, null, clientInfo);
    }

    public ActionInvocation(Action<S> action,
                            List<ActionArgumentValue<S>> input,
                            List<ActionArgumentValue<S>> output) {
        this(action, input, output, null);
    }

    public ActionInvocation(Action<S> action,
                            List<ActionArgumentValue<S>> input,
                            List<ActionArgumentValue<S>> output,
                            ClientInfo clientInfo) {
        if (action == null) {
            throw new IllegalArgumentException("Action can not be null");
        }
        this.action = action;

        setInput(input);
        setOutput(output);

        this.clientInfo = clientInfo;
    }

    public ActionInvocation(ActionException failure) {
        this.action = null;
        this.input = null;
        this.output = null;
        this.failure = failure;
        this.clientInfo = null;
    }

    public Action<S> getAction() {
        return action;
    }

    public Collection<ActionArgumentValue<S>> getInput() {
        return input.values();
    }

    public ActionArgumentValue<S> getInput(String argumentName) {
        return getInput(getInputArgument(argumentName));
    }

    public ActionArgumentValue<S> getInput(ActionArgument<S> argument) {
        return input.get(argument.getName());
    }

    public Map<String, ActionArgumentValue<S>> getInputMap() {
        return Collections.unmodifiableMap(input);
    }

    public Collection<ActionArgumentValue<S>> getOutput() {
        return output.values();
    }

    public ActionArgumentValue<S> getOutput(String argumentName) {
        return getOutput(getOutputArgument(argumentName));
    }

    public Map<String, ActionArgumentValue<S>> getOutputMap() {
        return Collections.unmodifiableMap(output);
    }

    public ActionArgumentValue<S> getOutput(ActionArgument<S> argument) {
        return output.get(argument.getName());
    }

    public void setInput(String argumentName, Object value) throws InvalidValueException {
        setInput(new ActionArgumentValue<>(getInputArgument(argumentName), value));
    }

    public void setInput(ActionArgumentValue<S> value) {
        input.put(value.getArgument().getName(), value);
    }

    public void setInput(List<ActionArgumentValue<S>> input) {
        if (input == null) return;
        for (ActionArgumentValue<S> argumentValue : input) {
            this.input.put(argumentValue.getArgument().getName(), argumentValue);
        }
    }

    public void setOutput(String argumentName, Object value) throws InvalidValueException {
        setOutput(new ActionArgumentValue<>(getOutputArgument(argumentName), value));
    }

    public void setOutput(ActionArgumentValue<S> value){
        output.put(value.getArgument().getName(), value);
    }

    public void setOutput(List<ActionArgumentValue<S>> output) {
        if (output == null) return;
        for (ActionArgumentValue<S> argumentValue : output) {
            this.output.put(argumentValue.getArgument().getName(), argumentValue);
        }
    }

    protected ActionArgument<S> getInputArgument(String name) {
        ActionArgument<S> argument = getAction().getInputArgument(name);
        if (argument == null) throw new IllegalArgumentException("Argument not found: " + name);
        return argument;
    }

    protected ActionArgument<S> getOutputArgument(String name) {
        ActionArgument<S> argument = getAction().getOutputArgument(name);
        if (argument == null) throw new IllegalArgumentException("Argument not found: " + name);
        return argument;
    }

    /**
     * @return <code>null</code> if execution was successful, failure details otherwise.
     */
    public ActionException getFailure() {
        return failure;
    }

    public void setFailure(ActionException failure) {
        this.failure = failure;
    }

    /**
     * @return <code>null</code> if no info was provided for a local invocation.
     */
    public ClientInfo getClientInfo() {
        return clientInfo;
    }

    @Override
    public String toString() {
        return "(" + getClass().getSimpleName() + ") " + getAction();
    }
}