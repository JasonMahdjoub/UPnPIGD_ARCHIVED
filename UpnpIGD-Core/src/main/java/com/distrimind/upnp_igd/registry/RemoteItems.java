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

import com.distrimind.upnp_igd.model.resource.Resource;
import com.distrimind.upnp_igd.model.gena.CancelReason;
import com.distrimind.upnp_igd.model.gena.RemoteGENASubscription;
import com.distrimind.upnp_igd.model.meta.LocalDevice;
import com.distrimind.upnp_igd.model.meta.RemoteDevice;
import com.distrimind.upnp_igd.model.meta.RemoteDeviceIdentity;
import com.distrimind.upnp_igd.model.types.UDN;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Internal class, required by {@link RegistryImpl}.
 *
 * @author Christian Bauer
 */
class RemoteItems extends RegistryItems<RemoteDevice, RemoteGENASubscription> {

    private static final Logger log = Logger.getLogger(Registry.class.getName());

    RemoteItems(RegistryImpl registry) {
        super(registry);
    }

    /**
     * Adds the given remote device to the registry, or udpates its expiration timestamp.
     * <p>
     * This method first checks if there is a remote device with the same UDN already registered. If so, it
     * updates the expiration timestamp of the remote device without notifying any registry listeners. If the
     * device is truly new, all its resources are tested for conflicts with existing resources in the registry's
     * namespace, then it is added to the registry and listeners are notified that a new fully described remote
     * device is now available.
   
     *
     * @param device The remote device to be added
     */
	@Override
	void add(final RemoteDevice device) {

        if (update(device.getIdentity())) {
			if (log.isLoggable(Level.FINE)) {
				log.fine("Ignoring addition, device already registered: " + device);
			}
			return;
        }
        Collection<Resource<?>> r=getResources(device);
        for (Resource<?> deviceResource : r) {
			if (log.isLoggable(Level.FINE)) {
				log.fine("Validating remote device resource; " + deviceResource);
			}
			if (registry.getResource(deviceResource.getPathQuery()) != null) {
                throw new RegistrationException("URI namespace conflict with already registered resource: " + deviceResource);
            }
        }

        for (Resource<?> validatedResource : r) {
            registry.addResource(validatedResource);
			if (log.isLoggable(Level.FINE)) {
				log.fine("Added remote device resource: " + validatedResource);
			}
		}

        // Override the device's maximum age if configured (systems without multicast support)
		Integer maxAgeSeconds = null;
		if (registry.getConfiguration() != null) {
			maxAgeSeconds = registry.getConfiguration().getRemoteDeviceMaxAgeSeconds();
		}
		if (maxAgeSeconds == null) {
			maxAgeSeconds = device.getIdentity().getMaxAgeSeconds();
		}

		RegistryItem<UDN, RemoteDevice> item = new RegistryItem<>(device.getIdentity().getUdn(), device, maxAgeSeconds);
		if (log.isLoggable(Level.FINE))
			log.fine("Adding hydrated remote device to registry with " + item.getExpirationDetails().getMaxAgeSeconds()
				+ " seconds expiration: " + device);
		getDeviceItems().add(item);

        if (log.isLoggable(Level.FINEST)) {
            StringBuilder sb = new StringBuilder();
            sb.append("\n");
            sb.append("-------------------------- START Registry Namespace -----------------------------------\n");
            for (Resource<?> resource : registry.getResources()) {
                sb.append(resource).append("\n");
            }
            sb.append("-------------------------- END Registry Namespace -----------------------------------");
            log.finest(sb.toString());
        }

        // Only notify the listeners when the device is fully usable
		if (log.isLoggable(Level.FINE)) {
			log.fine("Completely hydrated remote device graph available, calling listeners: " + device);
		}
		for (final RegistryListener listener : registry.getListeners()) {
            registry.getConfiguration().getRegistryListenerExecutor().execute(
					() -> listener.remoteDeviceAdded(registry, device)
			);
        }

    }

    boolean update(RemoteDeviceIdentity rdIdentity) {

        for (LocalDevice<?> localDevice : registry.getLocalDevices()) {
            if (localDevice.findDevice(rdIdentity.getUdn()) != null) {
                log.fine("Ignoring update, a local device graph contains UDN");
                return true;
            }
        }

        RemoteDevice registeredRemoteDevice = get(rdIdentity.getUdn(), false);
        if (registeredRemoteDevice != null) {

            if (!registeredRemoteDevice.isRoot()) {
				if (log.isLoggable(Level.FINE)) {
					log.fine("Updating root device of embedded: " + registeredRemoteDevice);
				}
				registeredRemoteDevice = registeredRemoteDevice.getRoot();
            }

            // Override the device's maximum age if configured (systems without multicast support)
            final RegistryItem<UDN, RemoteDevice> item = new RegistryItem<>(
                    registeredRemoteDevice.getIdentity().getUdn(),
                    registeredRemoteDevice,
                    registry.getConfiguration().getRemoteDeviceMaxAgeSeconds() != null
                            ? registry.getConfiguration().getRemoteDeviceMaxAgeSeconds()
                            : rdIdentity.getMaxAgeSeconds()
            );

			if (log.isLoggable(Level.FINE)) {
				log.fine("Updating expiration of: " + registeredRemoteDevice);
			}
			getDeviceItems().remove(item);
            getDeviceItems().add(item);

			if (log.isLoggable(Level.FINE)) {
				log.fine("Remote device updated, calling listeners: " + registeredRemoteDevice);
			}
			for (final RegistryListener listener : registry.getListeners()) {
                registry.getConfiguration().getRegistryListenerExecutor().execute(
						() -> listener.remoteDeviceUpdated(registry, item.getItem())
				);
            }

            return true;

        }
        return false;
    }

    /**
     * Removes the given device from the registry and notifies registry listeners.
     *
     * @param remoteDevice The device to remove from the registry.
     * @return <code>true</code> if the given device was found and removed from the registry, false if it wasn't registered.
     */
	@Override
	boolean remove(final RemoteDevice remoteDevice) {
        return remove(remoteDevice, false);
    }

    boolean remove(final RemoteDevice remoteDevice, boolean shuttingDown) throws RegistrationException {
        final RemoteDevice registeredDevice = get(remoteDevice.getIdentity().getUdn(), true);
        if (registeredDevice != null) {

			if (log.isLoggable(Level.FINE)) {
				log.fine("Removing remote device from registry: " + remoteDevice);
			}

			// Resources
            for (Resource<?> deviceResource : getResources(registeredDevice)) {
                if (registry.removeResource(deviceResource)) {
					if (log.isLoggable(Level.FINE)) {
						log.fine("Unregistered resource: " + deviceResource);
					}
				}
            }

            // Active subscriptions
            Iterator<RegistryItem<String, RemoteGENASubscription>> it = getSubscriptionItems().iterator();
            while (it.hasNext()) {
                final RegistryItem<String, RemoteGENASubscription> outgoingSubscription = it.next();

                UDN subscriptionForUDN =
                        outgoingSubscription.getItem().getService().getDevice().getIdentity().getUdn();

                if (subscriptionForUDN.equals(registeredDevice.getIdentity().getUdn())) {
					if (log.isLoggable(Level.FINE)) {
						log.fine("Removing outgoing subscription: " + outgoingSubscription.getKey());
					}
					it.remove();
                    if (!shuttingDown) {
                        registry.getConfiguration().getRegistryListenerExecutor().execute(
								() -> outgoingSubscription.getItem().end(CancelReason.DEVICE_WAS_REMOVED, null)
						);
                    }
                }
            }

            // Only notify listeners if we are NOT in the process of shutting down the registry
            if (!shuttingDown) {
                for (final RegistryListener listener : registry.getListeners()) {
                    registry.getConfiguration().getRegistryListenerExecutor().execute(
							() -> listener.remoteDeviceRemoved(registry, registeredDevice)
					);
                }
            }

            // Finally, remove the device from the registry
            getDeviceItems().remove(new RegistryItem<UDN, RemoteDevice>(registeredDevice.getIdentity().getUdn()));

            return true;
        }

        return false;
    }

    @Override
	void removeAll() {
        removeAll(false);
    }

    void removeAll(boolean shuttingDown) {
        for (RemoteDevice device : get()) {
            remove(device, shuttingDown);
        }
    }

    /* ############################################################################################################ */

    void start() {
        // Noop
    }

    @Override
	void maintain() {

        if (getDeviceItems().isEmpty()) return;

        // Remove expired remote devices
        Map<UDN, RemoteDevice> expiredRemoteDevices = new HashMap<>();
        for (RegistryItem<UDN, RemoteDevice> remoteItem : getDeviceItems()) {
            if (log.isLoggable(Level.FINEST))
                log.finest("Device '" + remoteItem.getItem() + "' expires in seconds: "
                                   + remoteItem.getExpirationDetails().getSecondsUntilExpiration());
            if (remoteItem.getExpirationDetails().hasExpired(false)) {
                expiredRemoteDevices.put(remoteItem.getKey(), remoteItem.getItem());
            }
        }
        for (RemoteDevice remoteDevice : expiredRemoteDevices.values()) {
            if (log.isLoggable(Level.FINE))
                log.fine("Removing expired: " + remoteDevice);
            remove(remoteDevice);
        }

        // Renew outgoing subscriptions
        Set<RemoteGENASubscription> expiredOutgoingSubscriptions = new HashSet<>();
        for (RegistryItem<String, RemoteGENASubscription> item : getSubscriptionItems()) {
            if (item.getExpirationDetails().hasExpired(true)) {
                expiredOutgoingSubscriptions.add(item.getItem());
            }
        }
        for (RemoteGENASubscription subscription : expiredOutgoingSubscriptions) {
            if (log.isLoggable(Level.FINE))
                log.fine("Renewing outgoing subscription: " + subscription);
            renewOutgoingSubscription(subscription);
        }
    }

    public void resume() {
        log.fine("Updating remote device expiration timestamps on resume");
        List<RemoteDeviceIdentity> toUpdate = new ArrayList<>();
        for (RegistryItem<UDN, RemoteDevice> remoteItem : getDeviceItems()) {
            toUpdate.add(remoteItem.getItem().getIdentity());
        }
        for (RemoteDeviceIdentity identity : toUpdate) {
            update(identity);
        }
    }

    @Override
	void shutdown() {
        log.fine("Cancelling all outgoing subscriptions to remote devices during shutdown");
        List<RemoteGENASubscription> remoteSubscriptions = new ArrayList<>();
        for (RegistryItem<String, RemoteGENASubscription> item : getSubscriptionItems()) {
            remoteSubscriptions.add(item.getItem());
        }
        for (RemoteGENASubscription remoteSubscription : remoteSubscriptions) {
            // This will remove the active subscription from the registry!
            registry.getProtocolFactory()
                    .createSendingUnsubscribe(remoteSubscription)
                    .run();
        }

        log.fine("Removing all remote devices from registry during shutdown");
        removeAll(true);
    }

    /* ############################################################################################################ */

    protected void renewOutgoingSubscription(final RemoteGENASubscription subscription) {
        registry.executeAsyncProtocol(
                registry.getProtocolFactory().createSendingRenewal(subscription)
        );
    }
}
