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

package com.distrimind.upnp_igd.support.shared.log.impl;

import com.distrimind.upnp_igd.support.shared.TextExpand;
import com.distrimind.upnp_igd.support.shared.log.LogView;
import com.distrimind.upnp_igd.swing.logging.LogMessage;

import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import javax.swing.SwingUtilities;

/**
 * @author Christian Bauer
 */
@ApplicationScoped
public class LogPresenter implements LogView.Presenter {

    @Inject
    protected LogView view;

    @Inject
    protected Event<TextExpand> textExpandEvent;

    @Override
	public void init() {
        view.setPresenter(this);
    }

    @Override
    public void onExpand(LogMessage logMessage) {
        textExpandEvent.fire(new TextExpand(logMessage.getMessage()));
    }

    @PreDestroy
    public void destroy() {
        SwingUtilities.invokeLater(() -> view.dispose());
    }

    @Override
    public void pushMessage(final LogMessage message) {
        SwingUtilities.invokeLater(() -> view.pushMessage(message));
    }

}
