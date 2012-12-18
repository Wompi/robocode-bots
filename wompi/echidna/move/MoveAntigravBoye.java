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

import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashSet;

import robocode.AdvancedRobot;
import robocode.Condition;
import robocode.RobotDeathEvent;
import robocode.Rules;
import robocode.util.Utils;
import wompi.echidna.target.ATarget;
import wompi.echidna.target.FunctionsTarget;
import wompi.paint.PaintHelper;
import wompi.robomath.RobotMath;

public class MoveAntigravBoye extends AMovement
{

	HashSet<ATarget>	myEnemys	= new HashSet<ATarget>();
	double				xForce;
	double				yForce;
	Rectangle2D			bField;

	public MoveAntigravBoye(AdvancedRobot robot)
	{
		super(robot);
	}

	@Override
	public void init()
	{
		bField = new Rectangle2D.Double(18, 18, myRobot.getBattleFieldWidth() - 2 * 18, myRobot.getBattleFieldHeight() - 2 * 18);
	}

	@Override
	public void onRobotDeath(RobotDeathEvent e)
	{
		for (ATarget t : myEnemys)
		{
			if (t.getName() == e.getName())
			{
				myEnemys.remove(t);
				break;
			}
		}
	}

	@Override
	public void onScannedRobot(ATarget target, boolean isMaintarget)
	{
		myEnemys.add(target);
		double rHead = myRobot.getHeadingRadians();
		double tAbsBearing = target.getBearing() + rHead;

		xForce = xForce * 0.9 - (((Math.sin(tAbsBearing) - Math.sin(rHead)) / myRobot.getOthers()) / target.getDistance());
		yForce = yForce * 0.9 - (((Math.cos(tAbsBearing) - Math.cos(rHead)) / myRobot.getOthers()) / target.getDistance());

		// xForce = xForce *.9 - Math.sin(tAbsBearing) / target.getDistance();
		// yForce = yForce *.9 - Math.cos(tAbsBearing) / target.getDistance();

		double eEnergyDelta = target.getLastEnergy() - target.getEnergy();
		if (eEnergyDelta < 3.01 && eEnergyDelta > 0.09)
		{
			// WallabyPainter.drawTargetSquare(myRobot.getGraphics(), target.getHeading(), target.getX(), target.getY());
			long deltaTime = target.getTime() - target.getLastScan();

			ATarget next = FunctionsTarget.caculateNearestEnemyDist(target, new ArrayList<ATarget>(myEnemys));

			GravBoye boye = new GravBoye();
			boye.bPos = new Point2D.Double(target.getX(), target.getY());
			boye.speed = Rules.getBulletSpeed(eEnergyDelta);

			if (next != null && target.getDistance() > boye.bPos.distance(next.getX(), next.getY()) && myRobot.getOthers() > 1)
			{
				Point2D nP = new Point2D.Double(next.getX(), next.getY());
				boye.bHeading = Utils.normalAbsoluteAngle(RobotMath.calculateAngle(boye.bPos, nP));
			}
			else
			{
				boye.bHeading = Utils.normalAbsoluteAngle(RobotMath.calculateAngle(boye.bPos, new Point2D.Double(myRobot.getX(), myRobot.getY())));
			}
			myRobot.addCustomEvent(boye);

		}
	}

	@Override
	public void run()
	{
		doMove();
	}

	private void doMove()
	{
		myRobot.setTurnRightRadians(Utils.normalRelativeAngle(Math.atan2(
				xForce + 1 / myRobot.getX() - 1 / (myRobot.getBattleFieldWidth() - myRobot.getX()),
				yForce + 1 / myRobot.getY() - 1 / (myRobot.getBattleFieldHeight() - myRobot.getY()))
				- myRobot.getHeadingRadians()));

		// if (myRobot.getTime() % 10 == 0)
		myRobot.setAhead(120 - Math.abs(myRobot.getTurnRemaining()));
	}

	class GravBoye extends Condition
	{
		double	bHeading;
		Point2D	bPos;
		double	speed;

		@Override
		public boolean test()
		{
			bPos = RobotMath.calculatePolarPoint(bHeading, speed, bPos);
			if (!bField.contains(bPos))
			{
				myRobot.removeCustomEvent(this);
			}
			else
			{
				Point2D rP = new Point2D.Double(myRobot.getX(), myRobot.getY());
				double bear = Utils.normalAbsoluteAngle(RobotMath.calculateAngle(bPos, rP));

				double dist = rP.distance(bPos);

				xForce = xForce * .9 + (Math.sin(bear) / dist) * 0.1;
				yForce = yForce * .9 + (Math.cos(bear) / dist) * 0.1;
			}
			PaintHelper.drawPoint(bPos, Color.DARK_GRAY, myRobot.getGraphics(), 10);
			return false;
		}

	}

}
