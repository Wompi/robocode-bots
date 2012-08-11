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
import java.util.Map;

import robocode.AdvancedRobot;
import robocode.RobotStatus;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;
import wompi.echidna.misc.DebugPointLists;
import wompi.echidna.misc.utils.BattleField;
import wompi.numbat.gun.fire.ANumbatFire;
import wompi.numbat.gun.misc.ANumbatSTCode;
import wompi.numbat.gun.misc.INumbatTick;
import wompi.numbat.gun.misc.NumbatMultiHolder;
import wompi.numbat.gun.misc.NumbatST_dH_V_A;
import wompi.numbat.gun.misc.NumbatSingleHolder;
import wompi.numbat.target.ITargetManager;
import wompi.numbat.target.NumbatTarget;

public class NumbatSTGun extends ANumbatGun
{
	private final static double		WZ						= 17.9999;
	public final static int			DEFAULT_PATTERN_LENGTH	= 30;

	private final ANumbatSTCode		myCode;

	// debug
	private final DebugPointLists	debugPointList			= new DebugPointLists();

	public NumbatSTGun()
	{
		myCode = new NumbatST_dH_V_A();
	}

	@Override
	public void init(RobotStatus status)
	{}

	@Override
	public void setGun(RobotStatus status, ITargetManager targetMan, ANumbatFire fire)
	{
		//TestPatternAccuracy.registerActualPattern(status.getTime(), 'X');
		NumbatTarget target = targetMan.getGunTarget();
		// myFire = fire;

		double heading = target.eHeading;
		double velocity = 0;
		double pHeadChange = 0;
		double xg = target.x;
		double yg = target.y;

		StringBuilder ePattern = new StringBuilder(target.eHistory);

		long deltaScan = target.getCurrentScanDifference(status);
		//System.out.format("[%d] deltaScan = %d \n", status.getTime(), deltaScan);

		debugPointList.reset();
		//StringBuilder sDesire = new StringBuilder();
		// long count = 0;

		boolean isFirst = true;

		// TODO: the gunheat rule ruins the avg pattern length - think about another saver rule or extract the startpattern in a function anyway 
		if (ePattern.length() > 0 && deltaScan < 10 /* && status.getGunHeat() <= 0.5 */)
		{
			for (double bDist = 0; (bDist - deltaScan * fire.getBulletSpeed()) < Point2D.distance(status.getX(), status.getY(), xg, yg); bDist += fire
					.getBulletSpeed())
			{
				int nextStep = 0;
				int patternLength = Math.min(target.eMatchKeyLength, ePattern.length());
				INumbatTick tick = null;
				int len = 0;
				for (len = patternLength; tick == null; --len)
				{
					// System.out.format("len=%d pLen=%d\n", len,patternLength);
					String debugStr = ePattern.substring(0, len);
					tick = target.matcherMap.get(debugStr.hashCode()); // automatic cast to Integer
				}
				nextStep = tick.getMaxTick().myID;
				if (isFirst)
				{
					target.registerPatternLength(len + 1);
					isFirst = false;
				}

				double[] tickDecode = myCode.decode(nextStep);
				heading += (pHeadChange = tickDecode[0]);
				velocity = tickDecode[1];

				xg += velocity * Math.sin(heading);
				yg += velocity * Math.cos(heading);

				// double dX = Math.min(xg, BattleField.BATTLE_FIELD_W-xg);
				// double dY = Math.min(yg, BattleField.BATTLE_FIELD_H-yg);

				boolean wHit = false;
				if (xg < WZ)
				{
					xg = 18;
					wHit = true;
				}
				else if (BattleField.BATTLE_FIELD_W - xg < WZ)
				{
					xg = BattleField.BATTLE_FIELD_W - WZ;
					wHit = true;
				}
				if (yg < WZ)
				{
					yg = 18;
					wHit = true;
				}
				else if (BattleField.BATTLE_FIELD_H - yg < WZ)
				{
					yg = BattleField.BATTLE_FIELD_H - WZ;
					wHit = true;
				}

				if (wHit)
				{
					nextStep = myCode.encode(pHeadChange, 0, velocity);
					debugPointList.badPoints.add(new Point2D.Double(xg, yg));
				}
				else debugPointList.goodPoints.add(new Point2D.Double(xg, yg));
				ePattern.insert(0, (char) nextStep);
				//sDesire.insert(0, (char) nextStep);
			}
		}
		//TestPatternAccuracy.registerDesiredPattern(status.getTime(), sDesire);
		debugPointList.targetPoint = new Point2D.Double(xg, yg);

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

		record(hDiff, target.eVelocity, target.eLastVelocity, target.eHistory, target.matcherMap, target.eMatchKeyLength, status.getTime());

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
				String debugStr = target.eHistory.substring(0, len);
				tick = target.matcherMap.get(debugStr.hashCode()); // automatic cast to Integer
			}
			target.registerPatternLength(len + 1);
		}
	}

	private void record(double deltaHead, double velocity, double lastVelocity, StringBuilder history, Map<Integer, INumbatTick> matchMap,
			int keylen, long time)
	{
		int thisStep = (char) myCode.encode(deltaHead, velocity, lastVelocity);

		for (int i = 0; i <= history.length(); ++i)
		{
			int pHash;
			INumbatTick tick; // automatic cast to Integer
			if ((tick = matchMap.get(pHash = history.substring(0, i).hashCode())) == null)
			{
				matchMap.put(pHash, tick = NumbatSingleHolder.getNewInstance(thisStep));
			}

			if (!tick.incrementCount(thisStep))
			{
				NumbatMultiHolder multiTick;
				(multiTick = new NumbatMultiHolder((NumbatSingleHolder) tick)).incrementCount(thisStep);
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
		debugPointList.onPaint(g);
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
