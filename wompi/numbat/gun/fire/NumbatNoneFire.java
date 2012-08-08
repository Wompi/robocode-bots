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
package wompi.numbat.gun.fire;

import robocode.RobotStatus;
import wompi.numbat.target.ITargetManager;

public class NumbatNoneFire extends ANumbatFire
{

	@Override
	void setFire(RobotStatus status, ITargetManager targetMan)
	{}

	@Override
	String getName()
	{
		return "No Fire";
	}

	@Override
	boolean checkActivateRule(RobotStatus status, ITargetManager targetMan)
	{
		return status.getOthers() == 0 || targetMan.getGunTarget() == null;
	}

}
