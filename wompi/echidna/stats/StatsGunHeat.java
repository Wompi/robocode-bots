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

public class StatsGunHeat
{
	static double			maxHeat	= Double.MIN_VALUE;
	static double			minHeat	= Double.MAX_VALUE;
	static SimpleAverage	avgHeat	= new SimpleAverage(100000, "gun heat");

	public static void registerHeat(double gunheat, long time)
	{
		maxHeat = Math.max(gunheat, maxHeat);
		minHeat = Math.min(gunheat, minHeat);
		avgHeat.avg(gunheat, time);
	}

	public static void printStats()
	{
		System.out.format("%10s %10s %10s\n", "MIN", "AVG", "MAX");
		System.out.format("%7.2f %7.2f %7.2f\n", minHeat, avgHeat.avg(), maxHeat);
	}
}
