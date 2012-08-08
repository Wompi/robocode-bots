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
import wompi.numbat.target.ITargetManager;

public class NumbatRadarNone extends ANumbatRadar
{

	@Override
	void setRadar(RobotStatus status, ITargetManager targetMan)
	{

	}

	@Override
	String getName()
	{
		return "No Radar";
	}

	@Override
	boolean checkActivateRule(RobotStatus status)
	{
		return status.getOthers() == 0;
	}
}
