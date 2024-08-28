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

package com.distrimind.upnp_igd.binding.annotations;

import com.distrimind.upnp_igd.binding.LocalServiceBindingException;
import com.distrimind.upnp_igd.binding.LocalServiceBinder;
import com.distrimind.upnp_igd.model.ValidationError;
import com.distrimind.upnp_igd.model.ValidationException;
import com.distrimind.upnp_igd.model.action.ActionExecutor;
import com.distrimind.upnp_igd.model.action.QueryStateVariableExecutor;
import com.distrimind.upnp_igd.model.meta.Action;
import com.distrimind.upnp_igd.model.meta.LocalService;
import com.distrimind.upnp_igd.model.meta.QueryStateVariableAction;
import com.distrimind.upnp_igd.model.meta.StateVariable;
import com.distrimind.upnp_igd.model.state.FieldStateVariableAccessor;
import com.distrimind.upnp_igd.model.state.GetterStateVariableAccessor;
import com.distrimind.upnp_igd.model.state.StateVariableAccessor;
import com.distrimind.upnp_igd.model.types.ServiceId;
import com.distrimind.upnp_igd.model.types.ServiceType;
import com.distrimind.upnp_igd.model.types.UDAServiceId;
import com.distrimind.upnp_igd.model.types.UDAServiceType;
import com.distrimind.upnp_igd.model.types.csv.CSV;
import com.distrimind.upnp_igd.util.Reflections;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Reads {@link LocalService} metadata from annotations.
 *
 * @author Christian Bauer
 */
public class AnnotationLocalServiceBinder implements LocalServiceBinder {

    private static final Logger log = Logger.getLogger(AnnotationLocalServiceBinder.class.getName());

    @Override
    public <T> LocalService<T> read(Class<T> clazz) throws LocalServiceBindingException {
        if (log.isLoggable(Level.FINE))
            log.fine("Reading and binding annotations of service implementation class: " + clazz);

        // Read the service ID and service type from the annotation
        if (clazz.isAnnotationPresent(UpnpService.class)) {

            UpnpService annotation = clazz.getAnnotation(UpnpService.class);
            UpnpServiceId idAnnotation = annotation.serviceId();
            UpnpServiceType typeAnnotation = annotation.serviceType();

            ServiceId serviceId = UDAServiceId.DEFAULT_NAMESPACE.equals(idAnnotation.namespace())
                    ? new UDAServiceId(idAnnotation.value())
                    : new ServiceId(idAnnotation.namespace(), idAnnotation.value());

            ServiceType serviceType = UDAServiceType.DEFAULT_NAMESPACE.equals(typeAnnotation.namespace())
                    ? new UDAServiceType(typeAnnotation.value(), typeAnnotation.version())
                    : new ServiceType(typeAnnotation.namespace(), typeAnnotation.value(), typeAnnotation.version());

            boolean supportsQueryStateVariables = annotation.supportsQueryStateVariables();

            Set<Class<?>> stringConvertibleTypes = readStringConvertibleTypes(List.of(annotation.stringConvertibleTypes()));

            return read(clazz, serviceId, serviceType, supportsQueryStateVariables, stringConvertibleTypes);
        } else {
            throw new LocalServiceBindingException("Given class is not an @UpnpService");
        }
    }
    public <T> LocalService<T> read(Class<T> clazz, ServiceId id, ServiceType type,
                                    boolean supportsQueryStateVariables, List<Class<?>> stringConvertibleTypes)
    {
		Set<Class<?>> set = new HashSet<>(stringConvertibleTypes);
        return read(clazz, id, type, supportsQueryStateVariables, set);
    }
    @Override
    public <T> LocalService<T> read(Class<T> clazz, ServiceId id, ServiceType type,
                                   boolean supportsQueryStateVariables, Set<Class<?>> stringConvertibleTypes)
            throws LocalServiceBindingException {

        Map<StateVariable<LocalService<T>>, StateVariableAccessor> stateVariables = readStateVariables(clazz, stringConvertibleTypes);
        Map<Action<LocalService<T>>, ActionExecutor> actions = readActions(clazz, stateVariables, stringConvertibleTypes);

        // Special treatment of the state variable querying action
        if (supportsQueryStateVariables) {
            actions.put(new QueryStateVariableAction<>(), new QueryStateVariableExecutor());
        }

        try {
            return new LocalService<>(type, id, actions, stateVariables, stringConvertibleTypes, supportsQueryStateVariables);

        } catch (ValidationException ex) {
            if (log.isLoggable(Level.SEVERE))
                log.severe("Could not validate device model: " + ex);
            for (ValidationError validationError : ex.getErrors()) {
                if (log.isLoggable(Level.SEVERE))
                    log.severe(validationError.toString());
            }
            throw new LocalServiceBindingException("Validation of model failed, check the log");
        }
    }

    protected Set<Class<?>> readStringConvertibleTypes(Collection<Class<?>> declaredTypes) throws LocalServiceBindingException {

        for (Class<?> stringConvertibleType : declaredTypes) {
            if (!Modifier.isPublic(stringConvertibleType.getModifiers())) {
                throw new LocalServiceBindingException(
                        "Declared string-convertible type must be public: " + stringConvertibleType
                );
            }
            try {
                stringConvertibleType.getConstructor(String.class);
            } catch (NoSuchMethodException ex) {
                throw new LocalServiceBindingException(
                        "Declared string-convertible type needs a public single-argument String constructor: " + stringConvertibleType
                );
            }
        }
        Set<Class<?>> stringConvertibleTypes = new HashSet<>(declaredTypes);

        // Some defaults
        stringConvertibleTypes.add(URI.class);
        stringConvertibleTypes.add(URL.class);
        stringConvertibleTypes.add(CSV.class);

        return stringConvertibleTypes;
    }

    protected <T> Map<StateVariable<LocalService<T>>, StateVariableAccessor> readStateVariables(Class<?> clazz, Set<Class<?>> stringConvertibleTypes)
            throws LocalServiceBindingException {

        Map<StateVariable<LocalService<T>>, StateVariableAccessor> map = new HashMap<>();

        // State variables declared on the class
        if (clazz.isAnnotationPresent(UpnpStateVariables.class)) {
            UpnpStateVariables variables = clazz.getAnnotation(UpnpStateVariables.class);
            for (UpnpStateVariable v : variables.value()) {

                if (v.name().isEmpty())
                    throw new LocalServiceBindingException("Class-level @UpnpStateVariable name attribute value required");

                String javaPropertyName = toJavaStateVariableName(v.name());

                Method getter = Reflections.getGetterMethod(clazz, javaPropertyName);
                Field field = Reflections.getField(clazz, javaPropertyName);

                StateVariableAccessor accessor = null;
                if (getter != null && field != null) {
                    accessor = variables.preferFields() ?
                            new FieldStateVariableAccessor(field)
                            : new GetterStateVariableAccessor(getter);
                } else if (field != null) {
                    accessor = new FieldStateVariableAccessor(field);
                } else if (getter != null) {
                    accessor = new GetterStateVariableAccessor(getter);
                } else {
                    if (log.isLoggable(Level.FINER))
                        log.finer("No field or getter found for state variable, skipping accessor: " + v.name());
                }

                @SuppressWarnings("unchecked") StateVariable<LocalService<T>> stateVar =(StateVariable<LocalService<T>>)
                        new AnnotationStateVariableBinder(v, v.name(), accessor, stringConvertibleTypes)
                                .createStateVariable();

                map.put(stateVar, accessor);
            }
        }

        // State variables declared on fields
        for (Field field : Reflections.getFields(clazz, UpnpStateVariable.class)) {

            UpnpStateVariable svAnnotation = field.getAnnotation(UpnpStateVariable.class);

            StateVariableAccessor accessor = new FieldStateVariableAccessor(field);

            @SuppressWarnings("unchecked") StateVariable<LocalService<T>> stateVar = (StateVariable<LocalService<T>>)new AnnotationStateVariableBinder(
                    svAnnotation,
					svAnnotation.name().isEmpty()
                            ? toUpnpStateVariableName(field.getName())
                            : svAnnotation.name(),
                    accessor,
                    stringConvertibleTypes
            ).createStateVariable();

            map.put(stateVar, accessor);
        }

        // State variables declared on getters
        for (Method getter : Reflections.getMethods(clazz, UpnpStateVariable.class)) {

            String propertyName = Reflections.getMethodPropertyName(getter.getName());
            if (propertyName == null) {
                throw new LocalServiceBindingException(
                        "Annotated method is not a getter method (: " + getter
                );
            }

            if (getter.getParameterTypes().length > 0)
                throw new LocalServiceBindingException(
                        "Getter method defined as @UpnpStateVariable can not have parameters: " + getter
                );

            UpnpStateVariable svAnnotation = getter.getAnnotation(UpnpStateVariable.class);

            StateVariableAccessor accessor = new GetterStateVariableAccessor(getter);

            @SuppressWarnings("unchecked") StateVariable<LocalService<T>> stateVar = (StateVariable<LocalService<T>>)new AnnotationStateVariableBinder(
                    svAnnotation,
					svAnnotation.name().isEmpty()
                            ?
                            toUpnpStateVariableName(propertyName)
                            : svAnnotation.name(),
                    accessor,
                    stringConvertibleTypes
            ).createStateVariable();

            map.put(stateVar, accessor);
        }

        return map;
    }

    protected <T> Map<Action<LocalService<T>>, ActionExecutor> readActions(Class<?> clazz,
                                                            Map<StateVariable<LocalService<T>>, StateVariableAccessor> stateVariables,
                                                      Set<Class<?>> stringConvertibleTypes)
            throws LocalServiceBindingException {

        Map<Action<LocalService<T>>, ActionExecutor> map = new HashMap<>();

        for (Method method : Reflections.getMethods(clazz, UpnpAction.class)) {
            AnnotationActionBinder<T> actionBinder =
                    new AnnotationActionBinder<>(method, stateVariables, stringConvertibleTypes);
            Action<?> action = actionBinder.appendAction(map);
            if(isActionExcluded(action)) {
            	map.remove(action);
            }
        }

        return map;
    }

    /**
     * Override this method to exclude action/methods after they have been discovered.
     */
    protected  boolean isActionExcluded(Action<?> action) {
    	return false;
    }
    
    // TODO: I don't like the exceptions much, user has no idea what to do

    static String toUpnpStateVariableName(String javaName) {
        if (javaName.isEmpty()) {
            throw new IllegalArgumentException("Variable name must be at least 1 character long");
        }
        return javaName.substring(0, 1).toUpperCase(Locale.ROOT) + javaName.substring(1);
    }

    static String toJavaStateVariableName(String upnpName) {
        if (upnpName.isEmpty()) {
            throw new IllegalArgumentException("Variable name must be at least 1 character long");
        }
        return upnpName.substring(0, 1).toLowerCase(Locale.ROOT) + upnpName.substring(1);
    }


    static String toUpnpActionName(String javaName) {
        if (javaName.isEmpty()) {
            throw new IllegalArgumentException("Action name must be at least 1 character long");
        }
        return javaName.substring(0, 1).toUpperCase(Locale.ROOT) + javaName.substring(1);
    }

    static String toJavaActionName(String upnpName) {
        if (upnpName.isEmpty()) {
            throw new IllegalArgumentException("Variable name must be at least 1 character long");
        }
        return upnpName.substring(0, 1).toLowerCase(Locale.ROOT) + upnpName.substring(1);
    }

}
