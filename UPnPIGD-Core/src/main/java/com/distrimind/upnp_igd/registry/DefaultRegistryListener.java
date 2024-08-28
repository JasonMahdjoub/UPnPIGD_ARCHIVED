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

package com.distrimind.upnp_igd.registry;

import com.distrimind.upnp_igd.model.meta.Device;
import com.distrimind.upnp_igd.model.meta.LocalDevice;
import com.distrimind.upnp_igd.model.meta.RemoteDevice;

/**
 * Convenience class, provides empty implementations of all methods.
 * <p>
 * Also unifies local and remote device additions and removals with
 * {@link #deviceAdded(Registry, Device)} and
 * {@link #deviceRemoved(Registry, Device)} methods.
 * </p>
 *
 * @author Christian Bauer
 */
public class DefaultRegistryListener implements RegistryListener {

    @Override
	public void remoteDeviceDiscoveryStarted(Registry registry, RemoteDevice device) {

    }

    @Override
	public void remoteDeviceDiscoveryFailed(Registry registry, RemoteDevice device, Exception ex) {

    }

    /**
     * Calls the {@link #deviceAdded(Registry, Device)} method.
     *
     * @param registry The UPnPIGD registry of all devices and services know to the local UPnP stack.
     * @param device   A validated and hydrated device metadata graph, with complete service metadata.
     */
    @Override
	public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
        deviceAdded(registry, device);
    }

    @Override
	public void remoteDeviceUpdated(Registry registry, RemoteDevice device) {

    }

    /**
     * Calls the {@link #deviceRemoved(Registry, Device)} method.
     *
     * @param registry The UPnPIGD registry of all devices and services know to the local UPnP stack.
     * @param device   A validated and hydrated device metadata graph, with complete service metadata.
     */
    @Override
	public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
        deviceRemoved(registry, device);
    }

    /**
     * Calls the {@link #deviceAdded(Registry, Device)} method.
     *
     * @param registry The UPnPIGD registry of all devices and services know to the local UPnP stack.
     * @param device   The local device added to the {@link Registry}.
     */
    @Override
	public void localDeviceAdded(Registry registry, LocalDevice<?> device) {
        deviceAdded(registry, device);
    }

    /**
     * Calls the {@link #deviceRemoved(Registry, Device)} method.
     *
     * @param registry The UPnPIGD registry of all devices and services know to the local UPnP stack.
     * @param device   The local device removed from the {@link Registry}.
     */
    @Override
	public void localDeviceRemoved(Registry registry, LocalDevice<?> device) {
        deviceRemoved(registry, device);
    }

    public void deviceAdded(Registry registry, Device<?, ?, ?> device) {
        
    }

    public void deviceRemoved(Registry registry, Device<?, ?, ?> device) {

    }

    @Override
	public void beforeShutdown(Registry registry) {

    }

    @Override
	public void afterShutdown() {

    }
}
