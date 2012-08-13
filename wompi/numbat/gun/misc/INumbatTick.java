/*******************************************************************************
 * Copyright (c)  2012  Wompi 
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the ZLIB
 * which accompanies this distribution, and is available at
 * http://robowiki.net/wiki/ZLIB
 * 
 * Contributors:
 *     Wompi - initial API and implementation
 ******************************************************************************/
package wompi.numbat.gun.misc;

public interface INumbatTick
{
	public NumbatSingleHolder getMaxTick();

	public void addTick(double deltaHeading, double velocity);

	public boolean incrementCount(int id);
}
