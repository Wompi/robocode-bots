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
	final static int				DELTA_HEADING_INDEX	= 20;
	final static int				VELOCITY_INDEX		= 8;
	final static double				HEAD_FACTOR			= 2.0;
	final static double				VELO_FACTOR			= 1.0;

	public static int				classCount;
	public static HashSet<Integer>	classIDStats		= new HashSet<Integer>();
	public static int				maxCount;
	public static int				avgSum;
	public static int				avg;

	public int						myID;
	public int						vCount;

	public double					tHeadingDelta;
	public double					tVelocity;

	public NumbatSingleHolder(double deltaHeading, double velocity)
	{
		tHeadingDelta = deltaHeading;
		tVelocity = velocity;
		myID = getEncodedID(deltaHeading, velocity);
		classCount++;
		classIDStats.add(myID);
	}

	@Override
	public NumbatSingleHolder getMaxTick()
	{
		//System.out.format("single\n");
		return this;
	}

	@Override
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
	public void addTick(double deltaHeading, double velocity)
	{
		throw new UnsupportedOperationException("NumbatSingleHolder can not add Ticks!");
	}

	@Override
	public int compareTo(NumbatSingleHolder o)
	{
		return o.vCount - this.vCount; // descending order
	}

	public static int getEncodedID(double deltaHead, double velocity)
	{
		int headInt = (int) Math.rint(Math.toDegrees(deltaHead * HEAD_FACTOR)) + DELTA_HEADING_INDEX;
		int veoInt = (int) (Math.rint(velocity * VELO_FACTOR)) + VELOCITY_INDEX;
		return (((headInt) << 8) + (veoInt));
	}

	public static NumbatSingleHolder getNewInstance(double deltaHead, double velocity)
	{
		return new NumbatSingleHolder(deltaHead, velocity);
	}
}
