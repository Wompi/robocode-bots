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
package wompi.numbat.target;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import robocode.Bullet;
import robocode.BulletHitEvent;
import robocode.HitRobotEvent;
import robocode.RobotStatus;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;
import wompi.echidna.misc.painter.PaintRobotPath;
import wompi.echidna.misc.painter.PaintTargetSquare;
import wompi.numbat.debug.DebugRadarProperties;
import wompi.numbat.gun.NumbatSTGun;
import wompi.numbat.gun.misc.INumbatTick;
import wompi.robomath.RobotMath;
import wompi.wallaby.PaintHelper;

public class NumbatTarget extends Point2D.Double
{
	private static final long			serialVersionUID	= -5406737205536713408L;

	public final static double			MAX_PATTERN_BORDER	= 13;
	public final static Color[]			BOT_COLORS			= { Color.BLUE, Color.CYAN, Color.GRAY, Color.GREEN, Color.MAGENTA, Color.ORANGE,
			Color.PINK, Color.RED, Color.YELLOW, Color.WHITE };

	private double						lastX;
	private double						lastY;

	private double						eAbsBearing;
	private double						eLastAbsBearing;											// TODO: not clear for what this is usefull

	public double						eHeading;
	public double						eLastHeading;

	public double						eEnergy;
	public double						eLastEnergy;

	public double						eVelocity;
	public double						eLastVelocity;

	public long							eScan;
	public long							eLastScan;
	public double						eSlipDir			= 1;

	private double						eLastDistance;
	private double						eDistance;

	public int							eScanState;
	public String						eName;
	public int							eVisits;

	public boolean						isAlive;

	private ArrayList<Bullet>			eFiredBullets;
	public final NumbatBulletTracker	myBulletTracker;

	// singe tick pattern gun
	public Map<Integer, INumbatTick>	matcherMap;
	public StringBuilder				eHistory;
	private int							avgPatternLength;
	private int							avgPatternCount;
	public int							eMatchKeyLength		= NumbatSTGun.DEFAULT_PATTERN_LENGTH;

	public double						eScore;

	// NumbatSimpleAverage vAvgSimple;

	// debug
	// private PaintDiagramm debugDiagram = new PaintDiagramm();
	// public NumbatPointHandler myPointHandler = new NumbatPointHandler();
	private final Color					myColor;

	private double						vAvg;
	private int							avgVCount;

	private static int					colorIndex;

	public NumbatTarget()
	{
		init();
		myColor = BOT_COLORS[colorIndex++];

		myBulletTracker = new NumbatBulletTracker();
	}

	public void init()
	{
		eHeading = java.lang.Double.NaN;
		eLastHeading = java.lang.Double.NaN;
		eEnergy = 100; // TODO: change this on team action
		eLastEnergy = 100;
		eDistance = 2000;
		eLastDistance = 2000;
		eVelocity = java.lang.Double.NaN;
		eLastVelocity = java.lang.Double.NaN;
		eScan = 0;
		eLastScan = 0;
		isAlive = true;
		eHistory = new StringBuilder(NumbatSTGun.DEFAULT_PATTERN_LENGTH);
		eFiredBullets = new ArrayList<Bullet>();
		eScore = 0;
		// vAvgSimple = new NumbatSimpleAverage(2);
		if (matcherMap == null)
		{
			matcherMap = new LinkedHashMap<Integer, INumbatTick>(500000);
		}
	}

	public void onScannedRobot(ScannedRobotEvent scan, RobotStatus status)
	{

		eVisits++;

		eAbsBearing = status.getHeadingRadians() + scan.getBearingRadians();

		eLastDistance = eDistance;
		eDistance = scan.getDistance();

		eLastHeading = eHeading;
		eHeading = scan.getHeadingRadians();

		eLastVelocity = eVelocity;
		eVelocity = scan.getVelocity();
		vAvg += Math.abs(scan.getVelocity());
		avgVCount++;

		eLastScan = eScan;
		eScan = scan.getTime();

		eLastEnergy = eEnergy;
		eEnergy = scan.getEnergy();

		isAlive = true;

		lastX = x;
		lastY = y;

		x = Math.sin(eAbsBearing) * eDistance + status.getX();
		y = Math.cos(eAbsBearing) * eDistance + status.getY();

		DebugRadarProperties.debugScanDifference(getLastScanDifference());

		myBulletTracker.registerTrack(eDistance, getEnergyDifference(), eScan);

		// debug
		//		if (myBulletTracker.hasFired(0))
		//		{
		//			System.out.format("[%d] target has fired %3.2f - %s (%d +-%d) \n", eScan, myBulletTracker.myLastFirePower, eName,
		//					myBulletTracker.nextZeroHeat(), getLastScanDifference());
		//		}

	}

	public double getEnergyDifference()
	{
		return eLastEnergy - eEnergy;
	}

	public long getLastScanDifference()
	{
		return eScan - eLastScan;
	}

	public long getCurrentScanDifference(RobotStatus status)
	{
		return status.getTime() - eScan;
	}

	public double getLastDistanceDifference()
	{
		return this.distance(lastX, lastY);
	}

	public double getHeadingDifference()
	{
		// TODO: 0 is not a good value better would be something like NaN but that has to be checked every time the function is called
		if (java.lang.Double.isNaN(eLastHeading)) return Math.PI;
		return Utils.normalRelativeAngle(eHeading - eLastHeading);
	}

	public double getAverageVelocity()
	{
		// simple but still effective somehow
		return vAvg * Math.signum(eVelocity) / avgVCount;
	}

	public double getAccelleration()
	{
		return RobotMath.getAcceleration(eVelocity, eLastVelocity);
	}

	/**
	 * If the scan time is equals the status time the last real bearing will be returned.
	 * Otherwise the calculated bearing to the last calculated x,y coordinates.
	 * TODO: maybe the angle should be interpolated if the scan is just 1-5 turns away to give a more precise view. Or i just use the pattern to
	 * interpolate
	 * the angle
	 * 
	 * @param status
	 * @return current absolute bearing of the target, the return value is an absolute angle and has to be normalized if relative angles are needed
	 */
	public double getAbsoluteBearing(RobotStatus status)
	{
		if (eScan == status.getTime()) return eAbsBearing;
		return Utils.normalAbsoluteAngle(Math.atan2(x - status.getX(), y - status.getY()));
	}

	public double getDistance(RobotStatus status)
	{
		if (eScan == status.getTime()) return eDistance;
		return this.distance(status.getX(), status.getY());
	}

	public int getAveragePatternLength()
	{
		if (avgPatternCount == 0) return 0;
		return avgPatternLength / avgPatternCount;
	}

	public void registerPatternLength(int pLength)
	{
		avgPatternLength += pLength;
		avgPatternCount++;

		// if (getAveragePatternLength() >= (eMatchKeyLength*0.5))
		{
			// eMatchKeyLength += 10;
			// eMatchKeyLength = Math.min(eMatchKeyLength, 40);
		}
	}

	public void onRobotDeath()
	{
		isAlive = false;
	}

	public void onBulletHit(BulletHitEvent e, RobotStatus myBotStatus)
	{
		double dmg = Rules.getBulletDamage(e.getBullet().getPower());
		eScore += dmg;
		eEnergy -= dmg;
	}

	/**
	 * Maybe it is useful to decide if the target should be switched. If the score bonus is grater 10 it might be better to finish the target off
	 * instead
	 * of switching to a new one
	 * 
	 * @return 20% of the current score against this target
	 */
	public double getScoreBonus()
	{
		return eScore * 0.2;
	}

	//	public void registerBullet(Bullet bullet)
	//	{
	//		eFiredBullets.add(bullet);
	//	}

	public void registerFireDamage(Bullet bullet)
	{
		eFiredBullets.add(bullet);
	}

	public double getLiveFireDamage()
	{
		Iterator<Bullet> iter = eFiredBullets.iterator();
		double fireDamage = 0;
		while (iter.hasNext())
		{
			Bullet bullet = iter.next();
			if (bullet.isActive())
			{
				fireDamage += Rules.getBulletDamage(bullet.getPower());
			}
			else
			{
				iter.remove();
			}
		}
		return fireDamage;
	}

	public void onPaint(Graphics2D g, RobotStatus status)
	{
		if (isAlive)
		{
			double dist = getCurrentScanDifference(status) * Rules.MAX_VELOCITY;
			PaintHelper.drawArc(this, dist, 0, Math.PI * 2.0, true, g, new Color(0x00, 0xFF, 0x00, 0x10));
			PaintHelper.drawArc(this, dist, 0, Math.PI * 2.0, false, g, PaintHelper.whiteTrans);
			double srate = Rules.MAX_VELOCITY / (getDistance(status) - getCurrentScanDifference(status) * Rules.MAX_VELOCITY);
			g.setColor(Color.CYAN);
			g.setFont(PaintHelper.myFont);
			g.drawString(String.format("[%3.2f]", srate), (int) x - 20, (int) y - 20 - 10);
		}

		if (isAlive)
		{
			if (getAveragePatternLength() >= MAX_PATTERN_BORDER)
			{
				PaintTargetSquare.drawTargetSquare(g, 0.0, x, y, true, PaintHelper.yellowTrans);
				g.setColor(Color.YELLOW);
			}
			else if (getAveragePatternLength() >= MAX_PATTERN_BORDER * 0.75)
			{
				PaintTargetSquare.drawTargetSquare(g, 0.0, x, y, true, PaintHelper.greenTrans);
				g.setColor(Color.GREEN);
			}
			else
			{
				PaintTargetSquare.drawTargetSquare(g, 0, x, y, true, PaintHelper.redTrans);
				g.setColor(Color.RED);

			}
			g.setFont(PaintHelper.myFont);
			g.drawString(String.format("%3.2f / %3.2f", getScoreBonus(), getLiveFireDamage()), (int) x, (int) y + 42);

		}
	}

	public void onSinglePaint(Graphics2D g, RobotStatus status)
	{
		// debugDiagram.onPaint(g, status, getAveragePatternLength(), Color.BLUE,eName);
		PaintRobotPath.onPaint(g, eName, status.getTime(), x, y, Color.GRAY);
	}

	public void onHitRobot(HitRobotEvent e, RobotStatus status)
	{
		eAbsBearing = status.getHeadingRadians() + e.getBearingRadians();

		eLastVelocity = eVelocity;
		eVelocity = 0;

		eLastEnergy = eEnergy;
		eEnergy = e.getEnergy();

		x = Math.sin(eAbsBearing) * 40 + status.getX();
		y = Math.cos(eAbsBearing) * 40 + status.getY();
	}

	public Color getBotColor()
	{
		return myColor;
	}
}
