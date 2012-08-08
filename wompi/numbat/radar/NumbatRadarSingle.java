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
import robocode.util.Utils;
import wompi.numbat.target.ITargetManager;
import wompi.numbat.target.NumbatTarget;

public class NumbatRadarSingle extends ANumbatRadar
{
	private final static double	LOCK_OFFSET	= 1.9;

	private double				slipDir;

	@Override
	void setRadar(RobotStatus status, ITargetManager targetMan)
	{
		NumbatTarget target = targetMan.getRadarTarget();

		if (target.getCurrentScanDifference(status) == 0)
		{
			rTurn = Utils.normalRelativeAngle(target.getAbsoluteBearing(status) - status.getRadarHeadingRadians()) * LOCK_OFFSET;
			slipDir = Math.signum(rTurn);
		}
		else
		{
			rTurn = Double.POSITIVE_INFINITY * ((slipDir == 0) ? 1 : slipDir);  // TODO: zero slipDir is sitting duck, maybe 1 is not good
		}
	}

	@Override
	boolean checkActivateRule(RobotStatus status)
	{
		return status.getOthers() == 1;		// activates on zero targets to
	}

	@Override
	String getName()
	{
		return "Single Radar";
	}

}
