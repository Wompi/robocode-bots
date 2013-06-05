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
import java.util.HashMap;

import robocode.AdvancedRobot;
import robocode.BulletHitBulletEvent;
import robocode.BulletMissedEvent;
import robocode.DeathEvent;
import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;
import robocode.StatusEvent;
import robocode.WinEvent;
import wompi.echidna.gun.AGun;
import wompi.echidna.gun.GunSimpleHeadOn;
import wompi.echidna.gun.fire.FireLogDistance;
import wompi.echidna.radar.ARadar;
import wompi.echidna.radar.RadarLock;
import wompi.echidna.stats.BulletHitBulletStats;
import wompi.echidna.stats.StatsWallhits;
import wompi.echidna.stats.StatsWinLoose;
import wompi.echidna.target.ATarget;
import wompi.echidna.target.TargetAveraged;
import wompi.paint.PaintRobotPath;
import wompi.wallaby.WallabyPainter;

public class Turret extends AdvancedRobot
{
	static HashMap<String, ATarget>	allTargets	= new HashMap<String, ATarget>();
	ATarget							myTarget;

	ARadar							myRadar;
	AGun							myGun;
	double							eRate;

	public Turret()
	{
		// myRadar = new RadarFieldScan(this);
		// myRadar = new RadarPreciseFieldScan(this);
		// myRadar = new RadarFieldScanAdvanced(this);
		// myRadar = new RadarSpinning(this);
		// myRadar = new RadarPreciseAngle(this);
		// myRadar = new RadarFieldAngleDependent(this);
		// myRadar = new RadarGunLock(this);
		// myRadar = new RadarCalculatedAngles(this);
		myRadar = new RadarLock(this);

		myGun = new GunSimpleHeadOn(this);
		//myGun = new GunPreciseHeadOn(this);
		// myGun = new GunHeadOnPreciseAnhanced(this);
	}

	@Override
	public void run()
	{
		setAllColors(Color.LIGHT_GRAY);
		setBulletColor(Color.ORANGE);
		setRadarColor(Color.LIGHT_GRAY);
		for (ATarget target : allTargets.values())
		{
			target.init();
		}

		myRadar.init();
		// myGun.init(new FireDoubleShot(this));
		myGun.init(new FireLogDistance(this));
		// myGun.init(new FireMaxShot(this));
		//myGun.init(new FireMinShot(this));
		eRate = 100000;
		while (true)
		{
			myGun.run(); // includes firecontrol ... look there for firepower
			myRadar.run();
			execute();
		}

	}

	@Override
	public void onStatus(StatusEvent e)
	{
		myGun.onStatus(e);
		myRadar.onStatus(e);
		if (myTarget != null) myTarget.onStatus(e);
	}

	@Override
	public void onRobotDeath(RobotDeathEvent e)
	{
		myRadar.onRobotDeath(e);
		myGun.onRobotDeath(e);
		ATarget aTarget = allTargets.get(e.getName());
		if (aTarget != null) // the nul chekc is not nescacary if you catch every bot on start (360 radar sweep)
		{
			aTarget.onRobotDeath(e);
		}
		if (myTarget == aTarget) eRate = 100000;
	}

	@Override
	public void onScannedRobot(ScannedRobotEvent e)
	{
		if (e.getName().startsWith("wompi.Turret")) return;

		ATarget target = allTargets.get(e.getName());
		if (target == null)
		{
			allTargets.put(e.getName(), target = new TargetAveraged(this));
			target.init();
		}

		double tRate = /* Math.abs(e.getVelocity())*150 + */e.getDistance();

		boolean isMainTarget = false;
		if (eRate > tRate || myTarget.getName() == e.getName())
		{
			eRate = tRate;
			myTarget = target;
			isMainTarget = true;
		}

		target.onScannedRobot(e);
		myRadar.onScannedRobot(target, isMainTarget);
		myGun.onScannedRobot(target, isMainTarget); // includes firecontrol

	}

	@Override
	public void onHitByBullet(HitByBulletEvent e)
	{}

	@Override
	public void onHitRobot(HitRobotEvent e)
	{}

	@Override
	public void onBulletHitBullet(BulletHitBulletEvent e)
	{
		BulletHitBulletStats.onBulletHitBullet(e);
	}

	@Override
	public void onBulletMissed(BulletMissedEvent event)
	{}

	@Override
	public void onHitWall(HitWallEvent e)
	{}

	@Override
	public void onDeath(DeathEvent e)
	{
		StatsWinLoose.registerDeath(getOthers(), getTime());
		StatsWinLoose.printStats();
		StatsWallhits.printWallStats();
		BulletHitBulletStats.onPrint();
		myRadar.onDeath(e);
		for (ATarget target : allTargets.values())
		{
			target.onDeath(e);
		}
	}

	@Override
	public void onWin(WinEvent e)
	{
		StatsWinLoose.registerWin(getTime(), getEnergy());
		StatsWinLoose.printStats();
		StatsWallhits.printWallStats();
		BulletHitBulletStats.onPrint();
		myRadar.onWin(e);
		for (ATarget target : allTargets.values())
		{
			target.onWin(e);
		}
	}

	@Override
	public void onPaint(Graphics2D g)
	{
		if (myTarget != null)
			PaintRobotPath.onPaint(g, myTarget.getName(), getTime(), myTarget.getAbsX(), myTarget.getAbsY(),
					Color.ORANGE);
		WallabyPainter.drawEnemyGuessedFiredirection(g, allTargets.values(), new Point2D.Double(getX(), getY()));
		myGun.onPaint(g);
		myRadar.onPaint(g);
	}
}
