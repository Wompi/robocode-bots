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

public class Bilby extends AdvancedRobot
{
	double	bTurns;
	double	MAX_BULLET_POWER	= 3.0;

	public void run()
	{
		setTurnRadarRightRadians(Double.POSITIVE_INFINITY);
		System.out.format("[%d] INIT\n", getTime());
	}

	@Override
	public void onStatus(StatusEvent e)
	{
		System.out.format("[%d] STATUS\n", e.getTime());
	}

	public void onScannedRobot(ScannedRobotEvent e)
	{
		double absoluteBearing = e.getBearingRadians() + getHeadingRadians();
		double distance = e.getDistance();
		setTurnGunRightRadians(robocode.util.Utils.normalRelativeAngle(absoluteBearing - getGunHeadingRadians())); // head-on gun

		setTurnRadarRightRadians(-getRadarTurnRemainingRadians());   // / just a simple RadarLock

		if (getGunTurnRemaining() == 0)  											// it's usefull to wait with shoting until the gun is in position
		{
			distance -= 18;
			double diff = bTurns - getTime();
			double buffy = (20 - distance / (diff)) / 3;
			double bPower = Math.min(MAX_BULLET_POWER, Math.max(buffy, 0.1));

			if (bPower <= 0.1)
			{
				bTurns = 0;
			}

			System.out.format("bPower[%d] bPower=%3.2f bTurns=%3.2f diff=%3.2f buffy=%3.2f gunHeat=%3.2f\n", getTime(), bPower, bTurns, diff, buffy,
					getGunHeat() / getGunCoolingRate());
			if (setFireBullet(bPower) != null)
			{
				if (diff <= (0)) bTurns = getTime() + distance / Rules.getBulletSpeed(bPower);

				System.out.format("SHOT[%d] %3.2f bTurns=%3.2f dist=%3.2f\n", getTime(), bPower, bTurns, distance);
			}
		}

		// Test this against multiple sample.SittingDuck - then remove it and look what has changed :)
		clearAllEvents();
	}

	@Override
	public void onBulletHit(BulletHitEvent e)
	{
		System.out.format("HIT[%d] %3.2f\n", getTime(), e.getBullet().getPower());
	}

}
