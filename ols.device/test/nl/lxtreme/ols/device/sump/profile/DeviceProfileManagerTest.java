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
package nl.lxtreme.ols.device.sump.profile;

import static nl.lxtreme.ols.device.sump.SumpConstants.*;
import static nl.lxtreme.ols.device.sump.profile.Constants.*;
import static org.junit.Assert.*;

import java.util.*;

import nl.lxtreme.ols.device.sump.*;

import org.junit.*;
import org.osgi.service.cm.*;


/**
 * Test cases for {@link DeviceProfileManager}.
 */
public class DeviceProfileManagerTest
{
  // METHODS

  /**
   * Test method for {@link DeviceProfileManager#findProfile(String)}.
   */
  @Test
  public void testFindProfile() throws Exception
  {
    DeviceProfile profile = null;

    DeviceProfileManager manager = new DeviceProfileManager();
    manager.updated( "1", getMockedProperties( "SUMP", "\"Sump\"" ) );
    manager.updated( "2", getMockedProperties( "OLS", "\"*\", \"Open Logic Sniffer v1.01\"" ) );
    manager.updated( "3", getMockedProperties( "BP", "\"BPv3\"" ) );
    manager.updated( "4", getMockedProperties( "SHRIMP", "\"Shrimp1.0\"" ) );
    manager.updated( "5", getMockedProperties( "OLS-EXTRA", "\"(&(name=Open Logic Sniffer v1*)(fpgaVersion>=3.07))\"" ) );

    profile = manager.findProfile( createMockedMetadata( "Shrimp1.0" ) );
    assertNotNull( "Shrimp", profile );
    assertEquals( "SHRIMP", profile.getType() );

    profile = manager.findProfile( createMockedMetadata( "Open Logic Sniffer v1.01" ) );
    assertNotNull( "OLS", profile );
    assertEquals( "OLS", profile.getType() );

    profile = manager.findProfile( createMockedMetadata( "Sump" ) );
    assertNotNull( "Sump", profile );
    assertEquals( "SUMP", profile.getType() );

    profile = manager.findProfile( createMockedMetadata( "BPv3" ) );
    assertNotNull( "BP", profile );
    assertEquals( "BP", profile.getType() );

    profile = manager.findProfile( createMockedMetadata( "My Unnamed Device" ) );
    assertNotNull( "Wildcard", profile );
    assertEquals( "OLS", profile.getType() );

    profile = manager.findProfile( createMockedMetadata( "Open Logic Sniffer v1.01", KEY_FPGA_VERSION, "3.07" ) );
    assertNotNull( "Filter", profile );
    assertEquals( "OLS-EXTRA", profile.getType() );

    profile = manager.findProfile( createMockedMetadata( "Open Logic Sniffer v1.01", KEY_FPGA_VERSION, "3.06" ) );
    assertNotNull( "Filter", profile );
    assertEquals( "OLS", profile.getType() );
  }

  /**
   * Test method for {@link DeviceProfileManager#getProfile(String)}.
   */
  @Test
  public void testGetProfile() throws ConfigurationException
  {
    DeviceProfileManager manager = new DeviceProfileManager();
    manager.updated( "1", getMockedProperties( "foo", "bar" ) );

    assertNotNull( manager.getProfile( "foo" ) );
    assertNull( manager.getProfile( "FOO" ) );
  }

  private DeviceMetadata createMockedMetadata( String aName, Object... aAdditionalProps )
  {
    DeviceMetadata result = new DeviceMetadata();
    result.add( SumpConstants.KEY_DEVICE_NAME, aName );
    for ( int i = 0; i < aAdditionalProps.length; i += 2 )
    {
      Integer key = ( Integer )aAdditionalProps[i];
      Object value = aAdditionalProps[i + 1];
      result.add( key, value );
    }
    return result;
  }

  /**
   * @return
   */
  private Properties getMockedProperties( final String aType, final String aMetadataKeys )
  {
    Properties properties = new Properties();
    properties.put( DEVICE_CAPTURECLOCK, "INTERNAL" );
    properties.put( DEVICE_CAPTURESIZE_BOUND, "false" );
    properties.put( DEVICE_CAPTURESIZES, "1,2,3,4" );
    properties.put( DEVICE_CHANNEL_COUNT, "4" );
    properties.put( DEVICE_CHANNEL_GROUPS, "1" );
    properties.put( DEVICE_CHANNEL_NUMBERING_SCHEMES, "DEFAULT" );
    properties.put( DEVICE_CLOCKSPEED, "1000000" );
    properties.put( DEVICE_DIVIDER_CLOCKSPEED, "1000000" );
    properties.put( DEVICE_DESCRIPTION, "Mocked Device Profile" );
    properties.put( DEVICE_FEATURE_NOISEFILTER, "false" );
    properties.put( DEVICE_FEATURE_RLE, "false" );
    properties.put( DEVICE_FEATURE_TEST_MODE, "true" );
    properties.put( DEVICE_FEATURE_TRIGGERS, "false" );
    properties.put( DEVICE_FEATURE_COMBINED_READDELAY_COUNT, "true" );
    properties.put( DEVICE_INTERFACE, "SERIAL" );
    properties.put( DEVICE_METADATA_KEYS, aMetadataKeys );
    properties.put( DEVICE_OPEN_PORT_DELAY, "10" );
    properties.put( DEVICE_OPEN_PORT_DTR, "true" );
    properties.put( DEVICE_RECEIVE_TIMEOUT, "12" );
    properties.put( DEVICE_LAST_SAMPLE_FIRST, "false" );
    properties.put( DEVICE_SAMPLERATES, "5,6,7" );
    properties.put( DEVICE_SUPPORTS_DDR, "true" );
    properties.put( DEVICE_TRIGGER_COMPLEX, "true" );
    properties.put( DEVICE_TRIGGER_HP165XX, "false" );
    properties.put( DEVICE_TRIGGER_STAGES, "0" );
    properties.put( DEVICE_TYPE, aType );
    return properties;
  }
}
