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
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import wompi.paint.PaintHelper;

public class PaintMinRiskPoints
{
	ArrayList<RiskPoint>	riskPoints	= new ArrayList<RiskPoint>();
	double					maxRisk		= Double.MIN_VALUE;
	double					minRisk		= Double.MAX_VALUE;
	long					lastTime;

	public void registerRiskPoint(long time, double riskX, double riskY, double rate, double rx, double ry,
			double paintDist)
	{
		if (time != lastTime)
		{
			riskPoints.clear();
			maxRisk = Double.MIN_VALUE;
			minRisk = Double.MAX_VALUE;
			lastTime = time;
		}
		maxRisk = Math.max(maxRisk, rate);
		minRisk = Math.min(minRisk, rate);

		Point2D botPos = new Point2D.Double(rx, ry);
		Point2D riskPos = new Point2D.Double(riskX, riskY);

//		double angle = Math.atan2(riskPos.getX() - botPos.getX(), riskPos.getY() - botPos.getY());
//
//		double _x = botPos.getX() + Math.sin(angle) * paintDist;
//		double _y = botPos.getY() + Math.cos(angle) * paintDist;
//
//		Point2D paintPoint = new Point2D.Double(_x, _y);
		Point2D paintPoint = riskPos;

		RiskPoint rP = new RiskPoint();
		rP.riskPoint = paintPoint;
		rP.risk = rate;
		riskPoints.add(rP);
	}

	public void onPaint(Graphics2D g2, boolean showValues)
	{
		for (RiskPoint riskPoint : riskPoints)
		{

			float factor = (float) ((riskPoint.risk - minRisk) / (maxRisk - minRisk));
			// if (factor == 0) continue;
			// System.out.format("max=%3.2f min=%3.2f value=%3.2f factor=%3.2f \n", maxRisk,minRisk,riskPoint.risk,factor);

			float r = 1;
			float g = 1;
			float b = 0;
			if (factor != 1)
			{
				r = factor;
				g = 0;
				b = (1 - factor);
			}

			Color color;
			if (riskPoint.risk == minRisk)
			{
				color = Color.CYAN;
			}
			else
			{
				color = new Color(r, g, b);
			}

			if (!showValues)
			{
				PaintHelper.drawPoint(riskPoint.riskPoint, color, g2, 5);
			}
			else
			{
				g2.setColor(color);
				Font myFont = new Font("Dialog", Font.PLAIN, 10);
				g2.setFont(myFont);
				g2.drawString(String.format("%3.5f", riskPoint.risk), (int) riskPoint.riskPoint.getX() - 10,
						(int) riskPoint.riskPoint.getY() - 5);
			}
		}
	}
}

class RiskPoint
{
	Point2D	riskPoint;
	double	risk;
}
