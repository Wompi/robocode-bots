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
package wompi.wallaby;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

public class PaintHelper
{
	public static double	PI_CIRCLE	= 2 * Math.PI;
	public static Font		myFont		= new Font("Dialog", Font.PLAIN, 16);
	public static Color		whiteTrans	= new Color(0xFF, 0xFF, 0xFF, 0x50);
	public static Color		redTrans	= new Color(0x50, 0x00, 0x00, 0x99);
	public static Color		blueTrans	= new Color(0x00, 0x00, 0xFF, 0x50);
	public static Color		greenTrans	= new Color(0x00, 0xFF, 0x00, 0x50);
	public static Color		yellowTrans	= new Color(0xFF, 0xFF, 0x00, 0x50);

	public static Color[]	colorField	=
										{ Color.GREEN, Color.GRAY, Color.CYAN, Color.YELLOW, Color.RED, Color.BLUE,
			Color.PINK, Color.MAGENTA, Color.ORANGE, Color.LIGHT_GRAY };

	public static void drawArc(Point2D start, double distance, double startAngle, double arcAngle, boolean fill,
			Graphics2D g2, Color color)
	{
		int r2 = (int) (distance * 2);
		int x = (int) (start.getX() - distance);
		int y = (int) (start.getY() - distance);

		g2.setColor(color);
		if (fill)
			g2.fillArc(x, y, r2, r2, (int) Math.toDegrees(startAngle), (int) Math.toDegrees(arcAngle));
		else
			g2.drawArc(x, y, r2, r2, (int) Math.toDegrees(startAngle), (int) Math.toDegrees(arcAngle));
	}

	public static GeneralPath addPathSegment(GeneralPath path, Point2D point)
	{
		if (path == null)
		{
			path = new GeneralPath(GeneralPath.WIND_EVEN_ODD, 2000);
			path.moveTo(point.getX(), point.getY());
		}
		else
		{
			path.lineTo(point.getX(), point.getY());
		}
		return path;
	}

	public static void drawLine(Line2D line, Graphics2D g, Color color)
	{
		if (line == null) return;
		if (Double.isInfinite(line.getP1().distance(line.getP2()))) return;
		g.setColor(color);
		g.draw(line);
	}

	public static void drawLine(Point2D start, Point2D ende, Graphics2D g2, Color color)
	{
		if (start == null || ende == null) return;
		g2.setColor(color);
		g2.drawLine((int) start.getX(), (int) start.getY(), (int) ende.getX(), (int) ende.getY());
	}

	public static void drawPoint(Point2D point, Color color, Graphics2D g2, float size)
	{
		Stroke old = g2.getStroke();
		g2.setStroke(new BasicStroke(size));
		g2.setColor(color);
		g2.drawLine((int) point.getX(), (int) point.getY(), (int) point.getX(), (int) point.getY());
		g2.setStroke(old);
	}

	public static void drawString(Graphics2D g, String string, double x, double y, Color color)
	{
		g.setColor(color);
		g.setFont(myFont);
		g.drawString(string, (int) x, (int) y);
	}

}
