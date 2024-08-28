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
import com.distrimind.upnp_igd.model.types.UDADeviceType;
import com.distrimind.upnp_igd.model.types.UDAServiceId;
import com.distrimind.upnp_igd.model.types.UDAServiceType;
import com.distrimind.upnp_igd.test.data.SampleData;
import com.distrimind.upnp_igd.test.data.SampleServiceOne;
import com.distrimind.upnp_igd.test.gena.OutgoingSubscriptionLifecycleTest;
import com.distrimind.upnp_igd.test.local.LocalActionInvocationEnumTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Annotating a service implementation
 * <p>
 * The previously shown service class had a few annotations on the class itself, declaring
 * the name and version of the service. Then annotations on fields were used to declare the
 * state variables of the service and annotations on methods to declare callable actions.
 * </p>
 * <p>
 * Your service implementation might not have fields that directly map to UPnP state variables.
 * </p>
 * <div class="section">
 * <a class="citation" href="javadoc://example.localservice.SwitchPowerAnnotatedClass"/>
 * </div>
 * <p>
 * UPnPIGD tries to provide smart defaults. For example, the previously shown service classes
 * did not name the related state variable of action output arguments, as required by UPnP.
 * UPnPIGD will automatically detect that the <code>getStatus()</code> method is a JavaBean
 * getter method (its name starts with <code>get</code> or <code>is</code>) and use the
 * JavaBean property name to find the related state variable. In this case that would be
 * the JavaBean property <code>status</code> and UPnPIGD is also smart enough to know that
 * you really want the uppercase UPnP state variable named <code>Status</code>.
 * </p>
 * <div class="section">
 * <a class="citation" href="javadoc://example.localservice.SwitchPowerNamedStateVariable"/>
 * </div>
 * <p>
 * For the next example, let's assume you have a class that was already written, not
 * necessarily  as a service backend for UPnP but for some other purpose. You can't
 * redesign and rewrite your class without interrupting all existing code. UPnPIGD offers
 * some flexibility in the mapping of action methods, especially how the output of
 * an action call is obtained.
 * </p>
 * <div class="section">
 * <a class="citation" href="javadoc://example.localservice.SwitchPowerExtraGetter"/>
 * </div>
 * <p>
 * Alternatively, and especially if an action has several output arguments, you
 * can return multiple values wrapped in a JavaBean from your action method.
 * </p>
 * <div class="section">
 * <a class="citation" href="javadoc://example.localservice.SwitchPowerBeanReturn"/>
 * </div>
 */
public class BasicBindingTest {

    public static final String GET_STATUS = "GetStatus";

    public <T> LocalDevice<T> createTestDevice(Class<T> serviceClass) throws Exception {

        LocalServiceBinder binder = new AnnotationLocalServiceBinder();
        LocalService<T> svc = binder.read(serviceClass);
        svc.setManager(new DefaultServiceManager<>(svc, serviceClass));

        return new LocalDevice<>(
                SampleData.createLocalDeviceIdentity(),
                new UDADeviceType("BinaryLight", 1),
                new DeviceDetails("Example Binary Light"),
                svc
        );
    }

    @DataProvider(name = "devices")
    public Object[][] getDevices() {


        try {
            return new LocalDevice[][]{
                    {createTestDevice(SwitchPowerNamedStateVariable.class)},
                    {createTestDevice(SwitchPowerAnnotatedClass.class)},
                    {createTestDevice(SwitchPowerExtraGetter.class)},
                    {createTestDevice(SwitchPowerBeanReturn.class)},
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

/*
        System.out.println("############################################################################");
        ServiceDescriptorBinder binder = new DefaultUpnpServiceConfiguration().getServiceDescriptorBinderUDA10();
        try {
            System.out.println(binder.generate(svc));
        } catch (DescriptorBindingException e) {
            throw new RuntimeException(e);
        }
        System.out.println("############################################################################");

*/
        assertEquals(svc.getServiceId().toString(), "urn:" + UDAServiceId.DEFAULT_NAMESPACE + ":serviceId:SwitchPower");
        assertEquals(svc.getServiceType().toString(), "urn:" + UDAServiceType.DEFAULT_NAMESPACE + ":service:SwitchPower:1");

        assertEquals(svc.getStateVariables().size(), 2);
        assertEquals(svc.getStateVariable(OutgoingSubscriptionLifecycleTest.TARGET).getTypeDetails().getDatatype().getBuiltin(), Datatype.Builtin.BOOLEAN);
        assertEquals(svc.getStateVariable(OutgoingSubscriptionLifecycleTest.TARGET).getTypeDetails().getDefaultValue(), "0");
		assertFalse(svc.getStateVariable(OutgoingSubscriptionLifecycleTest.TARGET).getEventDetails().isSendEvents());

        assertEquals(svc.getStateVariable(SampleServiceOne.STATUS).getTypeDetails().getDatatype().getBuiltin(), Datatype.Builtin.BOOLEAN);
        assertEquals(svc.getStateVariable(SampleServiceOne.STATUS).getTypeDetails().getDefaultValue(), "0");
		assertTrue(svc.getStateVariable(SampleServiceOne.STATUS).getEventDetails().isSendEvents());

        assertEquals(svc.getActions().size(), 4); // Has 3 actions plus QueryStateVariableAction!

        assertEquals(svc.getAction(SampleServiceOne.SET_TARGET).getName(), SampleServiceOne.SET_TARGET);
        assertEquals(svc.getAction(SampleServiceOne.SET_TARGET).getArguments().size(), 1);
        assertEquals(svc.getAction(SampleServiceOne.SET_TARGET).getArguments().iterator().next().getName(), "NewTargetValue");
        assertEquals(svc.getAction(SampleServiceOne.SET_TARGET).getArguments().iterator().next().getDirection(), ActionArgument.Direction.IN);
        assertEquals(svc.getAction(SampleServiceOne.SET_TARGET).getArguments().iterator().next().getRelatedStateVariableName(), OutgoingSubscriptionLifecycleTest.TARGET);

        assertEquals(svc.getAction(LocalActionInvocationEnumTest.GET_TARGET).getName(), LocalActionInvocationEnumTest.GET_TARGET);
        assertEquals(svc.getAction(LocalActionInvocationEnumTest.GET_TARGET).getArguments().size(), 1);
        assertEquals(svc.getAction(LocalActionInvocationEnumTest.GET_TARGET).getArguments().iterator().next().getName(), "RetTargetValue");
        assertEquals(svc.getAction(LocalActionInvocationEnumTest.GET_TARGET).getArguments().iterator().next().getDirection(), ActionArgument.Direction.OUT);
        assertEquals(svc.getAction(LocalActionInvocationEnumTest.GET_TARGET).getArguments().iterator().next().getRelatedStateVariableName(), OutgoingSubscriptionLifecycleTest.TARGET);
		assertTrue(svc.getAction(LocalActionInvocationEnumTest.GET_TARGET).getArguments().iterator().next().isReturnValue());

        assertEquals(svc.getAction(GET_STATUS).getName(), GET_STATUS);
        assertEquals(svc.getAction(GET_STATUS).getArguments().size(), 1);
        assertEquals(svc.getAction(GET_STATUS).getArguments().iterator().next().getName(), "ResultStatus");
        assertEquals(svc.getAction(GET_STATUS).getArguments().iterator().next().getDirection(), ActionArgument.Direction.OUT);
        assertEquals(svc.getAction(GET_STATUS).getArguments().iterator().next().getRelatedStateVariableName(), SampleServiceOne.STATUS);
		assertTrue(svc.getAction(GET_STATUS).getArguments().iterator().next().isReturnValue());

    }

    @Test(dataProvider =  "devices")
    public void invokeActions(LocalDevice<?> device) {
        // We mostly care about the binding without exceptions, but let's also test invocation
        LocalService<?> svc = device.getServices().iterator().next();

        ActionInvocation<? extends LocalService<?>> setTargetInvocation = new ActionInvocation<>(svc.getAction(SampleServiceOne.SET_TARGET));
        setTargetInvocation.setInput("NewTargetValue", true);
        svc.getExecutor(setTargetInvocation.getAction()).executeWithUntypedGeneric(setTargetInvocation);
		assertNull(setTargetInvocation.getFailure());
        assertEquals(setTargetInvocation.getOutput().size(), 0);

        ActionInvocation<? extends LocalService<?>> getStatusInvocation = new ActionInvocation<>(svc.getAction(GET_STATUS));
        svc.getExecutor(getStatusInvocation.getAction()).executeWithUntypedGeneric(getStatusInvocation);
		assertNull(getStatusInvocation.getFailure());
        assertEquals(getStatusInvocation.getOutput().size(), 1);
        assertEquals(getStatusInvocation.getOutput().iterator().next().toString(), "1");

        setTargetInvocation = new ActionInvocation<>(svc.getAction(SampleServiceOne.SET_TARGET));
        setTargetInvocation.setInput("NewTargetValue", false);
        svc.getExecutor(setTargetInvocation.getAction()).executeWithUntypedGeneric(setTargetInvocation);
		assertNull(setTargetInvocation.getFailure());
        assertEquals(setTargetInvocation.getOutput().size(), 0);

        ActionInvocation<? extends LocalService<?>> queryStateVariableInvocation = new ActionInvocation<>(svc.getAction("QueryStateVariable"));
        queryStateVariableInvocation.setInput("varName", SampleServiceOne.STATUS);
        svc.getExecutor(queryStateVariableInvocation.getAction()).executeWithUntypedGeneric(queryStateVariableInvocation);
		assertNull(queryStateVariableInvocation.getFailure());
        assertEquals(queryStateVariableInvocation.getOutput().size(), 1);
        assertEquals(queryStateVariableInvocation.getOutput().iterator().next().toString(), "0");

    }

}
