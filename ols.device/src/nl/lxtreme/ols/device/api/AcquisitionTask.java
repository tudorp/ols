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
package nl.lxtreme.ols.device.api;


import nl.lxtreme.ols.common.acquisition.*;
import nl.lxtreme.ols.task.execution.*;


/**
 * Denotes an acquisition task that we can talk to by calling {@link #call()} on
 * it. The implementation of that method should do all necessary tasks to
 * acquire data from the device and return that data in the form of an
 * {@link AcquisitionData} object.
 */
@Deprecated
public interface AcquisitionTask extends Task<AcquisitionData>
{
  // No additional methods
}
