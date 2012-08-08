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

import java.awt.geom.Point2D;

import robocode.util.Utils;

public class Iterpolate
{
	double	x;
	double	y;
	double	velocity;
	double	headDiff;

	public void interpolateMultiPoints(int steps, double velocity, double lastVelocity, double deltaHeading, double deltaDistance)
	{
		if (steps > 3)
		{
			System.out.format(" only up to 3 setps right now\n");
			return;
		}

		double sigV0 = Math.signum(lastVelocity);
		double sigV1 = Math.signum(velocity);

		if (sigV0 == sigV1)			// +..+ | -..-
		{
			if (sigV0 > sigV1)		// 8...6
			{

			}
			else if (sigV0 < sigV1)					// 6 ... 8
			{

			}
			else
			// 8..8
			{

			}

		}
		else
		{

		}

	}

	public boolean interpolateNewPoint(int count, double head0, double x0, double y0, double x2, double y2, double v0, double v2)
	{
		if (count > 1)
		{
			System.out.format("only 1 stepp interpolation right now\n");
			return false;
		}

		double s = Point2D.distance(x0, y0, x2, y2);
		if (Utils.isNear(s, v0 + v2))
		{
			x = (x2 - x0) / 2.0;
			y = (y2 - y0) / 2.0;
			velocity = v0;
			headDiff = 0;
		}
		else if (v0 == v2 && ((v0 + v2) > s))
		{
			double bearing = Math.atan2(x2 - x0, y2 - y0);

			double p = (s * s) / (2 * s);
			double h = Math.sqrt((v0 * v0) - (p * p));

			x = x0 + Math.cos(bearing) * p - Math.sin(bearing) * h;
			y = y0 + Math.sin(bearing) * p - Math.cos(bearing) * h;
			velocity = v0;

			return true;
		}

		System.out.format("only equal velocity interpolation right now\n");
		return false;
	}

}
