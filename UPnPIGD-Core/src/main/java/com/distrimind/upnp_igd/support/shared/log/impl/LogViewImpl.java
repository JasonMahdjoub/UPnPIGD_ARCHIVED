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

import com.distrimind.upnp_igd.support.shared.CenterWindow;
import com.distrimind.upnp_igd.support.shared.log.LogView;
import com.distrimind.upnp_igd.swing.Application;
import com.distrimind.upnp_igd.swing.logging.LogCategorySelector;
import com.distrimind.upnp_igd.swing.logging.LogController;
import com.distrimind.upnp_igd.swing.logging.LogMessage;
import com.distrimind.upnp_igd.swing.logging.LogTableCellRenderer;
import com.distrimind.upnp_igd.swing.logging.LogTableModel;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Christian Bauer
 */
@Singleton
public class LogViewImpl extends JPanel implements LogView {
	private static final long serialVersionUID = 1L;
    @Inject
    protected LogCategories logCategories;

    @Inject
    protected Event<CenterWindow> centerWindowEvent;

    protected LogCategorySelector logCategorySelector;
    protected JTable logTable;
    protected LogTableModel logTableModel;

    final protected JToolBar toolBar = new JToolBar();

    final protected JButton configureButton =
            new JButton("Options...", Application.createImageIcon(LogController.class, "img/configure.png"));

    final protected JButton clearButton =
            new JButton("Clear Log", Application.createImageIcon(LogController.class, "img/removetext.png"));

    final protected JButton copyButton =
            new JButton("Copy", Application.createImageIcon(LogController.class, "img/copyclipboard.png"));

    final protected JButton expandButton =
            new JButton("Expand", Application.createImageIcon(LogController.class, "img/viewtext.png"));

    final protected JButton pauseButton =
            new JButton("Pause/Continue Log", Application.createImageIcon(LogController.class, "img/pause.png"));

    final protected JLabel pauseLabel = new JLabel(" (Active)");

    final protected JComboBox<?> expirationComboBox = new JComboBox<>(LogController.Expiration.values());

    protected Presenter presenter;

    @PostConstruct
	@SuppressWarnings("PMD.CompareObjectsWithEquals")
    public void init() {
        setLayout(new BorderLayout());

        LogController.Expiration defaultExpiration = getDefaultExpiration();

        logCategorySelector = new LogCategorySelector(logCategories);

        logTableModel = new LogTableModel(defaultExpiration.getSeconds());
        logTable = new JTable(logTableModel);

        logTable.setDefaultRenderer(
                LogMessage.class,
                new LogTableCellRenderer() {
                    // TODO: These should be injected
                    @Override
					protected ImageIcon getWarnErrorIcon() {
                        return LogViewImpl.this.getWarnErrorIcon();
                    }

                    @Override
					protected ImageIcon getDebugIcon() {
                        return LogViewImpl.this.getDebugIcon();
                    }

                    @Override
					protected ImageIcon getTraceIcon() {
                        return LogViewImpl.this.getTraceIcon();
                    }

                    @Override
					protected ImageIcon getInfoIcon() {
                        return LogViewImpl.this.getInfoIcon();
                    }
                });

        logTable.setCellSelectionEnabled(false);
        logTable.setRowSelectionAllowed(true);
        logTable.getSelectionModel().addListSelectionListener(
				e -> {

					if (e.getValueIsAdjusting()) return;

					if (e.getSource() == logTable.getSelectionModel()) {
						int[] rows = logTable.getSelectedRows();

						if (rows == null || rows.length == 0) {
							copyButton.setEnabled(false);
							expandButton.setEnabled(false);
						} else if (rows.length == 1) {
							copyButton.setEnabled(true);
							LogMessage msg = (LogMessage) logTableModel.getValueAt(rows[0], 0);
							// TODO: This setting should be injected
							expandButton.setEnabled(msg.getMessage().length() > getExpandMessageCharacterLimit());
						} else {
							copyButton.setEnabled(true);
							expandButton.setEnabled(false);
						}
					}
				}
		);

        adjustTableUI();
        initializeToolBar(defaultExpiration);

        setPreferredSize(new Dimension(250, 100));
        setMinimumSize(new Dimension(250, 50));
        add(new JScrollPane(logTable), BorderLayout.CENTER);
        add(toolBar, BorderLayout.SOUTH);
    }

    @Override
    public Component asUIComponent() {
        return this;
    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void pushMessage(LogMessage logMessage) {
        logTableModel.pushMessage(logMessage);

        // Scroll to bottom if nothing is selected
        if (!logTableModel.isPaused()) {
            logTable.scrollRectToVisible(
                    logTable.getCellRect(logTableModel.getRowCount() - 1, 0, true)
            );
        }
    }

    @Override
    public void dispose() {
        logCategorySelector.dispose();
    }

    protected void adjustTableUI() {
        logTable.setFocusable(false);
        logTable.setRowHeight(18);
        logTable.getTableHeader().setReorderingAllowed(false);
        logTable.setBorder(BorderFactory.createEmptyBorder());

        logTable.getColumnModel().getColumn(0).setMinWidth(30);
        logTable.getColumnModel().getColumn(0).setMaxWidth(30);
        logTable.getColumnModel().getColumn(0).setResizable(false);


        logTable.getColumnModel().getColumn(1).setMinWidth(90);
        logTable.getColumnModel().getColumn(1).setMaxWidth(90);
        logTable.getColumnModel().getColumn(1).setResizable(false);

        logTable.getColumnModel().getColumn(2).setMinWidth(110);
        logTable.getColumnModel().getColumn(2).setMaxWidth(250);

        logTable.getColumnModel().getColumn(3).setPreferredWidth(150);
        logTable.getColumnModel().getColumn(3).setMaxWidth(400);

        logTable.getColumnModel().getColumn(4).setPreferredWidth(600);
    }

    protected void initializeToolBar(LogController.Expiration expiration) {
        configureButton.setFocusable(false);
        configureButton.addActionListener(e -> {
			centerWindowEvent.fire(new CenterWindow(logCategorySelector));
			logCategorySelector.setVisible(!logCategorySelector.isVisible());
		});

        clearButton.setFocusable(false);
        clearButton.addActionListener(e -> logTableModel.clearMessages());

        copyButton.setFocusable(false);
        copyButton.setEnabled(false);
        copyButton.addActionListener(e -> {
			StringBuilder sb = new StringBuilder();
			List<LogMessage> messages = getSelectedMessages();
			for (LogMessage message : messages) {
				sb.append(message.toString()).append("\n");
			}
			Application.copyToClipboard(sb.toString());
		});

        expandButton.setFocusable(false);
        expandButton.setEnabled(false);
        expandButton.addActionListener(e -> {
			List<LogMessage> messages = getSelectedMessages();
			if (messages.size() != 1) return;
			presenter.onExpand(messages.get(0));
		});

        pauseButton.setFocusable(false);
        pauseButton.addActionListener(e -> {
			logTableModel.setPaused(!logTableModel.isPaused());
			if (logTableModel.isPaused()) {
				pauseLabel.setText(" (Paused)");
			} else {
				pauseLabel.setText(" (Active)");
			}
		});

        expirationComboBox.setSelectedItem(expiration);
        expirationComboBox.setMaximumSize(new Dimension(100, 32));
        expirationComboBox.addActionListener(e -> {
			JComboBox<?> cb = (JComboBox<?>) e.getSource();
			LogController.Expiration expiration1 = (LogController.Expiration) cb.getSelectedItem();
			logTableModel.setMaxAgeSeconds(expiration1 ==null?0: expiration1.getSeconds());
		});

        toolBar.setFloatable(false);
        toolBar.add(copyButton);
        toolBar.add(expandButton);
        toolBar.add(Box.createHorizontalGlue());
        toolBar.add(configureButton);
        toolBar.add(clearButton);
        toolBar.add(pauseButton);
        toolBar.add(pauseLabel);
        toolBar.add(Box.createHorizontalGlue());
        toolBar.add(new JLabel("Clear after:"));
        toolBar.add(expirationComboBox);
    }

    protected LogController.Expiration getDefaultExpiration() {
        return LogController.Expiration.SIXTY_SECONDS;
    }

    protected ImageIcon getWarnErrorIcon() {
        return Application.createImageIcon(LogController.class, "img/warn.png");
    }

    protected ImageIcon getDebugIcon() {
        return Application.createImageIcon(LogController.class, "img/debug.png");
    }

    protected ImageIcon getTraceIcon() {
        return Application.createImageIcon(LogController.class, "img/trace.png");
    }

    protected ImageIcon getInfoIcon() {
        return Application.createImageIcon(LogController.class, "img/info.png");
    }

    protected int getExpandMessageCharacterLimit() {
        return 100;
    }

    protected List<LogMessage> getSelectedMessages() {
        List<LogMessage> messages = new ArrayList<>();
        for (int row : logTable.getSelectedRows()) {
            messages.add((LogMessage) logTableModel.getValueAt(row, 0));
        }
        return messages;
    }
}
