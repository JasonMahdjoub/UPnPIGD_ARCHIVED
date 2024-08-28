 /*
 * Copyright (C) 2012 4th Line GmbH, Switzerland
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
 package com.distrimind.upnp_igd.swing;

import javax.swing.AbstractButton;
import java.awt.Container;
import java.awt.event.ActionListener;
import java.awt.event.WindowListener;

/**
 * Interface for building a hierarchical controller structure (HMVC).
 * HMVC works with a tree of triads, these triads are a Model (usually several
 * JavaBeans and their binding models for the UI), a View (usually several Swing
 * UI components), and a Controller. This is a basic interface of a controller
 * that has a pointer to a parent controller (can be null if it's the root of the
 * tree) and a collection of subcontrollers (can be empty, usually isn't empty).
 * The hierarchy of controller supports propagation of action execution and
 * propagation of events.
 * If a controllers view is a {@link java.awt.Frame}, you should also register it as a
 * {@link WindowListener}, so that it can properly clean up its state when the
 * window is closed.
 *
 * @author Christian Bauer
 */
public interface Controller<V extends Container> extends ActionListener, WindowListener {

    V getView();

    Controller<? extends Container> getParentController();

    java.util.List<Controller<?>> getSubControllers();

    void dispose();

    <E extends Event<?>> void registerEventListener(Class<E> eventClass, EventListener<E> eventListener);
    <PAYLOAD> void fireEvent(Event<PAYLOAD> event);
    <PAYLOAD> void fireEventGlobal(Event<PAYLOAD> event);
    <PAYLOAD> void fireEvent(Event<PAYLOAD> event, boolean global);

    void registerAction(AbstractButton source, DefaultAction action);
    void registerAction(AbstractButton source, String actionCommand, DefaultAction action);
    void preActionExecute();
    void postActionExecute();
    void failedActionExecute();
    void finalActionExecute();
}
