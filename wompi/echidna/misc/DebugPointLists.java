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
import java.awt.geom.Point2D;
import java.util.ArrayList;

import wompi.wallaby.PaintHelper;

public class DebugPointLists
{
	public ArrayList<Point2D>	goodPoints	= new ArrayList<Point2D>();
	public ArrayList<Point2D>	badPoints	= new ArrayList<Point2D>();
	public Point2D				targetPoint;

	public DebugPointLists()
	{

	}

	public void reset()
	{
		goodPoints.clear();
		badPoints.clear();
		targetPoint = null;
	}

	public void onPaint(Graphics2D g)
	{
		for (Point2D point : goodPoints)
		{
			PaintHelper.drawPoint(point, Color.GREEN, g, 2);
		}

		for (Point2D point : badPoints)
		{
			PaintHelper.drawPoint(point, Color.RED, g, 10);
		}

		if (targetPoint != null) PaintHelper.drawPoint(targetPoint, Color.ORANGE, g, 5);
	}
}
