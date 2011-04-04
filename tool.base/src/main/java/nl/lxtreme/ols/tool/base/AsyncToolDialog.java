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


import nl.lxtreme.ols.tool.base.BaseAsyncTool.*;


/**
 * Provides an interface for asynchronous tool dialogs.
 * 
 * @see ToolDialog
 */
public interface AsyncToolDialog<RESULT_TYPE, WORKER extends BaseAsyncToolWorker<RESULT_TYPE>> extends ToolDialog
{
  // METHODS

  /**
   * Sets the tool worker factory to use for creating a new tool worker.
   * 
   * @param aToolWorkerFactory
   *          the tool worker factory to use to create tool workers, cannot be
   *          <code>null</code>.
   */
  public void setToolWorkerFactory( final ToolWorkerFactory<RESULT_TYPE, WORKER> aToolWorkerFactory );
}
