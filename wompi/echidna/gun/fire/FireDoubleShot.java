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

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;

import robocode.AdvancedRobot;
import robocode.Bullet;
import robocode.Rules;
import wompi.echidna.misc.painter.PaintBulletShield;
import wompi.echidna.target.ATarget;
import wompi.wallaby.WallabyPainter;

public class FireDoubleShot extends AFire
{
	// enemy values
	ATarget				myLastTarget;

	// robot values
	double				lastFireDistance;
	double				lastBulletPower;

	// debug
	ArrayList<Bullet>	myBullets	= new ArrayList<Bullet>();

	public FireDoubleShot(AdvancedRobot robot)
	{
		super(robot);
	}

	@Override
	public void init()
	{
		lastBulletPower = 0.1;
		myRobot.setAdjustGunForRobotTurn(true);
	}

	@Override
	public double getFirePower(ATarget fireTarget)
	{
		double lastSpeed = Rules.getBulletSpeed(lastBulletPower);
		double lastHeatTurns = Rules.getGunHeat(lastBulletPower) / myRobot.getGunCoolingRate();
		return Math.min(Rules.MAX_BULLET_POWER, (20.0 - (fireTarget.getDistance() / ((lastFireDistance / lastSpeed) - lastHeatTurns))) / 3.0);
	}

	@Override
	public void doFire(ATarget fireTarget)
	{
		if (myRobot.getGunTurnRemaining() == 0 && fireTarget.isAlive())
		{
			double bPower = getFirePower(fireTarget);
			if (bPower < 0.1 || myLastTarget != fireTarget) bPower = 3.0;

			if ((fireTarget.getLiveShotPower() * Rules.getBulletDamage(bPower)) < fireTarget.getEnergy())
			{
				Bullet shot = myRobot.setFireBullet(bPower);
				if (shot != null)
				{
					fireTarget.onBulletStatsDebug(shot);
					myBullets.add(shot);

					lastFireDistance = fireTarget.getDistance();
					lastBulletPower = bPower;
					addLiveShotPower(fireTarget, bPower);
				}
			}
		}
		myLastTarget = fireTarget;
	}

	@Override
	public void doPaint(ATarget fireTarget, Graphics2D g)
	{
		WallabyPainter.drawFirePower(g, getFirePower(fireTarget), myRobot);

		for (Bullet bullet : myBullets)
		{
			if (bullet.isActive())
			{
				PaintBulletShield.paintShield(g, fireTarget.getAbsX(), fireTarget.getAbsY(), bullet, Color.GRAY);
			}
		}
	}
}
