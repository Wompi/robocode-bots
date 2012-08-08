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

import wompi.echidna.misc.SimpleAverage;

public class StatsWinLoose
{
	private static int				DEFAULT_OPPONENTS	= 10;
	private static WinLooseHelper	statsField[]		= new WinLooseHelper[DEFAULT_OPPONENTS];

	public static void registerRobotDeath(int others, double energy, long time)
	{
		WinLooseHelper helper = getHelper(others);
		helper.minEnergy = Math.min(helper.minEnergy, energy);
		helper.maxEnergy = Math.max(helper.maxEnergy, energy);
		helper.energyAverage.avg(energy, time);
	}

	public static void registerDeath(int others, long time)
	{
		WinLooseHelper helper = getHelper(others);
		helper.minTurn = Math.min(helper.minTurn, time);
		helper.maxTurn = Math.max(helper.maxTurn, time);
		helper.turnAverage.avg(time, time);
		helper.rankCount++;
	}

	public static void registerWin(long time, double energy)
	{
		WinLooseHelper helper = (WinLooseHelper) getHelper(0);
		helper.minEnergy = Math.min(helper.minEnergy, energy);
		helper.maxEnergy = Math.max(helper.maxEnergy, energy);
		helper.energyAverage.avg(energy, time);
		helper.minTurn = Math.min(helper.minTurn, time);
		helper.maxTurn = Math.max(helper.maxTurn, time);
		helper.turnAverage.avg(time, time);
		helper.rankCount++;
	}

	public static void printStats()
	{
		System.out.format("%11s %10s %10s %10s %11s %11s %11s\n", "RANK", "MAX(T)", "MIN(T)", "AVG(T)", "MAX(E)", "MIN(E)", "AVG(E)");
		int count = 1;
		for (WinLooseHelper help : statsField)
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

	private static WinLooseHelper getHelper(int others)
	{
		WinLooseHelper helper = statsField[others];
		if (helper == null)
		{
			helper = statsField[others] = new WinLooseHelper();
		}
		return helper;
	}
}

class WinLooseHelper
{
	double			minEnergy		= Double.MAX_VALUE;
	double			maxEnergy		= Double.MIN_VALUE;
	SimpleAverage	energyAverage	= new SimpleAverage(100, "energy average");
	long			minTurn			= Long.MAX_VALUE;
	long			maxTurn			= Long.MIN_VALUE;
	SimpleAverage	turnAverage		= new SimpleAverage(100, "turn average");
	int				rankCount;

	public String toString()
	{
		double maxT = maxTurn;
		double minT = minTurn;

		if (maxTurn == Long.MIN_VALUE) maxT = Double.NaN;
		if (minTurn == Long.MAX_VALUE) minT = Double.NaN;
		if (minEnergy == Double.MAX_VALUE) minEnergy = Double.NaN;
		if (maxEnergy == Double.MIN_VALUE) maxEnergy = Double.NaN;

		return String.format("%+5d %+10.0f %+10.0f %+10.0f %+10.1f %+10.1f %+10.1f", rankCount, maxT, minT, turnAverage.avg(), maxEnergy, minEnergy,
				energyAverage.avg());
	}
}
