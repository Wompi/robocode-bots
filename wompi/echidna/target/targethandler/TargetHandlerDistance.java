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
package wompi.echidna.target.targethandler;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Collection;
import java.util.HashMap;

import robocode.AdvancedRobot;
import robocode.BulletHitEvent;
import robocode.DeathEvent;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;
import robocode.WinEvent;
import wompi.echidna.misc.painter.PaintTargetSquare;
import wompi.echidna.target.ATarget;
import wompi.echidna.target.TargetAveraged;

public class TargetHandlerDistance extends ATargetHandler
{
	HashMap<String, ATarget>	allTargets;

	public TargetHandlerDistance(AdvancedRobot robot)
	{
		super(robot);
		allTargets = new HashMap<String, ATarget>();
	}

	@Override
	public Collection<ATarget> getAllTargets()
	{
		return allTargets.values();
	}

	@Override
	public void init()
	{
		for (ATarget target : getAllTargets())
		{
			target.init();
		}
	}

	@Override
	public void onBulletHit(BulletHitEvent e)
	{
		ATarget target = allTargets.get(e.getName());
		if (target != null) target.onBulletHit(e);
	}

	@Override
	public void onScannedRobot(ScannedRobotEvent e)
	{
		myLastScannedTarget = getTargetForName(e.getName());
		myLastScannedTarget.onScannedRobot(e);
		calculateMainTarget();
		// System.out.format("[%d] %s is now main target\n", myRobot.getTime(),myTarget.getName());
	}

	@Override
	public void onRobotDeath(RobotDeathEvent e)
	{
		ATarget aTarget = getTargetForName(e.getName());
		aTarget.onRobotDeath(e);
		if (myTarget == aTarget)
		{
			myTarget = null;
			calculateMainTarget(); // go to the next target if possible
		}
	}

	@Override
	public void onDeath(DeathEvent e)
	{
		for (ATarget target : getAllTargets())
		{
			target.onDeath(e);
		}
	}

	@Override
	public void onWin(WinEvent e)
	{
		for (ATarget target : getAllTargets())
		{
			target.onWin(e);
		}
	}

	@Override
	public void onPaint(Graphics2D g)
	{
		for (ATarget target : getAllTargets())
		{
			target.onPaint(g);
		}
		if (myTarget != null) PaintTargetSquare.drawTargetSquare(g, myTarget.getHeading(), myTarget.getAbsX(), myTarget.getAbsY(), true, Color.RED);
	}

	// ---------------------------------------------------------------------------------------------------------------------

	@Override
	protected ATarget getTargetForName(String name)
	{
		ATarget target = allTargets.get(name);
		if (target == null)
		{
			allTargets.put(name, target = new TargetAveraged(myRobot));
			target.init();
		}
		return target;
	}

	@Override
	protected void calculateMainTarget()
	{
		double eRate = Double.POSITIVE_INFINITY;
		for (ATarget target : getAllTargets())
		{
			double cRate = (myRobot.getOthers() <= 3.0) ? target.getEnergy() : target.getBlindDistance(); // blind distance to make it compatible to
																											// run

			if (target.isAlive())
			{
				boolean rate = cRate < eRate;
				boolean shoot = target.getLiveShotPower() < (target.getEnergy() + 0.1);
				// System.out.format("[%d] rate=%b shoot=%b liveShot=%3.2f eEnergy=%3.2f %s",
				// myRobot.getTime(),rate,shoot,target.getLiveShotPower(),target.getEnergy(),target.getName());
				if (rate && shoot)
				{
					myTarget = target;
					eRate = cRate;
					if (myRobot.getOthers() > 3) eRate -= 100;
					// System.out.format(" MAIN");
				}
				// System.out.format("\n");
			}
		}
	}

}
