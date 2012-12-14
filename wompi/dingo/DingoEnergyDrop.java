package wompi.dingo;

import robocode.AdvancedRobot;
import robocode.BulletHitEvent;
import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.RobotStatus;
import robocode.Rules;
import robocode.StatusEvent;
import robocode.util.Utils;

public class DingoEnergyDrop
{
	private RobotStatus	myStatus;

	private double		eEnergy;
	private double		eVelocity;

	private double		eLastFired;
	private boolean		hasWallHit;
	private double		minWallDmg;
	private double		maxWallDmg;

	private boolean		wasLastTurnInactive;
	private boolean		isInactive;

	public void onInit(AdvancedRobot bot)
	{
		eEnergy = bot.getEnergy();
		onDebug("onInit");
	}

	public void onStatus(StatusEvent e)
	{
		if (myStatus != null)
		{
			eVelocity = myStatus.getVelocity();
			eEnergy = myStatus.getEnergy() - eLastFired;
		}
		myStatus = e.getStatus();
		hasWallHit = false;
		eLastFired = 0.0;
		minWallDmg = 0.0;
		maxWallDmg = 0.0;
		wasLastTurnInactive = isInactive;
		isInactive = false;
		onDebug("onStatus");
	}

	public void onHitByBullet(HitByBulletEvent e)
	{
		eEnergy -= Rules.getBulletDamage(e.getPower());
		onDebug("onHitByBullet");
	}

	public void onBulletHit(BulletHitEvent e)
	{
		eEnergy += Rules.getBulletHitBonus(e.getBullet().getPower());
		onDebug("onBulletHit");
	}

	public void onHitRobot(HitRobotEvent e)
	{
		eEnergy -= Rules.ROBOT_HIT_DAMAGE;
		onDebug("onHitRobot");
	}

	public void onHitWall(HitWallEvent e)
	{
		// range 0.0 - 3.0
		double av = Math.abs(eVelocity);
		if (av > 1.0)
		{
			hasWallHit = true;
			minWallDmg = Rules.getWallHitDamage(Math.max(av - Rules.DECELERATION, 0.0));
			maxWallDmg = Rules.getWallHitDamage(Math.min(av + 1, Rules.MAX_VELOCITY));

			System.out.format("(me) hit the wall [%3.10f:%3.10f] damage cDmg=%3.10f \n", minWallDmg, maxWallDmg,
					Rules.getWallHitDamage(av));
		}
		onDebug("onHitWall");
	}

	public void onFire(double bulletPower)
	{
		eLastFired = bulletPower;
		onDebug("onFire");
	}

	public void onRun()
	{
		double eDelta = eEnergy - myStatus.getEnergy();

		if (hasWallHit)
		{
			if (Utils.isNear(eDelta, minWallDmg) || Utils.isNear(eDelta - 0.1, minWallDmg))
				eDelta -= minWallDmg;
			else if (Utils.isNear(eDelta, maxWallDmg) || Utils.isNear(eDelta - 0.1, maxWallDmg))
				eDelta -= maxWallDmg;
			else
			{
				System.out.format("(me) stupid wall damage detected! %3.2f\n", eDelta);
			}
		}

		if (Utils.isNear(0.1, eDelta))
		{
			isInactive = true;
			eDelta -= 0.1;
			System.out.format("(me) is inactive now!\n");
		}

		if (!Utils.isNear(0.0, eDelta))
		{
			System.out.format("WARN: undetected energy drop for me - %3.15f! \n", eDelta);
			onDebug("onRun");
		}
	}

	public boolean isInactive()
	{
		return isInactive;
	}

	// debug and helper
	public void onDebug(String info)
	{
		System.out.format("[%04d] (me) statusEnergy=%3.5f eEnergy=%6.5f eVelocity=%3.5f (%s)\n", myStatus.getTime(),
				myStatus.getEnergy(), eEnergy, eVelocity, info);
	}
}
