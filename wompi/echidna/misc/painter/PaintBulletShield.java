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
import java.awt.Rectangle;
import java.awt.geom.Point2D;

import robocode.Bullet;
import robocode.Rules;
import wompi.paint.PaintHelper;
import wompi.robomath.RobotMath;

public class PaintBulletShield
{
	public static void onPaint(Graphics2D g, double rx, double ry, double ex, double ey, Color myColor)
	{
		Rectangle rectangle = new Rectangle((int) rx - 18, (int) ry - 18, 36, 36);

		g.setColor(myColor);
		g.fill(rectangle);
		g.draw(rectangle);

		Point2D start = new Point2D.Double(ex, ey);
		Point2D end1 = new Point2D.Double(rx - 18, ry - 18);
		Point2D end2 = new Point2D.Double(rx + 18, ry - 18);
		Point2D end3 = new Point2D.Double(rx + 18, ry + 18);
		Point2D end4 = new Point2D.Double(rx - 18, ry + 18);

		PaintHelper.drawLine(start, end1, g, PaintHelper.whiteTrans);
		PaintHelper.drawLine(start, end2, g, PaintHelper.whiteTrans);
		PaintHelper.drawLine(start, end3, g, PaintHelper.whiteTrans);
		PaintHelper.drawLine(start, end4, g, PaintHelper.whiteTrans);
	}

	public static void paintShield(Graphics2D g, double ex, double ey, Bullet bullet, Color myColor)
	{
		Point2D start = new Point2D.Double(ex, ey);
		Point2D end1 = new Point2D.Double(bullet.getX(), bullet.getY());
		Point2D end2 = RobotMath.calculatePolarPoint(bullet.getHeadingRadians() - Math.PI, Rules.getBulletSpeed(bullet.getPower()), end1);
		double angle1 = RobotMath.calculateAngle(start, end1);
		double angle2 = RobotMath.calculateAngle(start, end2);
		Point2D endB1 = RobotMath.calculatePolarPoint(angle1, 800, start);
		Point2D endB2 = RobotMath.calculatePolarPoint(angle2, 800, start);
		PaintHelper.drawLine(start, endB1, g, myColor);
		PaintHelper.drawLine(start, endB2, g, myColor);
	}

}
