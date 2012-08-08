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

import robocode.AdvancedRobot;
import robocode.Bullet;
import robocode.RobotStatus;
import wompi.numbat.target.ITargetManager;
import wompi.numbat.target.NumbatTarget;

public class NumbatMagicBulletPower extends ANumbatFire
{
	private boolean			isFire;

	private NumbatTarget	myTarget;
	private NumbatTarget	lastTarget;
	private double			lastBulletPower;

	@Override
	void setFire(RobotStatus status, ITargetManager targetMan)
	{
		isFire = false;
		hasFired = false;
		lastTarget = myTarget;
		lastBulletPower = bPower;
		myTarget = targetMan.getGunTarget();

		double dist = myTarget.getDistance(status);
		if (dist <= 100) bPower = 3.0;
		else if (dist <= 300) bPower = 1.99;
		else if (dist <= 600) bPower = 1.75;
		else bPower = 0.5;

		if (dist >= 100)
		{
			bPower = Math.min(bPower, myTarget.eEnergy / 3.0);
		}

		if (status.getGunTurnRemaining() == 0)
		{
			if (status.getEnergy() > bPower && lastTarget == myTarget)
			{
				isFire = true;
			}
		}
		else
		{
			// DebugGunProperties.debugMissedShootings();
		}
	}

	@Override
	public void excecute(AdvancedRobot myBot)
	{
		if (isFire)
		{
			Bullet bullet = myBot.setFireBullet(lastBulletPower);
			if (bullet != null)
			{
				if (myTarget != null) myTarget.registerBullet(bullet);
				// DebugGunProperties.debugGunHitRate(bullet);
				hasFired = true;
			}
		}
	}

	@Override
	String getName()
	{
		return "Magic Bullet Power";
	}

	@Override
	boolean checkActivateRule(RobotStatus status, ITargetManager targetMan)
	{
		return status.getOthers() > 0 && targetMan.getGunTarget() != null;
	}

}
