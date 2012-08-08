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
package wompi;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import robocode.AdvancedRobot;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;

/**
 * This is a robot that fires using naive, single-tick, symbolic pattern
 * matching, for demonstration purposes.
 * 
 * @author Simonton
 */
public class SingleTick extends AdvancedRobot
{

	/**
	 * Controls the largest pattern size that will try to be matched.
	 */
	private static final int			MAX_PATTERN_LENGTH	= 30;

	/**
	 * Just what it sounds like (set it to 3 for a targeting challenge, .5 for
	 * the pattern matcher challenge).
	 */
	private static final double			FIRE_POWER			= .5;
	private static final double			FIRE_SPEED			= Rules.getBulletSpeed(FIRE_POWER);

	/**
	 * Maps all the patterns in which the enemy bot moves to an array containing
	 * the number of times it took each possible step during the next tick.
	 * <p>
	 * The number passed to its constructor controls the number of patterns you allow SingleTick to store, and therefore the amount of memory it will
	 * consume. This is currently calibrated to take its fair share of memory on a 256MB client (about 100MB, depending on how predictable is the
	 * enemy and how long are the rounds). Increase this number and watch SingleTick's performance improve!
	 */
	private static Map<String, int[]>	matcher				= new LRUMap<String, int[]>(40000);

	/**
	 * Stores predicted positions for onPaint().
	 */
	private static List<Point2D.Double>	predictions			= new ArrayList<Point2D.Double>();

	/**
	 * The direction the enemy was facing last tick.
	 */
	private static double				lastEnemyHeading;

	/**
	 * A running history of each step the enemy takes.
	 */
	private static String				enemyHistory;

	public void run()
	{
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);

		// reset the enemy history every round, so that we don't get wierd
		// patterns from between rounds
		enemyHistory = "";

		// 100% radar lock :)
		turnRadarRight(Double.POSITIVE_INFINITY);
		do
		{
			scan();
		}
		while (true);
	}

	public void onScannedRobot(ScannedRobotEvent e)
	{

		// fire.
		setFire(FIRE_POWER);

		// look.
		double absoluteBearing = e.getBearingRadians() + getHeadingRadians();
		double radarTurn = absoluteBearing - getRadarHeadingRadians();
		setTurnRadarRightRadians(Utils.normalRelativeAngle(radarTurn));

		// memorize.
		double enemyH = e.getHeadingRadians();
		int enemyV = (int) Math.rint(e.getVelocity());
		int thisStep = encode(enemyH - lastEnemyHeading, enemyV);
		lastEnemyHeading = enemyH;
		if (thisStep == (char) -1)
		{
			// The turn rate we saw was greater than possible, so this must have
			// been our first scan of the round. Don't try to do anything with
			// this illegal symbol; just wait till next tick.
			return;
		}
		record(thisStep);
		enemyHistory = (char) thisStep + enemyHistory;

		// aim.
		predictions.clear(); // comment to speed execution a LITTLE.
		Point2D.Double myP = new Point2D.Double(getX(), getY());
		Point2D.Double enemyP = project(myP, absoluteBearing, e.getDistance());
		// // Uncomment to speed execution a LOT.
		// double turnsTillFire = getGunHeat() / getGunCoolingRate();
		// if (turnsTillFire < 3.000000001) { // account for double in-precision
		String pattern = enemyHistory;
		for (double d = 0; d < myP.distance(enemyP); d += FIRE_SPEED)
		{
			int nextStep = predict(pattern);
			enemyH += decodeDH(nextStep);
			enemyV = decodeV(nextStep);
			enemyP = project(enemyP, enemyH, enemyV);
			predictions.add(enemyP); // comment to speed execution a LITTLE.
			pattern = (char) nextStep + pattern;
		}
		// }
		// // Uncomment to speed execution a LOT.
		absoluteBearing = Math.atan2(enemyP.x - myP.x, enemyP.y - myP.y);
		double gunTurn = absoluteBearing - getGunHeadingRadians();
		setTurnGunRightRadians(Utils.normalRelativeAngle(gunTurn));
	}

	public void onPaint(Graphics2D g)
	{
		g.setColor(Color.WHITE);
		for (Point2D.Double p : predictions)
		{
			g.fillOval((int) p.x - 1, (int) p.y - 1, 3, 3);
		}
	}

	/**
	 * Increments the number of times "thisStep" was taken after the last series
	 * of moves the enemy made.
	 * 
	 * @param thisStep
	 *        The symbol representing the step the enemy took immediately after
	 *        those in "enemyHistory".
	 */
	private void record(int thisStep)
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
	private int predict(String pattern)
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

	/**
	 * @return The point which is "distance" pixels away from "p" in the
	 *         direction of "angle".
	 */
	private static Point2D.Double project(Point2D.Double p, double angle, double distance)
	{
		double x = p.x + distance * Math.sin(angle);
		double y = p.y + distance * Math.cos(angle);
		return new Point2D.Double(x, y);
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
	private final int	maxSize;
	private boolean		hitCap	= false;

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
