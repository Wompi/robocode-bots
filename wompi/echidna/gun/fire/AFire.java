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
package wompi.echidna.gun.fire;

import java.awt.Graphics2D;

import robocode.AdvancedRobot;
import robocode.Condition;
import robocode.Rules;
import wompi.echidna.target.ATarget;

public abstract class AFire
{
	AdvancedRobot	myRobot;

	double			eLastFireDistance;

	static int		id;

	public AFire(AdvancedRobot robot)
	{
		myRobot = robot;
	}

	public abstract void init();

	public abstract double getFirePower(ATarget fireTarget);

	public abstract void doFire(ATarget fireTarget);

	public void doPaint(ATarget fireTarget, Graphics2D g)
	{};

	protected final void addLiveShotPower(ATarget fireTarget, double bulletPower)
	{
		double dist = eLastFireDistance;
		if (dist == 0)
		{
			dist = fireTarget.getDistance();
		}
		myRobot.addCustomEvent(new LiveShotCondition(fireTarget, bulletPower, dist));
	}

	private class LiveShotCondition extends Condition
	{
		int		cID;

		ATarget	conditionTarget;
		double	conditionBulletDamage;
		int		bLiveTime;

		public LiveShotCondition(ATarget bulletTarget, double bulletPower, double bulletDistance)
		{
			conditionTarget = bulletTarget;
			conditionBulletDamage = Rules.getBulletDamage(bulletPower);
			bLiveTime = (int) (bulletDistance / Rules.getBulletSpeed(bulletPower));  // adjust
			conditionTarget.addLiveShotPower(conditionBulletDamage);
			cID = id++;
			// System.out.format("[%d][%d]  dmg=%3.2f time=%d shotCondition=%s\n",
			// myRobot.getTime(),cID,conditionBulletDamage,bLiveTime,conditionTarget.getName());
		}

		@Override
		public boolean test()
		{
			if (bLiveTime-- <= 0)
			{
				conditionTarget.removeLiveShotPower(conditionBulletDamage);
				// System.out.format("[%d] %d removed shot=%3.2f energy=%3.2f \n",
				// myRobot.getTime(),cID,conditionTarget.getLiveShotPower(),conditionTarget.getEnergy());
				myRobot.removeCustomEvent(this);
				return true;
			}
			// System.out.format("[%d] %d alive %d - %3.2f energy=%3.2f\n",
			// myRobot.getTime(),cID,bLiveTime,conditionTarget.getLiveShotPower(),conditionTarget.getEnergy());
			return false;
		}

	}
}
