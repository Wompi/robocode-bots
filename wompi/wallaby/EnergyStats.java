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
package wompi.wallaby;

import robocode.BulletHitEvent;
import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;

public class EnergyStats
{
	private static double	bulletBackEnergy;
	private static double	bulletDmgEnergy;
	private static double	robotHitEnergy;
	private static double	shootEnergy;

	public static void onBulletHit(BulletHitEvent event)
	{
		bulletBackEnergy += 3.0 * event.getBullet().getPower();
	}

	public static void onHitByBullet(HitByBulletEvent event)
	{
		bulletDmgEnergy -= 4 * event.getPower() + 2 * Math.max(event.getPower() - 1, 0);
	}

	public static void onHitRobot(HitRobotEvent event)
	{
		robotHitEnergy -= 0.6;
	}

	public static void onHitWall(HitWallEvent event)
	{

	}

	public static void onFire(double power)
	{
		shootEnergy -= power;
	}

	public static void printStats()
	{
		System.out.format("Energy: %3.2f\n", bulletBackEnergy + bulletDmgEnergy + robotHitEnergy + shootEnergy);
		System.out.format("back:     %3.2f \n", bulletBackEnergy);
		System.out.format("dmg:      %3.2f \n", bulletDmgEnergy);
		System.out.format("hitRobot: %3.2f \n", robotHitEnergy);
		System.out.format("shoot:    %3.2f \n", shootEnergy);
	}

}
