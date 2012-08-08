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

import robocode.AdvancedRobot;
import robocode.RadarTurnCompleteCondition;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.SkippedTurnEvent;
import robocode.StatusEvent;
import robocode.util.Utils;

/**
 * This BugBot shows the issues with skipped turn.
 * There are a couple of issues that should be fixed, but i guess they all have the same root.
 * - robots get skipped turn events but actually never skip the turn (happens only by one turn skips)
 * example:
 * [138] 27000 [0][0] 0 [397.0320:431.7585] - [2.2639] turn=0.1200
 * SYSTEM: BugBot* (2) skipped turn 139
 * [139] deltaTime[3] dist = 71.49 lastXY=[45.0974:728.9891] newXY=[102.8484:771.1232] eBearing=-2.8551 eDist=444.6455
 * [139] 10132000 [139][139] 74000 [393.1109:434.2889] - [2.1439] turn=0.1134
 * [140] ---- SKIPPED TURN 140 139 -----
 * [140] deltaTime[1] dist = 4.67 lastXY=[102.8484:771.1232] newXY=[107.2543:772.6611] eBearing=-2.7277 eDist=438.6792
 * [140] 4126000 [140][140] 66000 [388.9286:436.3591] - [2.0304] turn=0.1134
 * [141] deltaTime[1] dist = 4.67 lastXY=[107.2543:772.6611] newXY=[111.4579:774.6880] eBearing=-2.5984 eDist=433.5558
 * - robots skip turns but get no skipped turn events
 * example: turn 156 skipped without skipped event
 * [153] 4000 [0][0] 0 [336.8105:207.1484] - [0.2794] turn=0.1134 <---- no scan
 * [154] 4000 [0][0] 0 [336.0397:202.5458] - [0.1659] turn=0.1134
 * [155] 14000 [0][0] 0 [335.7949:197.8856] - [0.0525] turn=0.1134
 * [157] ---- TIME SKIP [2] ----
 * [157] deltaTime[8] dist = 33.49 lastXY=[86.7503:667.8687] newXY=[113.9531:687.4011] eBearing=-0.3597 eDist=544.2954
 * [157] 68000 [157][157] 64000 [336.2417:190.5659] - [6.2222] turn=0.1396
 * [157] 3000 [0][0] 0 [336.2417:190.5659] - [6.2222] turn=0.139
 * - after a skipped turn(s) the robot gets the wrong enemy bearing/distance and can not calculate the x,y position of the enemy
 * example:
 * [205] 568000 [205][205] 562000 [353.8750:244.7042] - [1.4029] turn=0.1134
 * SYSTEM: BugBot* (2) skipped turn 206
 * [206] deltaTime[1] dist = 4.67 lastXY=[71.9192:672.5521] newXY=[76.2529:670.8209] eBearing=-1.8581 eDist=507.2337
 * [206] 25091000 [206][206] 45000 [349.3918:243.4087] - [1.2895] turn=0.1134
 * [208] ---- SKIPPED TURN 208 206 -----
 * [208] ---- TIME SKIP [2] ----
 * // the next is very interesting it is a 1vs1 match and the bot gets 2! scan events one with time difference 2 (what it should be after the
 * // skipped turn) and one with time difference 0! and complete wrong bearing and distance
 * [208] deltaTime[2] dist = 6.32 lastXY=[76.2529:670.8209] newXY=[82.2664:668.8656] eBearing=-1.7244 eDist=501.0613
 * --> deltaT 0! [208] deltaTime[0] dist = 63.19 lastXY=[82.2664:668.8656] newXY=[28.7117:635.3258] eBearing=-1.8500 eDist=504.3277
 * [208] 131000 [208][208] 126000 [343.4453:241.2582] - [1.1760] turn=0.1265
 * --> deltaT 1 [209] deltaTime[1] dist = 66.83 lastXY=[28.7117:635.3258] newXY=[86.8874:668.2140] eBearing=-1.5812 eDist=498.0392
 * [209] 125000 [209][209] 117000 [339.3985:238.9342] - [1.0495] turn=0.1134
 * - minor - the skipped turn message shows the turn number of the last turn and not the actual skipped turn number
 * example:
 * SYSTEM: BugBot* (2) skipped turn 199
 * [199] deltaTime[1] dist = 4.67 lastXY=[50.5269:696.1244] newXY=[52.3297:691.8200] eBearing=-2.6109 eDist=555.1209
 * [199] 7087000 [199][199] 45000 [378.3388:242.5125] - [1.9833] turn=0.1134
 * --> turn 200 is skipped not 199
 * [201] ---- SKIPPED TURN 201 199 -----
 * [201] ---- TIME SKIP [2] ----[201] 11000 [0][0] 0 [372.3918:244.6615] - [1.8698] turn=0.1265
 * [202] deltaTime[3] dist = 13.94 lastXY=[52.3297:691.8200] newXY=[60.4690:680.5030] eBearing=-2.3583
 * - faster client speed causes more skipped turn
 * example:
 * well you have to try this by yourself :)
 * 
 * @author wompi
 *         12/06/2012
 */
public class BugBot extends AdvancedRobot
{

	long						lastTime;
	long						lastScan;
	long						startTime;
	long						onScanTime;
	long						onScanEventTime;
	long						onScanTimeDiff;
	double						lastX;
	double						lastY;

	double						DIR	= 1;

	StatusEvent					buggy1;
	RadarTurnCompleteCondition	buggy2;

	@Override
	public void onSkippedTurn(SkippedTurnEvent e)
	{
		System.out.format("[%d] ---- SKIPPED TURN %d %d -----\n", getTime(), e.getTime(), e.getSkippedTurn()); // if the system detects skipped turns
																												// it anounce it here
	}

	public void onStatus(StatusEvent e)
	{
		startTime = System.nanoTime();
		long delta = getTime() - lastTime;
		if (delta > 1) System.out.format("[%d] ---- TIME SKIP [%d] ----", getTime(), delta); // this should never happen without a skipped turn event

		onScanTime = 0;
		onScanEventTime = 0;
		onScanTimeDiff = 0;
		lastTime = getTime();
	}

	public void onScannedRobot(ScannedRobotEvent e)
	{
		double absBearing = getHeadingRadians() + e.getBearingRadians();
		double x = getX() + Math.sin(absBearing) * e.getDistance();
		double y = getY() + Math.cos(absBearing) * e.getDistance();

		// debug stuff
		// after skipped turns the coordinates of the enemy are wrong because the bearing and distance are wrong
		double dist = Math.sqrt((x - lastX) * (x - lastX) + (y - lastY) * (y - lastY));
		System.out.format("[%d] deltaTime[%d] dist = %3.2f lastXY=[%3.4f:%3.4f] newXY=[%3.4f:%3.4f] eBearing=%3.4f eDist=%3.4f\n", getTime(),
				(getTime() - lastScan), dist, lastX, lastY, x, y, e.getBearingRadians(), e.getDistance());

		// robot stuff
		if (getGunTurnRemaining() == 0) setFire(3.0);
		setTurnRadarLeftRadians(getRadarTurnRemainingRadians()); 								// simple radar lock
		setTurnGunRightRadians(Utils.normalRelativeAngle(absBearing - getGunHeadingRadians())); // head-on gun
		// ---------

		lastX = x;
		lastY = y;
		lastScan = getTime();
		onScanTime = e.getTime();
		onScanEventTime = getTime();
		onScanTimeDiff = System.nanoTime() - startTime;
	}

	public void run()
	{
		setAllColors(Color.RED);
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);

		setTurnRadarRightRadians(Double.POSITIVE_INFINITY);

		while (true)
		{
			// debug stuff
			if (Math.random() > 0.8)
			{
				try
				{
					Thread.sleep((long) (Math.random() * 30));   // / <--- increese this for more skipped turns
				}
				catch (InterruptedException e)
				{
					Thread.currentThread().interrupt();
				}
			}

			// bot stuff
			double turn = Rules.getTurnRateRadians(getVelocity());
			setTurnLeftRadians(turn);									// the bots turnrate is only one turn so it should stop turning in skipped turns
			if (getDistanceRemaining() == 0) DIR = -DIR;
			setAhead(8 * DIR);											// the bot moves only 8 and should stop after a couple of skipped turns
			// ----------

			// debug stuff
			System.out.format("[%d] %d [%d][%d] %d  [%3.4f:%3.4f] - [%3.4f] turn=%3.4f\n", getTime(), System.nanoTime() - startTime, onScanTime,
					onScanEventTime, onScanTimeDiff, getX(), getY(), getHeadingRadians(), turn);
			execute();
		}
	}
}
