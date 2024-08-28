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

package com.distrimind.upnp_igd.test.data;

import com.distrimind.upnp_igd.model.meta.Action;
import com.distrimind.upnp_igd.model.meta.ActionArgument;
import com.distrimind.upnp_igd.model.meta.Service;
import com.distrimind.upnp_igd.model.meta.StateVariable;
import com.distrimind.upnp_igd.model.meta.StateVariableAllowedValueRange;
import com.distrimind.upnp_igd.model.meta.StateVariableEventDetails;
import com.distrimind.upnp_igd.model.meta.StateVariableTypeDetails;
import com.distrimind.upnp_igd.model.types.Datatype;
import com.distrimind.upnp_igd.model.types.ServiceId;
import com.distrimind.upnp_igd.model.types.ServiceType;
import com.distrimind.upnp_igd.model.types.UDAServiceId;
import com.distrimind.upnp_igd.model.types.UDAServiceType;
import com.distrimind.upnp_igd.util.URIUtil;

import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.List;

import static org.testng.Assert.*;

/**
 * @author Christian Bauer
 */
public class SampleServiceOne extends SampleService {

    public static final String SET_TARGET = "SetTarget";
    public static final String TARGET = "Target";
    public static final String STATUS = "Status";
    public static final String SOME_VAR = "SomeVar";
    public static final String ANOTHER_VAR = "AnotherVar";

    public static URL getDescriptorURL() {
        return URIUtil.createAbsoluteURL(SampleDeviceRoot.getDeviceDescriptorURL(), getThisDescriptorURI());
    }

    public static URI getThisDescriptorURI() {
        return URI.create("service/upnp-org/MY-SERVICE-123/desc");
    }

    public static URI getThisControlURI() {
        return URI.create("service/upnp-org/MY-SERVICE-123/control");
    }

    public static URI getThisEventSubscriptionURI() {
        return URI.create("service/upnp-org/MY-SERVICE-123/events");
    }

    public static ServiceId getThisServiceId() {
        return new UDAServiceId("MY-SERVICE-123");
    }

    public static ServiceType getThisServiceType() {
        return new UDAServiceType("MY-SERVICE-TYPE-ONE", 1);
    }

    @Override
    public ServiceType getServiceType() {
        return getThisServiceType();
    }

    @Override
    public ServiceId getServiceId() {
        return getThisServiceId();
    }

    @Override
    public URI getDescriptorURI() {
        return getThisDescriptorURI();
    }

    @Override
    public URI getControlURI() {
        return getThisControlURI();
    }

    @Override
    public URI getEventSubscriptionURI() {
        return getThisEventSubscriptionURI();
    }

    @Override
    public Collection<Action<?>> getActions() {
        return List.of(
                new Action<>(
                        SET_TARGET,
						List.of(new ActionArgument<>("NewTargetValue", TARGET, ActionArgument.Direction.IN))
                ),
                new Action<>(
                        "GetTarget",
                        List.of(new ActionArgument<>("RetTargetValue", TARGET, ActionArgument.Direction.OUT, true))
                ),
                new Action<>(
                        "GetStatus",
                        List.of(new ActionArgument<>("ResultStatus", STATUS, ActionArgument.Direction.OUT))
                )
        );
    }

    @Override
    public Collection<StateVariable<?>> getStateVariables() {
        return List.of(
                new StateVariable<>(
                        TARGET,
                        new StateVariableTypeDetails(Datatype.Builtin.BOOLEAN.getDatatype(), "0"),
                        new StateVariableEventDetails(false)
                ),
                new StateVariable<>(
                        STATUS,
                        new StateVariableTypeDetails(Datatype.Builtin.BOOLEAN.getDatatype(), "0")
                ),
                new StateVariable<>(
                        SOME_VAR,
                        new StateVariableTypeDetails(Datatype.Builtin.STRING.getDatatype(), "foo", List.of("foo", "bar"), null)
                ),
                new StateVariable<>(
                        ANOTHER_VAR,
                        new StateVariableTypeDetails(Datatype.Builtin.UI4.getDatatype(), null, null, new StateVariableAllowedValueRange(0, 10, 2)),
                        new StateVariableEventDetails(false)
                ),
                new StateVariable<>(
                        "ModeratedMaxRateVar",
                        new StateVariableTypeDetails(Datatype.Builtin.STRING.getDatatype()),
                        new StateVariableEventDetails(true, 500, 0)
                ),
                new StateVariable<>(
                        "ModeratedMinDeltaVar",
                        new StateVariableTypeDetails(Datatype.Builtin.I4.getDatatype()),
                        new StateVariableEventDetails(true, 0, 3)
                )
                );
    }

    public static void assertMatch(Service<?, ?, ?> a, Service<?, ?, ?> b) {

        assertEquals(a.getActions().size(), b.getActions().size());

        assertEquals(a.getAction(SET_TARGET).getName(), b.getAction(SET_TARGET).getName());
        assertEquals(a.getAction(SET_TARGET).getArguments().size(), b.getAction(SET_TARGET).getArguments().size());
        assertEquals(a.getAction(SET_TARGET).getArguments().iterator().next().getName(), a.getAction(SET_TARGET).getArguments().iterator().next().getName());
        assertEquals(a.getAction(SET_TARGET).getArguments().iterator().next().getDirection(), b.getAction(SET_TARGET).getArguments().iterator().next().getDirection());
        assertEquals(a.getAction(SET_TARGET).getArguments().iterator().next().getRelatedStateVariableName(), b.getAction(SET_TARGET).getArguments().iterator().next().getRelatedStateVariableName());

        assertEquals(a.getAction("GetTarget").getArguments().iterator().next().getName(), b.getAction("GetTarget").getArguments().iterator().next().getName());
        // TODO: UPNP VIOLATION: WMP12 will discard RenderingControl service if it contains <retval> tags
        // assertEquals(a.getAction("GetTarget").getArguments()[0].isReturnValue(), b.getAction("GetTarget").getArguments()[0].isReturnValue());

        assertEquals(a.getStateVariables().size(), b.getStateVariables().size());
		assertNotNull(a.getStateVariable(TARGET));
		assertNotNull(b.getStateVariable(TARGET));
		assertNotNull(a.getStateVariable(STATUS));
		assertNotNull(b.getStateVariable(STATUS));
		assertNotNull(a.getStateVariable(SOME_VAR));
		assertNotNull(b.getStateVariable(SOME_VAR));

        assertEquals(a.getStateVariable(TARGET).getName(), TARGET);
        assertEquals(a.getStateVariable(TARGET).getEventDetails().isSendEvents(), b.getStateVariable(TARGET).getEventDetails().isSendEvents());

        assertEquals(a.getStateVariable(STATUS).getName(), STATUS);
        assertEquals(a.getStateVariable(STATUS).getEventDetails().isSendEvents(), b.getStateVariable(STATUS).getEventDetails().isSendEvents());
        assertEquals(a.getStateVariable(STATUS).getTypeDetails().getDatatype(), Datatype.Builtin.BOOLEAN.getDatatype());

        assertEquals(a.getStateVariable(SOME_VAR).getTypeDetails().getAllowedValues().size(), b.getStateVariable(SOME_VAR).getTypeDetails().getAllowedValues().size());
        assertEquals(a.getStateVariable(SOME_VAR).getTypeDetails().getDefaultValue(), b.getStateVariable(SOME_VAR).getTypeDetails().getDefaultValue());
        assertEquals(a.getStateVariable(SOME_VAR).getTypeDetails().getAllowedValues().iterator().next(), b.getStateVariable(SOME_VAR).getTypeDetails().getAllowedValues().iterator().next());
        assertEquals(a.getStateVariable(SOME_VAR).getTypeDetails().getAllowedValues().iterator().next(), b.getStateVariable(SOME_VAR).getTypeDetails().getAllowedValues().iterator().next());
        assertEquals(a.getStateVariable(SOME_VAR).getEventDetails().isSendEvents(), b.getStateVariable(SOME_VAR).getEventDetails().isSendEvents());

        assertEquals(a.getStateVariable(ANOTHER_VAR).getTypeDetails().getAllowedValueRange().getMinimum(), b.getStateVariable(ANOTHER_VAR).getTypeDetails().getAllowedValueRange().getMinimum());
        assertEquals(a.getStateVariable(ANOTHER_VAR).getTypeDetails().getAllowedValueRange().getMaximum(), b.getStateVariable(ANOTHER_VAR).getTypeDetails().getAllowedValueRange().getMaximum());
        assertEquals(a.getStateVariable(ANOTHER_VAR).getTypeDetails().getAllowedValueRange().getStep(), b.getStateVariable(ANOTHER_VAR).getTypeDetails().getAllowedValueRange().getStep());
        assertEquals(a.getStateVariable(ANOTHER_VAR).getEventDetails().isSendEvents(), b.getStateVariable(ANOTHER_VAR).getEventDetails().isSendEvents());
    }

}
