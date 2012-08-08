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
package wompi.numbat.gun;

import java.awt.geom.Point2D;

import robocode.RobotStatus;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;
import wompi.echidna.misc.utils.BattleField;
import wompi.numbat.gun.fire.ANumbatFire;
import wompi.numbat.gun.misc.NumbatOneTickHandler;
import wompi.numbat.target.ITargetManager;
import wompi.numbat.target.NumbatTarget;

public class NumbatGunResearch extends ANumbatGun
{
	private final int				DELTA_HEADING_INDEX	= 20;
	private final int				VELOCITY_INDEX		= 16;
	private final double			HEAD_FACTOR			= 2.0;
	private final double			VELO_FACTOR			= 2.0;

	private final static double		WZ					= 17.9999;

	private NumbatOneTickHandler	myTickHandler;

	public NumbatGunResearch()
	{
		myTickHandler = new NumbatOneTickHandler();
	}

	@Override
	void setGun(RobotStatus status, ITargetManager targetMan, ANumbatFire fire)
	{
		NumbatTarget target = targetMan.getGunTarget();

		int lastTick = myTickHandler.getLastTick();
		double heading = target.eHeading;
		double velocity = 0;
		double pHeadChange = 0;
		double xg = target.x;
		double yg = target.y;

		for (double bDist = 0; bDist < Point2D.distance(status.getX(), status.getY(), xg, yg); bDist += fire.getBulletSpeed())
		{
			// System.out.format("%s",(char) lastTick);
			if (lastTick != NumbatOneTickHandler.BREAK_KEY)
			{
				int headDiff = lastTick >> 9;
				pHeadChange = Math.toRadians((double) (headDiff - DELTA_HEADING_INDEX) / HEAD_FACTOR);		// delta heading

				int vkey = (lastTick >> 3) - (headDiff << 6);
				velocity = (double) (vkey - VELOCITY_INDEX) / VELO_FACTOR;								// velocity
			}
			// System.out.format("(%3.2f,%3.2f)",Math.toDegrees(pHeadChange),velocity);

			heading += pHeadChange;					// hmm maybe wrong it takes the same headchange and velocity as the last predicted tick
			xg += velocity * Math.sin(heading);
			yg += velocity * Math.cos(heading);

			boolean wHit = false;
			if (xg < WZ)
			{
				xg = 18;
				wHit = true;
			}
			else if (BattleField.BATTLE_FIELD_W - xg < WZ)
			{
				xg = BattleField.BATTLE_FIELD_W - WZ;
				wHit = true;
			}
			if (yg < WZ)
			{
				yg = 18;
				wHit = true;
			}
			else if (BattleField.BATTLE_FIELD_H - yg < WZ)
			{
				yg = BattleField.BATTLE_FIELD_H - WZ;
				wHit = true;
			}

			if (wHit)
			{
				int hint = (int) Math.rint(Math.toDegrees(pHeadChange * HEAD_FACTOR)) + DELTA_HEADING_INDEX;
				int vint = (int) (Math.rint(0 * VELO_FACTOR)) + VELOCITY_INDEX;

				int head = (hint) << 6;
				lastTick = (head + (vint)) << 3;
			}
			else lastTick = myTickHandler.getFollowTick(lastTick);
		}
		// System.out.format("\n");

		gTurn = Utils.normalRelativeAngle(Math.atan2(xg - status.getX(), yg - status.getY()) - status.getGunHeadingRadians());

	}

	public void onScannedRobot(ScannedRobotEvent scan, RobotStatus status, ITargetManager targetMan)
	{
		NumbatTarget target = targetMan.getLastScanTarget();

		double hDiff = target.getHeadingDifference();
		double hMax = Rules.getTurnRateRadians(target.eLastVelocity) + 0.00001;
		long scanDiff = target.getLastScanDifference();

		int key;
		if (scanDiff > 1 || (scanDiff <= 1 && Math.abs(hDiff) > hMax))
		{
			key = NumbatOneTickHandler.BREAK_KEY;
		}
		else
		{
			int hint = (int) Math.rint(Math.toDegrees(hDiff * HEAD_FACTOR)) + DELTA_HEADING_INDEX;
			int vint = (int) (Math.rint(target.eVelocity * VELO_FACTOR)) + VELOCITY_INDEX;

			int head = (hint) << 6;
			key = (head + (vint)) << 3;
		}

		System.out.format("onScan[%d]: key=%d (%3.2f,%3.2f)\n", scan.getTime(), key, Math.toDegrees(hDiff), target.eVelocity);

		myTickHandler.registerTick(key);
	}

	@Override
	String getName()
	{
		return "Research Gun";
	}

	@Override
	boolean checkActivateRule(RobotStatus status, ITargetManager targetMan)
	{
		boolean r1 = status.getOthers() > 0;
		boolean r2 = targetMan.getGunTarget() != null;
		boolean r3 = status.getTime() >= 1;

		return r1 && r2 & r3;
	}

}
