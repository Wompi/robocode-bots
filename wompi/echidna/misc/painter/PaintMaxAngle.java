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
package wompi.echidna.misc.painter;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;

import wompi.paint.PaintHelper;
import wompi.robomath.RobotMath;

public class PaintMaxAngle
{

	public static void onPaint(Graphics2D g, double rx, double ry, double maxAngle, double eAbsBearing, Color color)
	{
		Point2D start = new Point2D.Double(rx, ry);
		Point2D end1 = RobotMath.calculatePolarPoint(eAbsBearing - maxAngle, 1000, start);
		Point2D end2 = RobotMath.calculatePolarPoint(eAbsBearing + maxAngle, 1000, start);
		PaintHelper.drawLine(start, end1, g, color);
		PaintHelper.drawLine(start, end2, g, color);
	}
}
