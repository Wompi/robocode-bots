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

public class TestPatternString
{
	StringBuilder	myHistory;

	long			lastStatusTime;

	public TestPatternString()
	{
		myHistory = new StringBuilder();
	}

	public void registerHistoryPlaceHolder(long time)
	{
		long timeDiff = time - lastStatusTime;

		if (timeDiff == 1) myHistory.insert(0, 'x');
		else if (timeDiff > 1)
		{
			for (long i = 0; i < (timeDiff - 1); i++)
			{
				myHistory.insert(0, 'o');
			}
			myHistory.insert(0, 'x');
		}
	}

	public void printHistory(long time)
	{
		System.out.format("[%d] %s\n", time, myHistory.substring(0, (int) Math.min(70, time)));
	}

}
