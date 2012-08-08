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

import robocode.Rules;

public class VelocityContainer
{
	double[][]	velocityField	= new double[17][17];
	int			lastIndex;

	public void addValue(double value, long lastScan)
	{
		int index = (int) (Math.round(value) + Rules.MAX_VELOCITY);
		velocityField[lastIndex][index]++;
		lastIndex = index;

		// System.out.format("[%d] velocity=%3.2f index=%d\n", lastScan,value,index);
		//
		// for(int i=0;i<17;i++)
		// {
		// System.out.format("[%d] %s \n",i-8,Arrays.toString(velocityField[i]));
		// }
	}

	public double getNextVelocity(double velocity)
	{
		int index = (int) (Math.round(velocity));
		double max = 0;
		double result = 0;
		for (int i = 0; i < 17; i++)
		{
			double value = velocityField[index][i];
			if (value > max)
			{
				max = value;
				result = i - 8;
			}
		}
		return result;
	}
}
