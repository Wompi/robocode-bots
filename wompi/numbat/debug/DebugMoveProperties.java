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

public class DebugMoveProperties
{
	private static boolean			isActive		= true;

	private static String			mName;
	private static int				closest;
	private static CloseBotHelper[]	closeBotStats	= new CloseBotHelper[DebugBot.getBot().getOthers() + 1];

	public static void onKeyPressed(char c)
	{
		if ('m' != c) return;
		isActive = !isActive;
	}

	public static void debugCurrentMove(String moveName)
	{
		mName = moveName;
	}

	public static void debugClosestBots(int closeBots, int others)
	{
		closest = closeBots;

		CloseBotHelper helper = closeBotStats[others];
		if (helper == null)
		{
			closeBotStats[others] = helper = new CloseBotHelper();
		}
		helper.maxCloseBots = Math.max(closeBots, helper.maxCloseBots);
		helper.avgSum += closeBots;
		helper.avgCount++;
	}

	public static void execute()
	{
		if (isActive)
		{
			DebugBot.getBot().setDebugProperty("Move", mName);

			StringBuilder stats = new StringBuilder();
			int count = 0;
			for (CloseBotHelper helper : closeBotStats)
			{
				if (helper != null)
				{
					stats.append(String.format("%d:[%d|%d] ", count, helper.maxCloseBots, helper.avgSum / helper.avgCount));
				}
				count++;
			}

			DebugBot.getBot().setDebugProperty("ClosestTo", String.format("%d bots STATS: %s", closest, stats.toString()));
		}
		else
		{
			DebugBot.getBot().setDebugProperty("Move", null);
			DebugBot.getBot().setDebugProperty("ClosestTo", null);
		}
	}
}

class CloseBotHelper
{
	int	maxCloseBots;
	int	avgSum;
	int	avgCount;
}
