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

package com.distrimind.upnp_igd.binding.xml;

import com.distrimind.upnp_igd.binding.staging.MutableDevice;
import com.distrimind.upnp_igd.binding.staging.MutableIcon;
import com.distrimind.upnp_igd.binding.staging.MutableService;
import com.distrimind.upnp_igd.binding.staging.MutableUDAVersion;
import com.distrimind.upnp_igd.model.ValidationException;
import com.distrimind.upnp_igd.model.meta.Device;
import com.distrimind.upnp_igd.model.meta.Service;
import com.distrimind.upnp_igd.model.types.DLNACaps;
import com.distrimind.upnp_igd.model.types.DLNADoc;
import com.distrimind.upnp_igd.model.types.InvalidValueException;
import com.distrimind.upnp_igd.model.types.ServiceId;
import com.distrimind.upnp_igd.model.types.ServiceType;
import com.distrimind.upnp_igd.model.types.UDN;
import com.distrimind.upnp_igd.transport.spi.NetworkAddressFactory;
import com.distrimind.upnp_igd.util.MimeType;
import com.distrimind.upnp_igd.xml.SAXParser;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.distrimind.upnp_igd.binding.xml.Descriptor.Device.ELEMENT;

/**
 * A JAXP SAX parser implementation, which is actually slower than the DOM implementation (on desktop and on Android)!
 *
 * @author Christian Bauer
 */
public class UDA10DeviceDescriptorBinderSAXImpl extends UDA10DeviceDescriptorBinderImpl {

    private static final Logger log = Logger.getLogger(DeviceDescriptorBinder.class.getName());

    public UDA10DeviceDescriptorBinderSAXImpl(NetworkAddressFactory networkAddressFactory) {
        super(networkAddressFactory);
    }

    @Override
    public <D extends Device<?, D, S>, S extends Service<?, D, S>> D describe(D undescribedDevice, String descriptorXml) throws DescriptorBindingException, ValidationException {

        if (descriptorXml == null || descriptorXml.isEmpty()) {
            throw new DescriptorBindingException("Null or empty descriptor");
        }

        try {
            if (log.isLoggable(Level.FINE))
                log.fine("Populating device from XML descriptor: " + undescribedDevice);

            // Read the XML into a mutable descriptor graph

            SAXParser parser = new SAXParser();

            MutableDevice<D, S> descriptor = new MutableDevice<>();
            new RootHandler<>(descriptor, parser);

            parser.parse(
                    new InputSource(
                            // TODO: UPNP VIOLATION: Virgin Media Superhub sends trailing spaces/newlines after last XML element, need to trim()
                            new StringReader(descriptorXml.trim())
                    )
            );

            // Build the immutable descriptor graph
            return descriptor.build(undescribedDevice);

        } catch (ValidationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new DescriptorBindingException("Could not parse device descriptor: " + ex, ex);
        }
    }

    protected static class RootHandler<D extends Device<?, D, S>, S extends Service<?, D, S>> extends DeviceDescriptorHandler<MutableDevice<D, S>> {

        public RootHandler(MutableDevice<D, S> instance, SAXParser parser) {
            super(instance, parser);
        }

        @Override
        public void startElement(ELEMENT element, Attributes attributes) throws SAXException {

            if (element.equals(SpecVersionHandler.EL)) {
                MutableUDAVersion udaVersion = new MutableUDAVersion();
                getInstance().udaVersion = udaVersion;
                new SpecVersionHandler(udaVersion, this);
            }

            if (element.equals(DeviceHandler.EL)) {
                new DeviceHandler<>(getInstance(), this);
            }

        }

        @Override
        public void endElement(ELEMENT element) throws SAXException {
			if (Objects.requireNonNull(element) == ELEMENT.URLBase) {
				try {
					String urlString = getCharacters();
					if (urlString != null && !urlString.isEmpty()) {
						// We hope it's  RFC 2396 and RFC 2732 compliant
						getInstance().baseURL = new URL(urlString);
					}
				} catch (Exception ex) {
					throw new SAXException("Invalid URLBase: " + ex);
				}
			}
        }
    }

    protected static class SpecVersionHandler extends DeviceDescriptorHandler<MutableUDAVersion> {

        public static final ELEMENT EL = ELEMENT.specVersion;

        public SpecVersionHandler(MutableUDAVersion instance, DeviceDescriptorHandler<?> parent) {
            super(instance, parent);
        }

        @Override
        @SuppressWarnings("PMD.SwitchStmtsShouldHaveDefault")
        public void endElement(ELEMENT element) {
            switch (element) {
                case major:
                    String majorVersion = getCharacters().trim();
                    if (!"1".equals(majorVersion)) {
                        if (log.isLoggable(Level.WARNING)) log.warning("Unsupported UDA major version, ignoring: " + majorVersion);
                        majorVersion = "1";
                    }
                    getInstance().major = Integer.parseInt(majorVersion);
                    break;
                case minor:
                    String minorVersion = getCharacters().trim();
                    if (!"0".equals(minorVersion)) {
                        if (log.isLoggable(Level.WARNING)) log.warning("Unsupported UDA minor version, ignoring: " + minorVersion);
                    }
                    getInstance().minor = 0;
                    break;
            }
        }

        @Override
        public boolean isLastElement(ELEMENT element) {
            return element.equals(EL);
        }
    }

    protected static class DeviceHandler<D extends Device<?, D, S>, S extends Service<?, D, S>> extends DeviceDescriptorHandler<MutableDevice<D, S>> {

        public static final ELEMENT EL = ELEMENT.device;

        public DeviceHandler(MutableDevice<D, S> instance, DeviceDescriptorHandler<?> parent) {
            super(instance, parent);
        }

        @Override
        public void startElement(ELEMENT element, Attributes attributes) throws SAXException {

            if (element.equals(IconListHandler.EL)) {
                List<MutableIcon> icons = new ArrayList<>();
                getInstance().icons = icons;
                new IconListHandler(icons, this);
            }

            if (element.equals(ServiceListHandler.EL)) {
                List<MutableService<D, S>> services = new ArrayList<>();
                getInstance().services = services;
                new ServiceListHandler<>(services, this);
            }

            if (element.equals(DeviceListHandler.EL)) {
                List<MutableDevice<D, S>> devices = new ArrayList<>();
                getInstance().embeddedDevices = devices;
                new DeviceListHandler<>(devices, this);
            }
        }

        @Override
        public void endElement(ELEMENT element) {
            switch (element) {
                case deviceType:
                    getInstance().deviceType = getCharacters();
                    break;
                case friendlyName:
                    getInstance().friendlyName = getCharacters();
                    break;
                case manufacturer:
                    getInstance().manufacturer = getCharacters();
                    break;
                case manufacturerURL:
                    getInstance().manufacturerURI = parseURI(getCharacters());
                    break;
                case modelDescription:
                    getInstance().modelDescription = getCharacters();
                    break;
                case modelName:
                    getInstance().modelName = getCharacters();
                    break;
                case modelNumber:
                    getInstance().modelNumber = getCharacters();
                    break;
                case modelURL:
                    getInstance().modelURI = parseURI(getCharacters());
                    break;
                case presentationURL:
                    getInstance().presentationURI = parseURI(getCharacters());
                    break;
                case UPC:
                    getInstance().upc = getCharacters();
                    break;
                case serialNumber:
                    getInstance().serialNumber = getCharacters();
                    break;
                case UDN:
                    getInstance().udn = UDN.valueOf(getCharacters());
                    break;
                case X_DLNADOC:
                    String txt = getCharacters();
                    try {
                        getInstance().dlnaDocs.add(DLNADoc.valueOf(txt));
                    } catch (InvalidValueException ex) {
                        if (log.isLoggable(Level.INFO)) log.info("Invalid X_DLNADOC value, ignoring value: " + txt);
                    }
                    break;
                case X_DLNACAP:
                    getInstance().dlnaCaps = DLNACaps.valueOf(getCharacters());
                    break;
            }
        }

        @Override
        public boolean isLastElement(ELEMENT element) {
            return element.equals(EL);
        }
    }

    protected static class IconListHandler extends DeviceDescriptorHandler<List<MutableIcon>> {

        public static final ELEMENT EL = ELEMENT.iconList;

        public IconListHandler(List<MutableIcon> instance, DeviceDescriptorHandler<?> parent) {
            super(instance, parent);
        }

        @Override
        public void startElement(ELEMENT element, Attributes attributes) throws SAXException {
            if (element.equals(IconHandler.EL)) {
                MutableIcon icon = new MutableIcon();
                getInstance().add(icon);
                new IconHandler(icon, this);
            }
        }

        @Override
        public boolean isLastElement(ELEMENT element) {
            return element.equals(EL);
        }
    }

    protected static class IconHandler extends DeviceDescriptorHandler<MutableIcon> {

        public static final ELEMENT EL = ELEMENT.icon;

        public IconHandler(MutableIcon instance, DeviceDescriptorHandler<?> parent) {
            super(instance, parent);
        }

        @Override
        public void endElement(ELEMENT element)  {
            switch (element) {
                case width:
                    getInstance().width = Integer.parseInt(getCharacters());
                    break;
                case height:
                    getInstance().height = Integer.parseInt(getCharacters());
                    break;
                case depth:
                	try {
                		getInstance().depth = Integer.parseInt(getCharacters());
                	} catch(NumberFormatException ex) {
                        if (log.isLoggable(Level.WARNING)) log.warning("Invalid icon depth '" + getCharacters() + "', using 16 as default: " + ex);
                		getInstance().depth = 16;
                	}
                    break;
                case url:
                    getInstance().uri = parseURI(getCharacters());
                    break;
                case mimetype:
                    try {
                        getInstance().mimeType = getCharacters();
                        MimeType.valueOf(getInstance().mimeType);
                    } catch(IllegalArgumentException ex) {
                        if (log.isLoggable(Level.WARNING)) log.warning("Ignoring invalid icon mime type: " + getInstance().mimeType);
                        getInstance().mimeType = "";
                    }
                    break;
            }
        }

        @Override
        public boolean isLastElement(ELEMENT element) {
            return element.equals(EL);
        }
    }

    protected static class ServiceListHandler<D extends Device<?, D, S>, S extends Service<?, D, S>> extends DeviceDescriptorHandler<List<MutableService<D, S>>> {

        public static final ELEMENT EL = ELEMENT.serviceList;

        public ServiceListHandler(List<MutableService<D, S>> instance, DeviceDescriptorHandler<?> parent) {
            super(instance, parent);
        }

        @Override
        public void startElement(ELEMENT element, Attributes attributes) throws SAXException {
            if (element.equals(ServiceHandler.EL)) {
                MutableService<D, S> service = new MutableService<>();
                getInstance().add(service);
                new ServiceHandler<>(service, this);
            }
        }

        @Override
        public boolean isLastElement(ELEMENT element) {
            boolean last = element.equals(EL);
            if (last) {
				getInstance().removeIf(service -> service.serviceType == null || service.serviceId == null);
            }
            return last;
        }
    }

    protected static class ServiceHandler<D extends Device<?, D, S>, S extends Service<?, D, S>> extends DeviceDescriptorHandler<MutableService<D, S>> {

        public static final ELEMENT EL = ELEMENT.service;

        public ServiceHandler(MutableService<D, S> instance, DeviceDescriptorHandler<?> parent) {
            super(instance, parent);
        }

        @Override
        public void endElement(ELEMENT element) {
            try {
                switch (element) {
                    case serviceType:
                        getInstance().serviceType = ServiceType.valueOf(getCharacters());
                        break;
                    case serviceId:
                        getInstance().serviceId = ServiceId.valueOf(getCharacters());
                        break;
                    case SCPDURL:
                        getInstance().descriptorURI = parseURI(getCharacters());
                        break;
                    case controlURL:
                        getInstance().controlURI = parseURI(getCharacters());
                        break;
                    case eventSubURL:
                        getInstance().eventSubscriptionURI = parseURI(getCharacters());
                        break;
                }
            } catch (InvalidValueException ex) {

                if (log.isLoggable(Level.WARNING)) log.warning(
                    "UPnP specification violation, skipping invalid service declaration. " + ex.getMessage()
                );
            }
        }

        @Override
        public boolean isLastElement(ELEMENT element) {
            return element.equals(EL);
        }
    }

    protected static class DeviceListHandler<D extends Device<?, D, S>, S extends Service<?, D, S>> extends DeviceDescriptorHandler<List<MutableDevice<D, S>>> {

        public static final ELEMENT EL = ELEMENT.deviceList;

        public DeviceListHandler(List<MutableDevice<D, S>> instance, DeviceDescriptorHandler<?> parent) {
            super(instance, parent);
        }

        @Override
        public void startElement(ELEMENT element, Attributes attributes) throws SAXException {
            if (element.equals(DeviceHandler.EL)) {
                MutableDevice<D,S> device = new MutableDevice<>();
                getInstance().add(device);
                new DeviceHandler<>(device, this);
            }
        }

        @Override
        public boolean isLastElement(ELEMENT element) {
            return element.equals(EL);
        }
    }

    protected static class DeviceDescriptorHandler<I> extends SAXParser.Handler<I> {

        public DeviceDescriptorHandler(I instance) {
            super(instance);
        }

        public DeviceDescriptorHandler(I instance, SAXParser parser) {
            super(instance, parser);
        }

        public DeviceDescriptorHandler(I instance, DeviceDescriptorHandler<?> parent) {
            super(instance, parent);
        }

        public DeviceDescriptorHandler(I instance, SAXParser parser, DeviceDescriptorHandler<?> parent) {
            super(instance, parser, parent);
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            super.startElement(uri, localName, qName, attributes);
            ELEMENT el = ELEMENT.valueOrNullOf(localName);
            if (el == null) return;
            startElement(el, attributes);
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement(uri, localName, qName);
            ELEMENT el = ELEMENT.valueOrNullOf(localName);
            if (el == null) return;
            endElement(el);
        }

        @Override
        protected boolean isLastElement(String uri, String localName, String qName) {
            ELEMENT el = ELEMENT.valueOrNullOf(localName);
            return el != null && isLastElement(el);
        }

        public void startElement(ELEMENT element, Attributes attributes) throws SAXException {

        }

        public void endElement(ELEMENT element) throws SAXException {

        }

        public boolean isLastElement(ELEMENT element) {
            return false;
        }
    }
}
