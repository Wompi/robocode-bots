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

public class SimpleAverage
{
	String		myName;
	private int	index;
	double		values[];
	double		vBeforeZero;
	int			zeroVelo;

	public SimpleAverage(int order, String name)
	{
		values = new double[order];
		myName = name;
	}

	public double avg(double value, long lastScan)
	{
		values[index % values.length] = value;
		index++;

		if (value != 0)
		{
			vBeforeZero = value;
			zeroVelo = 0;
		}
		else zeroVelo++;

		// return avg() * ((value == 0) ? ((zeroVelo < 10)?-Math.signum(vBeforeZero): 0): Math.signum(value));
		return avg() * Math.signum(value);
	}

	public double avg()
	{
		double result = 0;
		int buffy = Math.min(index, values.length);
		for (int i = 0; i < buffy; i++)
		{
			result += Math.abs(values[i]);
			// result += values[i]; // without abs real values
		}
		return result / buffy;
	}

	public void onPrint(String name, boolean isShowAll)
	{
		System.out.format("SimpleAvg[%s]: %s -> avg:%3.2f\n", myName, name, avg());

		if (isShowAll)
		{
			int buffy = Math.min(index, values.length);
			for (int i = 0; i < buffy; i++)
			{
				System.out.format("%+4.2f ", values[i]);

				if (i % 10 == 9) System.out.format("\n");
			}
			System.out.format("\n");
		}

	}
}
