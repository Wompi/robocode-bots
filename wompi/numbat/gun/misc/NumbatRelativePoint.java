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

public class NumbatRelativePoint implements Comparable<NumbatRelativePoint>
{
	// M- M0 M+ R- R0 R+ A- A0 A+
	private int		myKey;
	private int		myVisits;

	private String	myBinaryStr;

	@Override
	public int compareTo(NumbatRelativePoint o)
	{
		return o.myVisits - this.myVisits;
	}

	public void registerState(double velocity, double lastVelocity, double heading, double lastHeading)
	{
		// TODO: implement valid checks to prevent false states, deltaHeading, max v, max a maybe scandiff

		int distShift = 7;
		int rotateShift = 4;
		int accelShift = 1;
		if (velocity < 0) distShift++;
		else if (velocity > 0) distShift--;

		double rotate = heading - lastHeading;
		if (rotate < 0) rotateShift++;
		else if (rotate > 0) rotateShift--;

		double accel = velocity - lastVelocity;
		if (accel < 0) accelShift++;
		else if (accel > 0) accelShift--;

		myKey = 1 << distShift | 1 << rotateShift | 1 << accelShift;

		// debug
		StringBuilder buffy = new StringBuilder("000000000");
		buffy.append(Integer.toBinaryString(myKey));
		myBinaryStr = buffy.substring(buffy.length() - 9);
	}

	public void increaseVisits()
	{
		myVisits++;
	}

	public String getBinaryString()
	{
		return myBinaryStr;
	}

	public int getKey()
	{
		return myKey;
	}

	public int getVisits()
	{
		return myVisits;
	}
}
