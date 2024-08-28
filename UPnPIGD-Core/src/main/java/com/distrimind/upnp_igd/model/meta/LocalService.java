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
import com.distrimind.upnp_igd.model.ServiceManager;
import com.distrimind.upnp_igd.model.ValidationException;
import com.distrimind.upnp_igd.model.action.ActionExecutor;
import com.distrimind.upnp_igd.model.state.StateVariableAccessor;
import com.distrimind.upnp_igd.model.types.ServiceId;
import com.distrimind.upnp_igd.model.types.ServiceType;

import java.util.*;

/**
 * The metadata of a service created on this host, by application code.
 * <p>
 * After instantiation {@link #setManager(ServiceManager)} must
 * be called to bind the service metadata to the service implementation.
 * </p>
 *
 * @author Christian Bauer
 */
public class LocalService<T> extends Service<DeviceIdentity, LocalDevice<T>, LocalService<T>> {

    final protected Map<Action<LocalService<T>>, ActionExecutor> actionExecutors;
    final protected Map<StateVariable<LocalService<T>>, StateVariableAccessor> stateVariableAccessors;
    final protected Set<Class<?>> stringConvertibleTypes;
    final protected boolean supportsQueryStateVariables;

    protected ServiceManager<T> manager;

    public LocalService(ServiceType serviceType, ServiceId serviceId,
                        Collection<Action<LocalService<T>>> actions, Collection<StateVariable<LocalService<T>>> stateVariables) throws ValidationException {
        super(serviceType, serviceId, actions, stateVariables);
        this.manager = null;
        this.actionExecutors = new HashMap<>();
        this.stateVariableAccessors = new HashMap<>();
        this.stringConvertibleTypes = new HashSet<>();
        this.supportsQueryStateVariables = true;
    }

    public LocalService(ServiceType serviceType, ServiceId serviceId,
                        Map<Action<LocalService<T>>, ActionExecutor> actionExecutors,
                        Map<StateVariable<LocalService<T>>, StateVariableAccessor> stateVariableAccessors,
                        Set<Class<?>> stringConvertibleTypes,
                        boolean supportsQueryStateVariables) throws ValidationException {

        super(serviceType, serviceId,
                actionExecutors.keySet(),
                stateVariableAccessors.keySet()
        );

        this.supportsQueryStateVariables = supportsQueryStateVariables;
        this.stringConvertibleTypes = stringConvertibleTypes;
        this.stateVariableAccessors = stateVariableAccessors;
        this.actionExecutors = actionExecutors;
    }

    synchronized public void setManager(ServiceManager<T> manager) {
        if (this.manager != null) {
            throw new IllegalStateException("Manager is final");
        }
        this.manager = manager;
    }

    synchronized public ServiceManager<T> getManager() {
        if (manager == null) {
            throw new IllegalStateException("Unmanaged service, no implementation instance available");
        }
        return manager;
    }

    public boolean isSupportsQueryStateVariables() {
        return supportsQueryStateVariables;
    }

    public Set<Class<?>> getStringConvertibleTypes() {
        return stringConvertibleTypes;
    }

    public boolean isStringConvertibleType(Object o) {
        return o != null && isStringConvertibleType(o.getClass());
    }

    public boolean isStringConvertibleType(Class<?> clazz) {
        return ModelUtil.isStringConvertibleType(getStringConvertibleTypes(), clazz);
    }

    public StateVariableAccessor getAccessor(String stateVariableName) {
        StateVariable<LocalService<T>> sv;
        return (sv = getStateVariable(stateVariableName)) != null ? getAccessor(sv) : null;
    }

    public StateVariableAccessor getAccessor(StateVariable<LocalService<T>> stateVariable) {
        return stateVariableAccessors.get(stateVariable);
    }

    public ActionExecutor getExecutor(String actionName) {
        Action<? extends LocalService<?>> action;
        return (action = getAction(actionName)) != null ? getExecutor(action) : null;
    }

    public ActionExecutor getExecutor(Action<? extends LocalService<?>> action) {
        return actionExecutors.get(action);
    }

    @Override
    public Action<LocalService<T>> getQueryStateVariableAction() {
        return getAction(QueryStateVariableAction.ACTION_NAME);
    }

    @Override
    public String toString() {
        return super.toString()  + ", Manager: " + manager;
    }
}