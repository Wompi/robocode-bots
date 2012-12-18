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
package wompi.echidna.gun;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;

import robocode.AdvancedRobot;
import robocode.RobotDeathEvent;
import robocode.StatusEvent;
import robocode.util.Utils;
import wompi.Echidna;
import wompi.echidna.gun.fire.AFire;
import wompi.echidna.misc.DebugPointLists;
import wompi.echidna.target.ATarget;
import wompi.echidna.target.FunctionsTarget;
import wompi.paint.PaintHelper;
import wompi.wallaby.WallabyPainter;

public class GunCircularPrecise extends AGun
{

	// enemy Values
	ATarget			myTarget;
	ATarget			myLastTarget;

	// robot values

	// control values

	// debug
	DebugPointLists	debugGunPoints	= new DebugPointLists();

	public GunCircularPrecise(AdvancedRobot robot)
	{
		super(robot);
	}

	@Override
	public void init(AFire fireControl)
	{
		myRobot.setAdjustGunForRobotTurn(true);
		myFire = fireControl;
		myFire.init();
	}

	@Override
	public void onStatus(StatusEvent e)
	{}

	@Override
	public void onScannedRobot(ATarget enemy, boolean isMaintarget)
	{
		if (isMaintarget)
		{
			myLastTarget = myTarget;
			myTarget = enemy;

			if (myLastTarget == myTarget)
			{
				if (myRobot.getGunHeat() == 0)
				{
					long timeDiff = (myRobot.getTime() - myTarget.getLastScan());
					if (timeDiff == 1)
					{
						myFire.doFire(myTarget);
					}
					else
					{
						System.out.format("[%d] gun %d is not adjusted to target\n", myRobot.getTime(), timeDiff);
					}
				}
			}
			doGun();
		}
	}

	@Override
	public void run()
	{
		if (myTarget == null) return;
		// if (myRobot.getGunHeat() == 0 && myTarget.getLastScanDiff() > 0 && myLastTarget == myTarget)
		// System.out.format("[%d] dude be faster with scanning\n", myRobot.getTime());
	}

	@Override
	public void onPaint(Graphics2D g)
	{
		if (myTarget != null)
		{
			WallabyPainter.drawCenterPos(g, new Point2D.Double(myTarget.getX() + myRobot.getX(), myTarget.getY() + myRobot.getY()),
					myTarget.getDistance());
			// WallabyPainter.drawTargetSquare(g, myTarget.getHeading(), myTarget.getX(), myTarget.getY());
			WallabyPainter.drawTargetStats(g, myTarget, myRobot, myFire);
			myFire.doPaint(myTarget, g);
		}

		WallabyPainter.drawAngleLine(g, new Point2D.Double(myRobot.getX(), myRobot.getY()), myRobot.getGunHeadingRadians(), 700,
				PaintHelper.blueTrans);
		WallabyPainter.drawGunHeat(g, myRobot.getGunHeat(), myRobot);
		debugGunPoints.onPaint(g);
	}

	public void onRobotDeath(RobotDeathEvent e)
	{
		if (myTarget != null && e.getName() == myTarget.getName())
		{
			myTarget = null;
		}
	}

	// ------------------------------------------ working stuff ---------------------------------------------
	private void doGun()
	{
		if (myTarget == null) return;

		ATarget cMainTarget = Echidna.myTargetHandler.getMainTarget();

		Point2D gP = FunctionsTarget.calculateGuessPosition(cMainTarget, myFire.getFirePower(cMainTarget), false, debugGunPoints);

		// debug
		debugGunPoints.targetPoint = new Point2D.Double(gP.getX() + myRobot.getX(), gP.getY() + myRobot.getY());

		myRobot.setTurnGunRightRadians(Utils.normalRelativeAngle(Math.atan2(gP.getX(), gP.getY()) - myRobot.getGunHeadingRadians()));
	}
}
