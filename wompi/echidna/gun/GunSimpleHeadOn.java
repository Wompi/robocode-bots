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
import robocode.util.Utils;
import wompi.echidna.gun.fire.AFire;
import wompi.echidna.target.ATarget;
import wompi.wallaby.WallabyPainter;

public class GunSimpleHeadOn extends AGun
{
	// enemy values
	ATarget	myTarget;

	// control values
	AFire	myFire;

	public GunSimpleHeadOn(AdvancedRobot robot)
	{
		super(robot);
	}

	@Override
	public void init(AFire fireControl)
	{
		myFire = fireControl;
		myFire.init();
	}

	@Override
	public void onScannedRobot(ATarget target, boolean isMaintarget)
	{
		if (isMaintarget) myTarget = target;
		doGun();
	}

	@Override
	public void run()
	{}

	public void onPaint(Graphics2D g)
	{
		if (myTarget == null) return;
		WallabyPainter.drawCenterPos(g, new Point2D.Double(myTarget.getAbsX(), myTarget.getAbsY()), myTarget.getDistance());
		myFire.doPaint(myTarget, g);
	}

	// ------------------------------------------ working stuff ---------------------------------------------
	private void doGun()
	{
		if (myTarget == null) return;
		myFire.doFire(myTarget);

		double mainDelta = myRobot.getHeadingRadians() + myTarget.getBearing() - myRobot.getGunHeadingRadians();
		myRobot.setTurnGunRightRadians(Utils.normalRelativeAngle(mainDelta));
	}

}
