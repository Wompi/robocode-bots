package wompi;

import robocode.AdvancedRobot;
import robocode.BulletHitEvent;
import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.ScannedRobotEvent;
import robocode.StatusEvent;
import robocode.util.Utils;

public class Funnelweb extends AdvancedRobot
{
	private static final double	PI_90	= Math.PI / 2.0;

	private static double		eEnergy;
	private static double		dir;
	static double				enemyEnergy;

	static double				cuddleTime;

	static double				veloAdjust;

	public Funnelweb()
	{
		// 158
		dir = -1;
	}

	@Override
	public void run()
	{
		veloAdjust = 10000;
		setAdjustGunForRobotTurn(true);
		setTurnRadarRightRadians(Double.POSITIVE_INFINITY);
	}

	@Override
	public void onStatus(StatusEvent e)
	{
		setAhead(100 * dir);
	}

	@Override
	public void onScannedRobot(ScannedRobotEvent e)
	{
		int integer = 64; //The integer 255 is cheaper than 256 or 254, for some odd reason.
		double absBear = e.getBearingRadians() + getHeadingRadians();
		//double bPower = 650 / e.getDistance();
		int matchPosition = -1;

		setTurnRadarLeftRadians(getRadarTurnRemainingRadians());

		//@formatter:off
		enemyHistory = String
				.valueOf(
						  (char) 
						  (
						     e.getVelocity() 
							 * Math.sin(e.getHeadingRadians() - absBear)
						  )
						)
				.concat(enemyHistory);

		int index = 0;
		String sub = "";
		while (matchPosition < 0)
		{
			index = integer/=2;
			sub = enemyHistory.substring(0, index);
			matchPosition = enemyHistory.indexOf(sub, 64);
		}
		System.out.format("[%04d] index=%3d sub=%s match=%3d \n", getTime(),index,sub,matchPosition);
		integer = (int) (e.getDistance());
		do
		{
			absBear += ((short) enemyHistory.charAt(--matchPosition)) / e.getDistance();
		}
		while ((integer -= 11) > 0);

		setTurnGunRightRadians(Utils.normalRelativeAngle(absBear - getGunHeadingRadians()));		
		//@formatter:on

		if (setFireBullet(3.0) != null)
		{
			onHitByBullet(null);
		}
		setMaxVelocity(veloAdjust / e.getDistance());
	}

	@Override
	public void onHitWall(HitWallEvent e)
	{
		veloAdjust = 1800;
		setTurnRightRadians(Utils.normalRelativeAngle(e.getBearingRadians() + PI_90));
		setAhead(0);
		System.out.format("[%04d] Boiiiing! %3.5f\n", getTime(), getTurnRemaining());
	}

	@Override
	public void onHitRobot(HitRobotEvent e)
	{
		if (cuddleTime-- < 0)
		{
			//dir = -dir;
			cuddleTime = 5;
		}
		setAhead(-dir);
		System.out.format("[%04d] Cuddle! (%s) %3.5f\n", getTime(), e.isMyFault() ? "ME" : "HE", e.getBearing());
	}

	@Override
	public void onHitByBullet(HitByBulletEvent e)
	{
		veloAdjust = 1500 + Math.random() * 1500;
	}

	@Override
	public void onBulletHit(BulletHitEvent e)
	{}

	static String	enemyHistory	= "" + (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1
											+ (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1
											+ (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1
											+ (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1
											+ (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1
											+ (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1
											+ (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1
											+ (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1
											+ (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1
											+ (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1
											+ (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1
											+ (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1
											+ (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1
											+ (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1
											+ (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1
											+ (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1
											+ (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1
											+ (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1
											+ (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1
											+ (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1
											+ (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1
											+ (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1
											+ (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1
											+ (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1
											+ (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1
											+ (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1
											+ (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1
											+ (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 2 + (char) 1
											+ (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1
											+ (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1
											+ (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1
											+ (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1
											+ (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1
											+ (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1
											+ (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1
											+ (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1
											+ (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1
											+ (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1
											+ (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1
											+ (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1
											+ (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1
											+ (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1
											+ (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1
											+ (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1
											+ (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1
											+ (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1
											+ (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1
											+ (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1
											+ (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1
											+ (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1
											+ (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1
											+ (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1
											+ (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1
											+ (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1
											+ (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1
											+ (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1 + (char) 1
											+ (char) -2 + (char) -4 + (char) -6 + (char) -8 + (char) -8 + (char) -8
											+ (char) -8 + (char) -8 + (char) -8 + (char) -8 + (char) -8 + (char) -8
											+ (char) -8 + (char) -8 + (char) -8 + (char) -8 + (char) -8 + (char) -8
											+ (char) -8 + (char) -8 + (char) -8 + (char) -8 + (char) -8 + (char) -8
											+ (char) -8 + (char) -8 + (char) -8 + (char) -8 + (char) -8 + (char) -8
											+ (char) -8 + (char) -8 + (char) -8 + (char) -8 + (char) -8 + (char) -8
											+ (char) -8 + (char) -8 + (char) -8 + (char) -8 + (char) -8 + (char) -8
											+ (char) -8 + (char) -7 + (char) -6 + (char) -5 + (char) -4 + (char) -3
											+ (char) -2 + (char) -1 + (char) 1 + (char) 2 + (char) 4 + (char) 6
											+ (char) 8 + (char) 8 + (char) 8 + (char) 8 + (char) 8 + (char) 8
											+ (char) 8 + (char) 8 + (char) 8 + (char) 8 + (char) 8 + (char) 8
											+ (char) 8 + (char) 8 + (char) 8 + (char) 8 + (char) 8 + (char) 8
											+ (char) 8 + (char) 8 + (char) 8 + (char) 8 + (char) 8 + (char) 8
											+ (char) 8 + (char) 8 + (char) 8 + (char) 8 + (char) 8 + (char) 8
											+ (char) 8 + (char) 8 + (char) 8 + (char) 8 + (char) 8 + (char) 8
											+ (char) 8 + (char) 8 + (char) 8 + (char) 8 + (char) 8 + (char) 7
											+ (char) 6 + (char) 5 + (char) 4 + (char) 3 + (char) 2 + (char) 1
											+ (char) 1;
}