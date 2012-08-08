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
package wompi.numbat.gun;

import robocode.RobotStatus;
import wompi.numbat.gun.fire.ANumbatFire;
import wompi.numbat.target.ITargetManager;

public class NumbatGunNone extends ANumbatGun
{

	@Override
	void setGun(RobotStatus status, ITargetManager targetMan, ANumbatFire fire)
	{}

	@Override
	String getName()
	{
		return "No Gun";
	}

	@Override
	boolean checkActivateRule(RobotStatus status, ITargetManager targetMan)
	{
		return true;
	}

}
