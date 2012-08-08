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

import java.awt.Graphics2D;
import java.util.Arrays;

import robocode.Rules;

public class SegmentationStats
{
	public double[]	segmentField;
	int				fadeIndex;
	int				zeroCount;

	int				MAX_FACTOR_VISITS	= 20;

	public SegmentationStats(int dimension)
	{
		segmentField = new double[dimension];
	}

	public void addValue(double value)
	{
		int index = (int) (Math.round(value) + Rules.MAX_VELOCITY);
		segmentField[index]++;
	}

	// private void decrementTheOtherFactors(int index)
	// {
	// all = 0;
	// for (int i = 0, numFactors = segmentField.length; i < numFactors; i++)
	// {
	// if (i != index && segmentField[i] > 0)
	// {
	// segmentField[i]--;
	// }
	// if (i != 8) all += segmentField[i];
	// }
	// }

	public double getDirection(double value)
	{

		int index = (int) (Math.round(value) + Rules.MAX_VELOCITY);

		int W = 3;

		double[] sub;

		// double result = segmentField[9] - segmentField[7];
		// boolean isPositive = true;
		// if (index == 8)
		// {
		// if (Math.abs(result) < 1.5)
		// {
		// isPositive = false;
		// }
		// }

		int minIndex;
		if (index > 8)
		{
			sub = Arrays.copyOfRange(segmentField, 7, segmentField.length);
			minIndex = 7;
		}
		else
		// (index < 8)
		{
			sub = Arrays.copyOfRange(segmentField, 0, 7);
			minIndex = 0;
		}

		double all = 0;
		for (double seg : sub)
		{
			all += seg;
		}

		// quick test
		// System.out.format("Sub: %s\n",Arrays.toString(sub));
		double avg = 0;
		for (int i = 0; i < sub.length; i++)
		{
			double velo = (minIndex + i - 8);
			avg += (sub[i] / all) * velo;
			// System.out.format("[%d] index=%d  velo=%3.0f all=%3.2f avg=%3.2f \n",i,index,velo,all,avg);
		}

		if (index != 8)
		{
			zeroCount = 0;
			return avg;
		}
		else if (zeroCount++ > 10) return 0;

		return avg;
	}

	public void onPaint(Graphics2D g)
	{
		System.out.format("Segment: %s\n", Arrays.toString(segmentField));
	}
}
