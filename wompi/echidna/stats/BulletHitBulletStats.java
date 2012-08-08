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

import robocode.BulletHitBulletEvent;

public class BulletHitBulletStats
{
	private static int		countBHB;
	private static int		countNOBHB;
	private static double	wastedEnergy;
	private static double	destroyedEnergy;

	public static void onBulletHitBullet(BulletHitBulletEvent e)
	{
		if (e.getHitBullet().getName().equals(e.getBullet().getName()))  // count only our bullets
		{
			countBHB++;
			wastedEnergy += e.getBullet().getPower();
			// System.out.format("[%d] power=%3.2f\n", e.getTime(),e.getBullet().getPower());
		}
		else
		{
			countNOBHB++;
			destroyedEnergy += e.getHitBullet().getPower();
		}
	}

	public static void onPrint()
	{
		System.out.format("Bullet hit bullets: self=[%d,%3.2f] other=[%d,%3.2f]\n", countBHB, wastedEnergy, countNOBHB, destroyedEnergy);
	}
}
