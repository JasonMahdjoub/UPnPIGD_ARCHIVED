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

package com.distrimind.upnp_igd.model.gena;

import com.distrimind.upnp_igd.model.ServiceManager;
import com.distrimind.upnp_igd.model.UserConstants;
import com.distrimind.upnp_igd.model.message.header.SubscriptionIdHeader;
import com.distrimind.upnp_igd.model.meta.LocalService;
import com.distrimind.upnp_igd.model.meta.StateVariable;
import com.distrimind.upnp_igd.model.state.StateVariableValue;
import com.distrimind.upnp_igd.model.types.UnsignedIntegerFourBytes;
import com.distrimind.upnp_igd.util.Exceptions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An incoming subscription to a local service.
 * <p>
 * Uses the {@link ServiceManager} to read the initial state of
 * the {@link LocalService} on instantation. Typically, the
 * {@link #registerOnService()} method is called next, and from this point forward all
 * {@link ServiceManager#EVENTED_STATE_VARIABLES} property change
 * events are detected by this subscription. After moderation of state variable values
 * (frequency and range of changes), the {@link #eventReceived()} method is called.
 * Delivery of the event message to the subscriber is not part of this class, but the
 * implementor of {@link #eventReceived()}.
 * </p>
 *
 * @author Christian Bauer
 */
public abstract class LocalGENASubscription<T> extends GENASubscription<LocalService<T>> implements PropertyChangeListener {

    private static final Logger log = Logger.getLogger(LocalGENASubscription.class.getName());

    final List<URL> callbackURLs;

    // Moderation history
    final Map<String, Long> lastSentTimestamp = new HashMap<>();
    final Map<String, Long> lastSentNumericValue = new HashMap<>();

    protected LocalGENASubscription(LocalService<T> service, List<URL> callbackURLs) throws Exception {
        super(service);
        this.callbackURLs = callbackURLs;
    }

    public LocalGENASubscription(LocalService<T> service,
                                 Integer requestedDurationSeconds, List<URL> callbackURLs) throws Exception {
        super(service);

        setSubscriptionDuration(requestedDurationSeconds);

        log.fine("Reading initial state of local service at subscription time");
        long currentTime = new Date().getTime();
        this.currentValues.clear();

        Collection<StateVariableValue<LocalService<T>>> values = getService().getManager().getCurrentState();

		if (log.isLoggable(Level.FINER)) {
			log.finer("Got evented state variable values: " + values.size());
		}

		for (StateVariableValue<LocalService<T>> value : values) {
            this.currentValues.put(value.getStateVariable().getName(), value);

            if (log.isLoggable(Level.FINER)) {
                log.finer("Read state variable value '" + value.getStateVariable().getName() + "': " + value);
            }

            // Preserve "last sent" state for future moderation
            lastSentTimestamp.put(value.getStateVariable().getName(), currentTime);
            if (value.getStateVariable().isModeratedNumericType()) {
                lastSentNumericValue.put(value.getStateVariable().getName(), Long.valueOf(value.toString()));
            }
        }

        this.subscriptionId = SubscriptionIdHeader.PREFIX + UUID.randomUUID();
        this.currentSequence = new UnsignedIntegerFourBytes(0);
        this.callbackURLs = callbackURLs;
    }

    synchronized public List<URL> getCallbackURLs() {
        return callbackURLs;
    }

    /**
     * Adds a property change listener on the {@link ServiceManager}.
     */
    synchronized public void registerOnService() {
        getService().getManager().getPropertyChangeSupport().addPropertyChangeListener(this);
    }

    synchronized public void establish() {
        established();
    }

    /**
     * Removes a property change listener on the {@link ServiceManager}.
     */
    synchronized public void end(CancelReason reason) {
        try {
            getService().getManager().getPropertyChangeSupport().removePropertyChangeListener(this);
        } catch (Exception ex) {
			if (log.isLoggable(Level.WARNING)) log.warning("Removal of local service property change listener failed: " + Exceptions.unwrap(ex));
        }
        ended(reason);
    }

    /**
     * Moderates {@link ServiceManager#EVENTED_STATE_VARIABLES} events and state variable
     * values, calls {@link #eventReceived()}.
     */
    @Override
	@SuppressWarnings("unchecked")
	synchronized public void propertyChange(PropertyChangeEvent e) {
        if (!ServiceManager.EVENTED_STATE_VARIABLES.equals(e.getPropertyName())) return;

		if (log.isLoggable(Level.FINE)) {
			log.fine("Eventing triggered, getting state for subscription: " + getSubscriptionId());
		}

		long currentTime = new Date().getTime();

        @SuppressWarnings("unchecked") Collection<StateVariableValue<?>> newValues = (Collection<StateVariableValue<?>>) e.getNewValue();
        Set<String> excludedVariables = moderateStateVariables(currentTime, newValues);

        currentValues.clear();
        for (StateVariableValue<?> newValue : newValues) {
            String name = newValue.getStateVariable().getName();
            if (!excludedVariables.contains(name)) {
				if (log.isLoggable(Level.FINE)) {
					log.fine("Adding state variable value to current values of event: " + newValue.getStateVariable() + " = " + newValue);
				}
				currentValues.put(newValue.getStateVariable().getName(), (StateVariableValue<LocalService<T>>) newValue);

                // Preserve "last sent" state for future moderation
                lastSentTimestamp.put(name, currentTime);
                if (newValue.getStateVariable().isModeratedNumericType()) {
                    lastSentNumericValue.put(name, Long.valueOf(newValue.toString()));
                }
            }
        }

        if (!currentValues.isEmpty()) {
			if (log.isLoggable(Level.FINE)) {
				log.fine("Propagating new state variable values to subscription: " + this);
			}
			// TODO: I'm not happy with this design, this dispatches to a separate thread which _then_
            // is supposed to lock and read the values off this instance. That obviously doesn't work
            // so it's currently a hack in SendingEvent.java
            eventReceived();
        } else {
            log.fine("No state variable values for event (all moderated out?), not triggering event");
        }
    }

    /**
     * Checks whether a state variable is moderated, and if this change is within the maximum rate and range limits.
     *
     * @param currentTime The current unix time.
     * @param values The state variable values to moderate.
     * @return A collection of state variable values that although they might have changed, are excluded from the event.
     */
    synchronized protected Set<String> moderateStateVariables(long currentTime, Collection<StateVariableValue<?>> values) {

        Set<String> excludedVariables = new HashSet<>();

        // Moderate event variables that have a maximum rate or minimum delta
        for (StateVariableValue<?> stateVariableValue : values) {

            StateVariable<?> stateVariable = stateVariableValue.getStateVariable();
            String stateVariableName = stateVariableValue.getStateVariable().getName();

            if (stateVariable.getEventDetails().getEventMaximumRateMilliseconds() == 0 &&
                    stateVariable.getEventDetails().getEventMinimumDelta() == 0) {
				if (log.isLoggable(Level.FINER)) {
					log.finer("Variable is not moderated: " + stateVariable);
				}
				continue;
            }

            // That should actually never happen, because we always "send" it as the initial state/event
            if (!lastSentTimestamp.containsKey(stateVariableName)) {
				if (log.isLoggable(Level.FINER)) {
					log.finer("Variable is moderated but was never sent before: " + stateVariable);
				}
				continue;
            }

            if (stateVariable.getEventDetails().getEventMaximumRateMilliseconds() > 0) {
                long timestampLastSent = lastSentTimestamp.get(stateVariableName);
                long timestampNextSend = timestampLastSent + (stateVariable.getEventDetails().getEventMaximumRateMilliseconds());
                if (currentTime <= timestampNextSend) {
					if (log.isLoggable(Level.FINER)) {
						log.finer("Excluding state variable with maximum rate: " + stateVariable);
					}
					excludedVariables.add(stateVariableName);
                    continue;
                }
            }

            if (stateVariable.isModeratedNumericType() && lastSentNumericValue.get(stateVariableName) != null) {

                long oldValue = lastSentNumericValue.get(stateVariableName);
                long newValue = Long.parseLong(stateVariableValue.toString());
                long minDelta = stateVariable.getEventDetails().getEventMinimumDelta();

                if (newValue > oldValue && newValue - oldValue < minDelta) {
					if (log.isLoggable(Level.FINER)) {
						log.finer("Excluding state variable with minimum delta: " + stateVariable);
					}
					excludedVariables.add(stateVariableName);
                    continue;
                }

                if (newValue < oldValue && oldValue - newValue < minDelta) {
					if (log.isLoggable(Level.FINER)) {
						log.finer("Excluding state variable with minimum delta: " + stateVariable);
					}
					excludedVariables.add(stateVariableName);
                }
            }

        }
        return excludedVariables;
    }

    synchronized public void incrementSequence() {
        this.currentSequence.increment(true);
    }

    /**
     * @param requestedDurationSeconds If <code>null</code> defaults to
     *                                 {@link UserConstants#DEFAULT_SUBSCRIPTION_DURATION_SECONDS}
     */
    synchronized public void setSubscriptionDuration(Integer requestedDurationSeconds) {
        this.requestedDurationSeconds =
                requestedDurationSeconds == null
                        ? UserConstants.DEFAULT_SUBSCRIPTION_DURATION_SECONDS
                        : requestedDurationSeconds;

        setActualSubscriptionDurationSeconds(this.requestedDurationSeconds);
    }

    public abstract void ended(CancelReason reason);

}
