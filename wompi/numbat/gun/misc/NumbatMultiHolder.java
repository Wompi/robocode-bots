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
	public static int							classCount;
	public static int							classElements;
	public static int							classMaxElements;

	private final ArrayList<NumbatSingleHolder>	vCount;			// sorted list with max vCount at index 0

	public NumbatMultiHolder(NumbatSingleHolder tick)
	{
		vCount = new ArrayList<NumbatSingleHolder>();
		vCount.add(tick);
		classCount++;
		classElements++;
	}

	@Override
	public void addTick(double deltaHeading, double velocity)
	{
		NumbatSingleHolder newTick = NumbatSingleHolder.getNewInstance(deltaHeading, velocity);
		newTick.vCount++;
		vCount.add(newTick);
		sort(newTick);
		classElements++;
		if (vCount.size() > classMaxElements)
		{
			classMaxElements = vCount.size();
			//	System.out.format("newMax = %d", classMaxElements);
		}
		//System.out.format("\n");
	}

	@Override
	public NumbatSingleHolder getMaxTick()
	{
		// TODO: rethink about this - the first naiv try of taking the random index was not very rewarding
		// find something other to take the right tick, maybe it was to time intensive (haven't checked yet)
		//		int[] value = new int[3];
		//		double[] head = new double[3];
		//		double[] velo = new double[3];
		//
		//		double sum = 0;
		//
		//		//		for (int i = 0; i < Math.min(3, vCount.size()); i++)
		//		for (int i = 0; i < vCount.size(); i++)
		//		{
		//			//			value[i] = vCount.get(i).vCount;
		//			//			head[i] = vCount.get(i).tHeadingDelta;
		//			//			velo[i] = vCount.get(i).tVelocity;
		//			sum += vCount.get(i).vCount;
		//		}
		//
		//		double rand = Math.random();
		//		double border = 0;
		//		int index = 0;
		//		//for (int i = 0; i < Math.min(3, vCount.size()); i++)
		//		for (int i = 0; i < vCount.size(); i++)
		//		{
		//			border += vCount.get(i).vCount / sum;
		//			if (rand <= border)
		//			{
		//				index = i;
		//				break;
		//			}
		//		}

		//		System.out.format("max=%d (%3.2f,%3.2f) second=%d (%3.2f,%3.2f) third=%d (%3.2f,%3.2f)\n", value[0], head[0], velo[0], value[1], head[1],
		//				velo[1], value[2], head[2], velo[2]);
		//return vCount.get(index);
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
				sort(singleTick);
				return true;
			}
		}
		return false;
	}

	private void sort(NumbatSingleHolder checkTick)
	{
		NumbatSingleHolder maxTick = vCount.get(0);
		if (maxTick != checkTick && checkTick.vCount > maxTick.vCount)
		{
			Collections.sort(vCount);
		}
	}

}
