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
package wompi.wombat;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.Vector;

public class WombatPainter
{
	static double			PI_CIRCLE		= 2 * Math.PI;

	static WombatPainter	instance;

	public Point2D			robotPos;
	public Vector<Point2D>	guessPointsHead	= new Vector<Point2D>();
	public Vector<Point2D>	guessPointsBack	= new Vector<Point2D>();

	public long				time;
	public Point2D			centerPos;
	public double			maxRadius;

	Color					whiteTrans		= new Color(0x50, 0x50, 0x50, 0x50);
	Color					redTrans		= new Color(0x50, 0x00, 0x00, 0x50);
	Color					blueTrans		= new Color(0x00, 0x00, 0x50, 0x50);
	Color					greenTrans		= new Color(0x00, 0x50, 0x00, 0x50);

	private int				pathThinner		= 5;
	GeneralPath				robotPath;

	private WombatPainter()
	{}

	public static WombatPainter getInstance()
	{
		if (instance == null)
		{
			instance = new WombatPainter();
		}
		return instance;
	}

	public void onPaint(Graphics2D g)
	{
		if (centerPos != null) drawArc(centerPos, maxRadius, 0.0, PI_CIRCLE, false, g, Color.white);  // kreis center->maxradius

		drawRobotPath(g);
		// drawCircleGeometry(g);
		test(g);
	}

	private void test(Graphics2D g)
	{
		try
		{
			drawArc(robotPos, 200.0, 0.0, PI_CIRCLE, false, g, Color.BLUE);			// firecircle -> 200
			drawArc(robotPos, 100.0, 0.0, PI_CIRCLE, false, g, whiteTrans);			// null gravi circle

			for (Point2D guessPoint : guessPointsHead)
			{
				drawPoint(guessPoint, Color.RED, g);
			}
			for (Point2D guessPoint : guessPointsBack)
			{
				drawPoint(guessPoint, Color.GREEN, g);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		};
	}

	private void drawRobotPath(Graphics2D g)
	{
		if (robotPos == null) return;

		g.setColor(Color.GREEN);
		if (pathThinner % 5 == 0)
		{
			robotPath = addPathSegment(robotPath, robotPos);
		}
		if (robotPath != null) g.draw(robotPath);						// robot path line
		pathThinner++;
	}

	// ---------------------------------------------- Register and other functions -------------------------------------------------

	/**
	 * Because the painter is a singleton, we have to reset the graphic objects every round
	 */
	public void resetPainter()
	{
		robotPath = null;
	}

	// --------------------------------------------------- Graphic Helper functions ---------------------------------------------------------------

	private void drawArc(Point2D start, double distance, double startAngle, double arcAngle, boolean fill, Graphics2D g2, Color color)
	{
		int r2 = (int) (distance * 2);
		int x = (int) (start.getX() - distance);
		int y = (int) (start.getY() - distance);

		g2.setColor(color);
		if (fill) g2.fillArc(x, y, r2, r2, (int) Math.toDegrees(startAngle), (int) Math.toDegrees(arcAngle));
		else g2.drawArc(x, y, r2, r2, (int) Math.toDegrees(startAngle), (int) Math.toDegrees(arcAngle));
	}

	private GeneralPath addPathSegment(GeneralPath path, Point2D point)
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

	public void drawLine(Point2D start, Point2D ende, Graphics2D g2, Color color)
	{
		g2.setColor(color);
		g2.drawLine((int) start.getX(), (int) start.getY(), (int) ende.getX(), (int) ende.getY());
	}

	public void drawPoint(Point2D point, Color color, Graphics2D g2)
	{
		Stroke old = g2.getStroke();
		g2.setStroke(new BasicStroke(5));
		g2.setColor(color);
		g2.drawLine((int) point.getX(), (int) point.getY(), (int) point.getX(), (int) point.getY());
		g2.setStroke(old);
	}

}
