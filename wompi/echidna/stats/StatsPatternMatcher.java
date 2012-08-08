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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import robocode.Bullet;
import wompi.echidna.misc.SimpleAverage;

public class StatsPatternMatcher
{
	static HashMap<String, HelperPatternMatcher>	statsMap	= new HashMap<String, HelperPatternMatcher>();

	public static void registerStartPattern(String name, int startPatterLength, long time)
	{
		HelperPatternMatcher helper = statsMap.get(name);
		if (helper == null)
		{
			statsMap.put(name, helper = new HelperPatternMatcher());
		}

		helper.minLen = Math.min(startPatterLength, helper.minLen);
		helper.maxLen = Math.max(startPatterLength, helper.maxLen);
		helper.avgLen.avg(startPatterLength, time);
		// System.out.format("[%d] max=%d\n", time,helper.maxLen);
	}

	public static void registerBullet(String name, Bullet newBullet, long time, int startPatterLength)
	{
		HelperPatternMatcher helper = statsMap.get(name);
		if (helper == null)
		{
			statsMap.put(name, helper = new HelperPatternMatcher());
		}
		HelperFiredBullets bHelper = new HelperFiredBullets();
		bHelper.myBullet = newBullet;
		bHelper.startLen = startPatterLength;
		helper.firedBullets.add(bHelper);
	}

	public static int getAvgLen(String name)
	{
		HelperPatternMatcher helper = statsMap.get(name);
		if (helper == null) return 0;
		return (int) (helper.avgLen.avg());
	}

	public static void printStats()
	{
		System.out.format("%10s %10s %10s\n", "MIN", "AVG", "MAX");
		for (Entry<String, HelperPatternMatcher> entry : statsMap.entrySet())
		{
			double ratio = entry.getValue().getHitRatio();
			System.out.format("%10d %10d %10d ---- %10d %10d %10d -- ratio=%3.2f (%d) MISS:  %10d %10d %10d  %s\n", entry.getValue().minLen,
					(int) (entry.getValue().avgLen.avg()), entry.getValue().maxLen, entry.getValue().minFiredLen,
					(int) (entry.getValue().avgFiredLen.avg()), entry.getValue().maxFiredLen, ratio, entry.getValue().firedBullets.size(),
					entry.getValue().minMisFiredLen, (int) (entry.getValue().avgMisFiredLen.avg()), entry.getValue().maxMisFiredLen, entry.getKey());
		}
	}
}

class HelperPatternMatcher
{
	ArrayList<HelperFiredBullets>	firedBullets	= new ArrayList<HelperFiredBullets>();

	int								minLen			= 300;
	int								maxLen			= 0;
	public SimpleAverage			avgLen			= new SimpleAverage(100000, "start pattern length");

	// hit
	int								minFiredLen		= 300;
	int								maxFiredLen		= 0;
	public SimpleAverage			avgFiredLen		= new SimpleAverage(100000, "start pattern length");

	// miss
	int								minMisFiredLen	= 300;
	int								maxMisFiredLen	= 0;
	public SimpleAverage			avgMisFiredLen	= new SimpleAverage(100000, "start pattern length");

	public double getHitRatio()
	{
		double result = 0;

		for (HelperFiredBullets bullet : firedBullets)
		{
			if (bullet.myBullet.getVictim() != null)
			{
				result++;
				minFiredLen = Math.min(minFiredLen, bullet.startLen);
				maxFiredLen = Math.max(maxFiredLen, bullet.startLen);
				avgFiredLen.avg(bullet.startLen, 0);
			}
			else
			{
				minMisFiredLen = Math.min(minMisFiredLen, bullet.startLen);
				maxMisFiredLen = Math.max(maxMisFiredLen, bullet.startLen);
				avgMisFiredLen.avg(bullet.startLen, 0);

			}
		}
		return result / firedBullets.size();
	}
}

class HelperFiredBullets
{
	int		startLen;
	Bullet	myBullet;
}
