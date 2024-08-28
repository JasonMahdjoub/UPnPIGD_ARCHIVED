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

import com.distrimind.upnp_igd.model.ValidationException;
import com.distrimind.upnp_igd.model.meta.Device;
import com.distrimind.upnp_igd.model.meta.Service;
import com.distrimind.upnp_igd.transport.spi.NetworkAddressFactory;
import com.distrimind.upnp_igd.util.Exceptions;
import com.distrimind.upnp_igd.xml.ParserException;
import com.distrimind.upnp_igd.xml.XmlPullParserUtils;
import org.xml.sax.SAXParseException;

import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Michael Pujos
 */
public class RecoveringUDA10DeviceDescriptorBinderImpl extends UDA10DeviceDescriptorBinderImpl {

    private static final Logger log = Logger.getLogger(RecoveringUDA10DeviceDescriptorBinderImpl.class.getName());
    static final String endRootTag = "</root>";
    public RecoveringUDA10DeviceDescriptorBinderImpl(NetworkAddressFactory networkAddressFactory) {
        super(networkAddressFactory);
    }

    @Override
    public <D extends Device<?, D, S>, S extends Service<?, D, S>> D describe(D undescribedDevice, String _descriptorXml) throws DescriptorBindingException, ValidationException {

        D device = null;
        DescriptorBindingException originalException;
        String descriptorXml=null;
        if (_descriptorXml != null)
            descriptorXml = _descriptorXml.trim(); // Always trim whitespace
        try {

            try {

                device = super.describe(undescribedDevice, descriptorXml);
                return device;
            } catch (DescriptorBindingException ex) {
                if (log.isLoggable(Level.WARNING))
                    log.warning("Regular parsing failed: " + Exceptions.unwrap(ex).getMessage());
                originalException = ex;
            }

            String fixedXml;
            // The following modifications are not cumulative!

            fixedXml = fixGarbageLeadingChars(descriptorXml);
            if (fixedXml != null) {
                try {
                    device = super.describe(undescribedDevice, fixedXml);
                    return device;
                } catch (DescriptorBindingException ex) {
                    if (log.isLoggable(Level.WARNING))
                        log.warning("Removing leading garbage didn't work: " + Exceptions.unwrap(ex).getMessage());
                }
            }

            fixedXml = fixGarbageTrailingChars(descriptorXml, originalException);
            if (fixedXml != null) {
                try {
                    device = super.describe(undescribedDevice, fixedXml);
                    return device;
                } catch (DescriptorBindingException ex) {
                    if (log.isLoggable(Level.WARNING))
                        log.warning("Removing trailing garbage didn't work: " + Exceptions.unwrap(ex).getMessage());
                }
            }

            // Try to fix "up to five" missing namespace declarations
            DescriptorBindingException lastException = originalException;
            fixedXml = descriptorXml;
            for (int retryCount = 0; retryCount < 5; retryCount++) {
                fixedXml = fixMissingNamespaces(fixedXml, lastException);
                if (fixedXml != null) {
                    try {
                        device = super.describe(undescribedDevice, fixedXml);
                        return device;
                    } catch (DescriptorBindingException ex) {
                        if (log.isLoggable(Level.WARNING))
                            log.warning("Fixing namespace prefix didn't work: " + Exceptions.unwrap(ex).getMessage());
                        lastException = ex;
                    }
                } else {
                    break; // We can stop, no more namespace fixing can be done
                }
            }

            fixedXml = XmlPullParserUtils.fixXMLEntities(descriptorXml);
            if(fixedXml==null || !fixedXml.equals(descriptorXml)) {
                try {
                    device = super.describe(undescribedDevice, fixedXml);
                    return device;
                } catch (DescriptorBindingException ex) {
                    if (log.isLoggable(Level.WARNING))
                        log.warning("Fixing XML entities didn't work: " + Exceptions.unwrap(ex).getMessage());
                }
            }

            handleInvalidDescriptor(descriptorXml, originalException);

        } catch (ValidationException ex) {
            device = handleInvalidDevice(descriptorXml, device, ex);
            if (device != null)
                return device;
        }
        throw new IllegalStateException("No device produced, did you swallow exceptions in your subclass?");
    }

    private String fixGarbageLeadingChars(String descriptorXml) {
        if (descriptorXml == null)
            return null;
    		/* Recover this:

    		HTTP/1.1 200 OK
    		Content-Length: 4268
    		Content-Type: text/xml; charset="utf-8"
    		Server: Microsoft-Windows/6.2 UPnP/1.0 UPnP-Device-Host/1.0 Microsoft-HTTPAPI/2.0
    		Date: Sun, 07 Apr 2013 02:11:30 GMT

    		@7:5 in java.io.StringReader@407f6b00) : HTTP/1.1 200 OK
    		Content-Length: 4268
    		Content-Type: text/xml; charset="utf-8"
    		Server: Microsoft-Windows/6.2 UPnP/1.0 UPnP-Device-Host/1.0 Microsoft-HTTPAPI/2.0
    		Date: Sun, 07 Apr 2013 02:11:30 GMT

    		<?xml version="1.0"?>...
    	    */

        int index = descriptorXml.indexOf("<?xml");
        if (index == -1) return descriptorXml;
        return descriptorXml.substring(index);
    }

    protected String fixGarbageTrailingChars(String descriptorXml, DescriptorBindingException ex) {
        if (descriptorXml==null)
            return null;

        int index = descriptorXml.indexOf(endRootTag);
        if (index == -1) {
            if (log.isLoggable(Level.WARNING))
                log.warning("No closing </root> element in descriptor");
            return null;
        }
        if (descriptorXml.length() != index + endRootTag.length()) {
            if (log.isLoggable(Level.WARNING))
                log.warning("Detected garbage characters after <root> node, removing"+(ex==null?"":ex.getMessage()));
            return descriptorXml.substring(0, index) + endRootTag;
        }
        return null;
    }

    protected String fixMissingNamespaces(String descriptorXml, DescriptorBindingException ex) {
        // Windows: DescriptorBindingException: Could not parse device descriptor: org.seamless.xml.ParserException: org.xml.sax.SAXParseException: The prefix "dlna" for element "dlna:X_DLNADOC" is not bound.
        // Android: org.xmlpull.v1.XmlPullParserException: undefined prefix: dlna (position:START_TAG <{null}dlna:X_DLNADOC>@19:17 in java.io.StringReader@406dff48)

        // We can only handle certain exceptions, depending on their type and message
        Throwable cause = ex.getCause();
        if (!((cause instanceof SAXParseException) || (cause instanceof ParserException)))
            return null;
        String message = cause.getMessage();
        if (message == null)
            return null;

        Pattern pattern = Pattern.compile("The prefix \"(.*)\" for element"); // Windows
        Matcher matcher = pattern.matcher(message);
        if (!matcher.find() || matcher.groupCount() != 1) {
            pattern = Pattern.compile("undefined prefix: ([^ ]*)"); // Android
            matcher = pattern.matcher(message);
            if (!matcher.find() || matcher.groupCount() != 1)
                return null;
        }

        String missingNS = matcher.group(1);
        if (log.isLoggable(Level.WARNING))
            log.warning("Fixing missing namespace declaration for: " + missingNS);

        // Extract <root> attributes
        pattern = Pattern.compile("<root([^>]*)");
        matcher = pattern.matcher(descriptorXml);
        if (!matcher.find() || matcher.groupCount() != 1) {
            if (log.isLoggable(Level.FINE))
                log.fine("Could not find <root> element attributes");
            return null;
        }

        String rootAttributes = matcher.group(1);
        if (log.isLoggable(Level.FINE))
            log.fine("Preserving existing <root> element attributes/namespace declarations: " + matcher.group(0));

        // Extract <root> body
        pattern = Pattern.compile("<root[^>]*>(.*)</root>", Pattern.DOTALL);
        matcher = pattern.matcher(descriptorXml);
        if (!matcher.find() || matcher.groupCount() != 1) {
            log.fine("Could not extract body of <root> element");
            return null;
        }

        String rootBody = matcher.group(1);

        // Add missing namespace, it only matters that it is defined, not that it is correct
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>"
            + "<root "
            + String.format(Locale.ROOT, "xmlns:%s=\"urn:schemas-dlna-org:device-1-0\"", missingNS) + rootAttributes + ">"
            + rootBody
            + endRootTag;

        // TODO: Should we match different undeclared prefixes with their correct namespace?
        // So if it's "dlna" we use "urn:schemas-dlna-org:device-1-0" etc.
    }

    /**
     * Handle processing errors while reading XML descriptors.
     *
     * <p>
     * Typically, you want to log this problem or create an error report, and in any
     * case, throw a {@link DescriptorBindingException} to notify the caller of the
     * binder of this failure. The default implementation simply rethrows the
     * given exception.
   
     *
     * @param xml       The original XML causing the parsing failure.
     * @param exception The original exception while parsing the XML.
     */
    protected void handleInvalidDescriptor(String xml, DescriptorBindingException exception)
        throws DescriptorBindingException {
        throw exception;
    }

    /**
     * Handle processing errors while binding XML descriptors.
     *
     * <p>
     * Typically, you want to log this problem or create an error report. You
     * should throw a {@link ValidationException} to notify the caller of the
     * binder of failure. The default implementation simply rethrows the
     * given exception.
   
     * <p>
     * This method gives you a final chance to fix the problem, instead of
     * throwing an exception, you could try to create valid {@link Device}
     * model and return it.
   
     *
     * @param xml       The original XML causing the binding failure.
     * @param device    The unfinished {@link Device} that failed validation
     * @param exception The errors found when validating the {@link Device} model.
     * @return Device A "fixed" {@link Device} model, instead of throwing an exception.
     */
    protected <D extends Device<?, D, ?>> D handleInvalidDevice(String xml, D device, ValidationException exception)
        throws ValidationException {
        throw exception;
    }
}
