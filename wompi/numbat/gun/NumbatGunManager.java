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

import java.awt.Graphics2D;
import java.util.ArrayList;

import robocode.AdvancedRobot;
import robocode.RobotDeathEvent;
import robocode.RobotStatus;
import robocode.ScannedRobotEvent;
import wompi.numbat.debug.DebugGunProperties;
import wompi.numbat.gun.fire.NumbatFireManager;
import wompi.numbat.target.ITargetManager;

public class NumbatGunManager
{
	private RobotStatus					botStatus;
	private ITargetManager				myTargetMan;

	private ANumbatGun					myGun;
	private final NumbatFireManager		myFireMan;
	private final ArrayList<ANumbatGun>	allGuns;

	//NumbatCenterGun						researchGun	= new NumbatCenterGun();
	//NumbatGunSquare						researchGun	= new NumbatGunSquare();

	public NumbatGunManager()
	{
		myFireMan = new NumbatFireManager();
		allGuns = new ArrayList<ANumbatGun>();
		// allGuns.add(new NumbatGunWallaby());
		allGuns.add(new NumbatSTGun());
		// allGuns.add(new NumbatGunResearch());
		allGuns.add(new NumbatGunHeadOn());
		allGuns.add(new NumbatGunNone());
	}

	public void init()
	{
		checkActivate();
		myFireMan.init();
		myGun.init(botStatus);
	}

	public void onRobotDeath(RobotDeathEvent e)
	{
		checkActivate();
		myFireMan.onRobotDeath(e);
	}

	public void onScannedRobot(ScannedRobotEvent e)
	{
		checkActivate();
		myFireMan.onScannedRobot(e);
		myGun.onScannedRobot(e, botStatus, myTargetMan);

		//researchGun.onScannedRobot(e, botStatus, myTargetMan);
	}

	public void setGun()
	{
		myFireMan.setFire();
		myGun.setGun(botStatus, myTargetMan, myFireMan.getFire());

		//researchGun.setGun(botStatus, myTargetMan, myFireMan.getFire());
	}

	public void excecute(AdvancedRobot bot)
	{
		myFireMan.excecute(bot);
		myGun.excecute(bot);
	}

	public void onPaint(Graphics2D g)
	{
		myGun.onPaint(g, botStatus); // TODO: handle multiple guns and whatnot better

		//researchGun.onPaint(g, botStatus);
	}

	private void checkActivate()
	{
		for (ANumbatGun gun : allGuns)
		{
			if (gun.checkActivateRule(botStatus, myTargetMan))
			{
				if (myGun != gun)
				{
					myGun = gun;
					myGun.init(botStatus); // TODO: change this
					DebugGunProperties.debugCurrentGun(myGun.getName());
				}
				return;
			}
		}
		throw new IllegalStateException("NumbatGunManager no gun rule fits");
	}

	public void setTargetManager(ITargetManager targetMan)
	{
		myTargetMan = targetMan;
		myFireMan.setTargetManager(targetMan);
	}

	public void setBotStatus(RobotStatus status)
	{
		botStatus = status;
		myFireMan.setBotStatus(status);
	}
}
