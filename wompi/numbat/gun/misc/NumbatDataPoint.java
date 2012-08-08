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

public class NumbatDataPoint
{
	private final int		DELTA_HEADING_INDEX	= 20;
	private final int		VELOCITY_INDEX		= 16;
	private final double	HEAD_FACTOR			= 2.0;
	private final double	VELO_FACTOR			= 2.0;

	private int				myKey;

	private double			myDeltaHeading;
	private double			myVelocity;

	public void registerValues(double deltaHeading, double velocity)
	{
		myDeltaHeading = deltaHeading;
		myVelocity = velocity;

		myKey = encode();
	}

	public int getKey()
	{
		return myKey;
	}

	private int encode()
	{
		int hint = (int) Math.rint(Math.toDegrees(myDeltaHeading * HEAD_FACTOR)) + DELTA_HEADING_INDEX;
		int vint = (int) (Math.rint(myVelocity * VELO_FACTOR)) + VELOCITY_INDEX;
		int head = (hint) << 6;
		int key = (head + (vint)) << 3;
		return key;
	}
}
