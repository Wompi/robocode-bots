package wompi;

import java.awt.geom.Rectangle2D;

import robocode.AdvancedRobot;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;

public class Funnelweb extends AdvancedRobot
{
//	private static final double	WZ		= 18.0;
//	private static final double	WZ_W	= 800 - 2 * WZ;
//	private static final double	WZ_H	= 600 - 2 * WZ;
//	private final static double	DIST	= 160;
//	private final static double	PI_360	= Math.PI * 2.0;
//	private final static double	PI_180	= Math.PI;
//	private final static double	PI_90	= Math.PI / 2.0;
//	private final static double	PI_30	= Math.PI / 6.0;
//
//	private static double		kx;
//	private static double		ky;
//
	private static double	eHeading;

	public Funnelweb()
	{}

	@Override
	public void run()
	{
		//setAllColors(Color.CYAN);

//		setAdjustGunForRobotTurn(true);
//		setAdjustRadarForRobotTurn(true);
//		setAdjustRadarForGunTurn(true);
		setTurnRadarRightRadians(Double.POSITIVE_INFINITY);
	}

//	@Override
//	public void onStatus(StatusEvent e)
//	{
	// 0 = cos(18.0 * pi/800 + k);
	// acos(0) = 18.0 * pi/800 + k; acos(0) = pi/2
	// pi/2 - 18.0 * pi/800 = k;
	// pi*(0.5 - 0.0225) = k
	// pi * 0.4775 = k;
	// k = 1.500110492089126;
//		double d = 19;

//		double kx = PI_180 * (0.5 - d / 800);
//		double ky = PI_180 * (0.5 - d / 600);

//		double dx = Math.cos(getX() * PI_180 / 800 + kx);
//		double dy = Math.cos(getY() * PI_180 / 600 + ky);
//
//		//System.out.format("[%04d] dx=%3.5f dy=%3.5f \n", getTime(), dx, dy);
//
//		double angle;
//		setTurnRightRadians(Utils.normalRelativeAngle(angle = (Math.atan2(dx, dy) - getHeadingRadians())));
//		setAhead(100 * Math.cos(angle));

//	}

	@Override
	public void onScannedRobot(ScannedRobotEvent e)
	{
		double absBearing;
		double bPower;

		double xe = Math.sin(absBearing = e.getBearingRadians() + getHeadingRadians()) * e.getDistance();
		double ye = Math.cos(absBearing) * e.getDistance();

		setTurnRadarLeftRadians(getRadarTurnRemainingRadians());
		setFire(bPower = Math.min(2 + (int) (100 / e.getDistance()), e.getEnergy() / 4));

		absBearing = e.getVelocity();
		double dHead = eHeading - (eHeading = e.getHeadingRadians());
		double head = e.getHeadingRadians();
		double i = 0;
		while (++i * Rules.getBulletSpeed(bPower) < Math.hypot(xe, ye))
		{
			head = head + dHead;
			if (!new Rectangle2D.Double(18.0, 18.0, 768, 568).contains((xe += Math.sin(e.getHeadingRadians())
					* absBearing)
					+ getX(), (ye += Math.cos(e.getHeadingRadians()) * absBearing) + getY()))
			{
				absBearing = -absBearing;
			}
		}
//		double dx = Math.cos(getX() * PI_180 / 800 + PI_180 * (0.5 - (xe + getX()) / 800));
//		double dy = Math.cos(getY() * PI_180 / 600 + PI_180 * (0.5 - (ye + getY()) / 600));

		setTurnGunRightRadians(Utils.normalRelativeAngle((absBearing = Math.atan2(xe, ye)) - getGunHeadingRadians()));
		setTurnRightRadians(Utils.normalRelativeAngle(absBearing = Math.atan2(xe, ye) - getHeadingRadians()));
		setAhead(100 * Math.cos(absBearing));
	}
//	@Override
//	public void onHitWall(HitWallEvent e)
//	{
//		System.out.format("[%04d] Boiiiing! (%3.5f) \n", getTime(), e.getBearing());
//	}

//	@Override
//	public void onPaint(Graphics2D g)
//	{}

}
