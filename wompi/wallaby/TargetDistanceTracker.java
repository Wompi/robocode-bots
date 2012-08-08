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
package wompi.wallaby;

public class TargetDistanceTracker
{
	private int	index;
	double		values[];
	double		lastTime;

	public TargetDistanceTracker(int order)
	{
		values = new double[order];
	}

	// the tracker gets an statusupdate on begin of the turn to track missed scanns
	public void statusUpdate(long time)
	{
		int bin = index % values.length;
		values[bin] = 2;
		index++;
	}

	public void register(double velocity)
	{
		int bin = (index - 1) % values.length;  // because of the status update the index has to be -1 to fill the current bin
		double dir = Math.signum(velocity);
		values[bin] = dir;

		// if ((index-1) >= 0 && dir != values[((index-1)%values.length)])
		// {
		// //System.out.format("dir change\n");
		// }
	}

	public double getDirectionForIndex(int bulletIndex)
	{
		if (index < bulletIndex) return 1.0;

		int lastIndex = index;
		int bin = 0;
		int buffy = bulletIndex;
		while (bulletIndex >= 0)
		{
			bin = lastIndex-- % values.length;
			bulletIndex--;
		}
		// /System.out.format("bin=%d bulletIndex=%d value=%3.0f \n", bin,buffy,values[bin]);
		return values[bin];
	}

	public int isOscilator(String name)
	{
		int buffy = Math.min(index, values.length);
		int dirChanges = 0;
		double lastDir = 0;
		for (int i = 1; i < buffy; i++)
		{
			double dir = Math.signum(values[i]);
			if (dir != 0 && lastDir != dir)
			{
				lastDir = dir;
				dirChanges++;
			}
		}
		// System.out.format("dirChange[%d]=%d %s\n",buffy,dirChanges,name);
		return dirChanges;
	}

	public void onPrint(String name)
	{
		System.out.format("Name: %d %s\n", isOscilator(name), name);
		int buffy = Math.min(index, values.length);
		double last = -3;
		for (int i = 1; i < buffy; i++)
		{
			if (values[i] != 2)
			{
				double cur = Math.signum(values[i]);
				if (cur != last && last != -3)
				{
					System.out.format("\n");
				}
				last = cur;
				System.out.format(" %+3.0f", cur);
			}
			else System.out.format(" _");
		}
		System.out.format("\n");
	}

}
