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
 * Copyright (C) 2006-2010 Michael Poppitz, www.sump.org
 * Copyright (C) 2010 J.W. Janssen, www.lxtreme.nl
 */
package nl.lxtreme.ols.client2.views;


import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import nl.lxtreme.ols.common.acquisition.Cursor;
import nl.lxtreme.ols.util.swing.*;
import nl.lxtreme.ols.util.swing.StandardActionFactory.DialogStatus;
import nl.lxtreme.ols.util.swing.StandardActionFactory.StatusAwareCloseableDialog;
import nl.lxtreme.ols.util.swing.component.*;


/**
 * Provides a Swing dialog for editing a cursor label.
 */
public class EditCursorDialog extends JDialog implements StatusAwareCloseableDialog
{
  // CONSTANTS

  private static final long serialVersionUID = 1L;

  // VARIABLES

  private final String defaultLabel;
  private final Color defaultColor;

  private JTextField labelEditor;
  private JColorEditor colorEditor;

  boolean dialogResult = false;

  // CONSTRUCTORS

  /**
   * Creates a new EditCursorLabelAction.EditCursorDialog instance.
   */
  public EditCursorDialog( Window aParent, Cursor aCursor )
  {
    super( aParent, ModalityType.APPLICATION_MODAL );

    this.defaultLabel = aCursor.getLabel();
    this.defaultColor = aCursor.getColor();

    initDialog( aCursor );
  }

  // METHODS

  /**
   * {@inheritDoc}
   */
  @Override
  public void close()
  {
    setVisible( false );
    dispose();
  }

  /**
   * Returns the new cursor color.
   * 
   * @return the cursor color, can be <code>null</code>.
   */
  public Color getColor()
  {
    return this.colorEditor.getColor();
  }

  /**
   * Returns the new cursor label.
   * 
   * @return the cursor label, can be <code>null</code>.
   */
  public String getLabel()
  {
    return this.labelEditor.getText();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean setDialogStatus( final DialogStatus aStatus )
  {
    this.dialogResult = ( aStatus == DialogStatus.OK );
    return true;
  }

  /**
   * Makes this dialog visible on screen and waits until it is dismissed.
   * 
   * @return <code>true</code> if the dialog is acknowledged by the user,
   *         <code>false</code> if it is cancelled by the user.
   */
  public boolean showDialog()
  {
    this.dialogResult = false;
    setVisible( true );
    return this.dialogResult;
  }

  /**
   * Applies the default properties to the properties.
   */
  protected void applyDefaultProperties()
  {
    this.labelEditor.setText( this.defaultLabel );
    this.colorEditor.setColor( this.defaultColor );
  }

  /**
   * Initializes this dialog.
   */
  private void initDialog( final Cursor aCursor )
  {
    setTitle( "Edit cursor properties" );
    setLocationRelativeTo( getParent() );
    setResizable( false );

    JLabel labelEditorLabel = SwingComponentUtils.createRightAlignedLabel( "Label" );
    this.labelEditor = new JTextField( aCursor.getLabel(), 10 );

    JLabel colorEditorLabel = SwingComponentUtils.createRightAlignedLabel( "Color" );
    this.colorEditor = new JColorEditor( UIMgr.getCursorColor( aCursor ) );

    JButton okButton = StandardActionFactory.createOkButton();
    JButton cancelButton = StandardActionFactory.createCancelButton();

    JButton resetButton = new JButton( "Reset to defaults" );
    resetButton.addActionListener( new ActionListener()
    {
      @Override
      public void actionPerformed( final ActionEvent aEvent )
      {
        applyDefaultProperties();
      }
    } );

    JPanel resetButtonPanel = new JPanel();
    resetButtonPanel.add( resetButton, BorderLayout.LINE_END );

    JPanel editorPane = new JPanel( new SpringLayout() );
    editorPane.add( labelEditorLabel );
    editorPane.add( this.labelEditor );

    editorPane.add( colorEditorLabel );
    editorPane.add( this.colorEditor );

    editorPane.add( new JLabel( "" ) );
    editorPane.add( resetButtonPanel );
    
    SpringLayoutUtils.makeEditorGrid( editorPane, 10, 10 );

    JComponent buttonPane = SwingComponentUtils.createButtonPane( okButton, cancelButton );
    
    SwingComponentUtils.setupWindowContentPane( this, editorPane, buttonPane, okButton );
  }
}
