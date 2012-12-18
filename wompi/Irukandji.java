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
import robocode.Bullet;
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
import wompi.numbat.debug.DebugBot;
import wompi.numbat.debug.DebugGunProperties;
import wompi.paint.PaintHelper;
import wompi.paint.WompiSimPaint;
import wompi.paint.WompiSimPaint.WSimData;
import wompi.robomath.RobotMath;
import wompi.teststuff.WompiSim;

public class Irukandji extends AdvancedRobot
{
	private final static double	RADAR				= 1.9;
	private final static double	PI_90				= Math.PI / 2;
	private final static double	PI_45				= Math.PI / 4;

	private static Rectangle2D	bField;

	// debug
	private final List<Point2D>	myChasePoints		= new ArrayList<Point2D>();
	private final List<Point2D>	myTargetGoodPoints	= new ArrayList<Point2D>();
	private final List<Point2D>	myTargetWallPoints	= new ArrayList<Point2D>();
	private final List<Bullet>	myBullets			= new ArrayList<Bullet>();

	Point2D						myLastPosition;
	Point2D						myPosition;
	double						bPower;
	double						lastHead;

	IruTarget					myTarget			= new IruTarget();
	double						dir					= 1;
	boolean						eShot;

	public Irukandji()
	{}

	@Override
	public void run()
	{
		DebugBot.init(this);
		WompiSimPaint.init(this);
		setAllColors(Color.RED);
		myLastPosition = myPosition = new Point2D.Double(getX(), getY());
		myTarget.eEnergy = getEnergy();
		myTarget.eGunHeat = getGunHeat();
		bField = new Rectangle2D.Double(17d, 17d, getBattleFieldWidth() - 34d, getBattleFieldHeight() - 34d);
	}

	@Override
	public void onStatus(StatusEvent e)
	{
		setTurnRadarRightRadians(Double.POSITIVE_INFINITY);
		myLastPosition = myPosition;
		myPosition = new Point2D.Double(getX(), getY());

		DebugGunProperties.execute();
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
		myTarget.eMaxVelocity = Math.max(myTarget.eMaxVelocity, Math.abs(e.getVelocity()));

		if (myTarget.isShooting(e.getEnergy())) dir = -dir;
		//setTurnRightRadians(Math.cos(e.getBearingRadians()));
		setTurnRightRadians(Math.cos(e.getBearingRadians() - (e.getDistance() - 160) * (getVelocity() / 2500)));
		setAhead(100 * dir);

		if (getGunTurnRemainingRadians() == 0)
		{
			Bullet b = setFireBullet(bPower);
			if (b != null)
			{
				DebugGunProperties.debugGunHitRate(b);
				myBullets.add(b);
			}
		}

		//bPower = Math.min(2.99, Math.max(0.1, Math.min(e.getEnergy() / 4.0, 350 / e.getDistance())));
		bPower = 0.1;
		//bPower = 3.0;

		double v2 = e.getVelocity();
		double h1 = e.getHeadingRadians();
		double i = 0;
		double xg = myTarget.x - myPosition.getX();
		double yg = myTarget.y - myPosition.getY();

		double h0 = WompiSim.limit(Rules.getTurnRateRadians(v2),
				Utils.normalRelativeAngle(e.getHeadingRadians() - lastHead));
		double dist = 0;
		// consider dist + 18 ; to save all the intersect operations (should be close enough)

		double dir = Math.signum(e.getVelocity());

		while ((dist = ((i++ * Rules.getBulletSpeed(bPower)))) < Math.hypot(xg, yg))
		{
			v2 = WompiSim.nextVeocity(v2, dir, myTarget.eMaxVelocity);
			xg += Math.sin(h1) * v2;
			yg += Math.cos(h1) * v2;

			Point2D tPoint = new Point2D.Double(xg + myPosition.getX(), yg + myPosition.getY());

			if (!bField.contains(tPoint))
			{
				v2 = 0;
				dir = -dir;
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

		lastHead = e.getHeadingRadians();
	}

	int	minTick	= 1000;
	int	maxTick	= 0;

	@Override
	public void onHitWall(HitWallEvent event)
	{
		dir = -dir;
	}

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

//		PaintRobotPath.onPaint(g, "", getTime(), myTarget.x, myTarget.y, Color.GRAY);
//		WompiPaint.paintWallDistance(getX(), getY(), 0, g, getBattleFieldWidth(), getBattleFieldHeight());
//		WompiPaint.paintWallDistance(myTarget.x, myTarget.y, 0, getGraphics(), getBattleFieldWidth(),
//				getBattleFieldHeight());

		WSimData data = new WompiSimPaint().new WSimData();
		data.bPos = myPosition;
		data.tPos = myTarget;
		data.bPower = bPower;
		data.eDistance = myTarget.eDistance;
		data.eHeading = myTarget.eHeading;
		data.eVelocity = myTarget.eVelocity;
		data.eMaxVelocity = myTarget.eMaxVelocity;
		data.bHeat = getGunHeat();
		data.bBullets = myBullets;

		WompiSimPaint.onPaint(g, data);

		WSimData bData = new WompiSimPaint().new WSimData();
		bData.bPos = myTarget;
		bData.tPos = myPosition;
		bData.bPower = myTarget.ePower;
		bData.eDistance = myTarget.eDistance;
		bData.eHeading = getHeadingRadians();
		bData.eVelocity = getVelocity();
		bData.eMaxVelocity = 8.0;
		bData.bHeat = getGunHeat();
		bData.bBullets = myBullets;

		WompiSimPaint.onPaint(g, bData);
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
	double						eMaxVelocity;

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
