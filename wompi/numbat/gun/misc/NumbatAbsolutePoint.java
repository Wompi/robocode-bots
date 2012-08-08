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

public class NumbatAbsolutePoint extends NumbatRelativePoint
{
	private final static int	DEFAULT_WIDTH	= 5;	// can be every uneven number, bigger means more values get the same hash

	private int					myHash;
	private int					myX;
	private int					myY;

	public void registerState(double x, double y, double velocity, double lastVelocity, double heading, double lastHeading)
	{
		super.registerState(velocity, lastVelocity, heading, lastHeading);
		myX = getAdjustedCoordinate((int) x);
		myY = getAdjustedCoordinate((int) y);
		myHash = myX << 10 | myY;
		// System.out.format("%d  [%d,%d]  -> adjusted [%d:%d] \n", myHash,(int)x,(int)y,myX,myY);
	}

	public int getHash()
	{
		return myHash;
	}

	public int getX()
	{
		return myX;
	}

	public int getY()
	{
		return myY;
	}

	private int getAdjustedCoordinate(int coordinate)
	{
		return coordinate - (coordinate % DEFAULT_WIDTH) + DEFAULT_WIDTH / 2;

	}
}
