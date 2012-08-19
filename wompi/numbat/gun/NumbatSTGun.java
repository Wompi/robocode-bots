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
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Map;

import robocode.AdvancedRobot;
import robocode.RobotStatus;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;
import wompi.numbat.gun.fire.ANumbatFire;
import wompi.numbat.gun.misc.INumbatTick;
import wompi.numbat.gun.misc.NumbatMultiHolder;
import wompi.numbat.gun.misc.NumbatSingleHolder;
import wompi.numbat.misc.NumbatBattleField;
import wompi.numbat.target.ITargetManager;
import wompi.numbat.target.NumbatTarget;
import wompi.robomath.RobotMath;

public class NumbatSTGun extends ANumbatGun
{
	private final static double	WZ						= 17.9999;
	public final static int		DEFAULT_PATTERN_LENGTH	= 30;
	public final static double	DEAULT_HALF_BOTWEIGHT	= 18;
	private static Rectangle2D	B_FIELD;

	// debug
	//private final DebugPointLists	debugPointList			= new DebugPointLists();

	public NumbatSTGun()
	{}

	@Override
	public void init(RobotStatus status)
	{
		B_FIELD = new Rectangle2D.Double(WZ, WZ, NumbatBattleField.BATTLE_FIELD_W - 2 * WZ, NumbatBattleField.BATTLE_FIELD_H - 2 * WZ);
	}

	@Override
	public void setGun(RobotStatus status, ITargetManager targetMan, ANumbatFire fire)
	{
		//TestPatternAccuracy.registerActualPattern(status.getTime(), 'X');
		NumbatTarget target = targetMan.getGunTarget();
		// myFire = fire;

		double heading = target.eHeading;
		double xg = target.x;
		double yg = target.y;

		StringBuilder ePattern = new StringBuilder(target.eHistory);

		long deltaScan = target.getCurrentScanDifference(status);
		//System.out.format("[%d] deltaScan = %d \n", status.getTime(), deltaScan);

		//debugPointList.reset();
		//StringBuilder sDesire = new StringBuilder();
		// long count = 0;

		boolean isFirst = true;

		// TODO: the gunheat rule ruins the avg pattern length - think about another saver rule or extract the startpattern in a function anyway 
		if (ePattern.length() > 0 && deltaScan < 10 /* && status.getGunHeat() <= 0.5 */)
		{
			//System.out.format("[%d] NEW PATTERN %s\n", status.getTime(), "");
			int count = 0;
			for (double bDist = 0; (bDist - deltaScan * fire.getBulletSpeed()) < Point2D.distance(status.getX(), status.getY(), xg, yg); bDist += fire
					.getBulletSpeed())
			{
				int nextStep = 0;
				int patternLength = Math.min(target.eMatchKeyLength, ePattern.length());
				INumbatTick tick = null;
				String debugStr = "";
				int len = 0;
				for (len = patternLength; tick == null; --len)
				{
					// System.out.format("len=%d pLen=%d\n", len,patternLength);
					if (len == 0)
					{
						//System.out.format("[%d] never seen this state lets take the heading and velocity ...\n", count);
						break;
						// len 0 means a new pattern we haven't seen before - what do to?
						// taking the heading change and velocity of the target would lead to circular gun
						// breaking the loop and just head on target would be also possible
						// maybe backtrack the pattern to find something that is similar to this pattern - just restart the loop without the last state 
					}
					else
					{
						debugStr = ePattern.substring(0, len);
						tick = target.matcherMap.get(debugStr.hashCode()); // automatic cast to Integer
					}
				}

				double velocity;
				double headingDelta;
				if (tick != null)
				{
					NumbatSingleHolder singleTick = tick.getMaxTick();
					nextStep = singleTick.myID;
					headingDelta = singleTick.tHeadingDelta;
					velocity = singleTick.tVelocity;

					if (isFirst)
					{
						target.registerPatternLength(len + 1);
						isFirst = false;
					}
				}
				else
				{
					headingDelta = target.getHeadingDifference();
					velocity = target.getAverageVelocity();
					nextStep = NumbatSingleHolder.getEncodedID(headingDelta, velocity);
				}
				heading += headingDelta;

				xg += velocity * Math.sin(heading);
				yg += velocity * Math.cos(heading);

				if (!B_FIELD.contains(xg, yg))
				{
					xg = RobotMath.limit(DEAULT_HALF_BOTWEIGHT, xg, NumbatBattleField.BATTLE_FIELD_W - DEAULT_HALF_BOTWEIGHT);
					yg = RobotMath.limit(DEAULT_HALF_BOTWEIGHT, yg, NumbatBattleField.BATTLE_FIELD_H - DEAULT_HALF_BOTWEIGHT);
					nextStep = NumbatSingleHolder.getEncodedID(headingDelta, 0);
					//debugPointList.badPoints.add(new Point2D.Double(xg, yg));
				}
				//else debugPointList.goodPoints.add(new Point2D.Double(xg, yg));
				ePattern.insert(0, (char) nextStep);
				count++;
				//sDesire.insert(0, (char) nextStep);
			}
		}
		//TestPatternAccuracy.registerDesiredPattern(status.getTime(), sDesire);
		//debugPointList.targetPoint = new Point2D.Double(xg, yg);

		gTurn = Utils.normalRelativeAngle(Math.atan2(xg - status.getX(), yg - status.getY()) - status.getGunHeadingRadians());

	}

	@Override
	public void onScannedRobot(ScannedRobotEvent scan, RobotStatus status, ITargetManager targetMan)
	{
		NumbatTarget target = targetMan.getLastScanTarget();

		// TODO: get rid of this, because it breaks the pattern.
		// if you miss a scan the bot moves blind but the next step will be saved as the follow step to the step two ticks before. Certainly this
		// is very bad to hold consistent pattern and can lead to very bad hit results against simple mover like walls.
		// One scan misses can be calculated with NewPoint (geodesy) calculations and should quite precise (maybe a simple linear calculation will do
		// as well)
		double hDiff = target.getHeadingDifference();
		double hMax = Rules.getTurnRateRadians(target.eLastVelocity) + 0.00001;
		long scanDiff = target.getLastScanDifference();
		if (scanDiff > 1)
		{
			// TODO: interpolate this to 2 or maybe 3 steps if possible
			// if (scanDiff <= 3)
			// {
			// System.out.format("[%d] hDiff=%3.10f hMax=%3.10f %d dDist=%3.4f v=%3.2f lastV=%3.2f - scantime=%d %s\n",status.getTime(),Math.toDegrees(hDiff),Math.toDegrees(hMax),target.getLastScanDifference(),target.getLastDistanceDifference(),target.eVelocity,target.eLastVelocity,scan.getTime(),target.eName);
			// }
			target.eHistory.setLength(0);
			return;
		}
		else if (scanDiff <= 1 && Math.abs(hDiff) > hMax)
		{
			// this state is clearly an robocode bug, but it happens frequently because of the bad skipped turn behaivior
			// System.out.format("[%d] hDiff=%3.10f hMax=%3.10f %d dDist=%3.4f - scantime=%d %s\n",status.getTime(),Math.toDegrees(hDiff),Math.toDegrees(hMax),target.getLastScanDifference(),target.getLastDistanceDifference(),scan.getTime(),target.eName);
			target.eHistory.setLength(0);
			return;
		}

		record(hDiff, target.eVelocity, target.eHistory, target.matcherMap, target.eMatchKeyLength, status.getTime());

		// register the startPattern for every scanned target
		// TODO: get rid of this code copy and find something appropriate
		int patternLength = Math.min(target.eMatchKeyLength, target.eHistory.length());
		if (patternLength > 3) // because of the history reset on missed turns this would lead to very bad avg pattern length - hopefully 4 is enough to make it work
		{
			INumbatTick tick = null;
			int len = 0;
			for (len = patternLength; tick == null; --len)
			{
				// System.out.format("len=%d pLen=%d\n", len,patternLength);
				if (len == 0)
				{
					break;
				}
				else
				{
					String debugStr = target.eHistory.substring(0, len);
					tick = target.matcherMap.get(debugStr.hashCode()); // automatic cast to Integer
				}
			}
			if (tick != null) target.registerPatternLength(len);
		}
	}

	private void record(double deltaHead, double velocity, StringBuilder history, Map<Integer, INumbatTick> matchMap, int keylen, long time)
	{
		int thisStep = NumbatSingleHolder.getEncodedID(deltaHead, velocity);

		// No matter what happen don't use i=0 ever again. this trashes the whole system with unnecessary? objects and the loop slows down like hell
		// There is one nice effect with i=0, if the pattern is not known before it takes always the last key as next step. What makes it a circular gun
		// of some sort. Unfortunately is the circular gun just plain simple and has no additional enhancements which makes it useless against everything that 
		// is not moving in a easy pattern - what makes it even more useless because if the pattern is weak the gun hits quite well and the strong bots
		// did not get any hit.
		for (int i = 1; i <= history.length(); ++i)
		{
			int pHash;
			INumbatTick tick; // automatic cast to Integer
			if ((tick = matchMap.get(pHash = history.substring(0, i).hashCode())) == null)
			{
				matchMap.put(pHash, tick = NumbatSingleHolder.getNewInstance(deltaHead, velocity));
			}

			if (!tick.incrementCount(thisStep))
			{
				INumbatTick multiTick;
				if (tick instanceof NumbatSingleHolder)
				{
					multiTick = new NumbatMultiHolder((NumbatSingleHolder) tick);
				}
				else multiTick = tick;

				//System.out.format("[%d] hash=%d ", time, pHash);
				multiTick.addTick(deltaHead, velocity);
				matchMap.put(pHash, multiTick);
			}
		}

		history.insert(0, (char) (thisStep));
		history.setLength(Math.min(keylen, history.length())); // there the string is only max len space and needs less memory
		// System.out.format("[%d] len=%d %s\n", status.getTime(),target.eHistory.length(),target.eHistory.toString());
		//TestPatternAccuracy.registerActualPattern(time, (char) thisStep);
	}

	@Override
	public void excecute(AdvancedRobot myBot)
	{
		super.excecute(myBot);
		//TestPatternAccuracy.onPrint(myBot.getTime());
	}

	@Override
	public void onPaint(Graphics2D g, RobotStatus status)
	{
		//debugPointList.onPaint(g);
	}

	@Override
	String getName()
	{
		return "SingleTick Pattern Matcher";
	}

	@Override
	boolean checkActivateRule(RobotStatus status, ITargetManager targetMan)
	{
		boolean r1 = status.getOthers() > 0;
		boolean r2 = targetMan.getGunTarget() != null;
		boolean r3 = status.getTime() >= 1;

		return r1 && r2 && r3;
	}
}
