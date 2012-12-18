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

import robocode.AdvancedRobot;
import robocode.BulletHitBulletEvent;
import robocode.BulletHitEvent;
import robocode.CustomEvent;
import robocode.DeathEvent;
import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;
import robocode.StatusEvent;
import robocode.WinEvent;
import wompi.echidna.gun.AGun;
import wompi.echidna.gun.GunPMSingleTick;
import wompi.echidna.gun.fire.FireLogDistance;
import wompi.echidna.move.AMovement;
import wompi.echidna.move.MoveLikeWalaby;
import wompi.echidna.radar.ARadar;
import wompi.echidna.radar.RadarCalculatedAngles;
import wompi.echidna.stats.BulletHitBulletStats;
import wompi.echidna.stats.StatsWallhits;
import wompi.echidna.stats.StatsWinLoose;
import wompi.echidna.target.ATarget;
import wompi.echidna.target.targethandler.ATargetHandler;
import wompi.echidna.target.targethandler.TargetHandlerDistance;
import wompi.paint.PaintRobotPath;

public class Echidna extends AdvancedRobot
{
	private final boolean			paintMove		= false;
	private final boolean			paintRadar		= true;
	private final boolean			paintTarget		= true;
	private final boolean			paintGun		= true;
	private final boolean			paintBotPath	= false;

	private final boolean			enableMoving	= true;

	AMovement						myMove;
	ARadar							myRadar;
	AGun							myGun;
	public static ATargetHandler	myTargetHandler;

	public Echidna()
	{
		// myMove = new MoveOrbiting(this);
		// myMove = new MoveAntiGravNano(this);
		myMove = new MoveLikeWalaby(this);
		// myMove = new MoveStopAndGo(this);
		// myMove = new MoveJustSpinning(this);
		// myMove = new MoveMinAngle(this);
		// myMove = new MoveCapulet(this);
		// myMove = new MoveAntigravBoye(this);
		// myMove = new MoveMinRiskPerpOscillator(this);
		// myMove = new MoveLinear(this);

		// myRadar = new RadarFieldScan(this);
		// myRadar = new RadarPreciseFieldScan(this);
		// myRadar = new RadarFieldScanAdvanced(this);
		// myRadar = new RadarSpinning(this);
		// myRadar = new RadarPreciseAngle(this);
		// myRadar = new RadarFieldAngleDependent(this);
		// myRadar = new RadarGunLock(this);
		// myRadar = new RadarLock(this);
		myRadar = new RadarCalculatedAngles(this);
		//myRadar = new RadarWeighted(this);
		// myRadar = new RadarConditioned(this);

		// myGun = new GunSimpleHeadOn(this);
		// myGun = new GunCircularPrecise(this);
		// myGun = new GunGuessFactor(this);
		// myGun = new GunPMNano(this);
		myGun = new GunPMSingleTick(this);

		if (myTargetHandler == null)
		{
			myTargetHandler = new TargetHandlerDistance(this);
		}
	}

	@Override
	public void run()
	{
		setAllColors(Color.ORANGE);
		myTargetHandler.init();
		if (enableMoving) myMove.init();
		myRadar.init();
		// myGun.init(new FireDoubleShot(this));
		myGun.init(new FireLogDistance(this));
		// myGun.init(new FireMaxShot(this));
		// myGun.init(new FireMinShot(this));
		while (true)
		{

			if (enableMoving) myMove.run();
			myGun.run(); // includes fire control ... look there for fire power
			myRadar.run();

			// System.out.format("[%d] turn finished\n", getTime());
			execute();
		}
	}

	@Override
	public void onCustomEvent(CustomEvent e)
	{
		// TODO: re think this structure
		// try
		// {
		// ConditionType cType = ((ACondition)(e.getCondition())).getConditionType();
		// if (cType == ConditionType.CONDITION_RADAR_TYPE) myRadar.onCustomEvent(e);
		// else if (cType == ConditionType.CONDITION_GUN_TYPE) myGun.onCustomEvent(e);
		// else if (cType == ConditionType.CONDITION_MOVE_TYPE) if (enableMoving) myMove.onCustomEvent(e);
		// }
		// catch (Exception e1)
		// {
		// System.out.format("[%d] ERROR: unknown ConditionType\n", getTime());
		// e1.printStackTrace();
		// }
	}

	@Override
	public void onStatus(StatusEvent e)
	{
		myGun.onStatus(e);
		myRadar.onStatus(e);
		myTargetHandler.onStatus(e);
		StatsWallhits.registerVelocity(getVelocity());
	}

	@Override
	public void onRobotDeath(RobotDeathEvent e)
	{
		myTargetHandler.onRobotDeath(e);
		myRadar.onRobotDeath(e);
		myGun.onRobotDeath(e);
		if (enableMoving) myMove.onRobotDeath(e);
	}

	@Override
	public void onScannedRobot(ScannedRobotEvent e)
	{
		myTargetHandler.onScannedRobot(e);
		ATarget target = myTargetHandler.getLastScannedTarget();
		boolean isMainTarget = myTargetHandler.isMainTarget(target);

		if (enableMoving) myMove.onScannedRobot(target, isMainTarget);
		myGun.onScannedRobot(target, isMainTarget); // includes fire control
		myRadar.onScannedRobot(target, isMainTarget); // radar after gun is important if gun lock is enabled
	}

	@Override
	public void onHitByBullet(HitByBulletEvent e)
	{
		// nextDirChange = 0;
		// double bHeading = e.getHeadingRadians();
		// double bPower = e.getPower();
		// String bName = e.getName();
		if (enableMoving) myMove.onHitByBullet(e);
	}

	@Override
	public void onBulletHit(BulletHitEvent e)
	{
		myTargetHandler.onBulletHit(e);
	}

	@Override
	public void onHitRobot(HitRobotEvent e)
	{
		if (enableMoving) myMove.onHitRobot(e);
	}

	@Override
	public void onBulletHitBullet(BulletHitBulletEvent e)
	{
		BulletHitBulletStats.onBulletHitBullet(e);
	}

	@Override
	public void onHitWall(HitWallEvent e)
	{
		StatsWallhits.onHitWall(e);
		if (enableMoving) myMove.onHitWall(e);
	}

	@Override
	public void onDeath(DeathEvent e)
	{
		StatsWinLoose.registerDeath(getOthers(), getTime());
		StatsWinLoose.printStats();
		StatsWallhits.printWallStats();
		BulletHitBulletStats.onPrint();
		myRadar.onDeath(e);
		myTargetHandler.onDeath(e);
		if (enableMoving) myMove.onDeath(e);
	}

	@Override
	public void onWin(WinEvent e)
	{
		StatsWinLoose.registerWin(getTime(), getEnergy());
		StatsWinLoose.printStats();
		StatsWallhits.printWallStats();
		BulletHitBulletStats.onPrint();
		myRadar.onWin(e);
		myTargetHandler.onWin(e);
		if (enableMoving) myMove.onWin(e);
	}

	// ------------------------------------------------ HELPER -------------------------------------------------------------------
	@Override
	public void onPaint(Graphics2D g)
	{
		if (paintBotPath) PaintRobotPath.onPaint(g, getName(), getTime(), getX(), getY(), Color.GREEN);
		if (paintTarget) myTargetHandler.onPaint(g);
		if (paintMove && enableMoving) myMove.onPaint(g);
		if (paintGun) myGun.onPaint(g);
		if (paintRadar) myRadar.onPaint(g);
	}
}
