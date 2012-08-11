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

import java.awt.Graphics2D;
import java.util.ArrayList;

import robocode.AdvancedRobot;
import robocode.RobotDeathEvent;
import robocode.RobotStatus;
import robocode.ScannedRobotEvent;
import wompi.numbat.debug.DebugRadarProperties;
import wompi.numbat.target.ITargetManager;

public class NumbatRadarManager
{
	private RobotStatus						botStatus;
	private ITargetManager					myTargetMan;

	private ANumbatRadar					myRadar;

	private final ArrayList<ANumbatRadar>	allRadars;

	public NumbatRadarManager()
	{
		allRadars = new ArrayList<ANumbatRadar>();
		allRadars.add(new NumbatRadarSingle());
		//allRadars.add(new NumbatRadarMeleeField());
		allRadars.add(new NumbatWeightedRadar());
		allRadars.add(new NumbatRadarNone());
	}

	public void init()
	{
		checkActivate();
		myRadar.init(botStatus);
	}

	public void onRobotDeath(RobotDeathEvent e)
	{
		checkActivate();
	}

	public void onScannedRobot(ScannedRobotEvent e)
	{
		checkActivate();
		myRadar.onScannedRobot(e, botStatus);
	}

	public void setRadar()
	{
		if (!myRadar.isStartSearch(botStatus))
		{
			myRadar.setRadar(botStatus, myTargetMan);
		}
	}

	public void excecute(AdvancedRobot bot)
	{
		myRadar.excecute(bot);
	}

	private void checkActivate()
	{
		for (ANumbatRadar radar : allRadars)
		{
			if (radar.checkActivateRule(botStatus))
			{
				if (myRadar != radar)
				{
					myRadar = radar;
					DebugRadarProperties.debugCurrentRadar(myRadar.getName());
				}
				return;
			}
		}
		throw new IllegalStateException("NumbatRadarManager");
	}

	public void setTargetManager(ITargetManager targetMan)
	{
		myTargetMan = targetMan;
	}

	public void setBotStatus(RobotStatus status)
	{
		botStatus = status;
	}

	public void onPaint(Graphics2D g)
	{
		if (!myRadar.isStartSearch(botStatus))
		{
			myRadar.onPaint(g, botStatus, myTargetMan);
		}
	}

}
