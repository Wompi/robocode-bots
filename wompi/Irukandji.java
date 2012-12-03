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
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import robocode.AdvancedRobot;
import robocode.BulletHitEvent;
import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.RobotDeathEvent;
import robocode.RobotStatus;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.StatusEvent;
import robocode.util.Utils;
import wompi.echidna.misc.painter.PaintRobotPath;
import wompi.robomath.RobotMath;
import wompi.teststuff.WompiSim;
import wompi.wallaby.PaintHelper;

public class Irukandji extends AdvancedRobot
{
	private final static double	RADAR				= 1.9;
	private final static double	DELTA_P				= Math.PI / 45;
	private final static double	PI_360				= Math.PI * 2;
	private final static double	PI_90				= Math.PI / 2;
	private final static double	PI_45				= Math.PI / 4;

	private static Rectangle2D	bField;

	// debug
	private final List<Point2D>	myChasePoints		= new ArrayList<Point2D>();
	private final List<Point2D>	myWompiFrontPoints	= new ArrayList<Point2D>();
	private final List<Point2D>	myWompiBackPoints	= new ArrayList<Point2D>();
	private final List<Point2D>	myIntersectPoints	= new ArrayList<Point2D>();
	private final List<Point2D>	myTargetGoodPoints	= new ArrayList<Point2D>();
	private final List<Point2D>	myTargetWallPoints	= new ArrayList<Point2D>();

	Point2D						myLastPosition;
	Point2D						myPosition;
	double						bPower;
	double						lastHead;

	IruTarget					myTarget			= new IruTarget();

	public Irukandji()
	{}

	@Override
	public void run()
	{
		setAllColors(Color.RED);
		myLastPosition = myPosition = new Point2D.Double(getX(), getY());
		myTarget.eEnergy = getEnergy();
		myTarget.eGunHeat = getGunHeat();
		bField = new Rectangle2D.Double(17d, 17d, getBattleFieldWidth() - 34d, getBattleFieldHeight() - 34d);
		setEventPriority("StatusEvent", 85);
	}

	@Override
	public void onStatus(StatusEvent e)
	{
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

//		if (myTarget.isShooting(e.getEnergy()))
//		{
//			addCustomEvent(new Condition()
//			{
//				Point2D	start	= new Point2D.Double(myTarget.x, myTarget.y);
//				double	head	= RobotMath.calculateAngle(start, myLastPosition);
//				double	ePower	= myTarget.ePower;
//				long	count	= 1;
//
//				@Override
//				public boolean test()
//				{
//					Point2D end = RobotMath.calculatePolarPoint(head, ++count * Rules.getBulletSpeed(ePower), start);
//					PaintHelper.drawArc(start, count * Rules.getBulletSpeed(ePower), 0, PI_360, false, getGraphics(),
//							Color.DARK_GRAY);
//					PaintHelper.drawLine(start, end, getGraphics(), Color.DARK_GRAY);
//					PaintTargetSquare.drawTargetSquare(getGraphics(), 0, end.getX(), end.getY(), false, Color.GRAY);
//					for (int i = 2; i < 40; i++)
//					{
//						Point2D p = RobotMath.calculatePolarPoint(head, i * Rules.getBulletSpeed(ePower), start);
//						PaintHelper.drawPoint(p, Color.BLUE, getGraphics(), 2);
//					}
//					if (count > 40) removeCustomEvent(this);
//					return false;
//				}
//			});
//		}
		myTarget.x = myPosition.getX() + Math.sin(aBear) * e.getDistance();
		myTarget.y = myPosition.getY() + Math.cos(aBear) * e.getDistance();
		myTarget.eDistance = e.getDistance();
		myTarget.eHeading = e.getHeadingRadians();
		myTarget.eVelocity = e.getVelocity();

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
				if (goTo == null
						|| end.distance(myPosition.getX(), myPosition.getY()) < goTo.distance(myPosition.getX(),
								myPosition.getY()))
				{
					goTo = end;
				}
			}
			h += PI_90;
		}
//		PaintHelper.drawLine(myPosition, goTo, getGraphics(), Color.LIGHT_GRAY);

		double angle = Math.atan2(goTo.getX() - myPosition.getX(), goTo.getY() - myPosition.getY());

		// /nanoDebug(getHeadingRadians(), getX(), getY(), getVelocity(), 30,
		// 8.0, goTo.getX(), goTo.getY());
		// debug(getHeadingRadians(), getX(), getY(), getVelocity(), 30, 8.0,
		// goTo.getX(), goTo.getY());

		// nanoDebug(e.getHeadingRadians(), x, y, e.getVelocity(), 30, 8.0,
		// angle, x, y);

		// debug(e.getHeadingRadians(), x, y, e.getVelocity());
		setTurnRightRadians(Math.tan(angle -= getHeadingRadians()));
		setAhead(100 * Math.cos(angle));

		if (getGunTurnRemainingRadians() == 0)
		{
			setFire(bPower);
		}

		bPower = Math.min(2.99, Math.max(0.1, Math.min(e.getEnergy() / 4.0, 300 / e.getDistance())));
		// bPower = 0.1;

		double v2 = e.getVelocity();
		double h1 = e.getHeadingRadians();
		double i = 0;
		double xg = myTarget.x - myPosition.getX();
		double yg = myTarget.y - myPosition.getY();

		double h0 = WompiSim.limit(Rules.getTurnRateRadians(v2),
				Utils.normalRelativeAngle(e.getHeadingRadians() - lastHead));
		double dist = 0;
		while ((dist = (++i * Rules.getBulletSpeed(bPower))) < Math.hypot(xg, yg))
		{
			v2 = WompiSim.nextVeocity(v2, e.getVelocity(), Rules.MAX_VELOCITY);
			xg += Math.sin(h1) * v2;
			yg += Math.cos(h1) * v2;

			Point2D tPoint = new Point2D.Double(xg + myPosition.getX(), yg + myPosition.getY());

			if (!bField.contains(tPoint))
			{
				v2 = -v2;
				PaintHelper.drawPoint(tPoint, Color.MAGENTA, getGraphics(), 2);
			}
			else
			{
				Rectangle2D rect = new Rectangle2D.Double(tPoint.getX() - 18.0, tPoint.getY() - 18.0, 36.0, 36.0);

				double tangle = RobotMath.calculateAngle(myPosition, tPoint);
				Point2D lP = RobotMath.calculatePolarPoint(tangle, dist, myPosition);
				Line2D line = new Line2D.Double(myPosition, lP);

				if (rect.intersectsLine(line))
				{
					break;
				}

				PaintHelper.drawPoint(tPoint, Color.ORANGE, getGraphics(), 2);
			}
			h1 += WompiSim.limit(Rules.getTurnRateRadians(v2), Utils.normalRelativeAngle(h0));
		}
		setTurnGunRightRadians(Utils.normalRelativeAngle(Math.atan2(xg, yg) - getGunHeadingRadians()));

		PaintHelper.drawLine(myPosition, new Point2D.Double(xg + myPosition.getX(), yg + myPosition.getY()),
				getGraphics(), Color.BLUE);

		// setTurnRightRadians(Math.tan(angle =
		// Utils.normalRelativeAngle(Math.atan2(xg, yg)) -
		// getHeadingRadians()));
		// setAhead(100 * Math.cos(angle));

		lastHead = e.getHeadingRadians();
	}

	private void myDebug(double head, Point2D source, Point2D destination, int direction, double v, long ticks,
			double maxv, double power, double angle, List<Point2D> debugList)
	{
		WompiSim.h = head;
		WompiSim.v = v;
		WompiSim.x = source.getX();
		WompiSim.y = source.getY();

		int i = 0;
		double dist;
		double delta = (direction < 0) ? Math.PI : 0;
		while ((dist = (i++ * Rules.getBulletSpeed(power))) < destination.distance(WompiSim.x, WompiSim.y))
		{

			Point2D pg = RobotMath.calculatePolarPoint(angle, dist, source);
			WompiSim.simulate(Math.atan2(pg.getX() - WompiSim.x, pg.getY() - WompiSim.y) - WompiSim.h + delta,
					direction, maxv);

			Point2D simPoint = new Point2D.Double(WompiSim.x, WompiSim.y);
			Rectangle2D rect = new Rectangle2D.Double(WompiSim.x - 18.0, WompiSim.y - 18.0, 36.0, 36.0);

			double tangle = RobotMath.calculateAngle(destination, simPoint);
			Point2D tPoint = RobotMath.calculatePolarPoint(tangle, dist, destination);
			Line2D line = new Line2D.Double(destination, tPoint);

			if (bField.contains(simPoint))
			{
				if (!rect.intersectsLine(line))
				{
					debugList.add(simPoint);
				}
				else
				{
					myIntersectPoints.add(simPoint);
				}
			}
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

		PaintRobotPath.onPaint(g, "", getTime(), myTarget.x, myTarget.y, Color.GRAY);
//		WompiPaint.paintWallDistance(getX(), getY(), 0, g, getBattleFieldWidth(), getBattleFieldHeight());
//		WompiPaint.paintWallDistance(myTarget.x, myTarget.y, 0, getGraphics(), getBattleFieldWidth(),
//				getBattleFieldHeight());

		int ticks = (int) (myTarget.eDistance / Rules.getBulletSpeed(bPower));
		int eTicks = (int) (myTarget.eDistance / Rules.getBulletSpeed(myTarget.ePower));
		System.out.format("tick=%d eTick=%d speed=%3.2f\n", ticks, eTicks, Rules.getBulletSpeed(myTarget.ePower));

		if (ticks > 0)
		{
			double angle = -DELTA_P;
			while ((angle += DELTA_P) < PI_360)
			{
				// Point2D pg = RobotMath.calculatePolarPoint(angle, eTicks *
				// 8.0, new Point2D.Double(getX(), getY()));
				// nanoDebug(getHeadingRadians(), getX(), getY(), getVelocity(),
				// eTicks, 8.0, pg.getX(), pg.getY(), myTarget.ePower);

				myDebug(myTarget.eHeading, myTarget, myPosition, 1, myTarget.eVelocity, ticks, 8.0, bPower, angle,
						myWompiFrontPoints);
//				myDebug(myTarget.eHeading, myTarget, myPosition, -1, myTarget.eVelocity, ticks, 8.0, bPower, angle,
//						myWompiBackPoints);
//				myDebug(getHeadingRadians(), myPosition, myTarget, 1, getVelocity(), eTicks, 8.0, myTarget.ePower,
//						angle, myWompiFrontPoints);
//				myDebug(getHeadingRadians(), myPosition, myTarget, -1, getVelocity(), eTicks, 8.0, myTarget.ePower,
//						angle, myWompiBackPoints);

			}
			PaintHelper.drawArc(myTarget, ticks * Math.abs(myTarget.eVelocity), 0, PI_360, false, g, Color.DARK_GRAY);
		}
		// PaintHelper.drawArc(new Point2D.Double(getX(), getY()), eTicks * 8.0,
		// 0, PI_360, false, g, Color.DARK_GRAY);
		// paint wall distance

		for (Point2D point : myWompiFrontPoints)
		{
			// PaintTargetSquare.drawTargetSquare(getGraphics(), 0,
			// point.getX(), point.getY(), false, Color.GRAY);
			PaintHelper.drawPoint(point, Color.RED, g, 1);
		}
		myWompiFrontPoints.clear();

		for (Point2D point : myWompiBackPoints)
		{
			// PaintTargetSquare.drawTargetSquare(getGraphics(), 0,
			// point.getX(), point.getY(), false, Color.GRAY);
			PaintHelper.drawPoint(point, Color.GREEN, g, 1);
		}
		myWompiBackPoints.clear();

		for (Point2D point : myIntersectPoints)
		{
			PaintHelper.drawPoint(point, Color.GRAY, g, 1);
		}
		myIntersectPoints.clear();

		// for (Point2D point : myChasePoints)
		// {
		// PaintHelper.drawPoint(point, Color.GREEN, g, 2);
		// }
		// myChasePoints.clear();

	}
}

class IruTarget extends Point2D.Double
{
	private static final long	serialVersionUID	= 1796092649355538977L;

	double						eEnergy;
	double						eGunHeat;
	double						ePower;
	double						eDistance;
	double						eHeading;
	double						eVelocity;

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
