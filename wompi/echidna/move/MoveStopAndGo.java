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

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import robocode.AdvancedRobot;
import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import wompi.echidna.misc.painter.PaintTargetSquare;
import wompi.echidna.target.ATarget;
import wompi.wallaby.PaintHelper;

public class MoveStopAndGo extends AMovement
{
	private static final double	WZ					= 18.0;
	private static final double	WZ_SIZE				= 1000 - 2 * WZ;
	private final static double	WZ_INC				= 0.07;
	private final static double	WZ_STICK			= 160.0;

	private final static double	PERPENDICULAR		= Math.PI / 2.0;
	private final static double	PREFERRED_RANGE_1	= 500;				// 500 orig 1.25
	private final static double	CLOSE_FCT			= 400;				// 400 orig

	// / enemy values
	ATarget						myTarget;

	// robot values

	// controlling values
	ArrayList<ShotStats>		nextMoves;
	int							DIR;
	double						moveBearing;
	int							stopMoves;

	// debug

	public MoveStopAndGo(AdvancedRobot robot)
	{
		super(robot);
	}

	public void init()
	{
		nextMoves = new ArrayList<ShotStats>();
		DIR = 1;
	}

	@Override
	public void onScannedRobot(ATarget target, boolean isMaintarget)
	{
		if (isMaintarget)
		{
			myTarget = target;
			moveBearing = myTarget.getBearing();
		}

		double eEnergyDelta = myTarget.getLastEnergy() - myTarget.getEnergy();
		if (eEnergyDelta < 3.01 && eEnergyDelta > 0.09)
		{
			PaintTargetSquare.drawTargetSquare(myRobot.getGraphics(), target.getHeading(), target.getX(), target.getY(), PaintHelper.greenTrans);
			long deltaTime = target.getTime() - target.getLastScan();
			stopMoves = (int) (Math.random() * 10);
			System.out.format("FIRE[%d] power=%3.2f lastSeen=%d stop=%d %s\n", myRobot.getTime(), eEnergyDelta, deltaTime, stopMoves,
					target.getName());
		}
	}

	@Override
	public void onHitWall(HitWallEvent e)
	{
		DIR = -DIR;
	}

	public void onHitRobot(HitRobotEvent e)
	{
		DIR = -DIR;
	}

	public void onHitByBullet(HitByBulletEvent e)
	{
		// double eHeat = Rules.getGunHeat(e.getPower());
		// double eCool = eHeat/myRobot.getGunCoolingRate();
		// ShotStats next = new ShotStats();
		// next.bBearing = e.getBearingRadians();
		// next.nextShot = e.getTime() + 3;
		// System.out.format("HIT[%d] power=%3.2f cool=%3.2f nextShot=%d bear=%3.2f\n",
		// myRobot.getTime(),e.getPower(),eCool,next.nextShot,Math.toDegrees(next.bBearing));

		// nextMoves.add(next);
		moveBearing = e.getBearingRadians();
		DIR = -DIR;
	}

	@Override
	public void run()
	{
		doMove();
	}

	// --------------------------------------------- working stuff ----------------------------------------------------------
	private void doMove()
	{
		if (myTarget == null) return;

		// if (nextMoves.size() == 0) return;
		// ShotStats nextMove = nextMoves.get(0);
		// System.out.format("RUN[%d] nextMove=%d \n",myRobot.getTime(),nextMove.nextShot);

		// if (myRobot.getTime() > (nextMove.nextShot))
		{
			// nextMoves.remove(0);
			// double eAbsBearing = myTarget.getBearing() + myRobot.getHeadingRadians();
			double eAbsBearing = moveBearing + myRobot.getHeadingRadians();
			double rTurn;
			double rTurnOffset = PERPENDICULAR - (myTarget.getDistance() - PREFERRED_RANGE_1) / CLOSE_FCT;;
			Point2D.Double rP = new Point2D.Double(myRobot.getX(), myRobot.getY());

			while (!new Rectangle2D.Double(WZ, WZ, myRobot.getBattleFieldWidth() - 2 * WZ, myRobot.getBattleFieldHeight() - 2 * WZ).contains(project(
					rP, rTurn = eAbsBearing + DIR * (rTurnOffset -= WZ_INC), WZ_STICK)));

			if (stopMoves-- <= 0)
			{

				myRobot.setTurnRightRadians(Math.tan(rTurn -= myRobot.getHeadingRadians()));

				// if (myRobot.getTime() % 10 == 0)
				myRobot.setAhead(50 * Math.signum(Math.cos(rTurn)));
			}
			else myRobot.setAhead(0);

			// myRobot.setTurnRightRadians(Math.cos(nextMove.bBearing));
			// myRobot.setAhead(150 * DIR);
		}
	}

	class ShotStats
	{
		double	bBearing;
		long	nextShot;

	}

	private static Point2D.Double project(Point2D.Double location, double angle, double distance)
	{
		return new Point2D.Double(location.x + distance * Math.sin(angle), location.y + distance * Math.cos(angle));
	}
}
