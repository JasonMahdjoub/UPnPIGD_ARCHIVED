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

package com.distrimind.upnp_igd.model.action;

import com.distrimind.upnp_igd.model.meta.LocalService;
import com.distrimind.upnp_igd.model.meta.QueryStateVariableAction;
import com.distrimind.upnp_igd.model.meta.StateVariable;
import com.distrimind.upnp_igd.model.state.StateVariableAccessor;
import com.distrimind.upnp_igd.model.types.ErrorCode;

/**
 * Special executor for one action, the deprecated "query the value of any state variable" action.
 * 
 * @author Christian Bauer
 */
public class QueryStateVariableExecutor extends AbstractActionExecutor {
    
    @Override
    protected <T> void execute(ActionInvocation<LocalService<T>> actionInvocation, Object serviceImpl) throws Exception {

        // Querying a state variable doesn't mean an actual "action" method on this instance gets invoked
        if (actionInvocation.getAction() instanceof QueryStateVariableAction) {
            if (!actionInvocation.getAction().getService().isSupportsQueryStateVariables()) {
                actionInvocation.setFailure(
                        new ActionException(ErrorCode.INVALID_ACTION, "This service does not support querying state variables")
                );
            } else {
                executeQueryStateVariable(actionInvocation, serviceImpl);
            }
        } else {
            throw new IllegalStateException(
                    "This class can only execute QueryStateVariableAction's, not: " + actionInvocation.getAction()
            );
        }
    }

    protected <T> void executeQueryStateVariable(ActionInvocation<LocalService<T>> actionInvocation, Object serviceImpl) throws Exception {

        LocalService<T> service = actionInvocation.getAction().getService();

        String stateVariableName = actionInvocation.getInput("varName").toString();
        StateVariable<LocalService<T>> stateVariable = service.getStateVariable(stateVariableName);

        if (stateVariable == null) {
            throw new ActionException(
                    ErrorCode.ARGUMENT_VALUE_INVALID, "No state variable found: " + stateVariableName
            );
        }

        StateVariableAccessor accessor;
        if ((accessor = service.getAccessor(stateVariable.getName())) == null) {
            throw new ActionException(
                    ErrorCode.ARGUMENT_VALUE_INVALID, "No accessor for state variable, can't read state: " + stateVariableName
            );
        }

        try {
            setOutputArgumentValue(
                    actionInvocation,
                    actionInvocation.getAction().getOutputArgument("return"),
                    accessor.read(stateVariable, serviceImpl).toString()
            );
        } catch (Exception ex) {
            throw new ActionException(ErrorCode.ACTION_FAILED, ex.getMessage());
        }
    }

}
