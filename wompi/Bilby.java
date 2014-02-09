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

import java.awt.Color;

import robocode.AdvancedRobot;
import robocode.BulletHitBulletEvent;
import robocode.BulletHitEvent;
import robocode.HitWallEvent;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.StatusEvent;

public class Bilby extends AdvancedRobot
{
	double				bTurns;
	double				MAX_BULLET_POWER	= 3.0;

	double				lastBulletPower;
	double				bPower;
	double				lastFireDistance;
	String				lastName;
	double				dir					= 1;

	static final double	GUN_FACTOR			= 30;
	static final int	AIM_START			= 10;
	static final double	AIM_FACTOR			= 1.008;
	static final int	FIRE_FACTOR			= 7;

	@Override
	public void run()
	{
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
		setTurnRadarRightRadians(Double.POSITIVE_INFINITY);
		System.out.format("[%d] INIT\n", getTime());
		setAllColors(Color.green);
	}

	@Override
	public void onStatus(StatusEvent e)
	{
		//System.out.format("[%d] STATUS\n", e.getTime());
	}

	@Override
	public void onScannedRobot(ScannedRobotEvent e)
	{
		double aBear = e.getBearingRadians() + getHeadingRadians();

		setTurnRadarLeftRadians(getRadarTurnRemainingRadians()); // / just a simple RadarLock

		setAhead(dir * 10000000);

		if (getGunTurnRemaining() == 0 && e.getName().equals(lastName)) // it's usefull to wait with shoting until the gun is in position
		{
			double lastSpeed = Rules.getBulletSpeed(lastBulletPower);
			double lastHeatTurns = Rules.getGunHeat(lastBulletPower) / getGunCoolingRate();
			bPower = Math.min(Rules.MAX_BULLET_POWER,
					(20.0 - (e.getDistance() / ((lastFireDistance / lastSpeed) - lastHeatTurns))) / 3.0);

			if (bPower < 0.1) bPower = Rules.MAX_BULLET_POWER;

			if (setFireBullet(bPower) != null)
			{
				System.out.format("[%04d] bPower=%3.5f dist=%3.5f lastDist=%3.5f \n", getTime(), bPower,
						e.getDistance(), lastFireDistance);
				lastFireDistance = e.getDistance();
				lastBulletPower = bPower;
			}
		}
		//setTurnGunRightRadians(Utils.normalRelativeAngle(absoluteBearing - getGunHeadingRadians())); // head-on gun
		//@formatter:off
//		setTurnGunRightRadians(
//				Utils.normalRelativeAngle(
//						 aBear 
//						 + Math.asin(
//								 e.getVelocity() 
//								 / Rules.getBulletSpeed(bPower) 
//								 * Math.sin(
//										 e.getHeadingRadians() 
//								         - aBear
//								 )
//						 )
//						 - getGunHeadingRadians()
//				)
//		);
					setTurnGunRightRadians(robocode.util.Utils.normalRelativeAngle(aBear
				- getGunHeadingRadians()
				+ (e.getVelocity() / (AIM_START + Math.pow(AIM_FACTOR, e.getDistance())))
				* Math.sin(e.getHeadingRadians() - aBear)));
        //@formatter:on

		setTurnRightRadians(Math.cos(e.getBearingRadians()));

		// Test this against multiple sample.SittingDuck - then remove it and look what has changed :)
		lastName = e.getName();
		clearAllEvents();
	}

	@Override
	public void onHitWall(HitWallEvent event)
	{
		dir = -dir;
	}

	@Override
	public void onBulletHit(BulletHitEvent e)
	{
		System.out.format("HIT[%d] %3.2f\n", getTime(), e.getBullet().getPower());
	}

	@Override
	public void onBulletHitBullet(BulletHitBulletEvent e)
	{
		System.out.format("BULLET HIT[%d] %3.2f\n", getTime(), e.getBullet().getPower());
	}

}
