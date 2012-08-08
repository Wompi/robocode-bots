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
package wompi.numbat.gun.fire;

import java.util.ArrayList;

import robocode.AdvancedRobot;
import robocode.RobotDeathEvent;
import robocode.RobotStatus;
import robocode.ScannedRobotEvent;
import wompi.numbat.debug.DebugGunProperties;
import wompi.numbat.target.ITargetManager;

public class NumbatFireManager
{
	private RobotStatus				botStatus;
	private ITargetManager			myTargetMan;

	private ANumbatFire				myFire;
	private ArrayList<ANumbatFire>	allFire;

	public NumbatFireManager()
	{
		allFire = new ArrayList<ANumbatFire>();
		allFire.add(new NumbatFirePatternChallenge());
		// allFire.add(new NumbatFireMin());
		allFire.add(new NumbatFireMax());
		allFire.add(new NumbatLogDistanceFire());
		// allFire.add(new NumbatMagicBulletPower());
		allFire.add(new NumbatNoneFire());
	}

	public void init()
	{
		checkActivate();
		myFire.init(botStatus);
	}

	public void onScannedRobot(ScannedRobotEvent e)
	{
		checkActivate();
		myFire.onScannedRobot(e, botStatus, myTargetMan);
	}

	public void onRobotDeath(RobotDeathEvent e)
	{
		checkActivate();
	}

	public void setFire()
	{
		myFire.setFire(botStatus, myTargetMan);
	}

	public void excecute(AdvancedRobot bot)
	{
		myFire.excecute(bot);
	}

	private void checkActivate()
	{
		for (ANumbatFire fire : allFire)
		{
			if (fire.checkActivateRule(botStatus, myTargetMan))
			{
				myFire = fire;
				DebugGunProperties.debugCurrentFire(myFire.getName());
				return;
			}
		}
		throw new IllegalStateException("NumbatFireManager");
	}

	public void setTargetManager(ITargetManager targetMan)
	{
		myTargetMan = targetMan;
	}

	public void setBotStatus(RobotStatus status)
	{
		botStatus = status;
	}

	public ANumbatFire getFire()
	{
		return myFire;
	}

}
