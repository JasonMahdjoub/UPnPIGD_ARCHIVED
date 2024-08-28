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

package com.distrimind.upnp_igd.support.shared;

import com.distrimind.upnp_igd.UpnpService;
import com.distrimind.upnp_igd.util.logging.LoggingUtil;
import com.distrimind.upnp_igd.swing.AbstractController;
import com.distrimind.upnp_igd.swing.Application;
import com.distrimind.upnp_igd.swing.logging.LogCategory;
import com.distrimind.upnp_igd.swing.logging.LogController;
import com.distrimind.upnp_igd.swing.logging.LogMessage;
import com.distrimind.upnp_igd.swing.logging.LoggingHandler;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.UIManager;
import java.awt.Dimension;
import java.awt.Frame;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * @author Christian Bauer
 */
public abstract class MainController extends AbstractController<JFrame> {
    static final Logger loggger = Logger.getLogger(MainController.class.getName());
    // Dependencies
    final private LogController logController;

    // View
    final private JPanel logPanel;

    public MainController(JFrame view, List<LogCategory> logCategories) {
        super(view);

        // Some UI stuff (of course, why would the OS L&F be the default -- too easy?!)
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            loggger.log(Level.SEVERE, "Unable to load native look and feel: ", ex);
        }

        // Exception handler
        System.setProperty("sun.awt.exception.handler", AWTExceptionHandler.class.getName());

        // Shutdown behavior
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			if (getUpnpService() != null)
				getUpnpService().shutdown();
		}));

        // Logging UI
        logController = new LogController(this, logCategories) {
            @Override
            protected void expand(LogMessage logMessage) {
                fireEventGlobal(
                        new TextExpandEvent(logMessage.getMessage())
                );
            }

            @Override
            protected Frame getParentWindow() {
                return MainController.this.getView();
            }
        };
        logPanel = logController.getView();
        logPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));

        // Wire UI into JUL
        // Don't reset JUL root logger but add if there is a JUL config file
        Handler handler = new LoggingHandler() {
            @Override
			protected void log(LogMessage msg) {
                logController.pushMessage(msg);
            }
        };
        if (System.getProperty("java.util.logging.config.file") == null) {
            LoggingUtil.resetRootHandler(handler);
        } else {
            LogManager.getLogManager().getLogger("").addHandler(handler);
        }
    }

    public LogController getLogController() {
        return logController;
    }

    public JPanel getLogPanel() {
        return logPanel;
    }

    public void log(Level level, String msg) {
        log(new LogMessage(level, msg));
    }

    public void log(LogMessage message) {
        getLogController().pushMessage(message);
    }

    @Override
    public void dispose() {
        super.dispose();
        ShutdownWindow.INSTANCE.setVisible(true);
        //new Thread(() -> System.exit(0)).start();
    }

    public static class ShutdownWindow extends JWindow {
        private static final long serialVersionUID = 1L;
        final public static JWindow INSTANCE = new ShutdownWindow();

        protected ShutdownWindow() {
            JLabel shutdownLabel = new JLabel("Shutting down, please wait...");
            shutdownLabel.setHorizontalAlignment(JLabel.CENTER);
            getContentPane().add(shutdownLabel);
            setPreferredSize(new Dimension(300, 30));
            pack();
            Application.center(this);
        }
    }

    public abstract UpnpService getUpnpService();

}
