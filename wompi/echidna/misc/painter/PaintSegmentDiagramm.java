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
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;

import robocode.AdvancedRobot;
import wompi.paint.PaintHelper;

public class PaintSegmentDiagramm
{
	static long	lastTime;

	public static void onPaint(Graphics2D g, AdvancedRobot myRobot, double[] segmentField, Color myColor)
	{
		if (lastTime > myRobot.getTime())
		{}
		lastTime = myRobot.getTime();

		GeneralPath aPath = new GeneralPath(GeneralPath.WIND_EVEN_ODD, 2000);
		aPath.moveTo(0, 0);
		double max = Double.MIN_VALUE;
		double min = Double.MAX_VALUE;
		int count = 0;
		for (double seg : segmentField)
		{
			max = Math.max(max, seg);
			min = Math.min(min, seg);
			aPath.lineTo(count, seg);
			count++;
		}

		double x = myRobot.getBattleFieldWidth() * 0.1;
		double y = myRobot.getBattleFieldHeight() * 0.1;
		Rectangle2D pArea = new Rectangle2D.Double(x, y, x, y);

		Rectangle2D pathBound = aPath.getBounds2D();
		AffineTransform transform = new AffineTransform();
		double scaleY = pArea.getHeight() / (2. / 36.);
		double scaleX = pArea.getWidth() / pathBound.getWidth();

		transform.translate(pArea.getX(), pArea.getY() - scaleY * pathBound.getY());
		transform.scale(scaleX, scaleY);

		g.setColor(myColor);
		g.draw(transform.createTransformedShape(aPath));
		g.setColor(Color.DARK_GRAY);
		g.draw(pArea);

		g.setColor(myColor);
		g.setFont(PaintHelper.myFont);
		g.drawString(String.format("%3.10f %s", max, myRobot.getName()), (int) (pArea.getMinX()),
				(int) (pArea.getMaxY() + 3));

	}
}
