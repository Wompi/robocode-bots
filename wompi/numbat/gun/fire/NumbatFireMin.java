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
import wompi.echidna.misc.utils.BattleField;
import wompi.numbat.target.ITargetManager;
import wompi.numbat.target.NumbatTarget;

public class NumbatFireMin extends ANumbatFire
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

		bPower = 0.1;

		if (status.getGunTurnRemaining() == 0)
		{
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
		return "Minimum Bullet Fire";
	}

	@Override
	boolean checkActivateRule(RobotStatus status, ITargetManager targetMan)
	{
		NumbatTarget target = targetMan.getGunTarget();
		boolean r1 = status.getOthers() == 1;
		boolean r2 = target != null;
		boolean r3 = BattleField.getBattleState() != BattleField.EBattleState.SINGLE;
		boolean r4 = false;
		if (r2)
		{
			r4 = target.eEnergy < status.getEnergy();
		}
		return r1 && r2 && r3 && r4;
	}

}
