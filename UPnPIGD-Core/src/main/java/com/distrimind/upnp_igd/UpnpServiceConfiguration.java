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

package com.distrimind.upnp_igd;

import com.distrimind.upnp_igd.binding.xml.DeviceDescriptorBinder;
import com.distrimind.upnp_igd.binding.xml.ServiceDescriptorBinder;
import com.distrimind.upnp_igd.model.UserConstants;
import com.distrimind.upnp_igd.model.profile.ClientInfo;
import com.distrimind.upnp_igd.registry.Registry;
import com.distrimind.upnp_igd.transport.Router;
import com.distrimind.upnp_igd.model.Namespace;
import com.distrimind.upnp_igd.model.message.UpnpHeaders;
import com.distrimind.upnp_igd.model.meta.RemoteDeviceIdentity;
import com.distrimind.upnp_igd.model.meta.RemoteService;
import com.distrimind.upnp_igd.model.types.ServiceType;
import com.distrimind.upnp_igd.transport.spi.DatagramIO;
import com.distrimind.upnp_igd.transport.spi.DatagramProcessor;
import com.distrimind.upnp_igd.transport.spi.GENAEventProcessor;
import com.distrimind.upnp_igd.transport.spi.MulticastReceiver;
import com.distrimind.upnp_igd.transport.spi.NetworkAddressFactory;
import com.distrimind.upnp_igd.transport.spi.SOAPActionProcessor;
import com.distrimind.upnp_igd.transport.spi.StreamClient;
import com.distrimind.upnp_igd.transport.spi.StreamServer;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

/**
 * Shared configuration data of the UPnP stack.
 * <p>
 * This interface offers methods for retrieval of configuration data by the
 * {@link Router} and the {@link Registry},
 * as well as other parts of the UPnP stack.
 * </p>
 * <p>
 * You can re-use this interface if you implement a subclass of {@link UpnpServiceImpl} or
 * if you create a new implementation of {@link UpnpService}.
 * </p>
 *
 * @author Christian Bauer
 */
public interface UpnpServiceConfiguration {

    /**
     * @return A new instance of the {@link NetworkAddressFactory} interface.
     */
	NetworkAddressFactory createNetworkAddressFactory();

    /**
     * @return The shared implementation of {@link DatagramProcessor}.
     */
	DatagramProcessor getDatagramProcessor();

    /**
     * @return The shared implementation of {@link SOAPActionProcessor}.
     */
	SOAPActionProcessor getSoapActionProcessor();

    /**
     * @return The shared implementation of {@link GENAEventProcessor}.
     */
	GENAEventProcessor getGenaEventProcessor();

    /**
     * @return A new instance of the {@link StreamClient} interface.
     */
	StreamClient<?> createStreamClient();

    /**
     * @param networkAddressFactory The configured {@link NetworkAddressFactory}.
     * @return A new instance of the {@link MulticastReceiver} interface.
     */
	MulticastReceiver<?> createMulticastReceiver(NetworkAddressFactory networkAddressFactory);

    /**
     * @param networkAddressFactory The configured {@link NetworkAddressFactory}.
     * @return A new instance of the {@link DatagramIO} interface.
     */
	DatagramIO<?> createDatagramIO(NetworkAddressFactory networkAddressFactory);

    /**
     * @param networkAddressFactory The configured {@link NetworkAddressFactory}.
     * @return A new instance of the {@link StreamServer} interface.
     */
	StreamServer<?> createStreamServer(NetworkAddressFactory networkAddressFactory);

    /**
     * @return The executor which runs the listening background threads for multicast datagrams.
     */
	Executor getMulticastReceiverExecutor();

    /**
     * @return The executor which runs the listening background threads for unicast datagrams.
     */
	Executor getDatagramIOExecutor();

    /**
     * @return The executor which runs the listening background threads for HTTP requests.
     */
	ExecutorService getStreamServerExecutorService();

    /**
     * @return The shared implementation of {@link DeviceDescriptorBinder} for the UPnP 1.0 Device Architecture.
     */
	DeviceDescriptorBinder getDeviceDescriptorBinderUDA10();

    /**
     * @return The shared implementation of {@link ServiceDescriptorBinder} for the UPnP 1.0 Device Architecture.
     */
	ServiceDescriptorBinder getServiceDescriptorBinderUDA10();

    /**
     * Returns service types that can be handled by this UPnP stack, all others will be ignored.
     * <p>
     * Return <code>null</code> to completely disable remote device and service discovery.
     * All incoming notifications and search responses will then be dropped immediately.
     * This is mostly useful in applications that only provide services with no (remote)
     * control point functionality.
   
     * <p>
     * Note that a discovered service type with version 2 or 3 will match an exclusive
     * service type with version 1. UPnP services are required to be backwards
     * compatible, version 2 is a superset of version 1, and version 3 is a superset
     * of version 2, etc.
   
     *
     * @return An array of service types that are exclusively discovered, no other service will
     *         be discovered. A <code>null</code> return value will disable discovery!
     *         An empty array means all services will be discovered.
     */
	ServiceType[] getExclusiveServiceTypes();

    /**
     * @return The time in milliseconds to wait between each registry maintenance operation.
     */
	int getRegistryMaintenanceIntervalMillis();
    
    /**
     * Optional setting for flooding alive NOTIFY messages for local devices.
     * <p>
     * Use this to advertise local devices at the specified interval, independent of its
     *  value. Note
     * that this will increase network traffic.
   
     * <p>
     * Some control points (XBMC and other Platinum UPnP SDK based devices, OPPO-93) seem
     * to not properly receive SSDP M-SEARCH replies sent by UPnPIGD, but will handle NOTIFY
     * alive messages just fine.
   
     *
     * @return The time in milliseconds for ALIVE message intervals, set to <code>0</code> to disable
     */
	int getAliveIntervalMillis();

    /**
     * Ignore the received event subscription timeout from remote control points.
     * <p>
     * Some control points have trouble renewing subscriptions properly; enabling this option
     * in conjunction with a high value for
     * {@link UserConstants#DEFAULT_SUBSCRIPTION_DURATION_SECONDS}
     * ensures that your devices will not disappear on such control points.
   
     *
     * @return <code>true</code> if the timeout in incoming event subscriptions should be ignored
     *         and the default value ({@link UserConstants#DEFAULT_SUBSCRIPTION_DURATION_SECONDS})
     *         should be used instead.
     *
     */
	boolean isReceivedSubscriptionTimeoutIgnored();

    /**
     * Returns the time in seconds a remote device will be registered until it is expired.
     * <p>
     * This setting is useful on systems which do not support multicast networking
     * (Android on HTC phones, for example). On such a system you will not receive messages when a
     * remote device disappears from the network, and you will not receive its periodic heartbeat
     * alive messages. Only an initial search response (UDP unicast) has been received from the
     * remote device, with its proposed maximum age. To avoid (early) expiration of the remote
     * device, you can override its maximum age with this configuration setting, ignoring the
     * initial maximum age sent by the device. You most likely want to return
     * <code>0</code> in this case, so that the remote device is never expired unless you
     * manually remove it from the {@link Registry}. You typically remove
     * the device when an action or GENA subscription request to the remote device failed.
   
     *
     * @return <code>null</code> (the default) to accept the remote device's proposed maximum age, or
     *         <code>0</code> for unlimited age, or a value in seconds.
     */
	Integer getRemoteDeviceMaxAgeSeconds();

    /**
     * Optional extra headers for device descriptor retrieval HTTP requests.
     * <p>
     * Some devices might require extra headers to recognize your control point, use this
     * method to set these headers. They will be used for every descriptor (XML) retrieval
     * HTTP request by UPnPIGD. See {@link ClientInfo} for
     * action request messages.
   
     *
     * @param identity The (so far) discovered identity of the remote device.
     * @return <code>null</code> or extra HTTP headers.
     */
	@SuppressWarnings("PMD.LooseCoupling")
	UpnpHeaders getDescriptorRetrievalHeaders(RemoteDeviceIdentity identity);

    /**
     * Optional extra headers for event subscription (almost HTTP) messages.
     * <p>
     * Some devices might require extra headers to recognize your control point, use this
     * method to set these headers for GENA subscriptions. Note that the headers will
     * not be applied to actual event messages, only subscribe, unsubscribe, and renewal.
   
     *
     * @return <code>null</code> or extra HTTP headers.
     */
	@SuppressWarnings("PMD.LooseCoupling")
	UpnpHeaders getEventSubscriptionHeaders(RemoteService service);

    /**
     * @return The executor which runs the processing of asynchronous aspects of the UPnP stack (discovery).
     */
	Executor getAsyncProtocolExecutor();

    /**
     * @return The executor service which runs the processing of synchronous aspects of the UPnP stack (description, control, GENA).
     */
	ExecutorService getSyncProtocolExecutorService();

    /**
     * @return An instance of {@link Namespace} for this UPnP stack.
     */
	Namespace getNamespace();

    /**
     * @return The executor which runs the background thread for maintaining the registry.
     */
	Executor getRegistryMaintainerExecutor();

    /**
     * @return The executor which runs the notification threads of registry listeners.
     */
	Executor getRegistryListenerExecutor();

    /**
     * Called by the {@link UpnpService} on shutdown, useful to e.g. shutdown thread pools.
     */
	void shutdown();

}
