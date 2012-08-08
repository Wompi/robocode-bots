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
import java.util.ArrayList;

import robocode.AdvancedRobot;
import robocode.Bullet;
import robocode.Rules;
import wompi.echidna.misc.painter.PaintBulletLine;
import wompi.echidna.target.ATarget;
import wompi.wallaby.PaintHelper;
import wompi.wallaby.WallabyPainter;

public class FireMinShot extends AFire
{
	ArrayList<Bullet>	myBullets	= new ArrayList<Bullet>();

	public FireMinShot(AdvancedRobot robot)
	{
		super(robot);
	}

	@Override
	public void init()
	{}

	@Override
	public double getFirePower(ATarget fireTarget)
	{
		return Rules.MIN_BULLET_POWER;
	}

	@Override
	public void doFire(ATarget fireTarget)
	{
		double bPower = getFirePower(fireTarget);
		if (myRobot.getGunTurnRemaining() == 0)  // TODO: make this an object so it can be loaded with other fire rules
		{
			if ((fireTarget.getLiveShotPower() * Rules.getBulletDamage(bPower)) < fireTarget.getEnergy())
			{
				Bullet bullet = myRobot.setFireBullet(bPower);
				if (bullet != null)
				{
					// debug
					myBullets.add(bullet);
					fireTarget.onBulletStatsDebug(bullet);
					addLiveShotPower(fireTarget, bPower);
				}
			}
		}
	}

	@Override
	public void doPaint(ATarget fireTarget, Graphics2D g)
	{
		WallabyPainter.drawFirePower(g, getFirePower(fireTarget), myRobot);

		for (Bullet bullet : myBullets)
		{
			if (bullet.isActive())
			{
				PaintBulletLine.onPaint(g, bullet, PaintHelper.yellowTrans);
			}
		}
	}
}
