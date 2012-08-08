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
import robocode.Condition;
import robocode.Rules;
import robocode.util.Utils;
import wompi.echidna.gun.fire.AFire;
import wompi.echidna.target.ATarget;
import wompi.wallaby.WallabyPainter;

public class GunGuessFactor extends AGun
{
	private static final int		DISTANCE_INDEXES	= 8;
	private static final int		VELOCITY_INDEXES	= 5;
	private static final int		BINS				= 25;

	// enemy values
	ATarget							myTarget;

	// controll values
	AFire							myFire;

	HashMap<ATarget, int[][][][]>	segmentMap			= new HashMap<ATarget, int[][][][]>();

	public GunGuessFactor(AdvancedRobot robot)
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

		double tVelocity = target.getVelocity();

		int[][][][] segments = segmentMap.get(target);
		if (segments == null)
		{
			segmentMap.put(target, segments = new int[DISTANCE_INDEXES][VELOCITY_INDEXES][VELOCITY_INDEXES][BINS]);
		}

		int[] cSegments = segments[(int) (target.getDistance() / 200)][(int) Math.abs(tVelocity / 2)][(int) Math.abs(target.getLastVelocity() / 2)];

		GuessFactorWave wave = new GuessFactorWave();
		wave.myWaveTarget = target;
		wave.eAbsBearing = target.getAbsBearing();
		wave.eDirection = target.getDirection();
		wave.segResult = cSegments;
		wave.rx = myRobot.getX();
		wave.ry = myRobot.getY();
		wave.bSpeed = Rules.getBulletSpeed(myFire.getFirePower(target));
		myRobot.addCustomEvent(wave);
	}

	@Override
	public void run()
	{
		doGun();
	}

	public void onPaint(Graphics2D g)
	{
		if (myTarget == null) return;
		WallabyPainter.drawCenterPos(g, new Point2D.Double(myTarget.getAbsX(), myTarget.getAbsY()), myTarget.getBlindDistance());
		myFire.doPaint(myTarget, g);
	}

	// ------------------------------------------ working stuff ---------------------------------------------
	private void doGun()
	{
		if (myTarget == null) return;
		myFire.doFire(myTarget);

		double currentDistance = Point2D.distance(myRobot.getX(), myRobot.getY(), myTarget.getAbsX(), myTarget.getAbsY());

		int[] cSegments = segmentMap.get(myTarget)[(int) (currentDistance / 200)][(int) Math.abs(myTarget.getVelocity() / 2)][(int) Math.abs(myTarget
				.getLastVelocity() / 2)];

		int bIndex = BINS / 2 - 1;
		for (int i = 0; i < BINS; i++)
		{
			if (cSegments[bIndex] < cSegments[i]) bIndex = i;
		}
		double gFactor = (double) (bIndex - (cSegments.length - 1) / 2) / ((cSegments.length - 1) / 2);
		double gunDelta = myTarget.getDirection() * gFactor * Math.asin(8 / Rules.getBulletSpeed(myFire.getFirePower(myTarget)));

		double currentAbsBearing = Math.atan2(myTarget.getAbsX() - myRobot.getX(), myTarget.getAbsY() - myRobot.getY());
		myRobot.setTurnGunRightRadians(Utils.normalRelativeAngle(currentAbsBearing + gunDelta - myRobot.getGunHeadingRadians()));
	}

	class GuessFactorWave extends Condition
	{
		ATarget			myWaveTarget;
		double			eAbsBearing;
		double			eDirection;
		double			rx;
		double			ry;
		double			bSpeed;
		private double	count;
		int[]			segResult;

		@Override
		public boolean test()
		{
			if (Point2D.distance(myWaveTarget.getAbsX(), myWaveTarget.getAbsY(), rx, ry) < (++count * bSpeed))
			{
				double eGunDelta = Utils.normalRelativeAngle(Math.atan2(myWaveTarget.getAbsX() - myRobot.getX(),
						myWaveTarget.getAbsY() - myRobot.getY())
						- eAbsBearing);  //
				double guessFactor = Math.max(-1, Math.min(1, eGunDelta / Math.asin(8 / bSpeed))) * eDirection;
				int index = (int) Math.round((segResult.length - 1) / 2 * (guessFactor + 1));
				segResult[index]++;
				myRobot.removeCustomEvent(this);
				return true;
			}
			return false;
		}
	}

}
