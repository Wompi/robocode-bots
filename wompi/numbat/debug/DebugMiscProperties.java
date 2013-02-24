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
import java.util.HashSet;

public class DebugMiscProperties
{
	private static boolean			isActive		= true;

	// win stats
	private static int[]			winStats		= new int[DebugBot.getBot().getOthers() + 1];

	// skipped
	private static int				skippedTurns;
	private static long				blindSkippedTurn;
	private static long				lastTime;
	private static long				lastScanTime	= Long.MAX_VALUE;
	private static int				wrongScanTime;
	private static int				multipleScanEvents;
	private static HashSet<String>	scannedTurnOpponnents;

	public static void onKeyPressed(char c)
	{
		if ('s' != c) return;
		isActive = !isActive;
	}

	public static void debugWinStats(int others)
	{
		winStats[others]++;
	}

	public static void debugScanEvents()
	{
		multipleScanEvents++;
	}

	public static void debugBlindSkippedTurns(long time)
	{
		if (time - lastTime > 1)
		{
			blindSkippedTurn += time - lastTime - 1;
			System.out.format("[%d] blind skip %d - %d\n", time, lastTime, time);
		}
		lastTime = time;
	}

	public static void debugSkippedTurns()
	{
		skippedTurns++;
	}

	public static void execute()
	{
		if (isActive)
		{
			DebugBot.getBot().setDebugProperty("Win", Arrays.toString(winStats));
			DebugBot.getBot().setDebugProperty(
					"Skipped Turns",
					String.format("%d Blind: %d ScanTime: %d Multiple: %d", skippedTurns, blindSkippedTurn,
							wrongScanTime, multipleScanEvents));
		}
		else
		{
			DebugBot.getBot().setDebugProperty("Win", null);
			DebugBot.getBot().setDebugProperty("Skipped Turns", null);
		}
	}
}
