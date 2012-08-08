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
import java.util.HashMap;

import robocode.AdvancedRobot;
import robocode.Rules;
import robocode.util.Utils;
import wompi.echidna.gun.fire.AFire;
import wompi.echidna.misc.DebugPointLists;
import wompi.echidna.target.ATarget;
import wompi.robomath.RobotMath;

public class GunPMNano extends AGun
{
	// enemy values
	ATarget							myTarget;

	// control values
	AFire							myFire;
	// pattern gun
	static final int				PATTERN_DEPTH	= 30;

	int								A				= 'A';
	String							INIT_PATTERN	= ("" + (char) (0 + A) + (char) (0 + A) + (char) (0 + A) + (char) (0 + A) + (char) (0 + A)
															+ (char) (0 + A) + (char) (0 + A) + (char) (0 + A) + (char) (0 + A) + (char) (0 + A)
															+ (char) (0 + A) + (char) (0 + A) + (char) (0 + A) + (char) (0 + A) + (char) (0 + A)
															+ (char) (0 + A) + (char) (0 + A) + (char) (0 + A) + (char) (0 + A) + (char) (0 + A)
															+ (char) (0 + A) + (char) (0 + A) + (char) (0 + A) + (char) (0 + A) + (char) (0 + A)
															+ (char) (8 + A) + (char) (8 + A) + (char) (8 + A) + (char) (8 + A) + (char) (8 + A) + (char) (8 + A));
	static HashMap<ATarget, String>	patternMap		= new HashMap<ATarget, String>();

	DebugPointLists					pointList		= new DebugPointLists();

	public GunPMNano(AdvancedRobot robot)
	{
		super(robot);
	}

	@Override
	public void init(AFire fireControl)
	{
		myRobot.setAdjustGunForRobotTurn(true);
		myFire = fireControl;
		myFire.init();
	}

	@Override
	public void onScannedRobot(ATarget target, boolean isMaintarget)
	{
		if (isMaintarget) myTarget = target;

		String pattern = patternMap.get(target);
		if (pattern == null)
		{
			patternMap.put(target, INIT_PATTERN);
			System.out.format("[%d] name=%s pattern=%s \n", myRobot.getTime(), target.getName(), INIT_PATTERN);
		}
		doGun();
	}

	@Override
	public void run()
	{}

	public void onPaint(Graphics2D g)
	{
		if (myTarget == null) return;
		myFire.doPaint(myTarget, g);
		pointList.onPaint(g);
	}

	// ------------------------------------------ working stuff ---------------------------------------------
	private void doGun()
	{
		if (myTarget == null && myRobot.getOthers() > 0) return;

		myFire.doFire(myTarget);

		int i;
		int mLen = PATTERN_DEPTH;
		int indX;
		double absBearing = myTarget.getBlindAbsBearing();
		double trueDistance = myTarget.getBlindDistance();
		int b1 = (int) Math.round(myTarget.getVelocity() * Math.sin(myTarget.getHeading() - absBearing));
		// String eLog = String.valueOf((char)(b1+A)).concat(patternMap.get(myTarget));
		String eLog = Character.toString((char) (b1 + A)).concat(patternMap.get(myTarget));
		patternMap.put(myTarget, eLog);
		int bIndex = Math.min(eLog.length() - 1, (int) (trueDistance / Rules.getBulletSpeed(myFire.getFirePower(myTarget))));

		// System.out.format("[%d] eLog=%s\n b1=%s bIndex=%d %s\n", myRobot.getTime(),eLog,(char)(b1+A),bIndex,myTarget.getName());

		pointList.reset();

		double gunAngle = absBearing;
		while ((indX = eLog.indexOf(eLog.substring(0, mLen--), (i = bIndex))) < 0);
		do
		{
			gunAngle += Math.asin(((byte) (eLog.charAt(indX--) - A)) / trueDistance);

			// debug
			pointList.goodPoints.add(RobotMath.calculatePolarPoint(gunAngle, trueDistance, new Point2D.Double(myRobot.getX(), myRobot.getY())));
		}
		while (--i > 0);
		myRobot.setTurnGunRightRadians(Utils.normalRelativeAngle(gunAngle - myRobot.getGunHeadingRadians()));
	}

}
