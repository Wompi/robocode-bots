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

import java.util.ArrayList;

import wompi.numbat.target.NumbatTarget;

public class DebugTargetProperties
{
	private static boolean					isActive	= true;

	private static ArrayList<NumbatTarget>	myTargets	= new ArrayList<NumbatTarget>();

	public static void onKeyPressed(char c)
	{
		if ('t' != c) return;
		isActive = !isActive;
	}

	public static void debugCurrentTarget(NumbatTarget target)
	{
		myTargets.add(target);
	}

	public static void execute()
	{
		if (isActive)
		{
			for (NumbatTarget target : myTargets)
			{
				DebugBot.getBot().setDebugProperty(String.format("MatchLength %s", target.eName),
						String.format("%d avg=%d", target.eMatchKeyLength, target.getAveragePatternLength()));
			}
		}
		else
		{
			for (NumbatTarget target : myTargets)
			{
				DebugBot.getBot().setDebugProperty(String.format("MatchLength %s", target.eName), null);
			}
		}
	}
}
