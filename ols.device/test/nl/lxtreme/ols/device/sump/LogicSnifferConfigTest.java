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
 * Copyright (C) 2010-2011 J.W. Janssen, www.lxtreme.nl
 */
package nl.lxtreme.ols.device.sump;


import static org.junit.Assert.*;
import nl.lxtreme.ols.device.sump.profile.*;

import org.junit.*;


/**
 * Test cases for {@link SumpConfig}.
 */
public class LogicSnifferConfigTest
{
  // VARIABLES

  private SumpConfigBuilder builder;
  private DeviceProfile profile;

  // METHODS

  /**
   * Sets up the test case.
   */
  @Before
  public void setUp() throws Exception
  {
    this.profile = VirtualLogicSnifferDevice.createDeviceProfile( "MOCK", "MockedDevice", true );

    this.builder = new SumpConfigBuilder( this.profile );
  }

  /**
   * Test method for {@link SumpConfig#getChannelCount()}.
   */
  @Test
  public void testGetChannelCount()
  {
    this.builder.setSampleRate( 1 );
    assertEquals( 32, this.builder.build().getChannelCount() );

    this.builder.setSampleRate( LogicSnifferAcquisitionTask.CLOCK );
    assertEquals( 32, this.builder.build().getChannelCount() );

    this.builder.setSampleRate( LogicSnifferAcquisitionTask.CLOCK + 1 );
    assertEquals( 16, this.builder.build().getChannelCount() );
  }

  /**
   * Test method for {@link SumpConfig#getDivider()}.
   */
  @Test
  public void testGetDivider()
  {
    this.builder.setSampleRate( Integer.MAX_VALUE );
    assertEquals( 0, this.builder.build().getDivider() );

    this.builder.setSampleRate( LogicSnifferAcquisitionTask.CLOCK );
    assertEquals( 0, this.builder.build().getDivider() );

    this.builder.setSampleRate( LogicSnifferAcquisitionTask.CLOCK >> 1 );
    assertEquals( 1, this.builder.build().getDivider() );

    this.builder.setSampleRate( LogicSnifferAcquisitionTask.CLOCK >> 2 );
    assertEquals( 3, this.builder.build().getDivider() );
  }

  /**
   * Test method for {@link SumpConfig#getEnabledGroupCount()}.
   */
  @Test
  public void testGetEnabledGroupCountWithDDR()
  {
    // With DDR...
    this.builder.setSampleRate( LogicSnifferAcquisitionTask.CLOCK + 1 );

    this.builder.setEnabledChannels( 0x000000FF );
    assertEquals( 1, this.builder.build().getEnabledGroupCount() );

    this.builder.setEnabledChannels( 0x0000FFFF );
    assertEquals( 2, this.builder.build().getEnabledGroupCount() );

    this.builder.setEnabledChannels( 0x00FFFFFF );
    assertEquals( 2, this.builder.build().getEnabledGroupCount() );

    this.builder.setEnabledChannels( 0xFFFFFFFF );
    assertEquals( 2, this.builder.build().getEnabledGroupCount() );
  }

  /**
   * Test method for {@link SumpConfig#getEnabledGroupCount()}.
   */
  @Test
  public void testGetEnabledGroupCountWithoutDDR()
  {
    // Without DDR...
    this.builder.setSampleRate( 1 );

    try
    {
      this.builder.setEnabledChannels( 0 );
      this.builder.build();
    }
    catch ( IllegalArgumentException exception )
    {
      // Ok; expected...
    }

    this.builder.setEnabledChannels( 0x000000FF );
    assertEquals( 1, this.builder.build().getEnabledGroupCount() );

    this.builder.setEnabledChannels( 0x0000FFFF );
    assertEquals( 2, this.builder.build().getEnabledGroupCount() );

    this.builder.setEnabledChannels( 0x00FFFFFF );
    assertEquals( 3, this.builder.build().getEnabledGroupCount() );

    this.builder.setEnabledChannels( 0xFFFFFFFF );
    assertEquals( 4, this.builder.build().getEnabledGroupCount() );
  }

  /**
   * Test method for {@link SumpConfig#getGroupCount()}.
   */
  @Test
  public void testGetGroupCountWithDDR()
  {
    // With DDR...
    this.builder.setSampleRate( LogicSnifferAcquisitionTask.CLOCK + 1 );

    assertEquals( 2, this.builder.build().getGroupCount() );
  }

  /**
   * Test method for {@link SumpConfig#getGroupCount()}.
   */
  @Test
  public void testGetGroupCountWithoutDDR()
  {
    // Without DDR...
    this.builder.setSampleRate( 1 );

    assertEquals( 4, this.builder.build().getGroupCount() );
  }

  /**
   * Test method for {@link SumpConfig#getRLEDataWidth()}.
   */
  @Test
  public void testGetRLEDataWidthWithDDR()
  {
    // With DDR...
    this.builder.setSampleRate( LogicSnifferAcquisitionTask.CLOCK + 1 );

    // this.builder.setEnabledChannels( 0 );
    // assertEquals( 0, this.builder.getRLEDataWidth() );
    //
    // this.builder.setEnabledChannels( 0x000000FF );
    // assertEquals( 8, this.builder.getRLEDataWidth() );
    //
    // this.builder.setEnabledChannels( 0x0000FFFF );
    // assertEquals( 16, this.builder.getRLEDataWidth() );
    //
    // this.builder.setEnabledChannels( 0x00FFFFFF );
    // assertEquals( 16, this.builder.getRLEDataWidth() );
    //
    // this.builder.setEnabledChannels( 0xFFFFFFFF );
    // assertEquals( 16, this.builder.getRLEDataWidth() );
  }

  /**
   * Test method for {@link SumpConfig#getRLEDataWidth()}.
   */
  @Test
  public void testGetRLEDataWidthWithoutDDR()
  {
    // Without DDR...
    this.builder.setSampleRate( 1 );

    // this.builder.setEnabledChannels( 0 );
    // assertEquals( 0, this.builder.getRLEDataWidth() );
    //
    // this.builder.setEnabledChannels( 0x000000FF );
    // assertEquals( 8, this.builder.getRLEDataWidth() );
    //
    // this.builder.setEnabledChannels( 0x0000FFFF );
    // assertEquals( 16, this.builder.getRLEDataWidth() );
    //
    // this.builder.setEnabledChannels( 0x00FFFFFF );
    // assertEquals( 24, this.builder.getRLEDataWidth() );
    //
    // this.builder.setEnabledChannels( 0xFFFFFFFF );
    // assertEquals( 32, this.builder.getRLEDataWidth() );
  }

  /**
   * Test method for {@link SumpConfig#getSampleCount()}.
   */
  @Test
  public void testGetSampleCountWithDDR()
  {
    // With DDR...
    this.builder.setSampleRate( LogicSnifferAcquisitionTask.CLOCK + 1 );

    this.builder.setSampleCount( 7 );
    assertEquals( 0, this.builder.build().getSampleCount() );

    this.builder.setSampleCount( 8 );
    assertEquals( 8, this.builder.build().getSampleCount() );

    this.builder.setSampleCount( 0xffff8 );
    assertEquals( 0xffff8, this.builder.build().getSampleCount() );

    this.builder.setSampleCount( 0xffff9 );
    assertEquals( 0xffff8, this.builder.build().getSampleCount() );

    this.builder.setSampleCount( Integer.MAX_VALUE );
    assertEquals( 0x7ffffff8, this.builder.build().getSampleCount() );
  }

  /**
   * Test method for {@link SumpConfig#getSampleCount()}.
   */
  @Test
  public void testGetSampleCountWithoutDDR()
  {
    // Without DDR...
    this.builder.setSampleRate( 1 );

    this.builder.setSampleCount( 0x03 );
    assertEquals( 0, this.builder.build().getSampleCount() );

    this.builder.setSampleCount( 0x04 );
    assertEquals( 0x04, this.builder.build().getSampleCount() );

    this.builder.setSampleCount( 0xffffc );
    assertEquals( 0xffffc, this.builder.build().getSampleCount() );

    this.builder.setSampleCount( 0xffffd );
    assertEquals( 0xffffc, this.builder.build().getSampleCount() );

    this.builder.setSampleCount( Integer.MAX_VALUE );
    assertEquals( 0x7ffffffc, this.builder.build().getSampleCount() );
  }

  /**
   * Test method for {@link SumpConfig#getSampleRate()}.
   */
  @Test
  public void testGetSampleRate()
  {
    this.builder.setSampleRate( 1 );
    assertEquals( 1, this.builder.build().getSampleRate() );

    this.builder.setSampleRate( 0xFFFFFFE );
    assertEquals( 0xFFFFFFE, this.builder.build().getSampleRate() );

    this.builder.setSampleRate( 0xFFFFFFF );
    assertEquals( 0xFFFFFFF, this.builder.build().getSampleRate() );

    this.builder.setSampleRate( Integer.MAX_VALUE );
    assertEquals( 0xFFFFFFF, this.builder.build().getSampleRate() );
  }

  /**
   * Test method for {@link SumpConfig#getStopCounter()}.
   */
  @Test
  public void testGetStopCounter()
  {
    this.builder.setSampleCount( 1000 );
    // with a trigger, the delay count equals to the read count times the
    // ratio...
    this.builder.setTriggerEnabled( true );

    this.builder.setRatio( 1.0 );
    assertEquals( 249, this.builder.build().getDelayCount() );

    this.builder.setRatio( 0.5 );
    assertEquals( 124, this.builder.build().getDelayCount() );

    this.builder.setRatio( 0.1 );
    assertEquals( 24, this.builder.build().getDelayCount() );

    this.builder.setRatio( 0.0 );
    assertEquals( 0xFFFFFFF, this.builder.build().getDelayCount() );

    // without a trigger, the delay count equals to the read count...
    this.builder.setTriggerEnabled( false );

    this.builder.setRatio( 1.0 );
    assertEquals( 249, this.builder.build().getDelayCount() );

    this.builder.setRatio( 0.5 );
    assertEquals( 249, this.builder.build().getDelayCount() );

    this.builder.setRatio( 0.1 );
    assertEquals( 249, this.builder.build().getDelayCount() );

    this.builder.setRatio( 0.0 );
    assertEquals( 249, this.builder.build().getDelayCount() );
  }

  /**
   * Test method for {@link SumpConfig#isDoubleDataRateEnabled()}.
   */
  @Test
  public void testIsDoubleDataRateEnabled()
  {
    this.builder.setSampleRate( 1 );
    assertFalse( this.builder.build().isDoubleDataRateEnabled() );

    this.builder.setSampleRate( LogicSnifferAcquisitionTask.CLOCK );
    assertFalse( this.builder.build().isDoubleDataRateEnabled() );

    this.builder.setSampleRate( LogicSnifferAcquisitionTask.CLOCK + 1 );
    assertTrue( this.builder.build().isDoubleDataRateEnabled() );
  }

  /**
   * Test method for {@link SumpConfig#isGroupEnabled(int)}.
   */
  @Test
  public void testIsGroupEnabledWithDDR()
  {
    // With DDR...
    this.builder.setSampleRate( LogicSnifferAcquisitionTask.CLOCK + 1 );

    // One channel group...
    this.builder.setEnabledChannels( 0x000000FF );
    assertTrue( this.builder.build().isGroupEnabled( 0 ) );
    assertFalse( this.builder.build().isGroupEnabled( 1 ) );
    assertFalse( this.builder.build().isGroupEnabled( 2 ) );
    assertFalse( this.builder.build().isGroupEnabled( 3 ) );

    this.builder.setEnabledChannels( 0x0000FF00 );
    assertFalse( this.builder.build().isGroupEnabled( 0 ) );
    assertTrue( this.builder.build().isGroupEnabled( 1 ) );
    assertFalse( this.builder.build().isGroupEnabled( 2 ) );
    assertFalse( this.builder.build().isGroupEnabled( 3 ) );

    // this.builder.setEnabledChannels( 0x00FF0000 );
    // assertFalse( this.builder.build().isGroupEnabled( 0 ) );
    // assertFalse( this.builder.build().isGroupEnabled( 1 ) );
    // assertTrue( this.builder.build().isGroupEnabled( 2 ) );
    // assertFalse( this.builder.build().isGroupEnabled( 3 ) );
    //
    // this.builder.setEnabledChannels( 0xFF000000 );
    // assertFalse( this.builder.build().isGroupEnabled( 0 ) );
    // assertFalse( this.builder.build().isGroupEnabled( 1 ) );
    // assertFalse( this.builder.build().isGroupEnabled( 2 ) );
    // assertTrue( this.builder.build().isGroupEnabled( 3 ) );

    // Two channel groups...
    this.builder.setEnabledChannels( 0x0000FFFF );
    assertTrue( this.builder.build().isGroupEnabled( 0 ) );
    assertTrue( this.builder.build().isGroupEnabled( 1 ) );
    assertFalse( this.builder.build().isGroupEnabled( 2 ) );
    assertFalse( this.builder.build().isGroupEnabled( 3 ) );

    // this.builder.setEnabledChannels( 0x00FFFF00 );
    // assertFalse( this.builder.build().isGroupEnabled( 0 ) );
    // assertTrue( this.builder.build().isGroupEnabled( 1 ) );
    // assertTrue( this.builder.build().isGroupEnabled( 2 ) );
    // assertFalse( this.builder.build().isGroupEnabled( 3 ) );
    //
    // this.builder.setEnabledChannels( 0xFFFF0000 );
    // assertFalse( this.builder.build().isGroupEnabled( 0 ) );
    // assertFalse( this.builder.build().isGroupEnabled( 1 ) );
    // assertTrue( this.builder.build().isGroupEnabled( 2 ) );
    // assertTrue( this.builder.build().isGroupEnabled( 3 ) );

    // this.builder.setEnabledChannels( 0xFF00FF00 );
    // assertFalse( this.builder.build().isGroupEnabled( 0 ) );
    // assertTrue( this.builder.build().isGroupEnabled( 1 ) );
    // assertFalse( this.builder.build().isGroupEnabled( 2 ) );
    // assertTrue( this.builder.build().isGroupEnabled( 3 ) );
    //
    // this.builder.setEnabledChannels( 0x00FF00FF );
    // assertTrue( this.builder.build().isGroupEnabled( 0 ) );
    // assertFalse( this.builder.build().isGroupEnabled( 1 ) );
    // assertTrue( this.builder.build().isGroupEnabled( 2 ) );
    // assertFalse( this.builder.build().isGroupEnabled( 3 ) );
    //
    // this.builder.setEnabledChannels( 0xFF0000FF );
    // assertTrue( this.builder.build().isGroupEnabled( 0 ) );
    // assertFalse( this.builder.build().isGroupEnabled( 1 ) );
    // assertFalse( this.builder.build().isGroupEnabled( 2 ) );
    // assertTrue( this.builder.build().isGroupEnabled( 3 ) );

    // Three channel groups...
    // this.builder.setEnabledChannels( 0x00FFFFFF );
    // assertTrue( this.builder.build().isGroupEnabled( 0 ) );
    // assertTrue( this.builder.build().isGroupEnabled( 1 ) );
    // assertTrue( this.builder.build().isGroupEnabled( 2 ) );
    // assertFalse( this.builder.build().isGroupEnabled( 3 ) );
    //
    // this.builder.setEnabledChannels( 0xFFFFFF00 );
    // assertFalse( this.builder.build().isGroupEnabled( 0 ) );
    // assertTrue( this.builder.build().isGroupEnabled( 1 ) );
    // assertTrue( this.builder.build().isGroupEnabled( 2 ) );
    // assertTrue( this.builder.build().isGroupEnabled( 3 ) );
    //
    // this.builder.setEnabledChannels( 0xFFFF00FF );
    // assertTrue( this.builder.build().isGroupEnabled( 0 ) );
    // assertFalse( this.builder.build().isGroupEnabled( 1 ) );
    // assertTrue( this.builder.build().isGroupEnabled( 2 ) );
    // assertTrue( this.builder.build().isGroupEnabled( 3 ) );
    //
    // this.builder.setEnabledChannels( 0xFF00FFFF );
    // assertTrue( this.builder.build().isGroupEnabled( 0 ) );
    // assertTrue( this.builder.build().isGroupEnabled( 1 ) );
    // assertFalse( this.builder.build().isGroupEnabled( 2 ) );
    // assertTrue( this.builder.build().isGroupEnabled( 3 ) );
    //
    // // Four channel groups...
    // this.builder.setEnabledChannels( 0xFFFFFFFF );
    // assertTrue( this.builder.build().isGroupEnabled( 0 ) );
    // assertTrue( this.builder.build().isGroupEnabled( 1 ) );
    // assertTrue( this.builder.build().isGroupEnabled( 2 ) );
    // assertTrue( this.builder.build().isGroupEnabled( 3 ) );
  }

  /**
   * Test method for {@link SumpConfig#isGroupEnabled(int)}.
   */
  @Test
  public void testIsGroupEnabledWithoutDDR()
  {
    // Without DDR...
    this.builder.setSampleRate( 1 );

    // One channel group...
    this.builder.setEnabledChannels( 0x000000FF );
    assertTrue( this.builder.build().isGroupEnabled( 0 ) );
    assertFalse( this.builder.build().isGroupEnabled( 1 ) );
    assertFalse( this.builder.build().isGroupEnabled( 2 ) );
    assertFalse( this.builder.build().isGroupEnabled( 3 ) );

    this.builder.setEnabledChannels( 0x0000FF00 );
    assertFalse( this.builder.build().isGroupEnabled( 0 ) );
    assertTrue( this.builder.build().isGroupEnabled( 1 ) );
    assertFalse( this.builder.build().isGroupEnabled( 2 ) );
    assertFalse( this.builder.build().isGroupEnabled( 3 ) );

    this.builder.setEnabledChannels( 0x00FF0000 );
    assertFalse( this.builder.build().isGroupEnabled( 0 ) );
    assertFalse( this.builder.build().isGroupEnabled( 1 ) );
    assertTrue( this.builder.build().isGroupEnabled( 2 ) );
    assertFalse( this.builder.build().isGroupEnabled( 3 ) );

    this.builder.setEnabledChannels( 0xFF000000 );
    assertFalse( this.builder.build().isGroupEnabled( 0 ) );
    assertFalse( this.builder.build().isGroupEnabled( 1 ) );
    assertFalse( this.builder.build().isGroupEnabled( 2 ) );
    assertTrue( this.builder.build().isGroupEnabled( 3 ) );

    // Two channel groups...
    this.builder.setEnabledChannels( 0x0000FFFF );
    assertTrue( this.builder.build().isGroupEnabled( 0 ) );
    assertTrue( this.builder.build().isGroupEnabled( 1 ) );
    assertFalse( this.builder.build().isGroupEnabled( 2 ) );
    assertFalse( this.builder.build().isGroupEnabled( 3 ) );

    this.builder.setEnabledChannels( 0x00FFFF00 );
    assertFalse( this.builder.build().isGroupEnabled( 0 ) );
    assertTrue( this.builder.build().isGroupEnabled( 1 ) );
    assertTrue( this.builder.build().isGroupEnabled( 2 ) );
    assertFalse( this.builder.build().isGroupEnabled( 3 ) );

    this.builder.setEnabledChannels( 0xFFFF0000 );
    assertFalse( this.builder.build().isGroupEnabled( 0 ) );
    assertFalse( this.builder.build().isGroupEnabled( 1 ) );
    assertTrue( this.builder.build().isGroupEnabled( 2 ) );
    assertTrue( this.builder.build().isGroupEnabled( 3 ) );

    this.builder.setEnabledChannels( 0xFF00FF00 );
    assertFalse( this.builder.build().isGroupEnabled( 0 ) );
    assertTrue( this.builder.build().isGroupEnabled( 1 ) );
    assertFalse( this.builder.build().isGroupEnabled( 2 ) );
    assertTrue( this.builder.build().isGroupEnabled( 3 ) );

    this.builder.setEnabledChannels( 0x00FF00FF );
    assertTrue( this.builder.build().isGroupEnabled( 0 ) );
    assertFalse( this.builder.build().isGroupEnabled( 1 ) );
    assertTrue( this.builder.build().isGroupEnabled( 2 ) );
    assertFalse( this.builder.build().isGroupEnabled( 3 ) );

    this.builder.setEnabledChannels( 0xFF0000FF );
    assertTrue( this.builder.build().isGroupEnabled( 0 ) );
    assertFalse( this.builder.build().isGroupEnabled( 1 ) );
    assertFalse( this.builder.build().isGroupEnabled( 2 ) );
    assertTrue( this.builder.build().isGroupEnabled( 3 ) );

    // Three channel groups...
    this.builder.setEnabledChannels( 0x00FFFFFF );
    assertTrue( this.builder.build().isGroupEnabled( 0 ) );
    assertTrue( this.builder.build().isGroupEnabled( 1 ) );
    assertTrue( this.builder.build().isGroupEnabled( 2 ) );
    assertFalse( this.builder.build().isGroupEnabled( 3 ) );

    this.builder.setEnabledChannels( 0xFFFFFF00 );
    assertFalse( this.builder.build().isGroupEnabled( 0 ) );
    assertTrue( this.builder.build().isGroupEnabled( 1 ) );
    assertTrue( this.builder.build().isGroupEnabled( 2 ) );
    assertTrue( this.builder.build().isGroupEnabled( 3 ) );

    this.builder.setEnabledChannels( 0xFFFF00FF );
    assertTrue( this.builder.build().isGroupEnabled( 0 ) );
    assertFalse( this.builder.build().isGroupEnabled( 1 ) );
    assertTrue( this.builder.build().isGroupEnabled( 2 ) );
    assertTrue( this.builder.build().isGroupEnabled( 3 ) );

    this.builder.setEnabledChannels( 0xFF00FFFF );
    assertTrue( this.builder.build().isGroupEnabled( 0 ) );
    assertTrue( this.builder.build().isGroupEnabled( 1 ) );
    assertFalse( this.builder.build().isGroupEnabled( 2 ) );
    assertTrue( this.builder.build().isGroupEnabled( 3 ) );

    // Four channel groups...
    this.builder.setEnabledChannels( 0xFFFFFFFF );
    assertTrue( this.builder.build().isGroupEnabled( 0 ) );
    assertTrue( this.builder.build().isGroupEnabled( 1 ) );
    assertTrue( this.builder.build().isGroupEnabled( 2 ) );
    assertTrue( this.builder.build().isGroupEnabled( 3 ) );
  }

  /**
   * Test method for {@link SumpConfig#setEnabledChannels(int)}.
   */
  @Test
  public void testSetEnabledChannels()
  {
    this.builder.setEnabledChannels( 0xFFFFFFFF );
    assertEquals( 0xFFFFFFFF, this.builder.build().getEnabledChannelMask() );

    this.builder.setEnabledChannels( -1 );
    assertEquals( 0xFFFFFFFF, this.builder.build().getEnabledChannelMask() );
  }

  /**
   * Test method for {@link SumpConfig#isFilterAvailable()}.
   */
  @Test
  public void testSetFilterEnabledWithDDR()
  {
    // With DDR...
    this.builder.setSampleRate( LogicSnifferAcquisitionTask.CLOCK + 1 );
    this.builder.setFilterEnabled( true );

    assertFalse( ( this.builder.build().getFlags() & SumpConstants.FLAG_FILTER ) != 0 );
  }

  /**
   * Test method for {@link SumpConfig#isFilterAvailable()}.
   */
  @Test
  public void testSetFilterEnabledWithoutDDR()
  {
    // Without DDR...
    this.builder.setSampleRate( 1 );
    this.builder.setFilterEnabled( true );

    assertTrue( ( this.builder.build().getFlags() & SumpConstants.FLAG_FILTER ) != 0 );
  }

  /**
   * Test method for {@link SumpConfig#setRatio(double)}.
   */
  @Test( expected = IllegalArgumentException.class )
  public void testSetNegativeRatioFail()
  {
    this.builder.setRatio( -0.1 ); // should fail!
  }

  /**
   * Test method for {@link SumpConfig#setRatio(double)}.
   */
  @Test( expected = IllegalArgumentException.class )
  public void testSetTooGreatRatioFail()
  {
    this.builder.setRatio( 1.1 ); // should fail!
  }

  /**
   * Test method for {@link SumpConfig#setSampleCount(int)}.
   */
  @Test( expected = IllegalArgumentException.class )
  public void testSetZeroSampleCountFail()
  {
    this.builder.setSampleCount( 0 ); // should fail!
  }

  /**
   * Test method for {@link SumpConfig#setSampleRate(int)}.
   */
  @Test( expected = IllegalArgumentException.class )
  public void testSetZeroSampleRateFail()
  {
    this.builder.setSampleRate( 0 ); // should fail!
  }

  /**
   * Test method for {@link SumpConfig#setSampleRate(int)}.
   */
  @Test( expected = IllegalArgumentException.class )
  public void testSetZeroSampleRateFails()
  {
    this.builder.setSampleRate( 0 ); // should fail!
  }

}
