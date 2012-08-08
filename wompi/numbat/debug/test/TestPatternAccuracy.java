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
package wompi.numbat.debug.test;

import java.util.ArrayList;
import java.util.HashMap;

import wompi.numbat.math.LevenshteinDistance;

public class TestPatternAccuracy
{
	private static StringBuilder							sActual;
	private static long										lastTime	= Long.MAX_VALUE;
	private static HashMap<Long, ArrayList<StringBuilder>>	mapPattern;

	private static void reset()
	{
		sActual = new StringBuilder();
		mapPattern = new HashMap<Long, ArrayList<StringBuilder>>();
	}

	public static void registerActualPattern(long time, char tick)
	{
		if (time < lastTime) reset();

		if (time != lastTime)
		{
			sActual.insert(0, tick).setLength(72);
		}
		lastTime = time;
	}

	public static void registerDesiredPattern(long time, StringBuilder sb)
	{
		ArrayList<StringBuilder> buffy = mapPattern.get(time + sb.length());
		if (buffy == null)
		{
			mapPattern.put(time + sb.length(), buffy = new ArrayList<StringBuilder>());
		}
		buffy.add(sb);
	}

	public static void onPrint(long time)
	{
		String[] desire = null;
		int len = 0;
		ArrayList<StringBuilder> buffy = mapPattern.get(time);
		if (buffy != null)
		{
			desire = new String[buffy.size()];
			int count = 0;
			for (StringBuilder sDesire : buffy)
			{
				desire[count++] = sDesire.toString();
				len = Math.max(sDesire.length(), len);
			}
		}
		if (len > 0)
		{
			String actual = sActual.substring(0, Math.min(sActual.length(), len));
			System.out.format("[%04d]A %s\n", time, actual);
			for (String str : desire)
			{
				System.out.format("[%04d]D %s [%d]\n", time, str.toString(), LevenshteinDistance.computeLevenshteinDistance(str, actual));
			}
			System.out.format("\n");
		}
	}

}
