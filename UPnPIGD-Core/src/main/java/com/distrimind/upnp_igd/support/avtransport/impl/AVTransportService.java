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

package com.distrimind.upnp_igd.support.avtransport.impl;

import com.distrimind.upnp_igd.support.avtransport.AVTransportErrorCode;
import com.distrimind.upnp_igd.support.avtransport.AVTransportException;
import com.distrimind.upnp_igd.support.avtransport.AbstractAVTransportService;
import com.distrimind.upnp_igd.support.avtransport.impl.state.AbstractState;
import com.distrimind.upnp_igd.model.types.ErrorCode;
import com.distrimind.upnp_igd.model.types.UnsignedIntegerFourBytes;
import com.distrimind.upnp_igd.support.lastchange.LastChange;
import com.distrimind.upnp_igd.support.model.AVTransport;
import com.distrimind.upnp_igd.support.model.DeviceCapabilities;
import com.distrimind.upnp_igd.support.model.MediaInfo;
import com.distrimind.upnp_igd.support.model.PlayMode;
import com.distrimind.upnp_igd.support.model.PositionInfo;
import com.distrimind.upnp_igd.support.model.RecordQualityMode;
import com.distrimind.upnp_igd.support.model.SeekMode;
import com.distrimind.upnp_igd.support.model.StorageMedium;
import com.distrimind.upnp_igd.support.model.TransportAction;
import com.distrimind.upnp_igd.support.model.TransportInfo;
import com.distrimind.upnp_igd.support.model.TransportSettings;
import com.distrimind.upnp_igd.statemachine.StateMachineBuilder;
import com.distrimind.upnp_igd.statemachine.TransitionException;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * State-machine based implementation of AVTransport service.
 * <p>
 * One logical AVTransport is represented by:
 * </p>
 * <ul>
 * <li>
 * One {@link AVTransportStateMachine}
 * instance that accepts the action method call as a proxy.
 * </li>
 * <li>
 * Each state machine holds several instances of
 * {@link AbstractState}, created on
 * instantation of the state machine. The "current" state will be the target of
 * the action call. It is the state implementation that decides how to handle the
 * call and what the next state is after a possible transition.
 * </li>
 * <li>
 * Each state has a reference to an implementation of
 * {@link com.distrimind.upnp_igd.support.model.AVTransport}, where the state can hold
 * information about well, the state.
 * </li>
 * </ul>
 * <p>
 * Simplified, this means that each AVTransport instance ID is typically handled by
 * one state machine, and the internal state of that machine is stored in an
 * <code>AVTransport</code>.
 * </p>
 * <p>
 * Override the {@link #createTransport(com.distrimind.upnp_igd.model.types.UnsignedIntegerFourBytes, com.distrimind.upnp_igd.support.lastchange.LastChange)}
 * method to utilize a subclass of <code>AVTransport</code> as your internal state holder.
 * </p>
 *
 * @author Christian Bauer
 */
public class AVTransportService<T extends AVTransport> extends AbstractAVTransportService {

    final private static Logger log = Logger.getLogger(AVTransportService.class.getName());

    final private Map<Long, AVTransportStateMachine> stateMachines = new ConcurrentHashMap<>();

    final Class<? extends AVTransportStateMachine> stateMachineDefinition;
    final Class<? extends AbstractState<?>> initialState;
    final Class<? extends AVTransport> transportClass;

    @SuppressWarnings("unchecked")
	public AVTransportService(Class<? extends AVTransportStateMachine> stateMachineDefinition,
							  Class<? extends AbstractState<?>> initialState) {
        this(stateMachineDefinition, initialState, (Class<T>)AVTransport.class);
    }

    public AVTransportService(Class<? extends AVTransportStateMachine> stateMachineDefinition,
                              Class<? extends AbstractState<?>> initialState,
                              Class<T> transportClass) {
        this.stateMachineDefinition = stateMachineDefinition;
        this.initialState = initialState;
        this.transportClass = transportClass;
    }

    @Override
	public void setAVTransportURI(UnsignedIntegerFourBytes instanceId,
								  String currentURI,
								  String currentURIMetaData) throws AVTransportException {

        URI uri;
        try {
            uri = new URI(currentURI);
        } catch (Exception ex) {
            throw new AVTransportException(
                    ErrorCode.INVALID_ARGS, "CurrentURI can not be null or malformed"
            );
        }

        try {
            AVTransportStateMachine transportStateMachine = findStateMachine(instanceId, true);
            transportStateMachine.setTransportURI(uri, currentURIMetaData);
        } catch (TransitionException ex) {
            throw new AVTransportException(AVTransportErrorCode.TRANSITION_NOT_AVAILABLE, ex.getMessage());
        }
    }

    @Override
	public void setNextAVTransportURI(UnsignedIntegerFourBytes instanceId,
									  String nextURI,
									  String nextURIMetaData) throws AVTransportException {

        URI uri;
        try {
            uri = new URI(nextURI);
        } catch (Exception ex) {
            throw new AVTransportException(
                    ErrorCode.INVALID_ARGS, "NextURI can not be null or malformed"
            );
        }

        try {
            AVTransportStateMachine transportStateMachine = findStateMachine(instanceId, true);
            transportStateMachine.setNextTransportURI(uri, nextURIMetaData);
        } catch (TransitionException ex) {
            throw new AVTransportException(AVTransportErrorCode.TRANSITION_NOT_AVAILABLE, ex.getMessage());
        }
    }

    @Override
	public void setPlayMode(UnsignedIntegerFourBytes instanceId, String newPlayMode) throws AVTransportException {
        AVTransport transport = findStateMachine(instanceId).getCurrentState().getTransport();
        try {
            transport.setTransportSettings(
                    new TransportSettings(
                            PlayMode.valueOf(newPlayMode),
                            transport.getTransportSettings().getRecQualityMode()
                    )
            );
        } catch (IllegalArgumentException ex) {
            throw new AVTransportException(
                    AVTransportErrorCode.PLAYMODE_NOT_SUPPORTED, "Unsupported play mode: " + newPlayMode
            );
        }
    }

    @Override
	public void setRecordQualityMode(UnsignedIntegerFourBytes instanceId, String newRecordQualityMode) throws AVTransportException {
        AVTransport transport = findStateMachine(instanceId).getCurrentState().getTransport();
        try {
            transport.setTransportSettings(
                    new TransportSettings(
                            transport.getTransportSettings().getPlayMode(),
                            RecordQualityMode.valueOrExceptionOf(newRecordQualityMode)
                    )
            );
        } catch (IllegalArgumentException ex) {
            throw new AVTransportException(
                    AVTransportErrorCode.RECORDQUALITYMODE_NOT_SUPPORTED, "Unsupported record quality mode: " + newRecordQualityMode
            );
        }
    }

    @Override
	public MediaInfo getMediaInfo(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
        return findStateMachine(instanceId).getCurrentState().getTransport().getMediaInfo();
    }

    @Override
	public TransportInfo getTransportInfo(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
        return findStateMachine(instanceId).getCurrentState().getTransport().getTransportInfo();
    }

    @Override
	public PositionInfo getPositionInfo(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
        return findStateMachine(instanceId).getCurrentState().getTransport().getPositionInfo();
    }

    @Override
	public DeviceCapabilities getDeviceCapabilities(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
        return findStateMachine(instanceId).getCurrentState().getTransport().getDeviceCapabilities();
    }

    @Override
	public TransportSettings getTransportSettings(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
        return findStateMachine(instanceId).getCurrentState().getTransport().getTransportSettings();
    }

    @Override
	public void stop(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
        try {
            findStateMachine(instanceId).stop();
        } catch (TransitionException ex) {
            throw new AVTransportException(AVTransportErrorCode.TRANSITION_NOT_AVAILABLE, ex.getMessage());
        }
    }

    @Override
	public void play(UnsignedIntegerFourBytes instanceId, String speed) throws AVTransportException {
        try {
            findStateMachine(instanceId).play(speed);
        } catch (TransitionException ex) {
            throw new AVTransportException(AVTransportErrorCode.TRANSITION_NOT_AVAILABLE, ex.getMessage());
        }
    }

    @Override
	public void pause(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
        try {
            findStateMachine(instanceId).pause();
        } catch (TransitionException ex) {
            throw new AVTransportException(AVTransportErrorCode.TRANSITION_NOT_AVAILABLE, ex.getMessage());
        }
    }

    @Override
	public void record(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
        try {
            findStateMachine(instanceId).record();
        } catch (TransitionException ex) {
            throw new AVTransportException(AVTransportErrorCode.TRANSITION_NOT_AVAILABLE, ex.getMessage());
        }
    }

    @Override
	public void seek(UnsignedIntegerFourBytes instanceId, String unit, String target) throws AVTransportException {
        SeekMode seekMode;
        try {
             seekMode = SeekMode.valueOrExceptionOf(unit);
        } catch (IllegalArgumentException ex) {
            throw new AVTransportException(
                    AVTransportErrorCode.SEEKMODE_NOT_SUPPORTED, "Unsupported seek mode: " + unit
            );
        }

        try {
            findStateMachine(instanceId).seek(seekMode, target);
        } catch (TransitionException ex) {
            throw new AVTransportException(AVTransportErrorCode.TRANSITION_NOT_AVAILABLE, ex.getMessage());
        }
    }

    @Override
	public void next(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
        try {
            findStateMachine(instanceId).next();
        } catch (TransitionException ex) {
            throw new AVTransportException(AVTransportErrorCode.TRANSITION_NOT_AVAILABLE, ex.getMessage());
        }
    }

    @Override
	public void previous(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
        try {
            findStateMachine(instanceId).previous();
        } catch (TransitionException ex) {
            throw new AVTransportException(AVTransportErrorCode.TRANSITION_NOT_AVAILABLE, ex.getMessage());
        }
    }

    @Override
    protected List<TransportAction> getCurrentTransportActions(UnsignedIntegerFourBytes instanceId) throws Exception {
        AVTransportStateMachine stateMachine = findStateMachine(instanceId);
        try {
            return stateMachine.getCurrentState().getCurrentTransportActions();
        } catch (TransitionException ex) {
            return Collections.emptyList();
        }
    }

    @Override
    public UnsignedIntegerFourBytes[] getCurrentInstanceIds() {
        synchronized (stateMachines) {
            UnsignedIntegerFourBytes[] ids = new UnsignedIntegerFourBytes[stateMachines.size()];
            int i = 0;
            for (Long id : stateMachines.keySet()) {
                ids[i] = new UnsignedIntegerFourBytes(id);
                i++;
            }
            return ids;
        }
    }

    protected AVTransportStateMachine findStateMachine(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
        return findStateMachine(instanceId, true);
    }

    protected AVTransportStateMachine findStateMachine(UnsignedIntegerFourBytes instanceId, boolean createDefaultTransport) throws AVTransportException {
        synchronized (stateMachines) {
            long id = instanceId.getValue();
            AVTransportStateMachine stateMachine = stateMachines.get(id);
            if (stateMachine == null && id == 0 && createDefaultTransport) {
                log.fine("Creating default transport instance with ID '0'");
                stateMachine = createStateMachine(instanceId);
                stateMachines.put(id, stateMachine);
            } else if (stateMachine == null) {
                throw new AVTransportException(AVTransportErrorCode.INVALID_INSTANCE_ID);
            }
			if (log.isLoggable(Level.FINE)) {
				log.fine("Found transport control with ID '" + id + "'");
			}
			return stateMachine;
        }
    }

    protected AVTransportStateMachine createStateMachine(UnsignedIntegerFourBytes instanceId) {
        // Create a proxy that delegates all calls to the right state implementation, working on the T state
        return StateMachineBuilder.build(
                stateMachineDefinition,
                initialState,
                new Class[]{transportClass},
                new Object[]{createTransport(instanceId, getLastChange())}
        );
    }

    protected AVTransport createTransport(UnsignedIntegerFourBytes instanceId, LastChange lastChange) {
        return new AVTransport(instanceId, lastChange, StorageMedium.NETWORK);
    }

}
