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

import robocode.Bullet;
import robocode.Rules;
import wompi.paint.PaintHelper;
import wompi.robomath.RobotMath;

public class PaintBulletLine
{
	public static void onPaint(Graphics2D g, Bullet bullet, Color color)
	{
		Point2D start = new Point2D.Double(bullet.getX(), bullet.getY());
		Point2D ende = RobotMath.calculatePolarPoint(bullet.getHeadingRadians() - Math.PI, Rules.getBulletSpeed(bullet.getPower()), start);
		PaintHelper.drawLine(start, ende, g, color);
		// PaintHelper.drawPoint(start, color, g, 4);
	}
}
