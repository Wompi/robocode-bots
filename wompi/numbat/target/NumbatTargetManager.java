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
package wompi.numbat.target;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Collection;
import java.util.HashMap;

import robocode.AdvancedRobot;
import robocode.BulletHitEvent;
import robocode.DeathEvent;
import robocode.HitRobotEvent;
import robocode.RobotDeathEvent;
import robocode.RobotStatus;
import robocode.ScannedRobotEvent;
import robocode.WinEvent;
import wompi.numbat.debug.DebugMoveProperties;
import wompi.numbat.debug.DebugTargetProperties;
import wompi.paint.PaintHelper;

public class NumbatTargetManager implements ITargetManager
{
	private RobotStatus							botStatus;

	private final static int					MAX_ENDGAME_OPPONENTS	= 2;
	private final HashMap<String, NumbatTarget>	allTargets				= new HashMap<String, NumbatTarget>();

	private NumbatTarget						myGunTarget;
	private NumbatTarget						myMoveTarget;
	private NumbatTarget						myRadarTarget;
	private NumbatTarget						myLastScanTarget;
	public static boolean						isDebug					= false;

	int											closestBots;

	public void init()
	{
		myGunTarget = null;
		myMoveTarget = null;
		myRadarTarget = null;
		closestBots = 9; // well a little awkward but should work
		for (NumbatTarget enemy : allTargets.values())
		{
			enemy.init();
		}
	}

	public void onScannedRobot(ScannedRobotEvent scan)
	{
		registerTarget(scan.getName());
		myLastScanTarget = allTargets.get(scan.getName());
		myLastScanTarget.onScannedRobot(scan, botStatus);
		setTargets();
	}

	public void execute(AdvancedRobot bot)
	{
		DebugMoveProperties.debugClosestBots(closestBots, botStatus.getOthers());
	}

	public void onRobotDeath(RobotDeathEvent death)
	{
		NumbatTarget target = allTargets.get(death.getName());
		if (target != null)
		{
			target.onRobotDeath();
			setTargets();
		}
	}

	public void onDeath(DeathEvent e)
	{
		for (NumbatTarget target : allTargets.values())
		{
			target.myHitStats.printStats(target.eName, false);
		}
	}

	public void onWin(WinEvent e)
	{
		for (NumbatTarget target : allTargets.values())
		{
			target.myHitStats.printStats(target.eName, false);
		}
	}

	public void setTargets()
	{
		NumbatTarget result = null;
		setClosestBotCount();
		NumbatTarget enoughFireDamageTarget = null;
		for (NumbatTarget target : allTargets.values())
		{
			if (target.isAlive)
			{
				if (result == null)
				{
					result = target;
					continue;
				}

				//				if (target.getLiveFireDamage() > (target.eEnergy))
				//				{
				//					//System.out.format("[%d] enough damage on %s switch to another ...\n", botStatus.getTime(), target.eName);
				//					enoughFireDamageTarget = target;
				//					continue;
				//				}

				double tRate;
				double rRate;
				if (botStatus.getOthers() > MAX_ENDGAME_OPPONENTS)
				{
					if (closestBots == 0) // if we are not the closest bot to anyone - target something that is worth it ,do it only with enough
											// opponents
					{
						tRate = target.getDistance(botStatus) - target.eScore - target.getAveragePatternLength() * 10;
						rRate = result.getDistance(botStatus) - result.eScore - result.getAveragePatternLength() * 10;
					}
					else
					{
						tRate = target.getDistance(botStatus) - target.eScore;
						rRate = result.getDistance(botStatus) - result.eScore;
					}
				}
				else
				{
					tRate = target.eEnergy;
					rRate = result.eEnergy;
				}

				if (rRate > tRate)
				{
					result = target;
				}

			}
		}

		if (result != myGunTarget)
		{
			if (myGunTarget == null || !myGunTarget.isAlive || myGunTarget.getScoreBonus() <= 10
					|| myGunTarget == enoughFireDamageTarget)
			{
				myGunTarget = result;
				myMoveTarget = result;
				myRadarTarget = result;
				// if (result != null) System.out.format("[%d] target now %s \n", botStatus.getTime(),result.eName);
			}
		}
	}

	@Override
	public int getCloseBots()
	{
		return closestBots;
	}

	@Override
	public boolean isNearest(NumbatTarget target)
	{
		double cDist = target.getDistance(botStatus);
		double minDist = Double.MAX_VALUE;
		for (NumbatTarget enemy : allTargets.values())
		{
			if (enemy.isAlive && enemy != target)
			{
				minDist = Math.min(minDist, enemy.distance(target));
			}
		}
		return minDist >= cDist;
	}

	private void setClosestBotCount()
	{
		closestBots = 0;
		for (NumbatTarget target : allTargets.values())
		{
			if (target.isAlive)
			{
				double cDist = target.getDistance(botStatus);
				double minDist = Double.MAX_VALUE;
				for (NumbatTarget enemy : allTargets.values())
				{
					if (enemy.isAlive && enemy != target)
					{
						minDist = Math.min(minDist, enemy.distance(target));
					}
				}
				if (minDist >= cDist) closestBots++; // TODO: should be the other way around - or am I wrong?
			}
		}
	}

	private void registerTarget(String name)
	{
		NumbatTarget enemy = allTargets.get(name);
		if (enemy == null)
		{
			allTargets.put(name, enemy = new NumbatTarget());
			enemy.eName = name;
			DebugTargetProperties.debugCurrentTarget(enemy);
		}
	}

	@Override
	public NumbatTarget getGunTarget()
	{
		return myGunTarget;
	}

	@Override
	public NumbatTarget getRadarTarget()
	{
		return myRadarTarget;
	}

	@Override
	public NumbatTarget getMoveTarget()
	{
		return myMoveTarget;
	}

	@Override
	public NumbatTarget getLastScanTarget()
	{
		return myLastScanTarget;
	}

	@Override
	public Collection<NumbatTarget> getAllTargets()
	{
		return allTargets.values();
	}

	public void onBulletHit(BulletHitEvent e)
	{
		allTargets.get(e.getName()).onBulletHit(e, botStatus);
	}

	public void onHitRobot(HitRobotEvent e)
	{
		allTargets.get(e.getName()).onHitRobot(e, botStatus);
	}

	public void onPaint(Graphics2D g)
	{
		for (NumbatTarget target : allTargets.values())
		{
			target.onPaint(g, botStatus);
		}

		if (myGunTarget != null)
		{
			myGunTarget.onSinglePaint(g, botStatus);

			PaintHelper.drawArc(myGunTarget, 50, 0, Math.PI * 2, false, g, Color.RED);
		}
	}

	public void setBotStatus(RobotStatus status)
	{
		botStatus = status;
	}

}
