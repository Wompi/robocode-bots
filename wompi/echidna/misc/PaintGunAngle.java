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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;

import robocode.AdvancedRobot;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import wompi.echidna.misc.painter.PaintMaxAngle;
import wompi.paint.PaintHelper;

public class PaintGunAngle
{
	public static Color												whiteTrans	= new Color(0xFF, 0xFF, 0xFF, 0x50);
	public static Color												greenTrans	= new Color(0x00, 0xFF, 0x00, 0x50);

	private static HashMap<AdvancedRobot, ArrayList<GunAngleWave>>	waveMap		= new HashMap<AdvancedRobot, ArrayList<GunAngleWave>>();

	public static void onScannedRobot(ScannedRobotEvent e, AdvancedRobot bot, double bulletPower, double aimAngle, boolean showWaveGraphics,
			boolean hasFired)
	{
		double absBearing = bot.getHeadingRadians() + e.getBearingRadians();
		double ex = bot.getX() + Math.sin(absBearing) * e.getDistance();
		double ey = bot.getY() + Math.cos(absBearing) * e.getDistance();

		ArrayList<GunAngleWave> myWaves = waveMap.get(bot);
		if (myWaves == null) waveMap.put(bot, myWaves = new ArrayList<GunAngleWave>());
		for (int i = 0; i < myWaves.size(); i++)
		{
			GunAngleWave cWave = myWaves.get(i);
			if (cWave.check(ex, ey, bot.getX(), bot.getY()))
			{
				// if ( (cWave.isLocked && cWave.fadeCount-- < 2))
				{
					myWaves.remove(i);
				}
			}
		}

		if (hasFired)
		{
			GunAngleWave wave = new GunAngleWave();
			wave.myBot = bot;
			wave.showWave = showWaveGraphics;
			wave.aimPoint = new Point2D.Double(Math.sin(aimAngle) * e.getDistance(), Math.cos(aimAngle) * e.getDistance());
			wave.botPoint = new Point2D.Double(bot.getX(), bot.getY());
			wave.bSpeed = Rules.getBulletSpeed(bulletPower);
			wave.eAbsBearing = absBearing;
			myWaves.add(wave);
		}
	}
}

class GunAngleWave
{
	AdvancedRobot	myBot;
	Point2D			botPoint;
	Point2D			aimPoint;
	double			bSpeed;

	boolean			showWave;
	double			eAbsBearing;
	int				fadeCount	= 10;

	private double	count;

	public boolean check(double ex, double ey, double botx, double boty)
	{
		double bulletDist = ++count * bSpeed;

		int r2 = (int) (bulletDist * 2);
		int x = (int) (botPoint.getX() - bulletDist);
		int y = (int) (botPoint.getY() - bulletDist);

		if (botPoint.distance(ex, ey) < bulletDist)
		{
			myBot.getGraphics().setColor(PaintGunAngle.greenTrans);
			myBot.getGraphics().drawArc(x, y, r2, r2, 0, 360);
			myBot.getGraphics().setColor(new Color(0xFF, 0xFF, 0x00, (int) (25.0 * fadeCount)));
			myBot.getGraphics().drawLine((int) botPoint.getX(), (int) botPoint.getY(), (int) (ex), (int) (ey));
			myBot.getGraphics().drawLine((int) botPoint.getX(), (int) botPoint.getY(), (int) (aimPoint.getX() + botPoint.getX()),
					(int) (aimPoint.getY() + botPoint.getY()));

			PaintHelper.drawString(myBot.getGraphics(), String.format("%3.2f", bSpeed), botPoint.getX() + 10, botPoint.getY() + 10, Color.RED);

			PaintMaxAngle.onPaint(myBot.getGraphics(), botPoint.getX(), botPoint.getY(), Math.asin(8 / bSpeed), eAbsBearing, Color.BLUE);
			return true;
		}

		if (showWave)
		{
			myBot.getGraphics().setColor(PaintGunAngle.whiteTrans);
			myBot.getGraphics().drawArc(x, y, r2, r2, 0, 360);
		}

		Stroke old = myBot.getGraphics().getStroke();
		myBot.getGraphics().setStroke(new BasicStroke(3));
		myBot.getGraphics().setColor(Color.RED);
		myBot.getGraphics().drawLine((int) botPoint.getX(), (int) botPoint.getY(), (int) botPoint.getX(), (int) botPoint.getY());
		myBot.getGraphics().setColor(Color.GREEN);
		int x1 = (int) (aimPoint.getX() + botPoint.getX());
		int y1 = (int) (aimPoint.getY() + botPoint.getY());
		myBot.getGraphics().drawLine(x1, y1, x1, y1);
		myBot.getGraphics().setStroke(old);
		return false;
	}
}
