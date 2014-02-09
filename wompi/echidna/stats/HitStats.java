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

import robocode.Bullet;

public class HitStats
{
	private final HitStatsHelper[]	myLayerStats	= new HitStatsHelper[9];
	int								allShots;

	public void printStats(String name, boolean showRatioOnly)
	{
		String out = String.format("[%03d] ", allShots);
		for (HitStatsHelper stats : myLayerStats)
		{
			if (stats != null)
			{
				stats.calculateMembers(name);
				if (showRatioOnly)
				{
					out += String.format("%3.2f ", stats.myRatio);
				}
				else
				{
					out += String.format("[%02d|%03d|%3.2f|%02d] ", stats.myHits, stats.myMiss, stats.myRatio,

					stats.myColateral);
				}
			}
			else
			{
				if (showRatioOnly)
					out += "____ ";
				else
					out += "[______________] ";
			}
		}
		System.out.format("%s %s \n", out, name);
	}

	public void addBullet(Bullet shot, int others)
	{
		HitStatsHelper stats = myLayerStats[others - 1];
		if (stats == null)
		{
			myLayerStats[others - 1] = new HitStatsHelper();
		}
		myLayerStats[others - 1].myBullets.add(shot);
		allShots++;
	}
}

class HitStatsHelper
{
	ArrayList<Bullet>	myBullets	= new ArrayList<Bullet>();
	int					myHits;
	int					myMiss;
	int					myColateral;
	double				myRatio;

	protected void calculateMembers(String name)
	{

		myHits = 0;
		myMiss = 0;
		myColateral = 0;
		for (Bullet shot : myBullets)
		{
			String victim = shot.getVictim();
			if (victim != null)
			{
				if (victim.equals(name))
					myHits++;
				else
					myColateral++;

			}
			else
			{
				myMiss++;
			}
		}
		myRatio = (double) myHits / myBullets.size();

	}
}
