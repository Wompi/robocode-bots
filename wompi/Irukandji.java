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
package wompi;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import robocode.AdvancedRobot;
import robocode.BulletHitEvent;
import robocode.Condition;
import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.RobotDeathEvent;
import robocode.RobotStatus;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.StatusEvent;
import robocode.util.Utils;
import wompi.echidna.misc.painter.PaintTargetSquare;
import wompi.paint.WompiPaint;
import wompi.robomath.RobotMath;
import wompi.teststuff.NatSim;
import wompi.teststuff.NatSim.PredictionStatus;
import wompi.teststuff.WompiSim;
import wompi.wallaby.PaintHelper;

public class Irukandji extends AdvancedRobot
{
	private final static double				RADAR				= 1.9;
	private final static double				DELTA_P				= Math.PI / 45;
	private final static double				PI_360				= Math.PI * 2;
	private final static double				PI_90				= Math.PI / 2;
	private final static double				PI_45				= Math.PI / 4;

	private static Rectangle2D				bField;

	// debug
	private final List<PredictionStatus>	myPoints			= new ArrayList<NatSim.PredictionStatus>();
	private final List<Point2D>				myChasePoints		= new ArrayList<Point2D>();
	private final List<Point2D>				myWompiFrontPoints	= new ArrayList<Point2D>();
	private final List<Point2D>				myWompiBackPoints	= new ArrayList<Point2D>();

	Point2D									myLastPosition;
	Point2D									myPosition;
	double									bPower;
	double									lastHead;

	IruTarget								myTarget			= new IruTarget();

	public Irukandji()
	{}

	@Override
	public void run()
	{
		setAllColors(Color.RED);
		myLastPosition = myPosition = new Point2D.Double(getX(), getY());
		myTarget.eEnergy = getEnergy();
		myTarget.eGunHeat = getGunHeat();
	}

	@Override
	public void onStatus(StatusEvent e)
	{
		if (bField == null) bField = new Rectangle2D.Double(18d, 18d, getBattleFieldWidth() - 36d, getBattleFieldHeight() - 36d);
		setTurnRadarRightRadians(Double.POSITIVE_INFINITY);
		myLastPosition = myPosition;
		myPosition = new Point2D.Double(getX(), getY());
	}

	double	delta;

	@Override
	public void onScannedRobot(ScannedRobotEvent e)
	{
		double aBear = getHeadingRadians() + e.getBearingRadians();
		setTurnRadarRightRadians(Utils.normalRelativeAngle(aBear - getRadarHeadingRadians()) * RADAR);

		if (myTarget.isShooting(e.getEnergy()))
		{
			addCustomEvent(new Condition()
			{
				Point2D	start	= new Point2D.Double(myTarget.x, myTarget.y);
				double	head	= RobotMath.calculateAngle(start, myLastPosition);
				double	ePower	= myTarget.ePower;
				long	count	= 1;

				@Override
				public boolean test()
				{
					Point2D end = RobotMath.calculatePolarPoint(head, ++count * Rules.getBulletSpeed(ePower), start);
					PaintHelper.drawArc(start, count * Rules.getBulletSpeed(ePower), 0, PI_360, false, getGraphics(), Color.DARK_GRAY);
					PaintHelper.drawLine(start, end, getGraphics(), Color.DARK_GRAY);
					PaintTargetSquare.drawTargetSquare(getGraphics(), 0, end.getX(), end.getY(), false, Color.GRAY);
					for (int i = 2; i < 40; i++)
					{
						Point2D p = RobotMath.calculatePolarPoint(head, i * Rules.getBulletSpeed(ePower), start);
						PaintHelper.drawPoint(p, Color.BLUE, getGraphics(), 2);
					}
					if (count > 40) removeCustomEvent(this);
					return false;
				}
			});
		}
		myTarget.x = getX() + Math.sin(aBear) * e.getDistance();
		myTarget.y = getY() + Math.cos(aBear) * e.getDistance();

		double factor = 200 + delta;
		if (getTime() % 10 == 0) delta = Math.random() * 150;
		double h = e.getHeadingRadians() + PI_45;
		if (e.getDistance() < 65) h += PI_45;

		Point2D goTo = null;
		for (int i = 0; i < 4; i++)
		{
			Point2D end = RobotMath.calculatePolarPoint(h, factor, myTarget);
			if (bField.contains(end))
			{
				if (goTo == null || end.distance(getX(), getY()) < goTo.distance(getX(), getY()))
				{
					goTo = end;
				}
			}
			h += PI_90;
		}
		PaintHelper.drawLine(new Point2D.Double(getX(), getY()), goTo, getGraphics(), Color.LIGHT_GRAY);

		double angle = Math.atan2(goTo.getX() - getX(), goTo.getY() - getY());

		nanoDebug(getHeadingRadians(), getX(), getY(), getVelocity(), 30, 8.0, goTo.getX(), goTo.getY());
		//debug(getHeadingRadians(), getX(), getY(), getVelocity(), 30, 8.0, goTo.getX(), goTo.getY());

		//nanoDebug(e.getHeadingRadians(), x, y, e.getVelocity(), 30, 8.0, angle, x, y);

		//debug(e.getHeadingRadians(), x, y, e.getVelocity());
		setTurnRightRadians(Math.tan(angle -= getHeadingRadians()));
		setAhead(100 * Math.cos(angle));

		if (getGunTurnRemainingRadians() == 0)
		{
			setFire(bPower);
		}

		bPower = Math.min(2.99, Math.max(0.1, Math.min(e.getEnergy() / 4.0, 300 / e.getDistance())));

		double v2 = e.getVelocity();
		double h1 = e.getHeadingRadians();
		double i = 0;
		double xg = myTarget.x - getX();
		double yg = myTarget.y - getY();

		double h0 = e.getHeadingRadians() - lastHead;
		while (++i * Rules.getBulletSpeed(bPower) < Math.hypot(xg, yg))
		{
			if (!new Rectangle2D.Double(17.9, 17.9, getBattleFieldWidth() - 2 * 17.9, getBattleFieldHeight() - 2 * 17.9).contains(
					(xg += (Math.sin(h1) * v2)) + getX(), (yg += (Math.cos(h1) * v2)) + getY()))
			{
				v2 = -v2;
			}
			h1 += h0;
		}
		setTurnGunRightRadians(Utils.normalRelativeAngle(Math.atan2(xg, yg) - getGunHeadingRadians()));
		lastHead = e.getHeadingRadians();
	}

	private void nanoDebug(double head, double x, double y, double v, long ticks, double maxv, double xg, double yg)
	{

		WompiSim.h = head;
		WompiSim.v = v;
		WompiSim.x = x;
		WompiSim.y = y;
		for (int i = 0; i < ticks; i++)
		{
			WompiSim.simulate(Math.atan2(xg - WompiSim.x, yg - WompiSim.y) - WompiSim.h, 1, maxv);
			if (bField.contains(WompiSim.x, WompiSim.y)) myWompiFrontPoints.add(new Point2D.Double(WompiSim.x, WompiSim.y));
		}

		WompiSim.h = head;
		WompiSim.v = v;
		WompiSim.x = x;
		WompiSim.y = y;

		for (int i = 0; i < ticks; i++)
		{
			WompiSim.simulate(Math.atan2(xg - WompiSim.x, yg - WompiSim.y) - WompiSim.h + Math.PI, -1, maxv);
			if (bField.contains(WompiSim.x, WompiSim.y)) myWompiBackPoints.add(new Point2D.Double(WompiSim.x, WompiSim.y));
		}
	}

	@Override
	public void onHitWall(HitWallEvent event)
	{}

	@Override
	public void onRobotDeath(RobotDeathEvent e)
	{}

	@Override
	public void onHitByBullet(HitByBulletEvent e)
	{
		myTarget.eEnergy += 3.0 * e.getBullet().getPower();
	}

	@Override
	public void onBulletHit(BulletHitEvent e)
	{
		myTarget.eEnergy -= Rules.getBulletDamage(e.getBullet().getPower());
	}

	public void onHitRobot(HitRobotEvent e, RobotStatus status)
	{}

	@Override
	public void onPaint(Graphics2D g)
	{

		WompiPaint.paintWallDistance(getX(), getY(), 0, g, getBattleFieldWidth(), getBattleFieldHeight());
		WompiPaint.paintWallDistance(myTarget.x, myTarget.y, 0, getGraphics(), getBattleFieldWidth(), getBattleFieldHeight());

		// paint wall distance

		for (Point2D point : myWompiFrontPoints)
		{
			PaintHelper.drawPoint(point, Color.RED, g, 3);
		}
		myWompiFrontPoints.clear();

		for (Point2D point : myWompiBackPoints)
		{
			PaintHelper.drawPoint(point, Color.GREEN, g, 3);
		}
		myWompiBackPoints.clear();

		//		for (Point2D point : myChasePoints)
		//		{
		//			PaintHelper.drawPoint(point, Color.GREEN, g, 2);
		//		}
		//		myChasePoints.clear();

	}
}

class IruTarget extends Point2D.Double
{
	private static final long	serialVersionUID	= 1796092649355538977L;

	double						eEnergy;
	double						eGunHeat;
	double						ePower;

	public boolean isShooting(double energy)
	{
		boolean result = false;
		double diff = eEnergy - energy;
		if (diff >= 0.1 && diff <= 3.0)
		{
			System.out.format("he fired %f\n", diff);
			ePower = diff;
			result = true;
		}
		eEnergy = energy;
		return result;
	}
}
