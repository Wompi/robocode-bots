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
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import robocode.RobotDeathEvent;
import robocode.RobotStatus;
import wompi.echidna.misc.utils.EchidnaBattleField;
import wompi.wallaby.PaintHelper;

public class PaintDiagramm
{
	private static Rectangle2D	pArea;
	private GeneralPath			aPath;
	private long				lastTime;
	private ArrayList<Line2D>	deathTimes	= new ArrayList<Line2D>();

	double						maxValue	= Double.MIN_VALUE;
	double						minValue	= Double.MAX_VALUE;

	public void onRobotDeath(RobotDeathEvent e)
	{
		deathTimes.add(new Line2D.Double(e.getTime(), 0, e.getTime(), 0));
	}

	public void onPaint(Graphics2D g, RobotStatus status, double value, Color myColor, String name)
	{
		if (lastTime > status.getTime())
		{
			aPath = null;  // new battle reset
			deathTimes.clear();
		}
		lastTime = status.getTime();

		if (aPath == null)
		{
			double x = EchidnaBattleField.BATTLE_FIELD_W * 0.1;
			double y = EchidnaBattleField.BATTLE_FIELD_H * 0.1;
			pArea = new Rectangle2D.Double(x, y, x * 5, y);

			aPath = new GeneralPath(GeneralPath.WIND_EVEN_ODD, 2000);
			aPath.moveTo(0, 0);
		}

		if (status.getTime() % 5 == 0)
		{
			// aPath.lineTo(myRobot.getTime(), myRobot.getEnergy());
			// aPath.lineTo(myRobot.getTime(), myRobot.getVelocity());
			aPath.lineTo(status.getTime(), value);
		}

		// System.out.format("Area: %s\n", pArea.toString());

		Rectangle2D pathBound = aPath.getBounds2D();
		// System.out.format("PathBound: %s\n", pathBound.toString());
		AffineTransform transform = new AffineTransform();
		double scaleY = pArea.getHeight() / pathBound.getHeight();
		double scaleX = pArea.getWidth() / pathBound.getWidth();

		transform.translate(pArea.getX(), pArea.getY() - scaleY * pathBound.getY());
		transform.scale(scaleX, scaleY);

		// g.setColor(PaintHelper.greenTrans);
		g.setColor(myColor);
		g.draw(transform.createTransformedShape(aPath));

		g.setColor(Color.RED);
		for (Line2D dead : deathTimes)
		{
			dead.setLine(dead.getX1(), 0 + pathBound.getY(), dead.getX1(), pathBound.getHeight() + pathBound.getY());
			g.draw(transform.createTransformedShape(dead));
		}
		g.setColor(PaintHelper.whiteTrans);
		g.fill(pArea);
		g.setColor(Color.DARK_GRAY);
		g.draw(pArea);

		g.setColor(Color.GRAY);
		g.setFont(PaintHelper.myFont);
		g.drawString(String.format("%3.2f %s", maxValue = (Math.max(maxValue, value)), name), (int) (pArea.getMinX()), (int) (pArea.getMaxY() + 3));

	}
}
