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
package wompi.echidna.misc.utils;

import robocode.AdvancedRobot;

public enum TimeProfile
{
	TURN_TIME, GUN_UPDATE, GUN_EXECUTE, RADAR_TIME, MOVE_TIME, DEBUG_TIME_01, DEBUG_TIME_02;

	private ValueInfo	battleProfile;
	private ValueInfo	roundProfile;
	private ValueInfo	turnProfile;

	private long		startTime	= -1;

	public void start()
	{
		if (startTime != -1)
		{
			System.out.printf("[WARN] %s: Stop was not called\n", this.name());
			return;
		}
		startTime = System.nanoTime();
		if (startTime < 0) System.out.format("ERROR: %d", startTime);
	}

	public void stop()
	{
		if (startTime == -1)
		{
			System.out.printf("[WARN] %s: Start was not called\n", this.name());
			return;
		}
		long time = System.nanoTime() - startTime;
		if (battleProfile != null) battleProfile.addValue(time);
		if (roundProfile != null) roundProfile.addValue(time);
		if (turnProfile != null) turnProfile.addValue(time);
		startTime = -1;
	}

	public static void initBattle()
	{
		for (TimeProfile tp : values())
		{
			tp.battleProfile = new ValueInfo();
		}
	}

	public static void initRound()
	{
		for (TimeProfile tp : values())
		{
			tp.roundProfile = new ValueInfo();
		}
	}

	public static void initTurn()
	{
		for (TimeProfile tp : values())
		{
			tp.turnProfile = new ValueInfo();
		}
	}

	public static void setTurnProperties(AdvancedRobot bot)
	{
		for (TimeProfile tp : values())
		{
			bot.setDebugProperty(String.format("TURN %s", tp.name()), tp.turnProfile.toString());
		}
	}

	public static void setRoundProperties(AdvancedRobot bot)
	{
		for (TimeProfile tp : values())
		{
			bot.setDebugProperty(String.format("ROUND %s", tp.name()), tp.roundProfile.toString());
		}
	}

	public static void setBattleProperties(AdvancedRobot bot)
	{
		for (TimeProfile tp : values())
		{
			bot.setDebugProperty(String.format("BATTLE %s", tp.name()), tp.battleProfile.toString());
		}
	}

}

class ValueInfo
{
	// private final AvgValue avgValue;
	private long	maxValue	= Long.MIN_VALUE;
	private long	minValue	= Long.MAX_VALUE;

	private double	avgSum;
	private double	avgCount;
	private double	avg;

	private long	total;

	public ValueInfo()
	{}

	public void addValue(double value)
	{
		maxValue = (long) Math.max(maxValue, value);
		minValue = (long) StrictMath.min(minValue, value);

		avgSum += value;
		avgCount++;
		avg = avgSum / avgCount;
		// avgValue.addValue(value);
		total += value;
	}

	@Override
	public String toString()
	{
		if (maxValue == Long.MIN_VALUE)
		{
			return null;
		}
		else if (maxValue == minValue)
		{
			// long min = TimeUnit.NANOSECONDS.toMicros((long)minValue);
			// return String.format("[ %,14d ]", min);
			return String.format("[ %,14d ]", minValue);
		}
		else
		{
			// long min = TimeUnit.NANOSECONDS.toMicros((long)minValue);
			// long max = TimeUnit.NANOSECONDS.toMicros((long)maxValue);
			// long avg = TimeUnit.NANOSECONDS.toMicros((long)avgValue.getCurrentValue());
			// long all = TimeUnit.NANOSECONDS.toMicros((long)total);
			// return String.format("[ %,9d | %,9d | %,14d | %,20d]",min,avg,max,all);

			return String.format("[ %,9d | %,9.0f | %,14d | %,20d]", minValue, avg, maxValue, total);

		}
	}
}
