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

package com.distrimind.upnp_igd.support.igd;

import com.distrimind.upnp_igd.support.igd.callback.PortMappingAdd;
import com.distrimind.upnp_igd.support.igd.callback.PortMappingDelete;
import com.distrimind.upnp_igd.model.action.ActionInvocation;
import com.distrimind.upnp_igd.model.message.UpnpResponse;
import com.distrimind.upnp_igd.model.meta.Device;
import com.distrimind.upnp_igd.model.meta.DeviceIdentity;
import com.distrimind.upnp_igd.model.meta.RemoteDeviceIdentity;
import com.distrimind.upnp_igd.model.meta.Service;
import com.distrimind.upnp_igd.model.types.DeviceType;
import com.distrimind.upnp_igd.model.types.ServiceType;
import com.distrimind.upnp_igd.model.types.UDADeviceType;
import com.distrimind.upnp_igd.model.types.UDAServiceType;
import com.distrimind.upnp_igd.registry.DefaultRegistryListener;
import com.distrimind.upnp_igd.registry.Registry;
import com.distrimind.upnp_igd.support.model.PortMapping;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Maintains UPnP port mappings on an InternetGatewayDevice automatically.
 * <p>
 * This listener will wait for discovered devices which support either
 * {@code WANIPConnection} or the {@code WANPPPConnection} service. As soon as any such
 * service is discovered, the desired port mapping will be created. When the UPnP service
 * is shutting down, all previously established port mappings with all services will
 * be deleted.
 * </p>
 * <p>
 * The following listener maps external WAN TCP port 8123 to internal host 10.0.0.2:
 * </p>
 * <pre>{@code
 * upnpService.getRegistry().addListener(
 *newPortMappingListener(newPortMapping(8123, "10.0.0.2",PortMapping.Protocol.TCP))
 * );}</pre>
 * <p>
 * If all you need from the UPnPIGD UPnP stack is NAT port mapping, use the following idiom:
 * </p>
 * <pre>{@code
 * UpnpService upnpService = new UpnpServiceImpl(
 *     new PortMappingListener(new PortMapping(8123, "10.0.0.2", PortMapping.Protocol.TCP))
 * );
 * <p/>
 * upnpService.getControlPoint().search(new STAllHeader()); // Search for all devices
 * <p/>
 * upnpService.shutdown(); // When you no longer need the port mapping
 * }</pre>
 *
 * @author Christian Bauer
 * @author Richard Maw - Nullable internalClient, callbacks, InternetGatewayDevice:2
 */
public class PortMappingListener extends DefaultRegistryListener {

    private static final Logger log = Logger.getLogger(PortMappingListener.class.getName());

    public static final DeviceType IGD_DEVICE_TYPE_V1 = new UDADeviceType("InternetGatewayDevice", 1);
    public static final DeviceType IGD_DEVICE_TYPE_V2 = new UDADeviceType("InternetGatewayDevice", 2);
    public static final DeviceType CONNECTION_DEVICE_TYPE_V1 = new UDADeviceType("WANConnectionDevice", 1);
    public static final DeviceType CONNECTION_DEVICE_TYPE_V2 = new UDADeviceType("WANConnectionDevice", 2);

    public static final ServiceType IP_SERVICE_TYPE_V1 = new UDAServiceType("WANIPConnection", 1);
    public static final ServiceType IP_SERVICE_TYPE_V2 = new UDAServiceType("WANIPConnection", 2);
    public static final ServiceType PPP_SERVICE_TYPE = new UDAServiceType("WANPPPConnection", 1);

    protected List<PortMapping> portMappings;

    // The key of the map is Service and equality is object identity, this is by-design
    protected Map<Service<?, ?, ?>, List<PortMapping>> activePortMappings = new HashMap<>();

    public PortMappingListener(PortMapping portMapping) {
        this(List.of(portMapping));
    }

    public PortMappingListener(List<PortMapping> portMappings) {
        this.portMappings = portMappings;
    }

    @Override
    synchronized public void deviceAdded(Registry registry, Device<?, ?, ?> device) {

        Service<?, ?, ?> connectionService;
        if ((connectionService = discoverConnectionService(device)) == null) return;
        handleInternetGatewayDeviceFound(connectionService);

        String defaultInternalClient = null;
		final List<PortMapping> activeForService = new ArrayList<>();
        for (final PortMapping pm : portMappings) {
            final PortMapping newPm;
            if (pm.getInternalClient() != null) {
                newPm = pm;
            } else {
                if (defaultInternalClient == null) {
                    DeviceIdentity deviceIdentity = device.getIdentity();
                    if (!(deviceIdentity instanceof RemoteDeviceIdentity)) {
                        handleFailureMessage("Found a non-remote IGD, can't determine default internal client address");
                        continue;
                    }
                    RemoteDeviceIdentity remoteDeviceIdentity = (RemoteDeviceIdentity) deviceIdentity;
                    defaultInternalClient = remoteDeviceIdentity.getDiscoveredOnLocalAddress().getHostAddress();
                }

                newPm = new PortMapping();
                newPm.setEnabled(pm.isEnabled());
                newPm.setLeaseDurationSeconds(pm.getLeaseDurationSeconds());
                newPm.setRemoteHost(pm.getRemoteHost());
                newPm.setExternalPort(pm.getExternalPort());
                newPm.setInternalPort(pm.getInternalPort());
                newPm.setProtocol(pm.getProtocol());
                newPm.setDescription(pm.getDescription());
                newPm.setInternalClient(defaultInternalClient);
            }

            new PortMappingAdd(connectionService, registry.getUpnpService().getControlPoint(), newPm) {

                @Override
                public void success(ActionInvocation<?> invocation) {
                    handleSuccessfulMapping(connectionService, newPm, invocation);
                    activeForService.add(newPm);
                }

                @Override
                public void failure(ActionInvocation<?> invocation, UpnpResponse operation, String defaultMsg) {
                    handleFailedMapping(connectionService, newPm, invocation, operation, defaultMsg);
                }
            }.run(); // Synchronous!
        }

        activePortMappings.put(connectionService, activeForService);
    }

    @Override
    synchronized public void deviceRemoved(Registry registry, Device<?, ?, ?> device) {
        for (Service<?, ?, ?> service : device.findServices()) {
            Iterator<Map.Entry<Service<?, ?, ?>, List<PortMapping>>> it = activePortMappings.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<Service<?, ?, ?>, List<PortMapping>> activeEntry = it.next();
                if (!activeEntry.getKey().equals(service)) continue;

                if (!activeEntry.getValue().isEmpty())
                    handleFailureMessage("Device disappeared, couldn't delete port mappings: " + activeEntry.getValue().size());

                it.remove();
            }
        }
    }

    @Override
    synchronized public void beforeShutdown(Registry registry) {
        for (Map.Entry<Service<?, ?, ?>, List<PortMapping>> activeEntry : activePortMappings.entrySet()) {

            final Service connectionService = activeEntry.getKey();
            final Iterator<PortMapping> it = activeEntry.getValue().iterator();
            while (it.hasNext()) {
                final PortMapping pm = it.next();
				if (log.isLoggable(Level.FINE)) {
					log.fine("Trying to delete port mapping on IGD: " + pm);
				}
                new PortMappingDelete(connectionService, registry.getUpnpService().getControlPoint(), pm) {

                    @Override
                    public void success(ActionInvocation<?> invocation) {
                        handleSuccessfulUnmapping(connectionService, pm, invocation);
						it.remove();
                    }

                    @Override
                    public void failure(ActionInvocation<?> invocation, UpnpResponse operation, String defaultMsg) {
                        handleFailedUnmapping(connectionService, pm, invocation, operation, defaultMsg);
                    }

                }.run(); // Synchronous!
            }
        }
    }

    protected Service<?, ?, ?> discoverConnectionService(Device<?, ?, ?> device) {
        DeviceType deviceType = device.getType();
        if (!deviceType.equals(IGD_DEVICE_TYPE_V1) && !deviceType.equals(IGD_DEVICE_TYPE_V2)) {
            return null;
        }

        Collection<? extends Device<?, ?, ?>> connectionDevices = device.findDevices(CONNECTION_DEVICE_TYPE_V2);
        if (connectionDevices.isEmpty()) {
            if (log.isLoggable(Level.FINE)) {
                log.fine("IGD doesn't support '" + CONNECTION_DEVICE_TYPE_V2 + "': " + device);
            }
            connectionDevices = device.findDevices(CONNECTION_DEVICE_TYPE_V1);
        }
        if (connectionDevices.isEmpty()) {
            if (log.isLoggable(Level.FINE)) {
                log.fine("IGD doesn't support '" + CONNECTION_DEVICE_TYPE_V1 + "': " + device);
            }
            return null;
        }

        Device<?, ?, ?> connectionDevice = connectionDevices.iterator().next();
		if (log.isLoggable(Level.FINE)) {
			log.fine("Using first discovered WAN connection device: " + connectionDevice);
		}

        Service<?, ?, ?> connectionService = connectionDevice.findService(IP_SERVICE_TYPE_V2);
        if (connectionService == null) {
            connectionService = connectionDevice.findService(IP_SERVICE_TYPE_V1);
        }
        if (connectionService == null) {
            connectionService = connectionDevice.findService(PPP_SERVICE_TYPE);
        }

        if (connectionService == null) {
			if (log.isLoggable(Level.FINE)) {
				log.fine("IGD doesn't support IP or PPP WAN connection service: " + device);
			}
		}

        return connectionService;
    }

    protected void handleFailureMessage(String s) {
        log.warning(s);
    }

    protected void handleInternetGatewayDeviceFound(Service service) {
        if (log.isLoggable(Level.FINE)) {
            log.fine("Activating port mappings on: " + service);
        }
    }

    protected void handleSuccessfulMapping(Service service, PortMapping pm, ActionInvocation invocation) {
        if (log.isLoggable(Level.FINE)) {
            log.fine("Port mapping added: " + pm);
        }
    }

    protected void handleFailedMapping(Service service, PortMapping pm, ActionInvocation invocation,
            UpnpResponse operation, String defaultMsg) {
        handleFailureMessage("Failed to add port mapping: " + pm);
        handleFailureMessage("Reason: " + defaultMsg);
    }

    protected void handleSuccessfulUnmapping(Service service, PortMapping pm, ActionInvocation invocation) {
        if (log.isLoggable(Level.FINE)) {
            log.fine("Port mapping deleted: " + pm);
        }
    }

    protected void handleFailedUnmapping(Service service, PortMapping pm, ActionInvocation invocation,
            UpnpResponse operation, String defaultMsg) {
        handleFailureMessage("Failed to delete port mapping: " + pm);
        handleFailureMessage("Reason: " + defaultMsg);
    }
}

