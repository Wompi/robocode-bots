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
package wompi.numbat.radar;

import robocode.RobotStatus;
import robocode.Rules;
import robocode.util.Utils;
import wompi.numbat.target.ITargetManager;
import wompi.numbat.target.NumbatTarget;

public class NumbatWeightedRadar extends ANumbatRadar
{
	public final static double	MAX_RADAR_RATE		= 0.07; // less is more search
	public final static double	DEFAULT_RADAR_WIDTH	= 5.0;
	public final static double	DEFAULT_RADAR_LOCK	= 0.8;

	@Override
	void setRadar(RobotStatus status, ITargetManager targetMan)
	{
		NumbatTarget target = targetMan.getGunTarget();

		if (target.getCurrentScanDifference(status) == 0)
		{
			rTurn = Utils.normalRelativeAngle(target.getAbsoluteBearing(status) - status.getRadarHeadingRadians()) * DEFAULT_RADAR_WIDTH;
			target.eSlipDir = Math.signum(rTurn);
		}
		else
		{
			rTurn = Double.POSITIVE_INFINITY * ((target.eSlipDir == 0) ? 1 : target.eSlipDir); // TODO: zero slipDir is sitting duck, maybe 1 is not
																								// good;
		}

		for (NumbatTarget enemy : targetMan.getAllTargets())
		{
			if (enemy.isAlive)
			{
				if (enemy != target)
				{
					double rate = Rules.MAX_VELOCITY / (enemy.getDistance(status) - enemy.getCurrentScanDifference(status) * Rules.MAX_VELOCITY);

					// NOTE: the GunHeat rule is awesome i guess
					if ((rate >= MAX_RADAR_RATE || rate <= 0) && status.getGunHeat() > DEFAULT_RADAR_LOCK)
					{
						rTurn = Double.POSITIVE_INFINITY * ((enemy.eSlipDir == 0) ? 1 : enemy.eSlipDir); // TODO: zero slipDir is sitting duck, maybe
																											// 1 is not good;
						break;
					}
					else
					{
						enemy.eSlipDir = Utils.normalRelativeAngle(enemy.getAbsoluteBearing(status) - status.getRadarHeadingRadians());

					}
				}
			}
		}
	}

	@Override
	boolean checkActivateRule(RobotStatus status)
	{
		return status.getOthers() > 1;
	}

	@Override
	String getName()
	{
		return "Weighted Radar";
	}
}
