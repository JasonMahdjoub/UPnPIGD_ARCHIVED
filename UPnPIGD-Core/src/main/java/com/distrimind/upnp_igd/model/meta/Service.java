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

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.distrimind.upnp_igd.model.ServiceReference;
import com.distrimind.upnp_igd.model.ValidationError;
import com.distrimind.upnp_igd.model.ValidationException;
import com.distrimind.upnp_igd.model.types.Datatype;
import com.distrimind.upnp_igd.model.types.ServiceId;
import com.distrimind.upnp_igd.model.types.ServiceType;

/**
 * The metadata of a service, with actions and state variables.
 *
 * @author Christian Bauer
 */
public abstract class Service<DI extends DeviceIdentity, D extends Device<DI, D, ?>, S extends Service<DI, D, ?>> {

	final private static Logger log = Logger.getLogger(Service.class.getName());

    final private ServiceType serviceType;
    final private ServiceId serviceId;


    final private Map<String, Action<S>> actions = new HashMap<>();
    final private Map<String, StateVariable<S>> stateVariables = new HashMap<>();

    // Package mutable state
    private D device;

    public Service(ServiceType serviceType, ServiceId serviceId) throws ValidationException {
        this(serviceType, serviceId, null, null);
    }

    @SuppressWarnings("unchecked")
	public Service(ServiceType serviceType, ServiceId serviceId,
				   Collection<Action<S>> actions, Collection<StateVariable<S>> stateVariables) throws ValidationException {

        this.serviceType = serviceType;
        this.serviceId = serviceId;

        if (actions != null) {
            for (Action<S> action : actions) {
                this.actions.put(action.getName(), action);
                action.setService((S)this);
            }
        }

        if (stateVariables != null) {
            for (StateVariable<S> stateVariable : stateVariables) {
                this.stateVariables.put(stateVariable.getName(), stateVariable);
                stateVariable.setService((S)this);
            }
        }

    }

    public ServiceType getServiceType() {
        return serviceType;
    }

    public ServiceId getServiceId() {
        return serviceId;
    }

    public boolean hasActions() {
        return getActions() != null && !getActions().isEmpty();
    }

    public Collection<Action<S>> getActions() {
        return Collections.unmodifiableCollection(new ArrayList<>(actions.values()));
    }

    public boolean hasStateVariables() {
        // TODO: Spec says always has to have at least one...
        return getStateVariables() != null && !getStateVariables().isEmpty();
    }

    public Collection<StateVariable<S>> getStateVariables() {
        return Collections.unmodifiableCollection(new ArrayList<>(stateVariables.values()));
    }

    public D getDevice() {
        return device;
    }

    void setDevice(D device) {
        if (this.device != null)
            throw new IllegalStateException("Final value has been set already, model is immutable");
        this.device = device;
    }

    public Action<S> getAction(String name) {
        return actions.get(name);
    }

	public StateVariable<S> getStateVariable(String name) {
        // Some magic necessary for the deprecated 'query state variable' action stuff
        if (QueryStateVariableAction.VIRTUAL_STATEVARIABLE_INPUT.equals(name)) {
            return new StateVariable<>(
					QueryStateVariableAction.VIRTUAL_STATEVARIABLE_INPUT,
					new StateVariableTypeDetails(Datatype.Builtin.STRING.getDatatype())
			);
        }
        if (QueryStateVariableAction.VIRTUAL_STATEVARIABLE_OUTPUT.equals(name)) {
            return new StateVariable<>(
                    QueryStateVariableAction.VIRTUAL_STATEVARIABLE_OUTPUT,
                    new StateVariableTypeDetails(Datatype.Builtin.STRING.getDatatype())
            );
        }

        return stateVariables.get(name);
    }

    public StateVariable<S> getRelatedStateVariable(ActionArgument<?> argument) {
        return getStateVariable(argument.getRelatedStateVariableName());
    }

    public Datatype<?> getDatatype(ActionArgument<?> argument) {
        return getRelatedStateVariable(argument).getTypeDetails().getDatatype();
    }

    public ServiceReference getReference() {
        return new ServiceReference(getDevice().getIdentity().getUdn(), getServiceId());
    }

    public List<ValidationError> validate() {
        List<ValidationError> errors = new ArrayList<>();

        if (getServiceType() == null) {
            errors.add(new ValidationError(
                    getClass(),
                    "serviceType",
                    "Service type/info is required"
            ));
        }

        if (getServiceId() == null) {
            errors.add(new ValidationError(
                    getClass(),
                    "serviceId",
                    "Service ID is required"
            ));
        }

        // TODO: If the service has no evented variables, it should not have an event subscription URL, which means
        // the url element in the device descriptor must be present, but empty!!!!

        /* TODO: This doesn't fit into our meta model, we don't know if a service has state variables until
         we completely hydrate it from a service descriptor
        if (getStateVariables().length == 0) {
            errors.add(new ValidationError(
                    getClass(),
                    "stateVariables",
                    "Service must have at least one state variable"
            ));
        }
        */
        if (hasStateVariables()) {
            for (StateVariable<S> stateVariable : getStateVariables()) {
                errors.addAll(stateVariable.validate());
            }
        }

        if (hasActions()) {
            for (Action<S> action : getActions()) {

                // Instead of bailing out here, we try to continue if an action is invalid
                // errors.addAll(action.validate());

                List<ValidationError> actionErrors = action.validate();
            	if(!actionErrors.isEmpty()) {
                    actions.remove(action.getName()); // Remove it
                    if (log.isLoggable(Level.WARNING)) log.warning("Discarding invalid action of service '" + getServiceId() + "': " + action.getName());
                    for (ValidationError actionError : actionErrors) {
                        if (log.isLoggable(Level.WARNING)) log.warning("Invalid action '" + action.getName() + "': " + actionError);
                    }
            	}
            }
        }

        return errors;
    }

    public abstract Action<S> getQueryStateVariableAction();

    @Override
    public String toString() {
        return "(" + getClass().getSimpleName() + ") ServiceId: " + getServiceId();
    }
}