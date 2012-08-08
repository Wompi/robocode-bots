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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;

import robocode.Rules;
import robocode.ScannedRobotEvent;

public class DisplacementVector implements Serializable
{
	private static final long	serialVersionUID	= 5000L;

	long						displaceTime;

	double[][]					posField			= new double[5000][2];

	long						lastScan;

	ArrayList<Line2D>			debugLines			= new ArrayList<Line2D>();
	Line2D						debugAvgDist;

	public void registerPostion(double ex, double ey, long scanTime)
	{
		posField[(int) scanTime][0] = ex;
		posField[(int) scanTime][1] = ey;
	}

	public double avgDist(double bPower, double eDist, long scanTime)
	{
		double result = 0;
		int bTime = (int) (eDist / Rules.getBulletSpeed(bPower));
		long index = scanTime - bTime;
		int count = 0;
		debugLines.clear();
		for (int i = 0; i < index; i++)
		{
			double ex1 = posField[i][0];
			double ey1 = posField[i][1];
			double ex2 = posField[i + bTime][0];
			double ey2 = posField[i + bTime][1];

			// double rx1 = botField[i][0];
			// double ry1 = botField[i][1];
			// double rx2 = botField[i+bTime][0];
			// double ry2 = botField[i+bTime][1];

			if (ex2 != 0 && ex1 != 0)
			{
				// double dX = ex2-tX;
				// double dY = ey2-tY;
				// ex1 -= dX;
				// ey1 -= dY;
				// ex2 -= dX ;
				// ey2 -= dY;

				result += Point2D.distance(ex1, ey1, ex2, ey2);
				debugLines.add(new Line2D.Double(ex1, ey1, ex2, ey2));
				count++;
				// System.out.format("[%d] [%3.2f:%3.2f] [%3.2f:%3.2f] dist=%3.2f\n",i,x1,y1,x2,y2,result/count);
			}
			// double a0 = RobotMath.calculateAngle(new Point2D.Double(rx1, ry1), new Point2D.Double(ex2, ey2));
			// double x = Math.cos(a0) * Point2D.distance(ex1, ey1, ex2, ey2);
		}
		return result / count;
	}

	public void onScannedRobot(ScannedRobotEvent e)
	{

	}

	public void onPaint(Graphics2D g, Color myColor)
	{
		g.setColor(myColor);
		for (Line2D dVector : debugLines)
		{
			g.draw(dVector);
		}
	}

}
