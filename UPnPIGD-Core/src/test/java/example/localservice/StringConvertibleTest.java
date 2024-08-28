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
import com.distrimind.upnp_igd.model.meta.StateVariable;
import com.distrimind.upnp_igd.model.types.Datatype;
import com.distrimind.upnp_igd.model.types.DeviceType;
import com.distrimind.upnp_igd.test.data.SampleData;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

/**
 * String value converters
 * <p>
 * Consider the following service class with all state variables of
 * <code>string</code> UPnP datatype - but with a much more specific
 * Java type:
 * </p>
 * <a class="citation" href="javacode://example.localservice.MyServiceWithStringConvertibles" style="include: INC1"/>
 * <p>
 * The state variables are all of UPnP datatype <code>string</code> because
 * UPnPIGD knows that the Java type of the annotated field is "string convertible".
 * This is always the case for <code>java.net.URI</code> and <code>java.net.URL</code>.
 * </p>
 * <p>
 * Any other Java type you'd like to use for automatic string conversion has to be named
 * in the <code>@UpnpService</code> annotation on the class, like the
 * <code>MyStringConvertible</code>. Note that these types have to
 * have an appropriate <code>toString()</code> method and a single argument constructor
 * that accepts a <code>java.lang.String</code> ("from string" conversion).
 * </p>
 * <p>
 * The <code>List&lt;Integer></code> is the collection you'd use in your service
 * implementation to group several numbers. Let's assume that for UPnP communication
 * you need a comma-separated representation of the individual values in a string,
 * as is required by many of the UPnP A/V specifications. First, tell UPnPIGD that
 * the state variable really is a string datatype, it can't infer that
 * from the field type. Then, if an action has this output argument, instead of
 * manually creating the comma-separated string you pick the appropriate converter
 * from the classes in <code>com.distrimind.upnp_igd.model.types.csv.*</code> and return
 * it from your action method. These are actually <code>java.util.List</code>
 * implementations, so you could use them <em>instead</em> of
 * <code>java.util.List</code> if you don't care about the dependency. Any action
 * input argument value can also be converted from a comma-separated string
 * representation to a list automatically - all you have to do is use the
 * CSV converter class as an input argument type.
 * </p>
 */
public class StringConvertibleTest {

    public static final String SET_MY_URL = "SetMyURL";
    public static final String HTTP_FOO_BAR = "http://foo/bar";

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
                    {createTestDevice(MyServiceWithStringConvertibles.class)},
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

        assertEquals(svc.getStateVariables().size(), 4);
        for (StateVariable<?> stateVariable : svc.getStateVariables()) {
            assertEquals(stateVariable.getTypeDetails().getDatatype().getBuiltin(), Datatype.Builtin.STRING);
        }

        assertEquals(svc.getActions().size(), 9); // Has 8 actions plus QueryStateVariableAction!

        assertEquals(svc.getAction(SET_MY_URL).getArguments().size(), 1);
        assertEquals(svc.getAction(SET_MY_URL).getArguments().iterator().next().getName(), "In");
        assertEquals(svc.getAction(SET_MY_URL).getArguments().iterator().next().getDirection(), ActionArgument.Direction.IN);
        assertEquals(svc.getAction(SET_MY_URL).getArguments().iterator().next().getRelatedStateVariableName(), "MyURL");
        // The others are all the same...

    }

    @Test(dataProvider =  "devices")
    public void invokeActions(LocalDevice<?> device) {
        LocalService<?> svc = device.getServices().iterator().next();

        ActionInvocation<? extends LocalService<?>> setMyURL = new ActionInvocation<>(svc.getAction(SET_MY_URL));
        setMyURL.setInput("In", HTTP_FOO_BAR);
        svc.getExecutor(setMyURL.getAction()).executeWithUntypedGeneric(setMyURL);
		assertNull(setMyURL.getFailure());
        assertEquals(setMyURL.getOutput().size(), 0);

        ActionInvocation<? extends LocalService<?>> getMyURL = new ActionInvocation<>(svc.getAction("GetMyURL"));
        svc.getExecutor(getMyURL.getAction()).executeWithUntypedGeneric(getMyURL);
		assertNull(getMyURL.getFailure());
        assertEquals(getMyURL.getOutput().size(), 1);
        assertEquals(getMyURL.getOutput().iterator().next().toString(), HTTP_FOO_BAR);

        ActionInvocation<? extends LocalService<?>> setMyURI = new ActionInvocation<>(svc.getAction("SetMyURI"));
        setMyURI.setInput("In", HTTP_FOO_BAR);
        svc.getExecutor(setMyURI.getAction()).executeWithUntypedGeneric(setMyURI);
		assertNull(setMyURI.getFailure());
        assertEquals(setMyURI.getOutput().size(), 0);

        ActionInvocation<? extends LocalService<?>> getMyURI = new ActionInvocation<>(svc.getAction("GetMyURI"));
        svc.getExecutor(getMyURI.getAction()).executeWithUntypedGeneric(getMyURI);
		assertNull(getMyURI.getFailure());
        assertEquals(getMyURI.getOutput().size(), 1);
        assertEquals(getMyURI.getOutput().iterator().next().toString(), HTTP_FOO_BAR);

        ActionInvocation<? extends LocalService<?>> setMyNumbers = new ActionInvocation<>(svc.getAction("SetMyNumbers"));
        setMyNumbers.setInput("In", "1,2,3");
        svc.getExecutor(setMyNumbers.getAction()).executeWithUntypedGeneric(setMyNumbers);
		assertNull(setMyNumbers.getFailure());
        assertEquals(setMyNumbers.getOutput().size(), 0);

        ActionInvocation<? extends LocalService<?>> getMyNumbers = new ActionInvocation<>(svc.getAction("GetMyNumbers"));
        svc.getExecutor(getMyNumbers.getAction()).executeWithUntypedGeneric(getMyNumbers);
		assertNull(getMyNumbers.getFailure());
        assertEquals(getMyNumbers.getOutput().size(), 1);
        assertEquals(getMyNumbers.getOutput().iterator().next().toString(), "1,2,3");

        ActionInvocation<? extends LocalService<?>> setMyStringConvertible = new ActionInvocation<>(svc.getAction("SetMyStringConvertible"));
        setMyStringConvertible.setInput("In", "foobar");
        svc.getExecutor(setMyStringConvertible.getAction()).executeWithUntypedGeneric(setMyStringConvertible);
		assertNull(setMyStringConvertible.getFailure());
        assertEquals(setMyStringConvertible.getOutput().size(), 0);

        ActionInvocation<? extends LocalService<?>> getMyStringConvertible = new ActionInvocation<>(svc.getAction("GetMyStringConvertible"));
        svc.getExecutor(getMyStringConvertible.getAction()).executeWithUntypedGeneric(getMyStringConvertible);
		assertNull(getMyStringConvertible.getFailure());
        assertEquals(getMyStringConvertible.getOutput().size(), 1);
        assertEquals(getMyStringConvertible.getOutput().iterator().next().toString(), "foobar");

    }
}
