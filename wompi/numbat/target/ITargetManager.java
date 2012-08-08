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
package wompi.numbat.target;

import java.util.Collection;

/**
 * @author Wompi
 *         TODO: get rid of the NumbatTarget and use a proper interface or abstract class
 */
public interface ITargetManager
{
	public NumbatTarget getGunTarget();

	public NumbatTarget getRadarTarget();

	public NumbatTarget getMoveTarget();

	public NumbatTarget getLastScanTarget();

	public int getCloseBots();

	public Collection<NumbatTarget> getAllTargets();
}
