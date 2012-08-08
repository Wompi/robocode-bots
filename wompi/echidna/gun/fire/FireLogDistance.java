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
import robocode.Bullet;
import robocode.Rules;
import wompi.echidna.target.ATarget;
import wompi.wallaby.WallabyPainter;

public class FireLogDistance extends AFire
{
	public FireLogDistance(AdvancedRobot robot)
	{
		super(robot);
	}

	@Override
	public void init()
	{}

	@Override
	public double getFirePower(ATarget fireTarget)
	{
		double bPower = Math.min(Rules.MAX_BULLET_POWER,
				Math.max(Rules.MIN_BULLET_POWER, Math.min(fireTarget.getEnergy() / 3, 600 / fireTarget.getBlindDistance())));   // Credit: Capulet 1.1
		if (myRobot.getOthers() == 1 && fireTarget.getEnergy() < myRobot.getEnergy()) bPower = 0.1;
		return bPower;
	}

	@Override
	public void doFire(ATarget fireTarget)
	{
		double bPower = getFirePower(fireTarget);
		if (myRobot.getGunTurnRemaining() == 0)
		{
			double eDiff = fireTarget.getLiveShotPower() - (fireTarget.getEnergy() + 0.1);
			if (eDiff < 0 || myRobot.getOthers() == 1)
			{
				if (bPower < myRobot.getEnergy())
				{
					Bullet shot = myRobot.setFireBullet(bPower);
					if (shot != null)
					{
						// debug
						fireTarget.onBulletStatsDebug(shot);
						addLiveShotPower(fireTarget, shot.getPower());
					}
				}
			}
			// else
			// {
			// System.out.format("[%d] hold back fire %3.2f \n", myRobot.getTime(),eDiff);
			// }
		}

	}

	@Override
	public void doPaint(ATarget fireTarget, Graphics2D g)
	{
		WallabyPainter.drawFirePower(g, getFirePower(fireTarget), myRobot);
	}
}
