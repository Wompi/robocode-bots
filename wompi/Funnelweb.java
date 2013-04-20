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

	public Funnelweb()
	{
		// 158
		dir = -1;
	}

	@Override
	public void run()
	{
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
		double absBear = e.getBearingRadians() + getHeadingRadians();
		double bPower = 650 / e.getDistance();
		setTurnRadarLeftRadians(getRadarTurnRemainingRadians());

		// Infinity style gun fits!!!!
		//@formatter:off
		setTurnGunRightRadians(Utils.normalRelativeAngle(absBear
				- getGunHeadingRadians()
				+
//				Math.random() *
//				Math.max(1 - e.getDistance() / (400), 0) * 
				(e.getVelocity() / (10 + Math.pow(1.008, e.getDistance())))
				* Math.sin(e.getHeadingRadians() - absBear)));
		//@formatter:on

		setFire(bPower);
		setMaxVelocity(2000 / e.getDistance());
	}

	@Override
	public void onHitWall(HitWallEvent e)
	{
		setTurnRightRadians(Utils.normalRelativeAngle(e.getBearingRadians() + PI_90));
		setAhead(0);
		System.out.format("[%04d] Boiiiing! %3.5f\n", getTime(), getTurnRemaining());
	}

	@Override
	public void onHitRobot(HitRobotEvent event)
	{
		dir = -dir;
		setAhead(0);
	}

	@Override
	public void onHitByBullet(HitByBulletEvent e)
	{}

	@Override
	public void onBulletHit(BulletHitEvent e)
	{}
}