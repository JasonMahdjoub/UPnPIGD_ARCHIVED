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

package com.distrimind.upnp_igd.android;

import android.os.Build;
import com.distrimind.upnp_igd.DefaultUpnpServiceConfiguration;
import com.distrimind.upnp_igd.android.transport.impl.AsyncServletStreamServerConfigurationImpl;
import com.distrimind.upnp_igd.android.transport.impl.AsyncServletStreamServerImpl;
import com.distrimind.upnp_igd.android.transport.impl.jetty.JettyServletContainer;
import com.distrimind.upnp_igd.android.transport.impl.jetty.StreamClientConfigurationImpl;
import com.distrimind.upnp_igd.android.transport.impl.jetty.JettyStreamClientImpl;
import com.distrimind.upnp_igd.binding.xml.DeviceDescriptorBinder;
import com.distrimind.upnp_igd.binding.xml.RecoveringUDA10DeviceDescriptorBinderImpl;
import com.distrimind.upnp_igd.binding.xml.ServiceDescriptorBinder;
import com.distrimind.upnp_igd.binding.xml.UDA10ServiceDescriptorBinderSAXImpl;
import com.distrimind.upnp_igd.model.Constants;
import com.distrimind.upnp_igd.model.Namespace;
import com.distrimind.upnp_igd.model.ServerClientTokens;
import com.distrimind.upnp_igd.registry.Registry;
import com.distrimind.upnp_igd.transport.impl.NetworkAddressFactoryImpl;
import com.distrimind.upnp_igd.transport.impl.RecoveringGENAEventProcessorImpl;
import com.distrimind.upnp_igd.transport.impl.RecoveringSOAPActionProcessorImpl;
import com.distrimind.upnp_igd.transport.spi.*;
import jakarta.enterprise.inject.Alternative;

/**
 * Configuration settings for deployment on Android.
 * <p>
 * This configuration utilizes the Jetty transport implementation
 * found in {@link com.distrimind.upnp_igd.android.transport.impl.jetty} for TCP/HTTP networking, as
 * client and server. The servlet context path for UPnP is set to <code>/upnp</code>.
 * </p>
 * <p>
 * The kxml2 implementation of <code>org.xmlpull</code> is available on Android, therefore
 * this configuration uses {@link RecoveringUDA10DeviceDescriptorBinderImpl},
 * {@link com.distrimind.upnp_igd.transport.impl.RecoveringSOAPActionProcessorImpl}, and {@link com.distrimind.upnp_igd.transport.impl.RecoveringGENAEventProcessorImpl}.
 * </p>
 * <p>
 * This configuration utilizes {@link UDA10ServiceDescriptorBinderSAXImpl}, the system property
 * <code>org.xml.sax.driver</code> is set to  <code>org.xmlpull.v1.sax2.Driver</code>.
 * </p>
 * <p>
 * To preserve battery, the {@link Registry} will only
 * be maintained every 3 seconds.
 * </p>
 *
 * @author Christian Bauer
 */
@Alternative
public class AndroidUpnpServiceConfiguration extends DefaultUpnpServiceConfiguration {

    public AndroidUpnpServiceConfiguration() {
        this(NetworkAddressFactoryImpl.DEFAULT_TCP_HTTP_LISTEN_PORT, Constants.UPNP_MULTICAST_PORT);
    }

    public AndroidUpnpServiceConfiguration(int streamListenPort, int multicastPort) {
        super(streamListenPort, multicastPort, false);

        // This should be the default on Android 2.1, but it's not set by default
        System.setProperty("org.xml.sax.driver", "org.xmlpull.v1.sax2.Driver");
    }

    @Override
    protected NetworkAddressFactory createNetworkAddressFactory(int streamListenPort, int multicastPort) {
        return new AndroidNetworkAddressFactory(streamListenPort, multicastPort);
    }

    @Override
    protected Namespace createNamespace() {
        // For the Jetty server, this is the servlet context path
        return new Namespace("/upnp");
    }

    @Override
    public StreamClient<?> createStreamClient() {
        // Use Jetty
        return new JettyStreamClientImpl(
            new StreamClientConfigurationImpl(
                getSyncProtocolExecutorService()
            ) {
                @Override
                public String getUserAgentValue(int majorVersion, int minorVersion) {
                    // TODO: UPNP VIOLATION: Synology NAS requires User-Agent to contain
                    // "Android" to return DLNA protocolInfo required to stream to Samsung TV
			        // see: http://two-play.com/forums/viewtopic.php?f=6&t=81
                    ServerClientTokens tokens = new ServerClientTokens(majorVersion, minorVersion);
                    tokens.setOsName("Android");
                    tokens.setOsVersion(Build.VERSION.RELEASE);
                    return tokens.toString();
                }
            }
        );
    }

    @Override
    public StreamServer<?> createStreamServer(NetworkAddressFactory networkAddressFactory) {
        // Use Jetty, start/stop a new shared instance of JettyServletContainer
        return new AsyncServletStreamServerImpl(
            new AsyncServletStreamServerConfigurationImpl(
                JettyServletContainer.INSTANCE,
                networkAddressFactory.getStreamListenPort()
            )
        );
    }

    @Override
    protected DeviceDescriptorBinder createDeviceDescriptorBinderUDA10() {
        return new RecoveringUDA10DeviceDescriptorBinderImpl(getNetworkAddressFactory());
    }

    @Override
    protected ServiceDescriptorBinder createServiceDescriptorBinderUDA10() {
        return new UDA10ServiceDescriptorBinderSAXImpl(getNetworkAddressFactory());
    }

    @Override
    protected SOAPActionProcessor createSOAPActionProcessor() {
        return new RecoveringSOAPActionProcessorImpl();
    }

    @Override
    protected GENAEventProcessor createGENAEventProcessor() {
        return new RecoveringGENAEventProcessorImpl();
    }

    @Override
    public int getRegistryMaintenanceIntervalMillis() {
        return 3000; // Preserve battery on Android, only run every 3 seconds
    }

}
