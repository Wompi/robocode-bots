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
import java.util.HashMap;
import java.util.Iterator;

import robocode.AdvancedRobot;
import robocode.HitRobotEvent;
import robocode.RobotDeathEvent;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.StatusEvent;
import robocode.util.Utils;
import wompi.echidna.misc.painter.PaintMinRiskPoints;
import wompi.paint.PaintEscapePath;
import wompi.paint.PaintGunProfile;
import wompi.paint.PaintMyEscapePath;

/**
 * What the ... is a Wallaby? (See: http://en.wikipedia.org/wiki/Wallaby)
 * 
 * To keep track of what i have done, i update a little development diary at: (if you are keen to read this ... be prepared for very bad English (i'm
 * German) https://github.com/Wompi/robocode-bots/wiki/Wallaby
 * 
 * The official version history can be found at: http://robowiki.net/wiki/Walaby
 * 
 * If you want to talk about it - you find me at: http://robowiki.net/wiki/User:Wompi
 * 
 * @author Wompi
 * @date 08/08/2012
 */
public class Wallaby extends AdvancedRobot
{
	private static final double			FIELD_W				= 1000.0;
	private static final double			FIELD_H				= 1000.0;

	private static final double			WZ_G				= 18.0;
	private static final double			WZ_G_W				= FIELD_W - 2 * WZ_G;
	private static final double			WZ_G_H				= FIELD_H - 2 * WZ_G;

	private final static double			DIST				= 155;
	private final static double			DIST_REMAIN			= 20;

	private final static double			GUNLOCK				= 1.0;
	private final static double			TARGET_FORCE		= 55000;					// 100000 low dmg high surv - 10000 high dmg low surv  
	private final static double			TARGET_DISTANCE		= 450.0;					// 400 last best - shoot at TARGET_DISTANCE with bullet 1.0

	private final static double			PI_360				= Math.PI * 2.0;
	private final static double			PI_180				= Math.PI;
	private final static double			PI_90				= Math.PI / 2.0;
	private final static double			PI_30				= Math.PI / 6.0;
	private final static double			DELTA_RISK_ANGLE	= Math.PI / 32.0;
	private final static double			MAX_HEAD_DIFF		= 0.161442955809475;		// 9.25 degree
	private final static double			ENERGY_ADJUST		= 4.0;
	private final static double			INF					= Double.POSITIVE_INFINITY;
	private final static double			BMAX				= Rules.MAX_BULLET_POWER;
	private final static double			BMIN				= Rules.MIN_BULLET_POWER;

	private final static double			GUN_TURN_OFFSET		= 1.0;

	// index:  0:x 1:y 2:heading 3:avgVelocity 4:avgVelocityCounter 5: distance 6:avgHeading 7: avgHeadingCount
	static HashMap<String, double[]>	allTargets;

	static String						eName;
	static double						eRate;

	static double						ta;
	static double						tc;

	static double						bPower;
	static double						rDist;

	PaintGunProfile						myPaintGunProfile;
	PaintEscapePath						myPaintMaxEsc;
	PaintMyEscapePath					myPaintEscPath;
	PaintMinRiskPoints					myPaintMinRiskAll;

	public Wallaby()
	{
		myPaintGunProfile = new PaintGunProfile();
		myPaintMaxEsc = new PaintEscapePath();
		myPaintMinRiskAll = new PaintMinRiskPoints();
		myPaintEscPath = new PaintMyEscapePath();
	}

	@Override
	public void onStatus(StatusEvent e)
	{
		myPaintGunProfile.onStatus(e);
		myPaintMaxEsc.onStatus(e);
		myPaintEscPath.onStatus(e);
	}

	@Override
	public void onPaint(Graphics2D g)
	{

		//myPaintGunProfile.onPaint(g);
		myPaintMaxEsc.onPaint(g);
		//myPaintEscPath.onPaint(g);
		myPaintMinRiskAll.onPaint(g, false);
	}

	@Override
	public void run()
	{
		myPaintMaxEsc.onInit(this, 18.0);
		myPaintEscPath.onInit(this, 18.0);
		allTargets = new HashMap<String, double[]>();
		setAllColors(Color.RED); // 7 byte
		setAdjustGunForRobotTurn(true);
		//setAdjustRadarForGunTurn(true);
		//rDist = DIST;
		setTurnRadarRightRadians(eRate = INF);
	}

	long	lastTime;
	long	lastScanTime;
	double	lastX;
	double	lastY;

	@Override
	public void onScannedRobot(ScannedRobotEvent e)
	{
		//myPaintGunProfile.onScannedRobot(e);

		double[] enemy;
		double v0;
		double xg;
		double yg;
		double v1;
		double h0;
		double h1;
		double i;
		double r1;
		double rM;
		double v2;
		double x;
		double y;
		String name;
		if ((enemy = allTargets.get(name = e.getName())) == null)
		{
			allTargets.put(name, enemy = new double[6]);
		}
		xg = enemy[0] = Math.sin((rM = (getHeadingRadians() + e.getBearingRadians())))
				* (v0 = enemy[5] = e.getDistance());
		yg = enemy[1] = Math.cos(rM) * v0;
		v2 = ((enemy[3] += (Math.abs(v1 = e.getVelocity()))) * Math.signum(v1)) / ++enemy[4];

		rDist = Math.min(DIST, rDist += 5);

		//PaintHelper.drawPoint(new Point2D.Double(xg + getX(), yg + getY()), Color.BLUE, getGraphics(), 20);

		boolean isClose = false;
		if (eRate > v0 || eName == name)
		{

			if (getEnergy() > bPower && Math.abs(getGunTurnRemaining()) < GUN_TURN_OFFSET)
			{
				boolean isShooting = setFireBullet(bPower) == null;
				//myPaintGunProfile.setGunTargetPoint(lastX, lastY, bPower, isShooting, getGunHeadingRadians());
			}
			myPaintMaxEsc.setBulletSpeed(bPower);
			myPaintMaxEsc.onScannedRobot(e);
			eName = name;

			double diff = Math.abs(Utils.normalRelativeAngle(h0 = -enemy[2] + (h1 = enemy[2] = e.getHeadingRadians())));
			long scanDiff = e.getTime() - lastScanTime;
			lastScanTime = e.getTime();
			if (diff > MAX_HEAD_DIFF)
			//if (scanDiff > 2)
			{
//				System.out.format("[%04d] zero diff=%3.5f (%d) (%d) \n", getTime(), Math.toDegrees(diff), e.getTime()
//						- lastTime, scanDiff);
				//h0 = Rules.getTurnRateRadians(e.getVelocity());
				h0 = ta = tc = 0;
				lastTime = e.getTime();
			}

//			if (getGunHeat() < GUNLOCK)
			if (getGunHeat() < GUNLOCK || getOthers() == 1)
			{
				h0 = (ta += Math.abs(h0)) / ++tc * Math.signum(h0);
				if (!Double.isNaN(x = (Utils.normalRelativeAngle(rM - getRadarHeadingRadians()) * INF)))
				{
					setTurnRadarRightRadians(x);
				}
			}
			bPower = Math.min(BMAX, TARGET_DISTANCE / (eRate = v0));
			rM = Double.MAX_VALUE;
			v0 = i = 0;

			Rectangle2D bField;

//			ArrayList<Point2D> riskPoints = new ArrayList<Point2D>();
//			ArrayList<Double> riskValues = new ArrayList<Double>();

			double mx = 0;
			double my = 0;
			while ((v0 += DELTA_RISK_ANGLE) <= PI_360)
			{
//				x = rDist * Math.sin(v0); // circle coordinates
//				y = rDist * Math.cos(v0);
				double a = getHeadingRadians();
				double angle = v0 - Utils.normalRelativeAngle(getHeadingRadians()) + PI_90;
				double sin = rDist * Math.sin(angle);
				double cos = (rDist * 0.9) * Math.cos(angle);

				x = sin * Math.sin(a) - cos * Math.cos(a); // this is the right combination for a ellipse based on heading
				y = sin * Math.cos(a) + cos * Math.sin(a);

				if ((bField = new Rectangle2D.Double(WZ_G, WZ_G, WZ_G_W, WZ_G_H)).contains(x + getX(), y + getY()))
				{
					r1 = Math.abs(Math.cos(Math.atan2(enemy[0] - x, enemy[1] - y) - v0));
					try
					{
						Iterator<double[]> iter = allTargets.values().iterator();
						double[] coordinate;
						while (true)
						{
							r1 += TARGET_FORCE / Point2D.distanceSq((coordinate = iter.next())[0], coordinate[1], x, y);
							isClose |= coordinate[5] < rDist;
						}
					}
					catch (Exception e1)
					{}

					// debug
					myPaintMinRiskAll.registerRiskPoint(getTime(), x + getX(), y + getY(), r1, getX(), getY(), rDist);
//					riskPoints.add(new Point2D.Double(x + getX(), y + getY()));
//					riskValues.add(r1);

					if (Math.random() < 0.8 && r1 < rM)
					//if (r1 < rM)
					{
						rM = r1;
						v1 = v0;
						mx = x;
						my = y;
					}
				}

				if (((i += 0.9) * Rules.getBulletSpeed(bPower) < Math.hypot(xg, yg)))
				{
					h1 += h0;
					if (!bField.contains((xg += (Math.sin(h1) * v2)) + getX(), (yg += (Math.cos(h1) * v2)) + getY()))
					{
						v2 = -v2;
					}
				}
			}

			lastX = xg + getX();
			lastY = yg + getY();

			setTurnGunRightRadians(Utils.normalRelativeAngle(Math.atan2(xg, yg) - getGunHeadingRadians()));
			if (Math.abs(getDistanceRemaining()) <= DIST_REMAIN || isClose)
			{
//				int k = 0;
//				for (Point2D p : riskPoints)
//				{
//					myPaintMinRiskAll.registerRiskPoint(getTime(), p.getX(), p.getY(), riskValues.get(k), getX(),
//							getY(), rDist);
//					k++;
//				}

				//System.out.format("[%04d] v1=%3.5f head=%3.5f\n", getTime(), Math.toDegrees(v1), getHeading());

//				setTurnRightRadians(Math.tan(v1 -= getHeadingRadians()));
//				setAhead(rDist * Math.cos(v1));

				// simonton: goto procedure with coordinates
				double a;
				setTurnRightRadians(Utils.normalRelativeAngle(Math.tan(a = Math.atan2(mx, my) - getHeadingRadians())));
				setAhead(Math.hypot(mx, my) * Math.cos(a));

			}

			double maxV = Math.abs(getTurnRemaining()) > 30 ? Math.random() * 6 : 8;
//			double maxV = (Math.PI / 18 - Math.min(Math.abs(getTurnRemainingRadians()), Math.PI / 18))
//					/ (Math.PI / 240);
			setMaxVelocity(maxV);
			//System.out.format("[%04d] turn=%3.5f v=%3.4f maxV=%3.4f \n", getTime(), getTurnRemaining(), getVelocity(),
//					maxV);

			// debug
			if (isClose)
				setAllColors(Color.YELLOW); // 22 byte
			else
				setAllColors(Color.RED);

		}
	}

	@Override
	public void onHitRobot(HitRobotEvent e)
	{
		eName = e.getName();
		eRate = 0;
		rDist = 50.0;
	}

	//	@Override
	//	public void onBulletHit(BulletHitEvent e)
	//	{
	//		allTargets.get(e.getName())[6] += 20;
	//	}

	@Override
	public void onRobotDeath(RobotDeathEvent e)
	{
		eRate = INF;
		allTargets.remove(e.getName());
	}

	//	@Override
	//	public void onPaint(Graphics2D g)
	//	{
	//		PaintRobotPath.onPaint(g, getName(), getTime(), getX(), getY(), Color.GREEN);
	//
	//	}
}
