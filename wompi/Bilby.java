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
package wompi;

import robocode.AdvancedRobot;
import robocode.BulletHitEvent;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.StatusEvent;
import robocode.util.Utils;

public class Bilby extends AdvancedRobot
{
	double	bTurns;
	double	MAX_BULLET_POWER	= 3.0;

	double	lastBulletPower;
	double	bPower;
	double	lastFireDistance;
	String	lastName;

	@Override
	public void run()
	{
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
		setTurnRadarRightRadians(Double.POSITIVE_INFINITY);
		System.out.format("[%d] INIT\n", getTime());
	}

	@Override
	public void onStatus(StatusEvent e)
	{
		//System.out.format("[%d] STATUS\n", e.getTime());
	}

	@Override
	public void onScannedRobot(ScannedRobotEvent e)
	{
		double absoluteBearing = e.getBearingRadians() + getHeadingRadians();

		setTurnRadarLeftRadians(getRadarTurnRemainingRadians()); // / just a simple RadarLock

		if (getTime() > 29 && getGunHeat() == 0)
		{
			setAhead(e.getDistance());
		}

		if (getGunTurnRemaining() == 0 && e.getName().equals(lastName)) // it's usefull to wait with shoting until the gun is in position
		{
			double lastSpeed = Rules.getBulletSpeed(lastBulletPower);
			double lastHeatTurns = Rules.getGunHeat(lastBulletPower) / getGunCoolingRate();
			bPower = Math.min(Rules.MAX_BULLET_POWER,
					(20.0 - (e.getDistance() / ((lastFireDistance / lastSpeed) - lastHeatTurns))) / 3.0);

			if (bPower < 0.1) bPower = Rules.MAX_BULLET_POWER;

			if (setFireBullet(bPower) != null)
			{
				System.out.format("[%04d] bPower=%3.5f dist=%3.5f \n", getTime(), bPower, e.getDistance());
				lastFireDistance = e.getDistance();
				lastBulletPower = bPower;
			}
		}
		setTurnGunRightRadians(Utils.normalRelativeAngle(absoluteBearing - getGunHeadingRadians())); // head-on gun

		setTurnRightRadians(Math.sin(e.getBearingRadians()));

		// Test this against multiple sample.SittingDuck - then remove it and look what has changed :)
		lastName = e.getName();
		clearAllEvents();
	}

	@Override
	public void onBulletHit(BulletHitEvent e)
	{
		System.out.format("HIT[%d] %3.2f\n", getTime(), e.getBullet().getPower());
	}

}
