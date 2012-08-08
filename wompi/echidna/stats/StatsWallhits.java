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
package wompi.echidna.stats;

import robocode.HitWallEvent;

public class StatsWallhits
{
	private static double	lastVelocity;
	private static double	velocity;
	private static int		wallHits;
	private static double	wallDmg;

	public static void registerVelocity(double newVelocity)
	{
		lastVelocity = velocity;
		velocity = newVelocity;
	}

	public static void onHitWall(HitWallEvent event)
	{
		wallDmg += Math.max(0, Math.abs(lastVelocity) * 0.5 - 1.0);
		wallHits++;
		// System.out.format("WALL[%d][%d]: %3.2f %3.2f\n",event.getTime(),wallHits,wallDmg,lastVelocity);
	}

	public static void printWallStats()
	{
		System.out.format("WALL HITS: %d  %3.2f\n", wallHits, wallDmg);
	}
}

class WallHelper
{
	int[]	velocityCount	= new int[9];
	double	wallDmg;
	int		wallHits;
}
