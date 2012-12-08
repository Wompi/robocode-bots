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

import java.awt.geom.Rectangle2D;

import robocode.RobotStatus;
import robocode.Rules;
import robocode.util.Utils;
import wompi.numbat.gun.fire.ANumbatFire;
import wompi.numbat.misc.NumbatBattleField;
import wompi.numbat.target.ITargetManager;
import wompi.numbat.target.NumbatTarget;

public class NumbatGunWallaby extends ANumbatGun
{
	private final static double	PI_360				= Math.PI * 2.0;
	private final static double	DELTA_RISK_ANGLE	= Math.PI / 32.0;
	private final static double	MAX_HEAD_DIFF		= 0.161442955809475;	// 9.25 degree

	private static final double	WZ_G				= 17.0;

	private static Rectangle2D	bField;

	@Override
	void setGun(RobotStatus status, ITargetManager targetMan, ANumbatFire fire)
	{
		NumbatTarget target = targetMan.getGunTarget();

		if (bField == null)
		{
			bField = new Rectangle2D.Double(WZ_G, WZ_G, NumbatBattleField.BATTLE_FIELD_W - 2 * WZ_G, NumbatBattleField.BATTLE_FIELD_H - 2 * WZ_G);
		}

		double xg = target.x;
		double yg = target.y;
		double h1 = target.eHeading;
		double v2 = target.getAverageVelocity();

		double hDiff = target.getHeadingDifference();
		double hMax = Rules.getTurnRateRadians(target.eLastVelocity) + 0.00001;
		long scanDiff = target.getLastScanDifference();
		if (scanDiff > 1)
		{
			hDiff = 0;
		}
		else if (scanDiff <= 1 && Math.abs(hDiff) > hMax)
		{
			// this state is clearly an robocode bug, but it happens frequently because of the bad skipped turn behaivior
			System.out.format("[%d] hDiff=%3.10f hMax=%3.10f %d - scantime=%d %s\n", status.getTime(), Math.toDegrees(hDiff), Math.toDegrees(hMax),
					target.getLastScanDifference(), status.getTime(), target.eName);
			hDiff = 0;
		}

		double v0 = 0;
		int i = 0;

		while ((v0 += DELTA_RISK_ANGLE) <= PI_360)
		{
			if (((i += 0.9) * Rules.getBulletSpeed(fire.getBulletPower()) < Math.hypot(xg, yg)))
			{
				h1 += hDiff;
				xg += Math.sin(h1) * v2;
				yg += Math.cos(h1) * v2;
				if (!bField.contains(xg, yg))
				{
					v2 = -v2;
				}
			}
		}
		gTurn = Utils.normalRelativeAngle(Math.atan2(xg - status.getX(), yg - status.getY()) - status.getGunHeadingRadians());
	}

	@Override
	String getName()
	{
		return "Precise Circular Gun";
	}

	@Override
	boolean checkActivateRule(RobotStatus status, ITargetManager targetMan)
	{
		NumbatTarget target = targetMan.getGunTarget();
		boolean r1 = status.getOthers() > 0;
		boolean r2 = status.getTime() >= 1;
		boolean r3 = target != null;
		boolean r4 = false;
		boolean r5 = false;
		if (r3)
		{
			r4 = target.getDistance(status) <= 300;
			r5 = target.getAveragePatternLength() <= 10; /*&& target.eVisits > 300*/;

		}
		return r1 && r2 & r3 && r4 && r5;
		// return r1 && r2 && r3;
	}

}
