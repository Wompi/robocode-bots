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
import java.util.LinkedHashMap;
import java.util.Map;

import robocode.AdvancedRobot;
import robocode.Rules;
import robocode.util.Utils;
import wompi.echidna.gun.fire.AFire;
import wompi.echidna.target.ATarget;

public class GunPMSingleTick extends AGun
{
	private final static int					MAX_PATTERN_LENGTH	= 30;

	// enemy values
	ATarget										myTarget;
	static HashMap<ATarget, Map<String, int[]>>	matcherMap			= new HashMap<ATarget, Map<String, int[]>>();
	static HashMap<ATarget, String>				enemyHistoryMap		= new HashMap<ATarget, String>();

	// control values
	AFire										myFire;

	public GunPMSingleTick(AdvancedRobot robot)
	{
		super(robot);
	}

	@Override
	public void init(AFire fireControl)
	{
		myFire = fireControl;
		myFire.init();

		myRobot.setAdjustGunForRobotTurn(true);

		for (ATarget target : enemyHistoryMap.keySet())
		{
			enemyHistoryMap.put(target, "");
		}
	}

	@Override
	public void onScannedRobot(ATarget target, boolean isMaintarget)
	{
		if (isMaintarget) myTarget = target;

		String history = enemyHistoryMap.get(target);
		if (history == null)
		{
			enemyHistoryMap.put(target, "");
			matcherMap.put(target, new LRUMap<String, int[]>(10000));
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
	}

	// ------------------------------------------ working stuff ---------------------------------------------
	private void doGun()
	{
		if (myTarget == null) return;
		myFire.doFire(myTarget);

		// memorize.
		double enemyH = myTarget.getHeading();
		double absBearing = myTarget.getAbsBearing();
		int enemyV = (int) Math.rint(myTarget.getVelocity());
		int thisStep = encode(myTarget.getHeadDiff(), enemyV);
		if (thisStep == (char) -1)
		{
			// The turn rate we saw was greater than possible, so this must have
			// been our first scan of the round. Don't try to do anything with
			// this illegal symbol; just wait till next tick.
			return;
		}
		String enemyHistory = enemyHistoryMap.get(myTarget);
		record(thisStep, enemyHistory, matcherMap.get(myTarget));
		enemyHistory = (char) thisStep + enemyHistory;
		enemyHistoryMap.put(myTarget, enemyHistory);

		// aim.
		Point2D.Double myP = new Point2D.Double(myRobot.getX(), myRobot.getY());
		Point2D.Double enemyP = new Point2D.Double(myTarget.getAbsX(), myTarget.getAbsY());
		String pattern = enemyHistory;
		for (double d = 0; d < myP.distance(enemyP); d += Rules.getBulletSpeed(myFire.getFirePower(myTarget)))
		{
			int nextStep = predict(pattern, matcherMap.get(myTarget));
			enemyH += decodeDH(nextStep);
			enemyV = decodeV(nextStep);

			enemyP.x += enemyV * Math.sin(enemyH);
			enemyP.y += enemyV * Math.cos(enemyH);

			pattern = (char) nextStep + pattern;
		}
		absBearing = Math.atan2(enemyP.x - myP.x, enemyP.y - myP.y);
		double gunTurn = absBearing - myRobot.getGunHeadingRadians();
		myRobot.setTurnGunRightRadians(Utils.normalRelativeAngle(gunTurn));
	}

	/**
	 * Increments the number of times "thisStep" was taken after the last series
	 * of moves the enemy made.
	 * 
	 * @param thisStep
	 *        The symbol representing the step the enemy took immediately after
	 *        those in "enemyHistory".
	 */
	private void record(int thisStep, String enemyHistory, Map<String, int[]> matcher)
	{
		int maxLength = Math.min(MAX_PATTERN_LENGTH, enemyHistory.length());
		for (int i = 0; i <= maxLength; ++i)
		{
			String pattern = enemyHistory.substring(0, i);
			int[] frequencies = matcher.get(pattern);
			if (frequencies == null)
			{
				// frequency tables need to hold 21 possible dh values times 17 possible v values
				frequencies = new int[21 * 17];
				matcher.put(pattern, frequencies);
			}
			++frequencies[thisStep];
		}
	}

	/**
	 * @return The symbol representing the step the enemy has most frequently
	 *         taken after those in "pattern".
	 */
	private int predict(String pattern, Map<String, int[]> matcher)
	{
		int[] frequencies = null;
		for (int patternLength = Math.min(pattern.length(), MAX_PATTERN_LENGTH); frequencies == null; --patternLength)
		{
			frequencies = matcher.get(pattern.substring(0, patternLength));
		}
		int nextTick = 0;
		for (int i = 1; i < frequencies.length; ++i)
		{
			if (frequencies[nextTick] < frequencies[i])
			{
				nextTick = i;
			}
		}
		return nextTick;
	}

	/**
	 * @param dh
	 *        The change in the enemy's heading from last tick to this tick.
	 * @param v
	 *        The enemy's current velocity.
	 * @return The symbol to use in the symbolic pattern matcher, or "(char) -1"
	 *         if dh was out-of-bounds (greater than the maximum turn rate).
	 */
	private static int encode(double dh, int v)
	{
		if (Math.abs(dh) > Rules.MAX_TURN_RATE_RADIANS)
		{
			// this is to catch the start-of-round, when illegal
			// symbols could otherwise be generated.
			return (char) -1;
		}
		int dhCode = (int) Math.rint(Math.toDegrees(dh)) + 10;
		int vCode = v + 8;
		return (char) (17 * dhCode + vCode);
	}

	/**
	 * @return The change in the enemy's heading that "symbol" represents.
	 */
	private static double decodeDH(int symbol)
	{
		return Math.toRadians(symbol / 17 - 10);
	}

	/**
	 * @return The enemy's velocity that "symbol" represents.
	 */
	private static int decodeV(int symbol)
	{
		return symbol % 17 - 8;
	}
}

/**
 * A hash map with a maximum size. After this size has been reached, adding new
 * mappings will kick out the least recently used entries in the map.
 * 
 * @author Simonton
 */
class LRUMap<K, T> extends LinkedHashMap<K, T>
{
	private static final long	serialVersionUID	= 7405744855997087301L;

	private final int			maxSize;
	private boolean				hitCap				= false;

	public LRUMap(int maxSize)
	{
		super((int) (maxSize / .75) + 2, .75f, true);
		this.maxSize = maxSize;
	}

	protected boolean removeEldestEntry(Map.Entry<K, T> eldest)
	{
		if (size() > maxSize)
		{
			if (!hitCap)
			{
				System.out.println("Hit memory cap (" + maxSize + ")");
				hitCap = true;
			}
			return true;
		}
		return false;
	}
}
