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
package example.localservice;

import com.distrimind.upnp_igd.binding.LocalServiceBinder;
import com.distrimind.upnp_igd.binding.annotations.AnnotationLocalServiceBinder;
import com.distrimind.upnp_igd.model.DefaultServiceManager;
import com.distrimind.upnp_igd.model.action.ActionInvocation;
import com.distrimind.upnp_igd.model.meta.ActionArgument;
import com.distrimind.upnp_igd.model.meta.DeviceDetails;
import com.distrimind.upnp_igd.model.meta.LocalDevice;
import com.distrimind.upnp_igd.model.meta.LocalService;
import com.distrimind.upnp_igd.model.types.Datatype;
import com.distrimind.upnp_igd.model.types.DeviceType;
import com.distrimind.upnp_igd.test.data.SampleData;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

/**
 * Working with enums
 * <p>
 * Java <code>enum</code>'s are special, unfortunately: UPnPIGD can convert
 * your enum value into a string for transport in UPnP messages, but you
 * have to convert it back manually from a string. This is shown in the
 * following service example:
 * </p>
 * <a class="citation" href="javacode://example.localservice.MyServiceWithEnum" style="include: INC1"/>
 * <p>
 * UPnPIGD will automatically assume that the datatype is a UPnP string if the
 * field (or getter) or getter Java type is an enum. Furthermore, an
 * <code>&lt;allowedValueList&gt;</code> will be created in your service descriptor
 * XML, so control points know that this state variable has in fact a defined
 * set of possible values.
 * </p>
 */
public class EnumTest {

    public static final String GET_COLOR = "GetColor";
    public static final String SET_COLOR = "SetColor";

    public <T> LocalDevice<T> createTestDevice(Class<T> serviceClass) throws Exception {

        LocalServiceBinder binder = new AnnotationLocalServiceBinder();
        LocalService<T> svc = binder.read(serviceClass);
        svc.setManager(new DefaultServiceManager<>(svc, serviceClass));

        return new LocalDevice<>(
                SampleData.createLocalDeviceIdentity(),
                new DeviceType("mydomain", "CustomDevice", 1),
                new DeviceDetails("A Custom Device"),
                svc
        );
    }

    @DataProvider(name = "devices")
    public Object[][] getDevices() {


        try {
            return new LocalDevice[][]{
                    {createTestDevice(MyServiceWithEnum.class)},
            };
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            // Damn testng swallows exceptions in provider/factory methods
            throw new RuntimeException(ex);
        }
    }

    @Test(dataProvider = "devices")
    public void validateBinding(LocalDevice<?> device) {

        LocalService<?> svc = device.getServices().iterator().next();

        assertEquals(svc.getStateVariables().size(), 1);
        assertEquals(svc.getStateVariables().iterator().next().getTypeDetails().getDatatype().getBuiltin(), Datatype.Builtin.STRING);

        assertEquals(svc.getActions().size(), 3); // Has 2 actions plus QueryStateVariableAction!

        assertEquals(svc.getAction(GET_COLOR).getArguments().size(), 1);
        assertEquals(svc.getAction(GET_COLOR).getArguments().iterator().next().getName(), "Out");
        assertEquals(svc.getAction(GET_COLOR).getArguments().iterator().next().getDirection(), ActionArgument.Direction.OUT);
        assertEquals(svc.getAction(GET_COLOR).getArguments().iterator().next().getRelatedStateVariableName(), "Color");

        assertEquals(svc.getAction(SET_COLOR).getArguments().size(), 1);
        assertEquals(svc.getAction(SET_COLOR).getArguments().iterator().next().getName(), "In");
        assertEquals(svc.getAction(SET_COLOR).getArguments().iterator().next().getDirection(), ActionArgument.Direction.IN);
        assertEquals(svc.getAction(SET_COLOR).getArguments().iterator().next().getRelatedStateVariableName(), "Color");

    }

    @Test(dataProvider = "devices")
    public void invokeActions(LocalDevice<?> device) {
        LocalService<?> svc = device.getServices().iterator().next();

        ActionInvocation<? extends LocalService<?>> setColor = new ActionInvocation<>(svc.getAction(SET_COLOR));
        setColor.setInput("In", MyServiceWithEnum.Color.Blue);
        svc.getExecutor(setColor.getAction()).executeWithUntypedGeneric(setColor);
		assertNull(setColor.getFailure());
        assertEquals(setColor.getOutput().size(), 0);

        ActionInvocation<? extends LocalService<?>> getColor = new ActionInvocation<>(svc.getAction(GET_COLOR));
        svc.getExecutor(getColor.getAction()).executeWithUntypedGeneric(getColor);
		assertNull(getColor.getFailure());
        assertEquals(getColor.getOutput().size(), 1);
        assertEquals(getColor.getOutput().iterator().next().toString(), MyServiceWithEnum.Color.Blue.name());

    }
}
