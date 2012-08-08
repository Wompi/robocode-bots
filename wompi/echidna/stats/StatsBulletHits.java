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
import java.util.HashSet;

import robocode.Bullet;
import wompi.echidna.misc.SimpleAverage;

public class StatsBulletHits
{
	private static BulletHelper[]	statsField	= new BulletHelper[10];

	public static void registerBullet(Bullet bullet, int others, double distance, long time, String targetName)
	{
		BulletHelper helper = getBulletHelper(others);
		helper.maxPower = Math.max(helper.maxPower, bullet.getPower());
		helper.minPower = Math.min(helper.minPower, bullet.getPower());
		helper.avgPower.avg(bullet.getPower(), time);

		helper.maxDist = Math.max(helper.maxDist, distance);
		helper.minDist = Math.min(helper.minDist, distance);
		helper.avgDist.avg(distance, time);

		helper.stateBullets.add(bullet);
		helper.targetNames.add(targetName);
	}

	public static void printStats()
	{
		System.out.format("%8s %2s %2s %4s %6s %6s %2s %4s %4s %4s %6s %6s %6s\n", "ALL", "HIT", "MISS", "RATE", "GAIN", "DRAIN", "TARGETS",
				"MAX(P)", "MIN(P)", "AVG(P)", "MAX(D)", "MIN(D)", "AVG(D)");
		int count = 1;
		for (BulletHelper help : statsField)
		{
			if (help != null)
			{
				System.out.format("[%02d] %s\n", count, help.toString());
			}
			else System.out.format("[%02d] no data\n", count);
			count++;
		}
		System.out.format("\n");
	}

	private static BulletHelper getBulletHelper(int others)
	{
		BulletHelper buffy = statsField[others];
		if (buffy == null)
		{
			buffy = statsField[others] = new BulletHelper();
		}
		return buffy;
	}
}

class BulletHelper
{
	ArrayList<Bullet>	stateBullets	= new ArrayList<Bullet>();
	HashSet<String>		targetNames		= new HashSet<String>();

	private int			hits;
	private int			missed;
	private double		energyDrain;
	private double		energyGain;

	double				maxPower		= Double.MIN_VALUE;
	double				minPower		= Double.MAX_VALUE;
	SimpleAverage		avgPower		= new SimpleAverage(100, "avg power");

	double				maxDist			= Double.MIN_VALUE;
	double				minDist			= Double.MAX_VALUE;
	SimpleAverage		avgDist			= new SimpleAverage(100, "avg bullet dist");

	public String toString()
	{
		caclulateStats();
		double hitRate = (double) (hits) / (double) (stateBullets.size());
		return String.format("%03d   %03d   %03d   %01.2f   %05.1f   %05.1f   %01d   %01.1f   %01.1f   %01.1f   %06.1f   %06.1f   %06.1f",
				stateBullets.size(), hits, missed, hitRate, energyGain, energyDrain, targetNames.size(), maxPower, minPower, avgPower.avg(), maxDist,
				minDist, avgDist.avg());
	}

	private void caclulateStats()
	{
		hits = 0;
		missed = 0;
		energyDrain = 0;
		energyGain = 0;
		for (Bullet bullet : stateBullets)
		{
			if (bullet.getVictim() != null)
			{
				hits++;
				energyGain += bullet.getPower() * 3.0;
			}
			else missed++;
			energyDrain += bullet.getPower();
		}
	}

}
