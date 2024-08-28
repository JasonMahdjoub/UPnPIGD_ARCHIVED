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
package example.registry;

import com.distrimind.upnp_igd.binding.xml.DeviceDescriptorBinder;
import com.distrimind.upnp_igd.mock.MockRouter;
import com.distrimind.upnp_igd.mock.MockUpnpService;
import com.distrimind.upnp_igd.model.message.StreamResponseMessage;
import com.distrimind.upnp_igd.model.message.header.ContentTypeHeader;
import com.distrimind.upnp_igd.model.meta.LocalDevice;
import com.distrimind.upnp_igd.model.meta.RemoteDevice;
import com.distrimind.upnp_igd.model.meta.RemoteService;
import com.distrimind.upnp_igd.model.profile.RemoteClientInfo;
import com.distrimind.upnp_igd.model.types.UDAServiceId;
import com.distrimind.upnp_igd.protocol.RetrieveRemoteDescriptors;
import com.distrimind.upnp_igd.registry.DefaultRegistryListener;
import com.distrimind.upnp_igd.registry.Registry;
import com.distrimind.upnp_igd.test.data.SampleData;
import org.testng.annotations.Test;
import org.xml.sax.SAXParseException;

import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Listening to registry changes
 * <p>
 * The <code>RegistryListener</code> is your primary API when discovering devices and services with your
 * control point. UPnP operates asynchronous, so advertisements (either <em>alive</em> or <em>byebye</em>)
 * of devices can occur at any time. Responses to your network search messages are also asynchronous.
 * </p>
 * <p>
 * This is the interface:
 * </p>
 * <a class="citation" href="javacode://example.registry.RegistryListenerTest.RegistryListener"/>
 * <p>
 * Typically you don't want to implement all of these methods. Some are only useful if you write
 * a service or a generic control point. Most of the time you want to be notified when a particular
 * device with a particular service appears on your network. So it is much easier to extend
 * the <code>DefaultRegistryListener</code>, which has empty implementations for all methods of
 * the interface, and only override the methods you need.
 * </p>
 * <a class="citation" href="javadoc://this#quickstartListener" style="read-title: false"/>
 * <a class="citation" href="javadoc://this#regularListener" style="read-title: false"/>
 */
public class RegistryListenerTest {

    // Just for documentation inclusion!
    public interface RegistryListener {

        void remoteDeviceDiscoveryStarted(Registry registry, RemoteDevice device);

        void remoteDeviceDiscoveryFailed(Registry registry, RemoteDevice device, Exception ex);

        void remoteDeviceAdded(Registry registry, RemoteDevice device);

        void remoteDeviceUpdated(Registry registry, RemoteDevice device);

        void remoteDeviceRemoved(Registry registry, RemoteDevice device);

        void localDeviceAdded(Registry registry, LocalDevice<?> device);

        void localDeviceRemoved(Registry registry, LocalDevice<?> device);

    }

    /**
     * <p>
     * The <code>remoteDeviceDiscoveryStarted()</code> and <code>remoteDeviceDiscoveryFailed()</code>
     * methods are completely optional but useful on slow machines (such as Android handsets). UPnPIGD
     * will retrieve and initialize all device metadata for each UPnP device before it will announce
     * it on the <code>Registry</code>. UPnP metadata is split into several XML descriptors, so retrieval
     * via HTTP of these descriptors, parsing, and validating all metadata for a complex UPnP device
     * and service model can take several seconds. These two methods allow you to access the device
     * as soon as possible, after the first descriptor has been retrieved and parsed. At this time
     * the services metadata is however not available:
   
     * <a class="citation" href="javacode://example.registry.RegistryListenerTest.QuickstartRegistryListener" style="exclude: EXC1, EXC2;"/>
     * <p>
     * This is how you register and activate a listener:
   
     * <a class="citation" href="javacode://this" style="include: INC1"/>
     */
    @Test
    public void quickstartListener() throws Exception {

        final RemoteDevice discoveredDevice = new RemoteDevice(SampleData.createRemoteDeviceIdentity());
        final RemoteDevice hydratedDevice = SampleData.createRemoteDevice();

        MockUpnpService upnpService = new MockUpnpService() {
            @Override
            protected MockRouter createRouter() {
                return new MockRouter(getConfiguration(), getProtocolFactory()) {
                    @Override
                    public List<StreamResponseMessage> getStreamResponseMessages() {
                        try {
                            String deviceDescriptorXML =
                                getConfiguration().getDeviceDescriptorBinderUDA10().generate(
                                    hydratedDevice,
                                    new RemoteClientInfo(),
                                    getConfiguration().getNamespace()
                                );
                            String serviceOneXML =
                                getConfiguration().getServiceDescriptorBinderUDA10().generate(hydratedDevice.findServices().iterator().next());
                            String serviceTwoXML =
                                getConfiguration().getServiceDescriptorBinderUDA10().generate(hydratedDevice.findServices().iterator().next());
                            String serviceThreeXML =
                                getConfiguration().getServiceDescriptorBinderUDA10().generate(hydratedDevice.findServices().iterator().next());
                            return List.of(
                                new StreamResponseMessage(deviceDescriptorXML, ContentTypeHeader.DEFAULT_CONTENT_TYPE_UTF8),
                                new StreamResponseMessage(serviceOneXML, ContentTypeHeader.DEFAULT_CONTENT_TYPE_UTF8),
                                new StreamResponseMessage(serviceTwoXML, ContentTypeHeader.DEFAULT_CONTENT_TYPE_UTF8),
                                new StreamResponseMessage(serviceThreeXML, ContentTypeHeader.DEFAULT_CONTENT_TYPE_UTF8)
                            );
                        } catch (Exception ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                };
            }
        };

        QuickstartRegistryListener listener = new QuickstartRegistryListener(); // DOC: INC1
        upnpService.getRegistry().addListener(listener);                        // DOC: INC1

        RetrieveRemoteDescriptors retrieveDescriptors = new RetrieveRemoteDescriptors(upnpService, discoveredDevice);
        retrieveDescriptors.run();

		assertTrue(listener.valid);
    }

    @Test
    public void failureQuickstartListener() throws Exception {

        final RemoteDevice discoveredDevice = new RemoteDevice(SampleData.createRemoteDeviceIdentity());
        final RemoteDevice hydratedDevice = SampleData.createRemoteDevice();

        MockUpnpService upnpService = new MockUpnpService() {
            @Override
            protected MockRouter createRouter() {
                return new MockRouter(getConfiguration(), getProtocolFactory()) {
                    @Override
                    public List<StreamResponseMessage> getStreamResponseMessages() {
                        String deviceDescriptorXML;
                        DeviceDescriptorBinder binder = getConfiguration().getDeviceDescriptorBinderUDA10();
                        try {
                            deviceDescriptorXML =
                                binder.generate(
                                    hydratedDevice,
                                    new RemoteClientInfo(),
                                    getConfiguration().getNamespace()
                                );
                        } catch (Exception ex) {
                            throw new RuntimeException(ex);
                        }
                        return List.of(
                            new StreamResponseMessage(deviceDescriptorXML, ContentTypeHeader.DEFAULT_CONTENT_TYPE_UTF8),
                            new StreamResponseMessage(
                                "<?xml>THIS SHOULD BE SERVER DESCRIPTOR XML, BUT WE WANT IT TO FAIL WITH SAXParseException.",
                                ContentTypeHeader.DEFAULT_CONTENT_TYPE_UTF8
                            )
                                );
                    }
                };
            }
        };

        FailureQuickstartRegistryListener listener = new FailureQuickstartRegistryListener();
        upnpService.getRegistry().addListener(listener);

        RetrieveRemoteDescriptors retrieveDescriptors = new RetrieveRemoteDescriptors(upnpService, discoveredDevice);
        retrieveDescriptors.run();

		assertTrue(listener.valid);
    }

    public static class QuickstartRegistryListener extends DefaultRegistryListener {
        public boolean valid = false; // DOC: EXC1

        @Override
        public void remoteDeviceDiscoveryStarted(Registry registry, RemoteDevice device) {

            // You can already use the device here and you can see which services it will have
            assertEquals(device.findServices().size(), 3);

            // But you can't use the services
            for (RemoteService service : device.findServices()) {
                assertEquals(service.getActions().size(), 0);
                assertEquals(service.getStateVariables().size(), 0);
            }
            valid = true; // DOC: EXC2
        }

        @Override
        public void remoteDeviceDiscoveryFailed(Registry registry, RemoteDevice device, Exception ex) {
            // You might want to drop the device, its services couldn't be hydrated
        }
    }

    public static class FailureQuickstartRegistryListener extends DefaultRegistryListener {
        public boolean valid = false;

        @Override
        public void remoteDeviceDiscoveryFailed(Registry registry, RemoteDevice device, Exception ex) {
            if(ex.getCause() instanceof SAXParseException)
                valid = true;
        }
    }

    /**
     * <p>
     * Most of the time, on any device that is faster than a cellphone, your listeners will look
     * like this:
   
     * <a class="citation" href="javacode://example.registry.RegistryListenerTest.MyListener" style="exclude: EXC1, EXC2, EXC3;"/>
     * <p>
     * The device metadata of the parameter to <code>remoteDeviceAdded()</code> is fully hydrated, all
     * of its services, actions, and state variables are available. You can continue with this metadata,
     * writing action invocations and event monitoring callbacks. You also might want to react accordingly
     * when the device disappears from the network.
   
     */
    @Test
    public void regularListener() throws Exception {

        final RemoteDevice discoveredDevice = new RemoteDevice(SampleData.createRemoteDeviceIdentity());
        final RemoteDevice hydratedDevice = SampleData.createRemoteDevice();

        MockUpnpService upnpService = new MockUpnpService() {
            @Override
            protected MockRouter createRouter() {
                return new MockRouter(getConfiguration(), getProtocolFactory()) {
                    @Override
                    public List<StreamResponseMessage> getStreamResponseMessages() {
                        try {
                            String deviceDescriptorXML =
                                getConfiguration().getDeviceDescriptorBinderUDA10().generate(
                                    hydratedDevice,
                                    new RemoteClientInfo(),
                                    getConfiguration().getNamespace()
                                );
                            String serviceOneXML =
                                getConfiguration().getServiceDescriptorBinderUDA10().generate(hydratedDevice.findServices().iterator().next());
                            String serviceTwoXML =
                                getConfiguration().getServiceDescriptorBinderUDA10().generate(hydratedDevice.findServices().iterator().next());
                            String serviceThreeXML =
                                getConfiguration().getServiceDescriptorBinderUDA10().generate(hydratedDevice.findServices().iterator().next());
                            return List.of(
                                new StreamResponseMessage(deviceDescriptorXML, ContentTypeHeader.DEFAULT_CONTENT_TYPE_UTF8),
                                new StreamResponseMessage(serviceOneXML, ContentTypeHeader.DEFAULT_CONTENT_TYPE_UTF8),
                                new StreamResponseMessage(serviceTwoXML, ContentTypeHeader.DEFAULT_CONTENT_TYPE_UTF8),
                                new StreamResponseMessage(serviceThreeXML, ContentTypeHeader.DEFAULT_CONTENT_TYPE_UTF8)
                            );
                        } catch (Exception ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                };
            }
        };

        MyListener listener = new MyListener();
        upnpService.getRegistry().addListener(listener);

        RetrieveRemoteDescriptors retrieveDescriptors = new RetrieveRemoteDescriptors(upnpService, discoveredDevice);
        retrieveDescriptors.run();

        upnpService.getRegistry().removeAllRemoteDevices();

		assertTrue(listener.added);
		assertTrue(listener.removed);
    }

    public static class MyListener extends DefaultRegistryListener {
        public boolean added = false; // DOC: EXC1
        public boolean removed = false; // DOC: EXC1

        @Override
        public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
            RemoteService myService = device.findService(new UDAServiceId("MY-SERVICE-123"));
            if (myService != null) {
                // Do something with the discovered service
                added = true; // DOC: EXC2
            }
        }

        @Override
        public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
            // Stop using the service if this is the same device, it's gone now
            removed = true; // DOC: EXC3
        }
    }

}
