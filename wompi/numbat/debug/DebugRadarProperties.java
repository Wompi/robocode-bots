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
package wompi.numbat.debug;

import java.util.Arrays;

public class DebugRadarProperties
{
	private static boolean		isActive		= true;

	private static String		rName;

	private final static int	MAX_SCANDIFF	= 10;
	private static int[]		scanDiff		= new int[MAX_SCANDIFF];

	public static void debugCurrentRadar(String radarName)
	{
		rName = radarName;
	}

	public static void debugScanDifference(long scan)
	{
		scanDiff[(int) Math.min(scan, MAX_SCANDIFF - 1)]++;
	}

	public static void execute()
	{
		if (isActive)
		{
			DebugBot.getBot().setDebugProperty("Radar", rName);
			DebugBot.getBot().setDebugProperty("ScanDiff", Arrays.toString(scanDiff));
		}
		else
		{
			DebugBot.getBot().setDebugProperty("Radar", null);
			DebugBot.getBot().setDebugProperty("ScanDiff", null);
		}

	}

	public static void onKeyPressed(char c)
	{
		if ('r' != c) return;

		isActive = !isActive;
	}
}
