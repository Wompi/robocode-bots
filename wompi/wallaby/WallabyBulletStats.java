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

import java.util.HashMap;
import java.util.Vector;

import robocode.BulletHitBulletEvent;
import robocode.BulletHitEvent;
import robocode.BulletMissedEvent;

public class WallabyBulletStats
{
	private static WallabyBulletStats	instance;

	public long							battleRound;

	HashMap<Double, BulletStats>		myStats;

	private WallabyBulletStats()
	{
		myStats = new HashMap<Double, BulletStats>();
	}

	/**
	 * reset maybe after each battle
	 */
	public void resetBulletStats()
	{
		if (myStats.size() > 0)
		{
			printBulletStats();
			myStats.clear();
		}
	}

	public static WallabyBulletStats getInstance()
	{
		if (instance == null)
		{
			instance = new WallabyBulletStats();
		}
		return instance;
	}

	// public void registerFiredBullet(Bullet newBullet)
	// {
	// firedBullets++;
	// }

	/**
	 * my bullets who hit another bullet in this turn
	 * 
	 * @param bulletHitBulletEvents
	 */
	public void registerBHB(Vector<BulletHitBulletEvent> events)
	{
		for (BulletHitBulletEvent event : events)
		{
			double power = event.getBullet().getPower();
			BulletStats stats = myStats.get(power);
			if (stats == null)
			{
				stats = new BulletStats();
				stats.power = power;
				myStats.put(power, stats);
			}
			stats.firedBullets++;
			stats.hitBullet++;
		}
	}

	/**
	 * all my bullethits for this turn
	 * 
	 * @param bulletHitEvents
	 */
	public void registerBH(Vector<BulletHitEvent> events)
	{
		for (BulletHitEvent event : events)
		{
			double power = event.getBullet().getPower();
			BulletStats stats = myStats.get(power);
			if (stats == null)
			{
				stats = new BulletStats();
				stats.power = power;
				myStats.put(power, stats);
			}
			stats.firedBullets++;
			stats.hit++;
		}
	}

	/**
	 * all my bullets who missed at all for this turn
	 * 
	 * @param bulletMissedEvents
	 */
	public void registerBM(Vector<BulletMissedEvent> events)
	{
		for (BulletMissedEvent event : events)
		{
			double power = event.getBullet().getPower();
			BulletStats stats = myStats.get(power);
			if (stats == null)
			{
				stats = new BulletStats();
				stats.power = power;
				myStats.put(power, stats);
			}
			stats.firedBullets++;
			stats.missed++;
		}
	}

	/**
	 * print out the stats every 10 bullets
	 */
	public void printBulletStats()
	{
		System.out.format("Bullet Statistics:\n");
		System.out.format("%10s %10s %10s %10s %10s\n", "power", "fired", "missed", "hit", "hitBullet");
		for (BulletStats stats : myStats.values())
		{
			double a = 100 * stats.missed / stats.firedBullets;
			double b = (100 * stats.hit / stats.firedBullets);
			double c = (100 * stats.hitBullet / stats.firedBullets);
			System.out.format("%10.2f %10d %10.2f %10.2f %10.2f\n", stats.power, stats.firedBullets, a, b, c);
		}
	}

}

class BulletStats
{
	double	power;
	long	firedBullets;
	long	hit;
	long	missed;
	long	hitBullet;
}
