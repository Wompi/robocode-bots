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
import robocode.util.Utils;
import wompi.numbat.gun.fire.ANumbatFire;
import wompi.numbat.target.ITargetManager;

public class NumbatGunHeadOn extends ANumbatGun
{

	@Override
	void setGun(RobotStatus status, ITargetManager targetMan, ANumbatFire fire)
	{
		gTurn = Utils.normalRelativeAngle(targetMan.getGunTarget().getAbsoluteBearing(status) - status.getGunHeadingRadians());
	}

	@Override
	String getName()
	{
		return "Head On Target";
	}

	@Override
	boolean checkActivateRule(RobotStatus status, ITargetManager targetMan)
	{
		boolean r1 = status.getOthers() > 0;
		boolean r2 = targetMan.getGunTarget() != null;
		return r1 && r2;
	}

}
