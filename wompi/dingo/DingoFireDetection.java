package wompi.dingo;

import robocode.AdvancedRobot;
import robocode.BulletHitEvent;
import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.RobotStatus;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.StatusEvent;
import robocode.util.Utils;

public class DingoFireDetection
{
	private RobotStatus	myStatus;

	private double		eEnergy;
	private double		eVelocity;
	private double		eGunHeat;

	private boolean		eCheck;	// this checks the plausibility of the energy drop
	private long		eTime;		// this checks if we had some skipped turns

	private boolean		hasFired;
	private double		eDelta;
	private boolean		hasWallHit;
	private double		minWallDmg;
	private double		maxWallDmg;

	public void onInit(AdvancedRobot myBot)
	{
		eGunHeat = myBot.getGunHeat();
		eEnergy = myBot.getEnergy();
		eVelocity = 0;
		eCheck = false;
		hasFired = false;
	}

	public void onStatus(StatusEvent e)
	{
		myStatus = e.getStatus();

		long tDelta = e.getTime() - eTime;
		if (tDelta != 1)
		{
			System.out.format("WARN: [%04d] skipped turns detected - %d (adjust enemy gunheat)\n", e.getTime(), tDelta);
		}
		eTime = e.getTime();
		eGunHeat = Math.max(eGunHeat - (0.1 * tDelta), 0.0);

		eCheck = false;
		hasFired = false;
		eDelta = 0;
		hasWallHit = false;
		minWallDmg = 0.0;
		maxWallDmg = 0.0;
		onDebug("onStatus");
	}

	public void onHitByBullet(HitByBulletEvent e)
	{
		eEnergy += Rules.getBulletHitBonus(e.getPower());
		eCheck = true;
		onDebug("onHitBullet");
	}

	public void onBulletHit(BulletHitEvent e)
	{
		eEnergy -= Rules.getBulletDamage(e.getBullet().getPower());
		eCheck = true;
		onDebug("onBulletHit");
	}

	public void onHitRobot(HitRobotEvent e)
	{
		eEnergy -= Rules.ROBOT_HIT_DAMAGE;
		eVelocity = 0;
		eCheck = true;
		onDebug("onHitRobot");
	}

	public void onScannedRobot(ScannedRobotEvent e)
	{
		double av = Math.abs(eVelocity);
		if (e.getVelocity() == 0 && av > 2.0) // TODO: this is slightly wrong because it could be 1+ and if he accelerates it cause dmg 
		{
			hasWallHit = true;
			minWallDmg = Rules.getWallHitDamage(Math.max(av - Rules.DECELERATION, 0.0));
			maxWallDmg = Rules.getWallHitDamage(Math.min(av + 1, Rules.MAX_VELOCITY));
			System.out.format("(enemy) hit the wall [%3.10f:%3.10f] damage\n", minWallDmg, maxWallDmg);
			eCheck = true;
		}
		eDelta = eEnergy - e.getEnergy();

		if (eDelta != 0.0) System.out.format("[%04d] enmey energy drop %3.15f\n", e.getTime(), eDelta);

		eVelocity = e.getVelocity();
		eEnergy = e.getEnergy();
		onDebug("onScannedRobot");
	}

	public void onInactivity()
	{
		eDelta -= 0.1; // grr awkward - after scannedRobot i have to adjust eDelta (stupid design of me)
		onDebug("onInactivity");
	}

	public void onRun()
	{
		if (hasWallHit)
		{
			if (Utils.isNear(eDelta, minWallDmg))
				eDelta -= minWallDmg;
			else if (Utils.isNear(eDelta, maxWallDmg))
				eDelta -= maxWallDmg;
			else
			{
				// min = Math.min(Math.abs(eDelta - minD),Math.abs(eDelta - maxD))
				System.out.format("(enemy) stupid wall damage detected! %3.2f \n", eDelta);
			}
		}

		if (!eCheck && !Utils.isNear(0.0, eDelta) && eGunHeat > 0.0)
		{
			// this can happen on skipped turns and on very rare velocity uncertainty 
			// not sure what to do but probably the GunHeat should be reseted;
			// also scan lost can lead to fail energy drop detections (happens frequently) 
			System.out.format("WARN: [%04d] energy calculation went wrong - difference %3.2f \n ", myStatus.getTime(),
					eDelta);
		}

		if (eDelta >= Rules.MIN_BULLET_POWER && eDelta <= Rules.MAX_BULLET_POWER)
		{
			//System.out.format("[%04d] enemy has fired %3.2f \n", e.getTime(), eDelta);
			if (eGunHeat > 0.0)
			{
				System.out
						.format("[%04d] maybe wrong - enemy has still gunheat %3.2f \n", myStatus.getTime(), eGunHeat);
			}
			else
			{
				eGunHeat = Rules.getGunHeat(eDelta) - 0.1; // - 0.1 because we detect the bullet one turn after its fired
				hasFired = true;

				//System.out.format("[%04d] new enemy gunheat %3.2f \n", e.getTime(), eGunHeat);
			}

		}

	}

	// debug and helper
	public void onDebug(String info)
	{
//		System.out.format("[%04d] eEnergy=%6.5f eGunHeat=%3.15f eVelocity=%3.5f (%s)\n", myStatus.getTime(), eEnergy,
//				eGunHeat, eVelocity, info);
	}

	public boolean hasFired()
	{
		return hasFired;
	}

	public double getFiredBulletPower()
	{
		return eDelta;
	}
}
