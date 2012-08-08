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
package wompi.numbat.debug.paint;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import robocode.HitByBulletEvent;
import wompi.wallaby.PaintHelper;

public class PaintHitCloud
{
	private static ArrayList<HitByBulletEvent>	hitStats	= new ArrayList<HitByBulletEvent>();
	private static long							maxTime;

	public static void registerHit(HitByBulletEvent e)
	{
		if (hitStats.size() > 1)
		{
			long diff = e.getTime() - hitStats.get(hitStats.size() - 1).getTime();
			if (diff < 0) diff = e.getTime();

			maxTime = Math.max(maxTime, diff);
		}

		hitStats.add(e);
	}

	public static void onPaint(Graphics2D g)
	{

		for (int i = 0; i < hitStats.size(); i++)
		{
			long diff = hitStats.get(i).getTime();
			if (i > 0)
			{
				diff = diff - hitStats.get(i - 1).getTime();
				if (diff < 0) diff = hitStats.get(i).getTime();
			}
			double power = hitStats.get(i).getBullet().getPower();

			PaintHelper.drawPoint(new Point2D.Double(diff, power * 100), Color.RED, g, 5);
		}

	}
}
