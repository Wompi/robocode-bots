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
package wompi.numbat.gun.misc;

import java.util.HashMap;

public class NumbatOneTickHandler
{
	public final static int						BREAK_KEY	= 65;

	private HashMap<Integer, NumbatOneTickList>	allTicks;
	private int									lastTick	= BREAK_KEY;

	public NumbatOneTickHandler()
	{
		allTicks = new HashMap<Integer, NumbatOneTickList>();
	}

	public void registerTick(int tick)
	{
		NumbatOneTickList lastTickList = allTicks.get(lastTick);
		if (lastTickList == null)
		{
			System.out.format("register tick=%d\n", lastTick);
			allTicks.put(lastTick, lastTickList = new NumbatOneTickList());
		}
		lastTickList.registerTick(tick);
		lastTick = tick;
	}

	public int getFollowTick(int tick)
	{
		NumbatOneTickList tickList = allTicks.get(tick);
		if (tickList == null) return NumbatOneTickHandler.BREAK_KEY;
		return tickList.getMostUsedFollowTick();			// TODO: check NullPointer
	}

	public int getLastTick()
	{
		return lastTick;
	}
}

class NumbatOneTickList
{
	private HashMap<Integer, NumbatOnTick>	myFollowTicks;
	private int								allCounts;
	private NumbatOnTick					maxTick;

	public NumbatOneTickList()
	{
		myFollowTicks = new HashMap<Integer, NumbatOnTick>();
	}

	public void registerTick(int tick)
	{
		NumbatOnTick followTick = myFollowTicks.get(tick);
		if (followTick == null)
		{
			System.out.format("register follow tick=%d\n", tick);
			myFollowTicks.put(tick, followTick = new NumbatOnTick());
		}
		followTick.key = tick; // redundant
		followTick.count++;

		// enhance this to some statistics
		if (maxTick == null || followTick.count > maxTick.count)
		{
			maxTick = followTick;
		}
		allCounts++;
	}

	public int getMostUsedFollowTick()
	{
		for (NumbatOnTick tick : myFollowTicks.values())
		{
			System.out.format("%d-%d ", tick.key, tick.count);
		}
		System.out.format("\n");

		if (maxTick == null) return NumbatOneTickHandler.BREAK_KEY;
		return maxTick.key;  // TODO: check NullPointer, make this rolling to react on reactive movements
	}
}

class NumbatOnTick
{
	int	key;
	int	count;
}
