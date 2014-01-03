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
package nl.lxtreme.ols.device.sump;


import static nl.lxtreme.ols.device.sump.SumpConstants.*;

import java.io.*;
import java.util.*;

import nl.lxtreme.ols.common.*;


/**
 * Provides the configuration options for the LogicSniffer device.
 */
public final class SumpConfig extends HashMap<String, Serializable>
{
  // CONSTANTS

  private static final long serialVersionUID = 1L;

  // CONSTRUCTORS

  /**
   * Creates a new {@link SumpConfig} instance with all default settings.
   */
  public SumpConfig()
  {
    super();
  }

  /**
   * Creates a new {@link SumpConfig} instance with a given map with settings.
   * 
   * @param aConfig
   *          the configuration settings, cannot be <code>null</code>.
   */
  public SumpConfig( Map<String, ? extends Serializable> aConfig )
  {
    super( aConfig );
  }

  // METHODS

  /**
   * @return the total number of channels, &gt;= 0 && &lt;= 32.
   */
  public int getChannelCount()
  {
    return ( Integer )get( KEY_CHANNEL_COUNT );
  }

  /**
   * @return the combined value of the read and delay counters, as used in the
   *         original SUMP protocol.
   */
  public int getCombinedReadDelayCount()
  {
    // Get the "raw" values...
    int readCount = ( Integer )get( KEY_READ_COUNT );
    int delayCount = ( Integer )get( KEY_DELAY_COUNT );

    int maxSize = 0x3fffc;
    if ( isDoubleDataRateEnabled() )
    {
      // 0x7fff8 = 511Kb = the maximum size supported by the original SUMP
      // device when using the demultiplexer...
      maxSize = 0x7fff8;
      return ( ( ( delayCount - 8 ) & maxSize ) << 13 ) | ( ( ( readCount & maxSize ) >> 3 ) - 1 );
    }
    // 0x3fffc = 255Kb = the maximum size supported by the original SUMP
    // device...
    return ( ( ( delayCount - 4 ) & maxSize ) << 14 ) | ( ( ( readCount & maxSize ) >> 2 ) - 1 );
  }

  /**
   * @return the connection URI, as string, never <code>null</code>.
   */
  public String getConnectionURI()
  {
    return ( String )get( KEY_CONNECTION_URI );
  }

  /**
   * @return the actual delay count value as should be sent to the device, > 0.
   */
  public int getDelayCount()
  {
    int delayCount = ( Integer )get( KEY_DELAY_COUNT );
    if ( isDoubleDataRateEnabled() )
    {
      return ( ( delayCount - 8 ) & 0x7fffff8 ) >> 3;
    }
    return ( ( delayCount - 4 ) & 0x3ffffffc ) >> 2;
  }

  /**
   * @return the divider value to set, which represents the sample rate used by
   *         the SUMP device.
   */
  public int getDivider()
  {
    return ( Integer )get( KEY_DIVIDER );
  }

  /**
   * @return the number of *enabled* channels, based on the enabled channel
   *         groups, &gt; 0 && &lt= 32.
   */
  public int getEnabledChannelCount()
  {
    return Integer.bitCount( getEnabledChannelMask() );
  }

  /**
   * @return the 32-bit bit mask denoting which channels are enabled.
   */
  public int getEnabledChannelMask()
  {
    return ( Integer )get( KEY_ENABLED_CHANNELS );
  }

  /**
   * @return the number of enabled channel groups, &gt; 0 && &lt;= 3.
   */
  public int getEnabledGroupCount()
  {
    int enabledGroups = ( ~getFlags() >> 2 );
    if ( isDoubleDataRateEnabled() )
    {
      enabledGroups &= 0x3;
    }
    else
    {
      enabledGroups &= 0xF;
    }
    return Integer.bitCount( enabledGroups );
  }

  /**
   * @return the flags value representing the various flags for the SUMP device.
   */
  public int getFlags()
  {
    return ( Integer )get( KEY_FLAGS );
  }

  /**
   * @return the maximum number of groups supported by the SUMP device, &gt; 0
   *         && &lt;= 3.
   */
  public int getGroupCount()
  {
    return ( Integer )get( KEY_GROUP_COUNT );
  }

  /**
   * @return the actual "read count" value as should be sent to the device, > 0.
   */
  public int getReadCount()
  {
    int readCount = ( Integer )get( KEY_READ_COUNT );
    if ( isDoubleDataRateEnabled() )
    {
      return ( readCount >> 3 ) - 1;
    }

    return ( readCount >> 2 ) - 1;
  }

  /**
   * Returns the number of samples to be taken in current configuration.
   * 
   * @return number of samples, >= 0.
   */
  public int getSampleCount()
  {
    int samples = ( Integer )get( KEY_READ_COUNT );
    if ( isDoubleDataRateEnabled() )
    {
      // When the multiplexer is turned on, the upper two channel blocks are
      // disabled, leaving only 16 channels for capturing...
      samples &= 0xfffffff8;
    }
    else
    {
      samples &= 0xfffffffc;
    }
    return samples;
  }

  /**
   * @return the sample rate to use, in Hertz.
   */
  public int getSampleRate()
  {
    return ( Integer )get( KEY_SAMPLE_RATE );
  }

  /**
   * @param aStage
   *          the stage index, &gt;= 0 && &lt;= 3.
   * @return the trigger configuration, as integer value.
   */
  public int getTriggerConfig( int aStage )
  {
    int[] configs = ( int[] )get( KEY_TRIGGER_CONFIG );
    return configs[aStage];
  }

  /**
   * @param aStage
   *          the stage index, &gt;= 0 && &lt;= 3.
   * @return the trigger mask, as integer value.
   */
  public int getTriggerMask( int aStage )
  {
    int[] masks = ( int[] )get( KEY_TRIGGER_MASK );
    return masks[aStage];
  }

  /**
   * @return the trigger position, if triggers are enabled, or
   *         {@link OlsConstants#NOT_AVAILABLE} if triggers are disabled.
   */
  public int getTriggerPosition()
  {
    if ( !isTriggerEnabled() )
    {
      // Not available...
      return OlsConstants.NOT_AVAILABLE;
    }

    // Get the "raw" values...
    int readCount = ( Integer )get( KEY_READ_COUNT );
    int delayCount = ( Integer )get( KEY_DELAY_COUNT );
    int divider = ( Integer )get( KEY_DIVIDER );
    boolean ddr = isDoubleDataRateEnabled();

    // pure magic taken from the original LA sources...
    return readCount - delayCount - 3 - ( 4 / ( divider + 1 ) ) - ( ddr ? 5 : 0 );
  }

  /**
   * @return the maximum number of trigger stages supported by the SUMP device,
   *         &gt; 0.
   */
  public int getTriggerStageCount()
  {
    return ( Integer )get( KEY_TRIGGER_STAGES );
  }

  /**
   * @param aStage
   *          the stage index, &gt;= 0 && &lt;= 3.
   * @return the trigger value, as integer value.
   */
  public int getTriggerValue( int aStage )
  {
    int[] values = ( int[] )get( KEY_TRIGGER_VALUE );
    return values[aStage];
  }

  /**
   * @return <code>true</code> if the DDR/demux mode is enabled,
   *         <code>false</code> otherwise.
   */
  public boolean isDoubleDataRateEnabled()
  {
    return ( getFlags() & 0x01 ) != 0;
  }

  /**
   * @param aGroupIdx
   *          the group index, &gt;= 0 && &lt;= 3.
   * @return <code>true</code> if the group is enabled, <code>false</code>
   *         otherwise.
   */
  public boolean isGroupEnabled( int aGroupIdx )
  {
    int enabledGroups = ( ~getFlags() >> 2 );
    if ( isDoubleDataRateEnabled() )
    {
      enabledGroups &= 0x3;
    }
    else
    {
      enabledGroups &= 0xF;
    }
    return ( enabledGroups & ( 1 << aGroupIdx ) ) != 0;
  }

  /**
   * @return <code>true</code> if the <b>last</b> sample is sent first,
   *         <code>false</code> if the <b>first</b> sample is sent first.
   */
  public boolean isLastSampleSentFirst()
  {
    return ( Boolean )get( KEY_LAST_SAMPLE_SENT_FIRST );
  }

  /**
   * @return <code>true</code> if the read and delay counter values are to be
   *         combined to a single value before being send to the SUMP device, or
   *         <code>false</code> if they should be send as individual values.
   */
  public boolean isReadDelayCountValueCombined()
  {
    return ( Boolean )get( KEY_READ_DELAY_COUNT_COMBINED );
  }

  /**
   * @return <code>true</code> if RLE-compression is enabled, <code>false</code>
   *         if it is disabled.
   */
  public boolean isRleEnabled()
  {
    return ( getFlags() & 0x100 ) != 0;
  }

  /**
   * @return <code>true</code> if the trigger is to be enabled,
   *         <code>false</code> otherwise.
   */
  public boolean isTriggerEnabled()
  {
    return ( Boolean )get( KEY_TRIGGER_ENABLED );
  }

  /**
   * @return <code>true</code> if this configuration is valid,
   *         <code>false</code> otherwise.
   */
  public boolean isValid()
  {
    if ( !isNonEmptyString( KEY_CONNECTION_URI ) )
    {
      return false;
    }

    if ( !isBoolean( KEY_LAST_SAMPLE_SENT_FIRST ) )
    {
      return false;
    }
    if ( !isBoolean( KEY_READ_DELAY_COUNT_COMBINED ) )
    {
      return false;
    }
    if ( !isBoolean( KEY_TRIGGER_ENABLED ) )
    {
      return false;
    }

    if ( !isNumber( KEY_GROUP_COUNT, 1, OlsConstants.MAX_BLOCKS ) )
    {
      return false;
    }
    if ( !isNumber( KEY_CHANNEL_COUNT, 1, OlsConstants.MAX_CHANNELS ) )
    {
      return false;
    }
    if ( !isNumber( KEY_ENABLED_CHANNELS, 1, OlsConstants.MAX_CHANNELS ) )
    {
      return false;
    }
    if ( !isNumber( KEY_SAMPLE_RATE, 1, Integer.MAX_VALUE ) )
    {
      return false;
    }
    if ( !isNumber( KEY_DIVIDER, 0, Integer.MAX_VALUE ) )
    {
      return false;
    }
    if ( !isNumber( KEY_READ_COUNT, 0, Integer.MAX_VALUE ) )
    {
      return false;
    }
    if ( !isNumber( KEY_DELAY_COUNT, 0, Integer.MAX_VALUE ) )
    {
      return false;
    }
    if ( !isNumber( KEY_FLAGS, Integer.MIN_VALUE, Integer.MAX_VALUE ) )
    {
      return false;
    }
    if ( !isNumber( KEY_TRIGGER_STAGES, 0, 4 ) )
    {
      return false;
    }
    if ( !isNumber( KEY_TRIGGER_CONFIG, Integer.MIN_VALUE, Integer.MAX_VALUE ) )
    {
      return false;
    }
    if ( !isNumber( KEY_TRIGGER_MASK, Integer.MIN_VALUE, Integer.MAX_VALUE ) )
    {
      return false;
    }
    if ( !isNumber( KEY_TRIGGER_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE ) )
    {
      return false;
    }

    return true;
  }

  private boolean isNonEmptyString( String aKey )
  {
    Object value = get( aKey );
    return value != null && ( value instanceof String ) && !"".equals( ( ( String )value ).trim() );
  }

  private boolean isBoolean( String aKey )
  {
    Object value = get( aKey );
    return value != null && ( value instanceof Boolean );
  }

  private boolean isNumber( String aKey, int aMin, int aMax )
  {
    Object value = get( aKey );
    if ( value == null || !( value instanceof Number ) )
    {
      return false;
    }

    int intValue = ( ( Number )value ).intValue();
    return ( intValue >= aMin ) && ( intValue <= aMax );
  }
}
