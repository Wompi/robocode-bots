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
package wompi.echidna.misc;

public class RollingAverage
{
	double	myValue;
	double	myDepth;
	String	myName;

	double	index;

	// double mainDirection;

	public RollingAverage(double depth, String name)
	{
		myDepth = depth;
		myName = name;
	}

	public double avg(double newValue, long lastScan, double weight)
	{
		double cDepth = Math.min(index, myDepth);   // could be work with getTime() as narrow value
		index++;
		myValue = (myValue * cDepth + newValue * weight) / (cDepth + weight);
		// myValue = (myValue * cDepth + newValue * weight) / (cDepth + weight);

		// double curDir = Math.signum(newValue);
		// mainDirection += curDir;
		// double mainDir = Math.signum(mainDirection);
		// mainDirection = Math.min(5, Math.abs(mainDirection)) * mainDir;
		// return myValue * Math.signum(newValue); // * ((curDir == 0) ? mainDir: curDir);
		return myValue;
	}

	public double avg()
	{
		return myValue;
	}

	public void onPrint(String name, boolean isShowAll)
	{
		System.out.format("RollingAvg[%s]: %s -> avg:%3.2f\n", myName, name, avg());
	}
}
