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
import robocode.Rules;
import wompi.numbat.target.ITargetManager;
import wompi.numbat.target.NumbatTarget;

public class NumbatLogDistanceFire extends ANumbatFire
{
	private final static double	TARGET_DISTANCE	= 700.0;

	private boolean				isFire;

	private double				lastBulletPower;
	private NumbatTarget		myTarget;
	private NumbatTarget		myLastTarget;

	@Override
	public void setFire(RobotStatus status, ITargetManager targetMan)
	{
		isFire = false;
		hasFired = false;
		myLastTarget = myTarget;
		myTarget = targetMan.getGunTarget();

		lastBulletPower = bPower;
		bPower = Math.min(Rules.MAX_BULLET_POWER, Math.min(myTarget.eEnergy / 3.0, TARGET_DISTANCE / myTarget.getDistance(status)));

		if (status.getGunTurnRemaining() == 0)
		{
			// NOTE: this one don't need the scandiff rule, just because the weighted radar is now locking if near shooting and therefore the fire 
			// is always valid 
			if (status.getEnergy() > bPower && myLastTarget == myTarget)
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
				//if (myTarget != null) myTarget.registerBullet(bullet);
				// DebugGunProperties.debugGunHitRate(bullet);
				hasFired = true;
			}
		}
	}

	@Override
	String getName()
	{
		return "Log Distance Fire";
	}

	@Override
	boolean checkActivateRule(RobotStatus status, ITargetManager targetMan)
	{
		return status.getOthers() > 0 && targetMan.getGunTarget() != null;
	}
}
