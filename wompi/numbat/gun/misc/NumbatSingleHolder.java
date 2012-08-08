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

import java.util.HashSet;

public class NumbatSingleHolder implements INumbatTick, Comparable<NumbatSingleHolder>
{
	public static int				classCount;
	public static HashSet<Integer>	classIDStats	= new HashSet<Integer>();
	public static int				maxCount;
	public static int				avgSum;
	public static int				avg;

	public int						myID;
	public int						vCount;

	public NumbatSingleHolder(int id)
	{
		myID = id;
		classCount++;
		classIDStats.add(myID);
	}

	public NumbatSingleHolder getMaxTick()
	{
		return this;
	}

	public boolean incrementCount(int id)
	{
		if (id == myID)
		{
			vCount++;

			maxCount = Math.max(maxCount, vCount);
			avgSum++;
			return true;
		}
		return false;
	}

	@Override
	public int compareTo(NumbatSingleHolder o)
	{
		return o.vCount - this.vCount; // descending order
	}

	public static NumbatSingleHolder getNewInstance(int id)
	{
		return new NumbatSingleHolder(id);
	}
}
