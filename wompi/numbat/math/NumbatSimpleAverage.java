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
package wompi.numbat.math;

public class NumbatSimpleAverage
{
	private int	index;
	double		values[];

	public NumbatSimpleAverage(int order)
	{
		values = new double[order];
	}

	public double avg(double value, long lastScan)
	{
		values[index % values.length] = value;
		index++;

		return avg() * Math.signum(value);
	}

	public double avg()
	{
		double result = 0;
		int buffy = Math.min(index, values.length);
		for (int i = 0; i < buffy; i++)
		{
			result += Math.abs(values[i]);
		}
		return result / buffy;
	}
}
