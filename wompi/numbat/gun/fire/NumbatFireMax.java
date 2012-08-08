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

public class NumbatFireMax extends ANumbatFire
{
	private boolean			isFire;

	private NumbatTarget	myTarget;
	private NumbatTarget	myLastTarget;

	@Override
	void setFire(RobotStatus status, ITargetManager targetMan)
	{
		isFire = false;
		hasFired = false;
		myLastTarget = myTarget;
		myTarget = targetMan.getGunTarget();

		// double liveFireDamage = myTarget.getLiveFireDamage();
		// double dmgDiff = myTarget.eEnergy - liveFireDamage;
		double dmgDiff = myTarget.eEnergy;

		// TODO: this might not be so clever, because if you miss the next bullet will take ages to be shoot at the target
		// but in the contrary it is a high pattern target and it should reach the target anyway
		// another bad thing is BulletHitBullet events can be increase with this
		bPower = Math.min(dmgDiff / 3.0, Rules.MAX_BULLET_POWER);  // full power on simple pattern targets

		if (status.getGunTurnRemaining() == 0)
		{
			if (status.getEnergy() > bPower && myLastTarget == myTarget)
			{
				if (dmgDiff + 0.1 >= 0)
				{
					isFire = true;
				}
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
			Bullet bullet = myBot.setFireBullet(bPower);
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
		return "Maximum Bullet Fire";
	}

	@Override
	boolean checkActivateRule(RobotStatus status, ITargetManager targetMan)
	{
		NumbatTarget target = targetMan.getGunTarget();
		boolean r1 = status.getOthers() > 0;
		boolean r2 = target != null;
		boolean r3 = false;
		if (r2)
		{
			r3 = target.getAveragePatternLength() >= NumbatTarget.MAX_PATTERN_BORDER;
		}
		return r1 && r2 && r3;
	}

}
