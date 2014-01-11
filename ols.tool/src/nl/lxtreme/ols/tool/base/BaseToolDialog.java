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
package nl.lxtreme.ols.tool.base;


import static nl.lxtreme.ols.util.swing.SwingComponentUtils.*;

import java.awt.*;
import java.awt.Dialog.ModalExclusionType;
import java.awt.event.*;
import java.awt.Cursor;
import java.util.*;
import java.util.concurrent.*;

import javax.swing.*;

import nl.lxtreme.ols.common.acquisition.*;
import nl.lxtreme.ols.common.acquisition.Cursor.LabelStyle;
import nl.lxtreme.ols.task.execution.*;
import nl.lxtreme.ols.tool.api.*;
import nl.lxtreme.ols.util.swing.*;
import nl.lxtreme.ols.util.swing.StandardActionFactory.CloseAction.Closeable;
import nl.lxtreme.ols.util.swing.Configurable;

import org.osgi.framework.*;
import org.osgi.service.event.*;
import org.osgi.service.event.Event;


/**
 * Provides a base tool dialog.
 */
public abstract class BaseToolDialog<RESULT_TYPE> extends JFrame implements ToolDialog, EventHandler, Configurable,
    Closeable
{
  // INNER TYPES

  /**
   * Provides a renderer for the cursor combobox.
   */
  final class CursorComboBoxRenderer extends DefaultListCellRenderer
  {
    private static final long serialVersionUID = 1L;

    @Override
    public Component getListCellRendererComponent( final JList aList, final Object aValue, final int aIndex,
        final boolean aIsSelected, final boolean aCellHasFocus )
    {
      String text;
      if ( ( aValue != null ) && ( aValue instanceof nl.lxtreme.ols.common.acquisition.Cursor ) )
      {
        text = ( ( nl.lxtreme.ols.common.acquisition.Cursor )aValue ).getLabel( LabelStyle.LABEL_TIME );
      }
      else if ( aValue != null )
      {
        text = String.valueOf( aValue );
      }
      else
      {
        text = "";
      }

      return super.getListCellRendererComponent( aList, text, aIndex, aIsSelected, aCellHasFocus );
    }
  }

  // CONSTANTS

  private static final long serialVersionUID = 1L;

  /** Provides insets (padding) that can be used for labels. */
  protected static final Insets LABEL_INSETS = new Insets( 4, 4, 4, 2 );
  /** Provides insets (padding) that can be used for components. */
  protected static final Insets COMP_INSETS = new Insets( 4, 2, 4, 4 );

  // VARIABLES

  private final ToolContext context;
  private final Tool<RESULT_TYPE> tool;
  private final BundleContext bundleContext;

  private final TaskExecutionServiceTracker taskExecutionService;
  private final ToolProgressListenerServiceTracker toolProgressListener;

  private ServiceRegistration serviceReg;
  private volatile Future<RESULT_TYPE> toolFutureTask;
  private volatile ToolTask<RESULT_TYPE> toolTask;
  private volatile RESULT_TYPE lastResult;

  // CONSTRUCTORS

  private JCheckBox decodeAll;

  // METHODS

  private JComboBox markerA;

  private JComboBox markerB;

  /**
   * Creates a new {@link BaseToolDialog} instance that is document modal.
   * 
   * @param aOwner
   *          the owning window of this dialog;
   * @param aTitle
   *          the title of this dialog;
   * @param aContext
   *          the tool context to use in this dialog.
   */
  protected BaseToolDialog( final Window aOwner, final ToolContext aContext, final BundleContext aBundleContext,
      final Tool<RESULT_TYPE> aTool )
  {
    super( aTool.getName() );

    this.context = aContext;
    this.bundleContext = aBundleContext;
    this.tool = aTool;

    setModalExclusionType( ModalExclusionType.NO_EXCLUDE );

    this.taskExecutionService = new TaskExecutionServiceTracker( aBundleContext );
    this.toolProgressListener = new ToolProgressListenerServiceTracker( aBundleContext );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void cancelTool() throws IllegalStateException
  {
    if ( this.toolFutureTask == null )
    {
      throw new IllegalStateException( "Tool is already cancelled!" );
    }

    this.toolFutureTask.cancel( true /* mayInterruptIfRunning */);
    this.toolFutureTask = null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void close()
  {
    this.taskExecutionService.close();
    this.toolProgressListener.close();

    try
    {
      this.serviceReg.unregister();
      this.serviceReg = null;
    }
    catch ( IllegalStateException exception )
    {
      // Ignore; we're closing anyway...
    }

    onBeforeCloseDialog();

    setVisible( false );
    dispose();
  }

  /**
   * @return
   */
  public final ToolContext getContext()
  {
    return this.context;
  }

  /**
   * Returns the current value of lastResult.
   * 
   * @return the lastResult
   */
  public final RESULT_TYPE getLastResult()
  {
    return this.lastResult;
  }

  /**
   * Returns the current value of tool.
   * 
   * @return the tool
   */
  public final Tool<RESULT_TYPE> getTool()
  {
    return this.tool;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @SuppressWarnings( "unchecked" )
  public void handleEvent( Event aEvent )
  {
    String status = ( String )aEvent.getProperty( "status" );
    if ( "success".equals( status ) )
    {
      this.lastResult = ( RESULT_TYPE )aEvent.getProperty( "result" );
      // Long timeTaken = ( Long )aEvent.getProperty( "time" );

      SwingComponentUtils.invokeOnEDT( new Runnable()
      {
        @Override
        public void run()
        {
          setCursor( Cursor.getPredefinedCursor( Cursor.DEFAULT_CURSOR ) );

          setControlsEnabled( true );

          onToolEnded( BaseToolDialog.this.lastResult );
        }
      } );

      this.toolFutureTask = null;
      this.toolTask = null;
    }
    else if ( "failure".equals( status ) )
    {
      final Exception exception = ( Exception )aEvent.getProperty( "exception" );
      // Long timeTaken = ( Long )aEvent.getProperty( "time" );

      SwingComponentUtils.invokeOnEDT( new Runnable()
      {
        @Override
        public void run()
        {
          setCursor( Cursor.getPredefinedCursor( Cursor.DEFAULT_CURSOR ) );

          setControlsEnabled( true );

          onToolFailed( exception );
        }
      } );

      this.toolFutureTask = null;
      this.toolTask = null;
    }
    else if ( "started".equals( status ) )
    {
      SwingComponentUtils.invokeOnEDT( new Runnable()
      {
        @Override
        public void run()
        {
          setCursor( Cursor.getPredefinedCursor( Cursor.WAIT_CURSOR ) );

          setControlsEnabled( false );

          onToolStarted();
        }
      } );
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final boolean invokeTool() throws IllegalStateException
  {
    if ( this.toolFutureTask != null )
    {
      throw new IllegalStateException( "Tool is already running!" );
    }

    boolean settingsValid = validateToolSettings();
    if ( settingsValid )
    {
      this.toolTask = this.tool.createToolTask( this.context, this.toolProgressListener );
      prepareToolTask( this.toolTask );

      this.toolFutureTask = this.taskExecutionService.execute( this.toolTask,
          Collections.singletonMap( "toolName", this.tool.getName() ) );
    }
    return settingsValid;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void showDialog()
  {
    Properties props = new Properties();
    props.put( EventConstants.EVENT_TOPIC, TaskExecutionService.EVENT_TOPIC );
    props.put( EventConstants.EVENT_FILTER, "(toolName=" + this.tool.getName() + ")" );

    this.serviceReg = this.bundleContext.registerService( EventHandler.class.getName(), this, props );

    this.taskExecutionService.open();
    this.toolProgressListener.open();

    onBeforeShowDialog();

    setVisible( true );
  }

  protected final void addDecoderAreaPane( JPanel panel )
  {
    Vector<nl.lxtreme.ols.common.acquisition.Cursor> cursors = new Vector<nl.lxtreme.ols.common.acquisition.Cursor>();
    for ( nl.lxtreme.ols.common.acquisition.Cursor cursor : getData().getCursors() )
    {
      if ( cursor.isDefined() )
      {
        cursors.add( cursor );
      }
    }

    SpringLayoutUtils.addSeparator( panel, "Decoding area" );

    final JLabel markerALabel = createRightAlignedLabel( "Marker A" );
    markerALabel.setEnabled( false );
    this.markerA = createCursorComboBox( cursors, 0 );
    this.markerA.setEnabled( false );

    final JLabel markerBLabel = createRightAlignedLabel( "Marker B" );
    markerBLabel.setEnabled( false );
    this.markerB = createCursorComboBox( cursors, 1 );
    this.markerB.setEnabled( false );

    this.decodeAll = new JCheckBox();
    this.decodeAll.setSelected( true );
    this.decodeAll.setEnabled( !cursors.isEmpty() );
    this.decodeAll.addActionListener( new ActionListener()
    {
      @Override
      public void actionPerformed( ActionEvent aEvent )
      {
        boolean enabled = !decodeAll.isSelected();

        markerALabel.setEnabled( enabled );
        markerA.setEnabled( enabled );

        markerBLabel.setEnabled( enabled );
        markerB.setEnabled( enabled );
      }
    } );

    panel.add( createRightAlignedLabel( "Decode all?" ) );
    panel.add( this.decodeAll );

    panel.add( markerALabel );
    panel.add( this.markerA );

    panel.add( markerBLabel );
    panel.add( this.markerB );
  }

  /**
   * Returns the current value of bundleContext.
   * 
   * @return the bundleContext
   */
  protected final BundleContext getBundleContext()
  {
    return this.bundleContext;
  }

  /**
   * Returns the acquisition result data.
   * 
   * @return the acquisition data, never <code>null</code>.
   */
  protected final AcquisitionData getData()
  {
    return this.context.getData();
  }

  /**
   * @return the (sample) index of the first marker to start decoding from, >=
   *         0.
   */
  protected final int getMarkerAIndex()
  {
    if ( this.decodeAll.isSelected() )
    {
      return 0;
    }
    else
    {
      nl.lxtreme.ols.common.acquisition.Cursor cursor = ( nl.lxtreme.ols.common.acquisition.Cursor )this.markerA
          .getSelectedItem();
      return getData().getSampleIndex( cursor.getTimestamp() );
    }
  }

  /**
   * @return the (sample) index of the second marker to end decoding, >= 0.
   */
  protected final int getMarkerBIndex()
  {
    if ( this.decodeAll.isSelected() )
    {
      int[] values = getData().getValues();
      return values.length - 1;
    }
    else
    {
      nl.lxtreme.ols.common.acquisition.Cursor cursor = ( nl.lxtreme.ols.common.acquisition.Cursor )this.markerB
          .getSelectedItem();
      return getData().getSampleIndex( cursor.getTimestamp() );
    }
  }

  /**
   * Called right before this dialog is made invisible.
   */
  protected void onBeforeCloseDialog()
  {
    // NO-op
  }

  /**
   * Called right before this dialog is made visible.
   */
  protected void onBeforeShowDialog()
  {
    // NO-op
  }

  /**
   * Called when the tool finished its job.
   * <p>
   * <b>THIS METHOD WILL BE INVOKED ON THE EVENT-DISPATCH THREAD (EDT)!</b>
   * </p>
   * 
   * @param aResult
   *          the result of the tool, can be <code>null</code>.
   */
  protected abstract void onToolEnded( RESULT_TYPE aResult );

  /**
   * Called when the tool is failed.
   * <p>
   * By default, shows a error dialog with the details of the failure.
   * </p>
   * <p>
   * <b>THIS METHOD WILL BE INVOKED ON THE EVENT-DISPATCH THREAD (EDT)!</b>
   * </p>
   * 
   * @param aException
   *          the exception with the failure, can be <code>null</code>.
   */
  protected void onToolFailed( final Exception aException )
  {
    ToolUtils.showErrorMessage( getOwner(), "Tool failed!\nDetails: " + aException.getMessage() );
  }

  /**
   * Called when the tool is just started to do its task.
   * <p>
   * <b>THIS METHOD WILL BE INVOKED ON THE EVENT-DISPATCH THREAD (EDT)!</b>
   * </p>
   */
  protected abstract void onToolStarted();

  /**
   * Allows additional preparations to be performed on the given
   * {@link ToolTask} instance, such as setting parameters and such.
   * <p>
   * This method will be called right before the tool task is to be executed.
   * </p>
   * 
   * @param aToolTask
   *          the tool task to prepare, cannot be <code>null</code>.
   */
  protected void prepareToolTask( final ToolTask<RESULT_TYPE> aToolTask )
  {
    // NO-op
  }

  /**
   * Convenience method to set the combobox index to a "safe" value, based on
   * the given user settings.
   * 
   * @param aComboBox
   *          the combobox to set the selected index for;
   * @param aSettings
   *          the user settings to take the selected index from;
   * @param aSettingName
   *          the name of the user setting to use.
   */
  protected final void setComboBoxIndex( final JComboBox aComboBox, final UserSettings aSettings,
      final String aSettingName )
  {
    ToolUtils.setComboBoxIndex( aComboBox, aSettings.getInt( aSettingName, -1 ) );
  }

  /**
   * set the controls of the dialog enabled/disabled
   * 
   * @param aEnabled
   *          status of the controls
   */
  protected void setControlsEnabled( final boolean aEnabled )
  {
    // NO-op
  }

  /**
   * Called right before the tool is invoked to allow additional validation on
   * the tool settings.
   * 
   * @return <code>true</code> if the tool settings are correct and the task can
   *         be started, <code>false</code> if the settings are incorrect and
   *         the task should not be started.
   */
  protected boolean validateToolSettings()
  {
    return true;
  }

  /**
   * @param aCursors
   * @param aDefaultIdx
   * @return
   */
  private JComboBox createCursorComboBox( Vector<nl.lxtreme.ols.common.acquisition.Cursor> aCursors, int aDefaultIdx )
  {
    JComboBox cb = new JComboBox( aCursors );
    if ( aCursors.size() > aDefaultIdx )
    {
      cb.setSelectedIndex( aDefaultIdx );
    }
    cb.setRenderer( new CursorComboBoxRenderer() );
    return cb;
  }
}
