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
package wompi.quokka;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;

import wompi.paint.PaintHelper;
import wompi.robomath.RobotMath;

public class QuokkaPainter
{
	public static long	time;

	public static void drawCenterPos(Graphics2D g, Point2D center, double radius)
	{
		PaintHelper.drawArc(center, radius, 0.0, PaintHelper.PI_CIRCLE, false, g, Color.white); // kreis center->maxradius
	}

	public static void drawTargetStats(Graphics2D g, double velocity, double bulletSpeed, double distance,
			boolean fired, double deltaGun, double positive, double negative, Point2D firePoint, Point2D guessPoint,
			double guessAngle)
	{
		int showX = 700;
		int showY = 700;
		int count = 0;

		if (fired)
			g.setColor(Color.GRAY);
		else
			g.setColor(Color.RED);
		g.setFont(PaintHelper.myFont);
		g.drawString(String.format("VT: %3.2f ", velocity), showX, showY - count++);
		g.drawString(String.format("BS: %3.2f %3.2f", bulletSpeed, (20 - bulletSpeed) / 3.0), showX, showY - count++
				* PaintHelper.myFont.getSize());
		g.drawString(String.format("DT: %3.2f ", distance), showX, showY - count++ * PaintHelper.myFont.getSize());
		g.drawString(String.format("GD: %3.2f ", Math.toDegrees(deltaGun)), showX,
				showY - count++ * PaintHelper.myFont.getSize());
		g.drawString(String.format("DD: %3.2f %3.2f", positive, negative), showX,
				showY - count++ * PaintHelper.myFont.getSize());
		g.drawString(String.format("GA: %3.2f", Math.toDegrees(guessAngle)), showX, showY - count++
				* PaintHelper.myFont.getSize());

		if (firePoint != null)
		{
			PaintHelper.drawLine(firePoint, guessPoint, g, Color.RED);

		}
	}

	public static void drawRobotGeometry(Graphics2D g, Point2D robotPos, double gunHeading, double gunOffset,
			double distance)
	{
		PaintHelper.drawLine(robotPos, RobotMath.calculatePolarPoint(gunHeading, 1000, robotPos), g,
				PaintHelper.greenTrans);
		PaintHelper.drawLine(robotPos, RobotMath.calculatePolarPoint(gunOffset + gunHeading, 1000, robotPos), g,
				PaintHelper.redTrans);
		PaintHelper.drawArc(robotPos, distance, 0, PaintHelper.PI_CIRCLE, false, g, Color.WHITE);
	}
}
