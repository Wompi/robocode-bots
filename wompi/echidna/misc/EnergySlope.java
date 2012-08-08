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

public class EnergySlope
{
	public static double calculateSlope(double time, double energy)
	{
		double zeroTime = time * (1 + energy / (Math.max(100, energy) - energy));
		// System.out.format("[%3.0f] energy=%3.2f zeroTime=%3.2f \n", time,energy,zeroTime);
		return zeroTime;
	}

	public static double caclulateFirePower(double time, double myEnergy, double eZeroTime)
	{

		double bPower = Math.abs(myEnergy / (0.1 * (eZeroTime - time) - myEnergy / 5));
		System.out.format("[%3.0f] myEnergy=%3.2f bPower=%3.2f \n", time, myEnergy, bPower);
		return bPower;
	}
}
