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

public class StatsRisk
{
	static RiskHelper[]	helper	= new RiskHelper[9];

	public static void registerRisk(double risk, int others, long time)
	{
		RiskHelper buffy = getRiskHelper(others);
		buffy.maxRisk = Math.max(risk, buffy.maxRisk);
		buffy.minRisk = Math.min(risk, buffy.minRisk);
		buffy.avgRisk.avg(risk, time);
	}

	public static void registerRiskRamhits(int others)
	{
		getRiskHelper(others).ramHits++;
	}

	public static void printStats()
	{
		System.out.format("%14s %11s %10s %10s\n", "MAX", "MIN", "RAM", "AVG");

		int count = 1;
		for (RiskHelper help : helper)
		{
			if (help != null)
			{
				System.out.format("[%d] %s\n", count, help.toString());
			}
			else System.out.format("[%d] no data\n", count);
			count++;
		}
		System.out.format("\n");
	}

	private static RiskHelper getRiskHelper(int others)
	{
		RiskHelper buffy = helper[others - 1];
		if (buffy == null) buffy = helper[others - 1] = new RiskHelper();
		return buffy;
	}
}

class RiskHelper
{
	double			maxRisk	= Double.MIN_VALUE;
	double			minRisk	= Double.MAX_VALUE;
	int				ramHits;
	SimpleAverage	avgRisk	= new SimpleAverage(200000, "risk");

	public String toString()
	{
		return String.format("%+10.1f %+10.1f %+10d %+10.1f", maxRisk, minRisk, ramHits, avgRisk.avg());
	}
}
