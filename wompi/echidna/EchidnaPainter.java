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
package wompi.echidna;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;

import wompi.paint.PaintHelper;

public class EchidnaPainter
{
	static long	time;

	public static void drawRobotStats(Graphics2D g, double velocity, double heading, double turnRemain, Point2D robotPos)
	{
		int showX = 200;
		int showY = 900;
		int count = 0;

		g.setFont(PaintHelper.myFont);
		g.setColor(Color.LIGHT_GRAY);
		g.drawString(
				String.format("V: %+3.5f H: %3.2f TR: %3.2f", velocity, Math.toDegrees(heading),
						Math.toDegrees(turnRemain)), showX, showY - count++);

		PaintHelper.drawArc(robotPos, 50.0, 0, PaintHelper.PI_CIRCLE, false, g, Color.LIGHT_GRAY);
		PaintHelper.drawArc(robotPos, 50.0, heading, turnRemain, true, g, PaintHelper.redTrans);

	}
}
