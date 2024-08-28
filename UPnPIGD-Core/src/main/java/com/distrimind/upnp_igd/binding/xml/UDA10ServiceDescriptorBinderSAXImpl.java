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

import com.distrimind.upnp_igd.binding.staging.MutableAction;
import com.distrimind.upnp_igd.binding.staging.MutableActionArgument;
import com.distrimind.upnp_igd.binding.staging.MutableAllowedValueRange;
import com.distrimind.upnp_igd.binding.staging.MutableService;
import com.distrimind.upnp_igd.binding.staging.MutableStateVariable;
import com.distrimind.upnp_igd.model.ValidationException;
import com.distrimind.upnp_igd.model.meta.ActionArgument;
import com.distrimind.upnp_igd.model.meta.Device;
import com.distrimind.upnp_igd.model.meta.Service;
import com.distrimind.upnp_igd.model.meta.StateVariableEventDetails;
import com.distrimind.upnp_igd.model.types.CustomDatatype;
import com.distrimind.upnp_igd.model.types.Datatype;
import com.distrimind.upnp_igd.transport.spi.NetworkAddressFactory;
import com.distrimind.upnp_igd.xml.SAXParser;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.distrimind.upnp_igd.binding.xml.Descriptor.Service.ATTRIBUTE;
import static com.distrimind.upnp_igd.binding.xml.Descriptor.Service.ELEMENT;

/**
 * Implementation based on JAXP SAX.
 *
 * @author Christian Bauer
 */
public class UDA10ServiceDescriptorBinderSAXImpl extends UDA10ServiceDescriptorBinderImpl {

    private static final Logger log = Logger.getLogger(ServiceDescriptorBinder.class.getName());

    public UDA10ServiceDescriptorBinderSAXImpl(NetworkAddressFactory networkAddressFactory) {
        super(networkAddressFactory);
    }

    @Override
    public <D extends Device<?, D, S>, S extends Service<?, D, S>> S describe(S undescribedService, String descriptorXml) throws DescriptorBindingException, ValidationException {

        if (descriptorXml == null || descriptorXml.isEmpty()) {
            throw new DescriptorBindingException("Null or empty descriptor");
        }

        try {
            log.fine("Reading service from XML descriptor");

            SAXParser parser = new SAXParser();

            MutableService<D, S> descriptor = new MutableService<>();

            hydrateBasic(descriptor, undescribedService);

            new RootHandler<>(descriptor, parser);

            parser.parse(
                    new InputSource(
                            // TODO: UPNP VIOLATION: Virgin Media Superhub sends trailing spaces/newlines after last XML element, need to trim()
                            new StringReader(descriptorXml.trim())
                    )
            );

            // Build the immutable descriptor graph
            return descriptor.build(undescribedService.getDevice());

        } catch (ValidationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new DescriptorBindingException("Could not parse service descriptor: " + ex, ex);
        }
    }

    protected static class RootHandler<D extends Device<?, D, S>, S extends Service<?, D, S>> extends ServiceDescriptorHandler<MutableService<D, S>> {

        public RootHandler(MutableService<D, S> instance, SAXParser parser) {
            super(instance, parser);
        }

        @Override
        public void startElement(ELEMENT element, Attributes attributes) throws SAXException {

            /*
            if (element.equals(SpecVersionHandler.EL)) {
                MutableUDAVersion udaVersion = new MutableUDAVersion();
                getInstance().udaVersion = udaVersion;
                new SpecVersionHandler(udaVersion, this);
            }
            */

            if (element.equals(ActionListHandler.EL)) {
                List<MutableAction<S>> actions = new ArrayList<>();
                getInstance().actions = actions;
                new ActionListHandler<>(actions, this);
            }

            if (element.equals(StateVariableListHandler.EL)) {
                List<MutableStateVariable<S>> stateVariables = new ArrayList<>();
                getInstance().stateVariables = stateVariables;
                new StateVariableListHandler<>(stateVariables, this);
            }

        }
    }

    /*
    protected static class SpecVersionHandler extends ServiceDescriptorHandler<MutableUDAVersion> {

        public static final ELEMENT EL = ELEMENT.specVersion;

        public SpecVersionHandler(MutableUDAVersion instance, ServiceDescriptorHandler parent) {
            super(instance, parent);
        }

        @Override
        public void endElement(ELEMENT element) throws SAXException {
            switch (element) {
                case major:
                    getInstance().major = Integer.valueOf(getCharacters());
                    break;
                case minor:
                    getInstance().minor = Integer.valueOf(getCharacters());
                    break;
            }
        }

        @Override
        public boolean isLastElement(ELEMENT element) {
            return element.equals(EL);
        }
    }
    */

    protected static class ActionListHandler<S extends Service<?, ?, ?>> extends ServiceDescriptorHandler<List<MutableAction<S>>> {

        public static final ELEMENT EL = ELEMENT.actionList;

        public ActionListHandler(List<MutableAction<S>> instance, ServiceDescriptorHandler<?> parent) {
            super(instance, parent);
        }

        @Override
        public void startElement(ELEMENT element, Attributes attributes) throws SAXException {
            if (element.equals(ActionHandler.EL)) {
                MutableAction<S> action = new MutableAction<>();
                getInstance().add(action);
                new ActionHandler<>(action, this);
            }
        }

        @Override
        public boolean isLastElement(ELEMENT element) {
            return element.equals(EL);
        }
    }

    protected static class ActionHandler<S extends Service<?, ?, ?>> extends ServiceDescriptorHandler<MutableAction<S>> {

        public static final ELEMENT EL = ELEMENT.action;

        public ActionHandler(MutableAction<S> instance, ServiceDescriptorHandler<?> parent) {
            super(instance, parent);
        }

        @Override
        public void startElement(ELEMENT element, Attributes attributes) throws SAXException {
            if (element.equals(ActionArgumentListHandler.EL)) {
                List<MutableActionArgument<S>> arguments = new ArrayList<>();
                getInstance().arguments = arguments;
                new ActionArgumentListHandler<>(arguments, this);
            }
        }

        @Override
        public void endElement(ELEMENT element) throws SAXException {
			if (Objects.requireNonNull(element) == ELEMENT.name) {
				getInstance().name = getCharacters();
			}
        }

        @Override
        public boolean isLastElement(ELEMENT element) {
            return element.equals(EL);
        }
    }

    protected static class ActionArgumentListHandler<S extends Service<?, ?, ?>> extends ServiceDescriptorHandler<List<MutableActionArgument<S>>> {

        public static final ELEMENT EL = ELEMENT.argumentList;

        public ActionArgumentListHandler(List<MutableActionArgument<S>> instance, ServiceDescriptorHandler<?> parent) {
            super(instance, parent);
        }

        @Override
        public void startElement(ELEMENT element, Attributes attributes) throws SAXException {
            if (element.equals(ActionArgumentHandler.EL)) {
                MutableActionArgument<S> argument = new MutableActionArgument<>();
                getInstance().add(argument);
                new ActionArgumentHandler<>(argument, this);
            }
        }

        @Override
        public boolean isLastElement(ELEMENT element) {
            return element.equals(EL);
        }
    }

    protected static class ActionArgumentHandler<S extends Service<?, ?, ?>> extends ServiceDescriptorHandler<MutableActionArgument<S>> {

        public static final ELEMENT EL = ELEMENT.argument;

        public ActionArgumentHandler(MutableActionArgument<S> instance, ServiceDescriptorHandler<?> parent) {
            super(instance, parent);
        }

        @Override
        public void endElement(ELEMENT element) throws SAXException {
            switch (element) {
                case name:
                    getInstance().name = getCharacters();
                    break;
                case direction:
                    String directionString = getCharacters();
                    try {
                        getInstance().direction = ActionArgument.Direction.valueOf(directionString.toUpperCase(Locale.ROOT));
                    } catch (IllegalArgumentException ex) {
                        // TODO: UPNP VIOLATION: Pelco SpectraIV-IP uses illegal value INOUT
                        if (log.isLoggable(Level.WARNING)) log.warning("UPnP specification violation: Invalid action argument direction, assuming 'IN': " + directionString);
                        getInstance().direction = ActionArgument.Direction.IN;
                    }
                    break;
                case relatedStateVariable:
                    getInstance().relatedStateVariable = getCharacters();
                    break;
                case retval:
                    getInstance().retval = true;
                    break;
            }
        }

        @Override
        public boolean isLastElement(ELEMENT element) {
            return element.equals(EL);
        }
    }

    protected static class StateVariableListHandler<S extends Service<?, ?, ?>> extends ServiceDescriptorHandler<List<MutableStateVariable<S>>> {

        public static final ELEMENT EL = ELEMENT.serviceStateTable;

        public StateVariableListHandler(List<MutableStateVariable<S>> instance, ServiceDescriptorHandler<?> parent) {
            super(instance, parent);
        }

        @Override
        public void startElement(ELEMENT element, Attributes attributes) throws SAXException {
            if (element.equals(StateVariableHandler.EL)) {
                MutableStateVariable<S> stateVariable = new MutableStateVariable<>();

                String sendEventsAttributeValue = attributes.getValue(ATTRIBUTE.sendEvents.toString());
                stateVariable.eventDetails = new StateVariableEventDetails(
                        sendEventsAttributeValue != null && "YES".equals(sendEventsAttributeValue.toUpperCase(Locale.ROOT))
                );

                getInstance().add(stateVariable);
                new StateVariableHandler<>(stateVariable, this);
            }
        }

        @Override
        public boolean isLastElement(ELEMENT element) {
            return element.equals(EL);
        }
    }

    protected static class StateVariableHandler<S extends Service<?, ?, ?>> extends ServiceDescriptorHandler<MutableStateVariable<S>> {

        public static final ELEMENT EL = ELEMENT.stateVariable;

        public StateVariableHandler(MutableStateVariable<S> instance, ServiceDescriptorHandler<?> parent) {
            super(instance, parent);
        }

        @Override
        public void startElement(ELEMENT element, Attributes attributes) throws SAXException {
            if (element.equals(AllowedValueListHandler.EL)) {
                List<String> allowedValues = new ArrayList<>();
                getInstance().allowedValues = allowedValues;
                new AllowedValueListHandler(allowedValues, this);
            }

            if (element.equals(AllowedValueRangeHandler.EL)) {
                MutableAllowedValueRange allowedValueRange = new MutableAllowedValueRange();
                getInstance().allowedValueRange = allowedValueRange;
                new AllowedValueRangeHandler(allowedValueRange, this);
            }
        }

        @Override
        public void endElement(ELEMENT element) throws SAXException {
            switch (element) {
                case name:
                    getInstance().name = getCharacters();
                    break;
                case dataType:
                    String dtName = getCharacters();
                    Datatype.Builtin builtin = Datatype.Builtin.getByDescriptorName(dtName);
                    getInstance().dataType = builtin != null ? builtin.getDatatype() : new CustomDatatype(dtName);
                    break;
                case defaultValue:
                    getInstance().defaultValue = getCharacters();
                    break;
            }
        }

        @Override
        public boolean isLastElement(ELEMENT element) {
            return element.equals(EL);
        }
    }

    protected static class AllowedValueListHandler extends ServiceDescriptorHandler<List<String>> {

        public static final ELEMENT EL = ELEMENT.allowedValueList;

        public AllowedValueListHandler(List<String> instance, ServiceDescriptorHandler<?> parent) {
            super(instance, parent);
        }

        @Override
        public void endElement(ELEMENT element) throws SAXException {
			if (Objects.requireNonNull(element) == ELEMENT.allowedValue) {
				getInstance().add(getCharacters());
			}
        }

        @Override
        public boolean isLastElement(ELEMENT element) {
            return element.equals(EL);
        }
    }

    protected static class AllowedValueRangeHandler extends ServiceDescriptorHandler<MutableAllowedValueRange> {

        public static final ELEMENT EL = ELEMENT.allowedValueRange;

        public AllowedValueRangeHandler(MutableAllowedValueRange instance, ServiceDescriptorHandler<?> parent) {
            super(instance, parent);
        }

        @Override
        public void endElement(ELEMENT element) throws SAXException {
            try {
                switch (element) {
                    case minimum:
                        getInstance().minimum = Long.valueOf(getCharacters());
                        break;
                    case maximum:
                        getInstance().maximum = Long.valueOf(getCharacters());
                        break;
                    case step:
                        getInstance().step = Long.valueOf(getCharacters());
                        break;
                }
            } catch (Exception ignored) {
                // Ignore
            }
        }

        @Override
        public boolean isLastElement(ELEMENT element) {
            return element.equals(EL);
        }
    }

    protected static class ServiceDescriptorHandler<I> extends SAXParser.Handler<I> {

        public ServiceDescriptorHandler(I instance) {
            super(instance);
        }

        public ServiceDescriptorHandler(I instance, SAXParser parser) {
            super(instance, parser);
        }

        public ServiceDescriptorHandler(I instance, ServiceDescriptorHandler<?> parent) {
            super(instance, parent);
        }

        public ServiceDescriptorHandler(I instance, SAXParser parser, ServiceDescriptorHandler<?> parent) {
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
