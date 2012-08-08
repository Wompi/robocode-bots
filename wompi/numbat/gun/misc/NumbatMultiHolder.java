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

import java.util.ArrayList;
import java.util.Collections;

public class NumbatMultiHolder implements INumbatTick
{
	// debug
	public static int						classCount;
	public static int						classElements;
	public static int						classMaxElements;

	public ArrayList<NumbatSingleHolder>	vCount;			// sorted list with max vCount at index 0

	public NumbatMultiHolder(NumbatSingleHolder singleTick)
	{
		(vCount = new ArrayList<NumbatSingleHolder>()).add(singleTick);
		classCount++;
		classElements++;
	}

	@Override
	public NumbatSingleHolder getMaxTick()
	{
		return vCount.get(0);
	}

	@Override
	public boolean incrementCount(int id)
	{
		for (NumbatSingleHolder singleTick : vCount)
		{
			if (singleTick.myID == id)
			{
				singleTick.vCount++;
				NumbatSingleHolder maxTick = vCount.get(0);
				if (maxTick != singleTick && singleTick.vCount > maxTick.vCount)
				{
					Collections.sort(vCount);
				}
				return true;
			}
		}
		NumbatSingleHolder newTick;
		(newTick = NumbatSingleHolder.getNewInstance(id)).vCount++;
		vCount.add(newTick);
		Collections.sort(vCount);
		classElements++;
		classMaxElements = Math.max(classMaxElements, vCount.size());
		return true;
	}
}
