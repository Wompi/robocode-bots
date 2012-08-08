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
import wompi.numbat.debug.DebugGunProperties;
import wompi.numbat.target.ITargetManager;
import wompi.numbat.target.NumbatTarget;

public class NumbatFirePatternChallenge extends ANumbatFire
{
	private boolean			isFire;
	private NumbatTarget	myTarget;

	@Override
	void setFire(RobotStatus status, ITargetManager targetMan)
	{
		isFire = false;
		hasFired = false;
		myTarget = targetMan.getGunTarget();

		// in this challenge are only 0.5 bullets allowed
		bPower = 0.5;

		if (status.getGunTurnRemaining() == 0)
		{
			isFire = true;
		}
		else
		{
			DebugGunProperties.debugMissedShootings();
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
				DebugGunProperties.debugGunHitRate(bullet);
				hasFired = true;
			}
		}
	}

	@Override
	String getName()
	{
		return "Pattern Challenge";
	}

	@Override
	boolean checkActivateRule(RobotStatus status, ITargetManager targetMan)
	{
		boolean r1 = status.getOthers() == 1;
		boolean r2 = BattleField.getBattleState() == BattleField.EBattleState.MELEE;
		boolean r3 = targetMan.getGunTarget() != null && targetMan.getGunTarget().eName.startsWith("challenge.PatternBot");
		boolean r4 = status.getNumRounds() == 100;
		return r1 && r2 && r3 && r4;
	}

}
