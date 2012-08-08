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
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.TreeSet;

import robocode.AdvancedRobot;
import robocode.RobotDeathEvent;
import robocode.StatusEvent;
import robocode.util.Utils;
import wompi.echidna.target.ATarget;
import wompi.robomath.RobotMath;

public class MoveMinAngle extends AMovement
{
	private static final double	WZ				= 60.0;
	private static final double	WZ_SIZE			= 1000 - 2 * WZ;
	private final static double	WZ_INC			= 0.07;
	private final static double	WZ_STICK		= 160.0;

	private final static double	PERPENDICULAR	= Math.PI / 2.0;
	private final static double	PREFERRED_RANGE	= 500;
	private final static double	CLOSE_FCT		= 400;

	// / enemy values

	// robot values

	// controlling values
	ArrayList<ATarget>			myTargets		= new ArrayList<ATarget>();
	Point2D						movePoint;

	// debug

	public MoveMinAngle(AdvancedRobot robot)
	{
		super(robot);
	}

	public void init()
	{}

	public void onStatus(StatusEvent event)
	{
		System.out.format("STATUS MOVE[%d] \n", myRobot.getTime());
	}

	@Override
	public void onScannedRobot(ATarget target, boolean isMaintarget)
	{
		if (!myTargets.contains(target))
		{
			myTargets.add(target);
		}
	}

	public void onRobotDeath(RobotDeathEvent e)
	{
		for (ATarget target : myTargets)
		{
			if (target.getName() == e.getName())
			{
				myTargets.remove(target);
				break;
			}
		}
	}

	@Override
	public void run()
	{
		doMove();
	}

	@Override
	public void onPaint(Graphics2D g)
	{}

	// --------------------------------------------- working stuff ----------------------------------------------------------
	private void doMove()
	{
		final Point2D rP = new Point2D.Double(myRobot.getX(), myRobot.getY());

		if (movePoint == null)
		{
			double maxAngle = 0;
			// Point2D movePoint = null;

			double dist = 100 + Math.random() * 300;

			for (double i = 0; i < 360; i += 10)
			{
				final Point2D tP = RobotMath.calculatePolarPoint(Math.toRadians(i), dist, rP);

				if (new Rectangle2D.Double(WZ, WZ, WZ_SIZE, WZ_SIZE).contains(tP))
				{
					Comparator<ATarget> compi = new Comparator<ATarget>()
					{
						@Override
						public int compare(ATarget o1, ATarget o2)
						{
							double a1 = RobotMath.calculateAngle(tP, new Point2D.Double(o1.getX(), o1.getY()));
							double a2 = RobotMath.calculateAngle(tP, new Point2D.Double(o2.getX(), o2.getY()));
							double angle1 = Utils.normalAbsoluteAngle(a1);
							double angle2 = Utils.normalAbsoluteAngle(a2);
							return Double.compare(angle1, angle2);
						}
					};
					TreeSet<ATarget> depp = new TreeSet<ATarget>(compi);
					depp.addAll(myTargets);

					double angle = calcMaxAngle(tP, depp);
					if (angle > maxAngle)
					{
						maxAngle = angle;
						movePoint = tP;
					}
				}
			}
		}

		double rTurn = RobotMath.calculateAngle(rP, movePoint);
		myRobot.setTurnRightRadians(Math.tan(rTurn -= myRobot.getHeadingRadians()));

		// if (rP.distance(movePoint) < 20 || myRobot.getDistanceRemaining() == 0)
		if (myRobot.getTime() % 10 == 0)
		{
			myRobot.setAhead(250 * Math.signum(Math.cos(rTurn)));
			movePoint = null;
		}
	}

	private double calcMaxAngle(Point2D testPoint, TreeSet<ATarget> sortedEnemys)
	{
		double max = 0;
		ATarget lastT = null;
		double lastAngle = 0;
		double firstAngle = 0;
		double diff;
		for (ATarget t : sortedEnemys)
		{
			double a = RobotMath.calculateAngle(testPoint, new Point2D.Double(t.getX(), t.getY()));
			double angle = Utils.normalAbsoluteAngle(a);

			if (lastT == null)
			{
				// System.out.format("[%d] angle=%3.2f %s \n", myRobot.getTime(),Math.toDegrees(angle),t.getName());
				firstAngle = angle;
			}
			else
			{
				diff = angle - lastAngle;
				max = Math.max(diff, max);
				// System.out.format("[%d] angle=%3.2f diff=%3.2f %s \n", myRobot.getTime(),Math.toDegrees(angle),Math.toDegrees(diff),t.getName());
			}
			lastT = t;
			lastAngle = angle;
		}
		diff = (Math.toRadians(360) - lastAngle) + firstAngle;
		max = Math.max(diff, max);
		// System.out.format("[%d] roundDiff diff=%3.2f  max=%3.2f\n", myRobot.getTime(),Math.toDegrees(diff),Math.toDegrees(max));
		return max;
	}
}
