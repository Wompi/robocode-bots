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
import java.awt.event.KeyEvent;

import robocode.AdvancedRobot;
import robocode.BulletHitEvent;
import robocode.DeathEvent;
import robocode.HitRobotEvent;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;
import robocode.SkippedTurnEvent;
import robocode.StatusEvent;
import robocode.WinEvent;
import wompi.numbat.debug.DebugBot;
import wompi.numbat.debug.DebugGunProperties;
import wompi.numbat.debug.DebugMiscProperties;
import wompi.numbat.debug.DebugMoveProperties;
import wompi.numbat.debug.DebugRadarProperties;
import wompi.numbat.debug.DebugTargetProperties;
import wompi.numbat.debug.DebugTestProperties;
import wompi.numbat.gun.NumbatGunManager;
import wompi.numbat.misc.NumbatBattleField;
import wompi.numbat.misc.ScannedRobotHandler;
import wompi.numbat.misc.SkippedTurnHandler;
import wompi.numbat.move.NumbatMoveManager;
import wompi.numbat.radar.NumbatRadarManager;
import wompi.numbat.target.NumbatTargetManager;

/**
 * What the ... is a Numbat? (See: http://en.wikipedia.org/wiki/Numbat)
 * 
 * To keep track of what i have done, i update a little development diary at: https://github.com/Wompi/robocode-bots/wiki/Numbat
 * 
 * The official version history can be found at: http://robowiki.net/wiki/Numbat
 * 
 * If you want to talk about it - you find me at: http://robowiki.net/wiki/User:Wompi
 * 
 * @author Wompi
 * @date 08/08/2012
 */
public class Numbat extends AdvancedRobot
{
	public static NumbatRadarManager	myRadarMan	= new NumbatRadarManager();
	public static NumbatGunManager		myGunMan	= new NumbatGunManager();
	public static NumbatTargetManager	myTargetMan	= new NumbatTargetManager();
	public static NumbatMoveManager		myMoveMan	= new NumbatMoveManager();

	// debug stuff - refactor this a little
	// private boolean isTimeDebug = true;
	//PaintCenterSegments					debugCenter;

	// related to skipped turns bug, where double scan events occur
	private final SkippedTurnHandler	mySkipped;
	private final ScannedRobotHandler	myScans;

	//public static TestPatternString		myTestPattern	= new TestPatternString();

	public Numbat()
	{
		DebugBot.init(this);
		mySkipped = new SkippedTurnHandler();
		myScans = new ScannedRobotHandler();
		myRadarMan.setTargetManager(myTargetMan);
		myGunMan.setTargetManager(myTargetMan);
		myMoveMan.setTargetManager(myTargetMan);
	}

	@Override
	public void run()
	{
		// setAllColors(Color.RED);
		setColors(new Color(0xB9, 0x87, 0x56), Color.BLACK, Color.WHITE, Color.ORANGE, Color.RED);
		NumbatBattleField.BATTLE_FIELD_H = getBattleFieldHeight();
		NumbatBattleField.BATTLE_FIELD_W = getBattleFieldWidth();
		// if (isTimeDebug) TimeProfile.initRound();
		myRadarMan.init();
		myGunMan.init();
		myMoveMan.init();
		myTargetMan.init();

		while (true)
		{
			// if (isTimeDebug) TimeProfile.DEBUG_TIME_01.start();
			//DebugColorChanger.setColor(this);

			handleScannedRobotEvents();

			myTargetMan.setTargets();
			myTargetMan.execute(this);

			myRadarMan.setRadar();
			myRadarMan.excecute(this);

			myMoveMan.setMove();
			myMoveMan.excecute(this);

			// if (isTimeDebug) TimeProfile.GUN_EXECUTE.start();
			myGunMan.setGun();
			myGunMan.excecute(this);
			// if (isTimeDebug) TimeProfile.GUN_EXECUTE.stop();

			DebugGunProperties.debugPatternClasses();
			DebugGunProperties.execute();
			DebugRadarProperties.execute();
			DebugGunProperties.execute();
			DebugMiscProperties.execute();
			DebugMoveProperties.execute();
			DebugTargetProperties.execute();
			DebugTestProperties.execute();
			//
			// if (isTimeDebug)
			// {
			// TimeProfile.TURN_TIME.stop();
			// TimeProfile.DEBUG_TIME_01.stop();
			// TimeProfile.setRoundProperties(this);
			// }

			//myTestPattern.printHistory(getTime());
			execute();
		}
	}

	@Override
	public void onStatus(StatusEvent e)
	{
		// if (isTimeDebug) TimeProfile.TURN_TIME.start();

		//myTestPattern.registerHistoryPlaceHolder(e.getTime());

		mySkipped.onStatus(e);
		myScans.onStatus(e); // skipped turn bug prevent
		myTargetMan.setBotStatus(e.getStatus());
		myRadarMan.setBotStatus(e.getStatus());
		myGunMan.setBotStatus(e.getStatus());
		myMoveMan.setBotStatus(e.getStatus());
	}

	@Override
	public void onScannedRobot(ScannedRobotEvent e)
	{
		// scan events will be handled all together in run
		//myTestPattern.registerScan(e.getTime(), e.getName());
		myScans.onScannedRobot(e);
		mySkipped.onScannedRobot(e);
	}

	/**
	 * For more information look at ScannedRobotHandler This might be cause some problems with onPaint events because the scan events are now handled
	 * in run and after onPaint();
	 */
	private void handleScannedRobotEvents()
	{
		if (myScans.hasEvents())
		{
			// if (isTimeDebug) TimeProfile.GUN_UPDATE.start();
			for (ScannedRobotEvent scan : myScans.getAllScanEvents())
			{
				myTargetMan.onScannedRobot(scan); // sets also the targets new
				myGunMan.onScannedRobot(scan);
				myRadarMan.onScannedRobot(scan);
				myMoveMan.onScannedRobot(scan);
			}
			// if (isTimeDebug) TimeProfile.GUN_UPDATE.stop();
		}
	}

	@Override
	public void onRobotDeath(RobotDeathEvent e)
	{
		myTargetMan.onRobotDeath(e);
		myRadarMan.onRobotDeath(e);
		myGunMan.onRobotDeath(e);
		myMoveMan.onRobotDeath(e);
	}

	@Override
	public void onHitRobot(HitRobotEvent e)
	{
		myTargetMan.onHitRobot(e);
		myMoveMan.onHitRobot(e);
	}

	@Override
	public void onBulletHit(BulletHitEvent e)
	{
		myTargetMan.onBulletHit(e);
	}

	@Override
	public void onSkippedTurn(SkippedTurnEvent e)
	{
		mySkipped.onSkippedTurn(e);
	}

	//	@Override
	//	public void onHitByBullet(HitByBulletEvent e)
	//	{
	//		// PaintHitCloud.registerHit(e);
	//	}

	@Override
	public void onPaint(Graphics2D g)
	{
		//		if (debugCenter == null) debugCenter = new PaintCenterSegments();
		//		//debugCenter.onPaint(g);
		//		debugCenter.drawFieldGrid(g, null, NumbatBattleField.BATTLE_FIELD_W, NumbatBattleField.BATTLE_FIELD_H);

		//		PaintRobotPath.onPaint(g, getName(), getTime(), getX(), getY(), Color.GREEN);
		//		// PaintHitCloud.onPaint(g);
		myTargetMan.onPaint(g);
		myMoveMan.onPaint(g);
		myGunMan.onPaint(g);
		myRadarMan.onPaint(g);
	}

	@Override
	public void onDeath(DeathEvent event)
	{
		DebugMiscProperties.debugWinStats(getOthers());
		System.out.format("[%04d] DEAD\n", getTime());
	}

	@Override
	public void onWin(WinEvent event)
	{
		DebugMiscProperties.debugWinStats(getOthers());
	}

	@Override
	public void onKeyPressed(KeyEvent e)
	{
		char c = e.getKeyChar();
		DebugGunProperties.onKeyPressed(c);
		DebugMiscProperties.onKeyPressed(c);
		DebugRadarProperties.onKeyPressed(c);
		DebugMoveProperties.onKeyPressed(c);
		DebugTargetProperties.onKeyPressed(c);
		DebugTestProperties.onKeyPressed(c);
	}
}
