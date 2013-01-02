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
package wompi.echidna.move;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashSet;

import robocode.AdvancedRobot;
import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.RobotDeathEvent;
import robocode.util.Utils;
import wompi.echidna.target.ATarget;
import wompi.robomath.RobotMath;

public class MoveCapulet extends AMovement
{
	private static final double	WZ			= 20.0;
	private static final double	WZ_SIZE		= 1000 - 2 * WZ;

	private final static double	PI			= Math.PI;					// 360 degree
	private final static double	PI_360		= Math.PI * 2.0;			// 360 degree
	private final static double	PI_90		= Math.PI / 2.0;			// 90 degree
	private final static double	PI_DELTA	= Math.PI / 16;			// 90 degree

	private final static double	DIST_MIN	= 100;
	private final static double	DIST_MAX	= 185;
	private final static double	DIST_ADJUST	= 0.75;
	private final static double	DIST_REMAIN	= 20;

	private final static double	A_SCALE		= 200.0;

	// / enemy values
	ATarget						myTarget;

	HashSet<ATarget>			myEnemys	= new HashSet<ATarget>();

	// robot values

	// controlling values

	public MoveCapulet(AdvancedRobot robot)
	{
		super(robot);
	}

	@Override
	public void init()
	{}

	@Override
	public void onRobotDeath(RobotDeathEvent e)
	{
		for (ATarget target : myEnemys)
		{
			if (e.getName() == target.getName())
			{
				myEnemys.remove(target);
				break;
			}
		}
	}

	@Override
	public void onScannedRobot(ATarget target, boolean isMaintarget)
	{
		if (isMaintarget) myTarget = target;
		myEnemys.add(target);
	}

	@Override
	public void onHitWall(HitWallEvent e)
	{}

	@Override
	public void onHitRobot(HitRobotEvent e)
	{}

	@Override
	public void onHitByBullet(HitByBulletEvent e)
	{}

	@Override
	public void run()
	{
		doMove();
	}

	double	remianDistance;

	// --------------------------------------------- working stuff ----------------------------------------------------------
	private void doMove()
	{
		if (myTarget == null) return;

		if (Math.abs(myRobot.getDistanceRemaining()) <= DIST_REMAIN)
		{
			double dR = myTarget.getDistance() * DIST_ADJUST * Math.random();
			double R = RobotMath.limit(DIST_MIN, dR, DIST_MAX);
			double mRate = Double.POSITIVE_INFINITY;

			Point2D mP; // moving point
			Rectangle2D B_FIELD = new Rectangle2D.Double(WZ, WZ, myRobot.getBattleFieldWidth() - 2 * WZ,
					myRobot.getBattleFieldHeight() - 2 * WZ);

			double angle = 360;
			double bestAngle = 0;
			while (--angle >= 0)
			{
				double x = myRobot.getX() + R * Math.sin(Math.toRadians(angle));
				double y = myRobot.getY() + R * Math.cos(Math.toRadians(angle));

				mP = new Point2D.Double(x, y);

				if (B_FIELD.contains(mP))
				{
					double f1 = 0;
					double r3 = 0;
					for (ATarget target : myEnemys)
					{
						f1 += 10000 / Point.distanceSq(target.getX(), target.getY(), x, y);
					}

					Point2D eP = new Point2D.Double(myTarget.getX(), myTarget.getY());
					double w = Utils.normalAbsoluteAngle(RobotMath.calculateAngle(mP, eP));
					r3 = Math.abs(Math.cos(w - Math.toRadians(angle)));

					double cRate = r3 + 10 * f1;
					// System.out.format("[%3.0f] r1=%3.2f r2=%3.2f D=%3.2f r3=%3.2f f1=%3.2f cRate=%3.2f ",
					// Math.toDegrees(angle),r1,r2,r1+r2,r3,f1,cRate);

					if (cRate < mRate)
					{
						mRate = cRate;
						bestAngle = Math.toRadians(angle);

					}
				}
			}
			double headingDiff = bestAngle - myRobot.getHeadingRadians();
			myRobot.setTurnRightRadians(Math.tan(headingDiff));
			myRobot.setAhead(R * Math.cos(headingDiff));
			// System.out.format("\n");
		}
	}

	@Override
	public void onPaint(Graphics2D g)
	{}
}
