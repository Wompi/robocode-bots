package wompi;

import robocode.AdvancedRobot;
import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.StatusEvent;

public class Funnelweb extends AdvancedRobot
{
	private final static double	ADVANCE_FACTOR	= 1.0 / 1000;		// be careful with this parameter this breaks the wall movement
	private final static double	RADIUS			= 300;				// if you adjust this you also have to change BORDER an ADVANCE_FACTOR
	private final static double	W				= 800;
	private final static double	H				= 600;
	private final static double	BORDER			= 25;				// don't change this without tweaking RADIUS and ADVANCE_FACTOR
	private final static double	BORDER_RADIUS	= RADIUS - BORDER;

	static double				dir;
	boolean						isBoing;
	static double				speed;

	public Funnelweb()
	{}

	@Override
	public void run()
	{
		setTurnRadarRightRadians(dir = Double.POSITIVE_INFINITY);
	}

	@Override
	public void onStatus(StatusEvent e)
	{
		double x;
		double y;
		if (!isBoing) setAhead(dir);

		//@formatter:off
		setTurnRightRadians(
				(
						(BORDER_RADIUS 
							- (Math.hypot(
								x = (getLimit(RADIUS, getX(), W - RADIUS) - getX()),
								y = (getLimit(RADIUS, getY(), H - RADIUS) - getY())
							    )
							)
						) 
						* getVelocity() 
						* ADVANCE_FACTOR)
				+  Math.cos(getHeadingRadians() - Math.atan2(x, y))
				);
		//@formatter:on
		isBoing = false;
	}

	@Override
	public void onScannedRobot(ScannedRobotEvent e)
	{
		double aBear;
		double bPower;
		setFire(bPower = (500 / e.getDistance()));
		//@formatter:off
		setTurnGunRightRadians(
			Math.asin(
				Math.sin(
					(aBear=(getHeadingRadians() + e.getBearingRadians())) 
					- getGunHeadingRadians() 
					+ (1 - e.getDistance() / 500) 
					* 
					Math.asin(e.getVelocity() / Rules.getBulletSpeed(bPower)) 
					* Math.sin(e.getHeadingRadians() - aBear) 
				)
			)
		);
		if (getVelocity() == 0) setMaxVelocity(8.0);
		//@formatter:on
		setTurnRadarLeftRadians(getRadarTurnRemainingRadians());
	}

	@Override
	public void onHitWall(HitWallEvent e)
	{
		//System.out.format("[%04d] Boiiiing! (%3.5f) \n", getTime(), e.getBearing());
		setAhead(dir = -dir);
		isBoing = true;
	}

	@Override
	public void onHitByBullet(HitByBulletEvent event)
	{
		//setMaxVelocity(0);
		setAhead(dir = -dir);
	}

	@Override
	public void onHitRobot(HitRobotEvent e)
	{
		setAhead(dir = -dir);
	}

	private double getLimit(double min, double value, double max)
	{
		return (int) Math.min(max, Math.max(value, min));
	}
}
