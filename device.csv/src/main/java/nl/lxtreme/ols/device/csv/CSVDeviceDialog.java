/*
 * OpenBench LogicSniffer / SUMP project
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin St, Fifth Floor, Boston, MA 02110, USA
 *
 *
 * Copyright (C) 2010-2011 - J.W. Janssen, http://www.lxtreme.nl
 */
package nl.lxtreme.ols.device.csv;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;

import nl.lxtreme.ols.api.*;
import nl.lxtreme.ols.util.swing.*;
import nl.lxtreme.ols.util.swing.StandardActionFactory.CloseAction.Closeable;

/**
 * @author jawi
 */
@SuppressWarnings("boxing")
public class CSVDeviceDialog extends JDialog implements Configurable, Closeable {
    private static final long serialVersionUID = 1L;

    private JTextField filenameField;

    private boolean setupConfirmed;
    private File file;
    private JFileChooser fileChooser;

    public CSVDeviceDialog(final Window aParent) {
        super(aParent, "Test capture settings", ModalityType.DOCUMENT_MODAL);

        this.setupConfirmed = false;

        fileChooser = new JFileChooser();
                fileChooser.addChoosableFileFilter(new FileFilter() {

                    @Override
                    public boolean accept(File f) {
                        return f.isFile() && f.getName().toLowerCase().endsWith(".csv");
                    }

                    @Override
                    public String getDescription() {
                        return "Comma separated files (*.csv)";
                    }

                });
        initDialog();
    }

    /**
     * @see
     * nl.lxtreme.ols.util.swing.StandardActionFactory.CloseAction.Closeable#close()
     */
    @Override
    public void close() {
        setVisible(false);
        dispose();
    }

    public File getFile() {
        return file;
    }

    /**
     * @see
     * nl.lxtreme.ols.api.Configurable#readPreferences(nl.lxtreme.ols.api.UserSettings)
     */
    @Override
    public void readPreferences(final UserSettings aSettings) {
        if (!aSettings.get("filename", "").isEmpty()) {
            this.file = new File(aSettings.get("filename", ""));
        }
    }

    /**
     * Shows this dialog on screen.
     *
     * @return <code>true</code> if this dialog is confirmed, <code>false</code>
     * if it was cancelled.
     */
    public boolean showDialog() {
        this.setupConfirmed = false;

        setVisible(true);

        return this.setupConfirmed;
    }

    /**
     * @see
     * nl.lxtreme.ols.api.Configurable#writePreferences(nl.lxtreme.ols.api.UserSettings)
     */
    @Override
    public void writePreferences(final UserSettings aSettings) {
        try {
            aSettings.put("filename", this.file.getCanonicalPath());
        } catch (IOException ex) {
            Logger.getLogger(CSVDeviceDialog.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Confirms and closes this dialog.
     */
    final void confirmAndCloseDialog() {
        this.setupConfirmed = true;

        // Make the selected information available for the outside...
        this.file = new File(this.filenameField.getText());

        close();
    }

    /**
     * @return
     */
    private JPanel createContents() {

        filenameField = new JTextField();
        filenameField.setPreferredSize(new Dimension(400, 20));

        filenameField.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {

                if (fileChooser.showOpenDialog(CSVDeviceDialog.this) == JFileChooser.APPROVE_OPTION) {
                    try {
                        filenameField.setText(fileChooser.getSelectedFile().getCanonicalPath());
                    } catch (IOException ex) {
                        Logger.getLogger(CSVDeviceDialog.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }

        });

        final Insets labelInsets = new Insets(4, 4, 4, 2);
        final Insets compInsets = new Insets(4, 2, 4, 4);

        final JPanel result = new JPanel(new GridBagLayout());
        result.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));

        result.add(new JLabel("Input file"), //
                new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.BASELINE_LEADING,
                        GridBagConstraints.HORIZONTAL, labelInsets, 0, 0));
        result.add(this.filenameField, //
                new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.BASELINE_TRAILING,
                        GridBagConstraints.HORIZONTAL, compInsets, 0, 0));

        return result;
    }

    /**
     * Initializes this dialog.
     */
    private void initDialog() {
        final JComponent contents = createContents();
        final JButton closeButton = StandardActionFactory.createCloseButton();

        final JButton okButton = new JButton("Ok");
        okButton.setPreferredSize(closeButton.getPreferredSize());
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent aEvent) {
                confirmAndCloseDialog();
            }
        });

        final JComponent buttonPane = SwingComponentUtils.createButtonPane(okButton, closeButton);

        SwingComponentUtils.setupWindowContentPane(this, contents, buttonPane, okButton);
    }

}
