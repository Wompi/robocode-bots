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

import java.util.HashMap;
import java.util.Map.Entry;

public class TestPatternString
{
	HashMap<Integer, StringBuilder>	allHistory;
	HashMap<String, Integer>		botMap;
	//StringBuilder					myHistory;
	int								botIndex;
	long							lastStatusTime;

	public TestPatternString()
	{
		//		myHistory = new StringBuilder();
		botMap = new HashMap<String, Integer>();
		allHistory = new HashMap<Integer, StringBuilder>();
		for (int i = 0; i < 9; i++)
		{
			allHistory.put(i, new StringBuilder());
		}
	}

	public void registerHistoryPlaceHolder(long time)
	{
		long timeDiff = time - lastStatusTime;

		for (StringBuilder sb : allHistory.values())
		{
			if (timeDiff == 1) sb.insert(0, 'x');
			else if (timeDiff > 1)
			{
				for (long i = 0; i < (timeDiff - 1); i++)
				{
					sb.insert(0, 'o');
				}
				sb.insert(0, 'x');
			}
		}
		lastStatusTime = time;
	}

	public void registerScan(long time, String name)
	{
		Integer bot = botMap.get(name);
		if (bot == null)
		{
			botMap.put(name, bot = botIndex++);
		}
		allHistory.get(bot).setCharAt(0, 'h');
	}

	public void printHistory(long time)
	{
		for (Entry<Integer, StringBuilder> entry : allHistory.entrySet())
		{
			int bot = entry.getKey();
			StringBuilder hist = entry.getValue();
			String botName = "none";
			for (Entry<String, Integer> db : botMap.entrySet())
			{
				String dbName = db.getKey();
				int dbIndex = db.getValue();
				if (dbIndex == bot)
				{
					botName = dbName;
					break;
				}
			}
			System.out.format("[%d] %s - %s\n", time, hist.substring(0, (int) Math.min(70, time)), botName);
		}
		System.out.format("\n");
	}
}
