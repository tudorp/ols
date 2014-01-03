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
package nl.lxtreme.ols.device.sump.protocol;


import java.io.*;
import java.util.logging.*;

import nl.lxtreme.ols.device.sump.*;


/**
 * Wrapper to write SUMP-specific commands to a normal {@link DataOutputStream}.
 */
public class SumpCommandWriter implements SumpProtocolConstants, Closeable
{
  // CONSTANTS

  /** set trigger mask */
  private static final int SETTRIGMASK = 0xc0;
  /** set trigger value */
  private static final int SETTRIGVAL = 0xc1;
  /** set trigger configuration */
  private static final int SETTRIGCFG = 0xc2;
  /** set clock divider */
  private static final int SETDIVIDER = 0x80;
  /** set sample counters */
  private static final int SETSIZE = 0x81;
  /** set flags */
  private static final int SETFLAGS = 0x82;
  /** read count, used by Pipistrello. */
  private static final int READ_COUNT = 0x83;
  /** delay count, used by Pipistrello. */
  private static final int DELAY_COUNT = 0x84;

  /** demultiplex */
  public static final int FLAG_DEMUX = 0x00000001;
  /** noise filter */
  public static final int FLAG_FILTER = 0x00000002;
  /** channel group 1 enabled? */
  public static final int FLAG_GROUP1_DISABLED = 0x00000004;
  /** channel group 2 enabled? */
  public static final int FLAG_GROUP2_DISABLED = 0x00000008;
  /** channel group 3 enabled? */
  public static final int FLAG_GROUP3_DISABLED = 0x00000010;
  /** channel group 4 enabled? */
  public static final int FLAG_GROUP4_DISABLED = 0x00000020;
  /** external trigger? */
  public static final int FLAG_EXTERNAL = 0x00000040;
  /** inverted */
  public static final int FLAG_INVERTED = 0x00000080;
  /** run length encoding */
  public static final int FLAG_RLE = 0x00000100;
  public static final int FLAG_RLE_MODE_0 = 0x00000000;
  public static final int FLAG_RLE_MODE_1 = 0x00004000;
  public static final int FLAG_RLE_MODE_2 = 0x00008000;
  public static final int FLAG_RLE_MODE_3 = 0x0000C000;
  /** Number Scheme */
  public static final int FLAG_NUMBER_SCHEME = 0x00000200;
  /** Testing mode (external, bits 16:32) */
  public static final int FLAG_EXTERNAL_TEST_MODE = 0x00000400;
  /** Testing mode (internal) */
  public static final int FLAG_INTERNAL_TEST_MODE = 0x00000800;

  private static final Logger LOG = Logger.getLogger( SumpCommandWriter.class.getName() );

  // VARIABLES

  protected final SumpConfig config;
  private final DataOutputStream outputStream;

  // CONSTRUCTORS

  /**
   * Creates a new {@link SumpCommandWriter} instance.
   * 
   * @param aConfiguration
   *          the configuration to use, cannot be <code>null</code>;
   * @param aOutputStream
   *          the {@link DataOutputStream} to wrap, cannot be <code>null</code>.
   */
  public SumpCommandWriter( final SumpConfig aConfiguration, final DataOutputStream aOutputStream )
  {
    this.config = aConfiguration;
    this.outputStream = aOutputStream;
  }

  // METHODS

  /**
   * {@inheritDoc}
   */
  @Override
  public void close() throws IOException
  {
    this.outputStream.close();
  }

  /**
   * @param aConfiguration
   * @throws IOException
   */
  public void writeCmdFinishNow() throws IOException
  {
    final boolean isRleEnabled = this.config.isRleEnabled();
    if ( isRleEnabled )
    {
      LOG.info( "Prematurely finishing RLE-enabled capture ..." );
      sendCommand( CMD_RLE_FINISH_NOW );
    }
  }

  /**
   * @throws IOException
   */
  public void writeCmdGetId() throws IOException
  {
    sendCommand( CMD_ID );
  }

  /**
   * Writes the 'get metadata' command to the device.
   */
  public void writeCmdGetMetadata() throws IOException
  {
    sendCommand( CMD_METADATA );
  }

  /**
   * Resets the OLS device by sending 5 consecutive 'reset' commands.
   * 
   * @throws IOException
   *           in case of I/O problems.
   */
  public void writeCmdReset() throws IOException
  {
    for ( int i = 0; i < 5; i++ )
    {
      sendCommand( CMD_RESET );
    }
  }

  /**
   * @throws IOException
   */
  public void writeCmdRun() throws IOException
  {
    sendCommand( CMD_RUN );
  }

  /**
   * @param aConfiguration
   * @throws IOException
   */
  public void writeDeviceConfiguration() throws IOException
  {
    // set the sampling frequency...
    sendCommand( SETDIVIDER, this.config.getDivider() );

    configureTriggers();

    final int delayCount = this.config.getDelayCount();
    final int readCount = this.config.getReadCount();

    if ( this.config.isReadDelayCountValueCombined() )
    {
      // set the capture size...
      sendCommand( SETSIZE, this.config.getCombinedReadDelayCount() );
    }
    else
    {
      sendCommand( READ_COUNT, readCount );
      sendCommand( DELAY_COUNT, delayCount );
    }

    int flags = this.config.getFlags();

    // finally set the device flags...
    sendCommand( SETFLAGS, flags );
  }

  /**
   * Sends a short command to the given stream. This method is intended to be
   * used for short commands, but can also be called with long command opcodes
   * if the data portion is to be set to 0.
   * 
   * @param aOpcode
   *          one byte operation code
   * @throws IOException
   *           if writing to stream fails
   */
  protected final void sendCommand( final int aOpcode ) throws IOException
  {
    LOG.log( Level.INFO, "Sending short command: 0x{0}", new Object[] { Integer.toHexString( aOpcode & 0xFF ) } );

    this.outputStream.writeByte( aOpcode );
    this.outputStream.flush();
  }

  /**
   * Sends a long command to the given stream.
   * 
   * @param aOpcode
   *          one byte operation code
   * @param aData
   *          four byte data portion
   * @throws IOException
   *           if writing to stream fails
   */
  protected final void sendCommand( final int aOpcode, final int aData ) throws IOException
  {
    final byte[] raw = new byte[5];
    int mask = 0xff;
    int shift = 0;

    raw[0] = ( byte )aOpcode;
    for ( int i = 1; i < 5; i++ )
    {
      raw[i] = ( byte )( ( aData & mask ) >> shift );
      mask = mask << 8;
      shift += 8;
    }

    LOG.log( Level.INFO, "Sending long command: 0x{0} with data 0x{1} 0x{2} 0x{3} 0x{4}",
        new Object[] { Integer.toHexString( raw[0] & 0xFF ), String.format( "%02x", raw[1] ),
            String.format( "%02x", raw[2] ), String.format( "%02x", raw[3] ), String.format( "%02x", raw[4] ) } );

    this.outputStream.write( raw );
    this.outputStream.flush();
  }

  /**
   * Sends the trigger mask, value and configuration to the OLS device.
   * 
   * @throws IOException
   *           in case of I/O problems.
   */
  private void configureTriggers() throws IOException
  {
    if ( this.config.isTriggerEnabled() )
    {
      for ( int i = 0; i < this.config.getTriggerStageCount(); i++ )
      {
        final int indexMask = 4 * i;
        sendCommand( SETTRIGMASK | indexMask, this.config.getTriggerMask( i ) );
        sendCommand( SETTRIGVAL | indexMask, this.config.getTriggerValue( i ) );
        sendCommand( SETTRIGCFG | indexMask, this.config.getTriggerConfig( i ) );
      }
    }
    else
    {
      sendCommand( SETTRIGMASK, 0 );
      sendCommand( SETTRIGVAL, 0 );
      sendCommand( SETTRIGCFG, SumpConfigBuilder.TRIGGER_CAPTURE );
    }
  }
}
