package wompi;

import java.awt.Color;

import robocode.AdvancedRobot;
import robocode.HitWallEvent;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.StatusEvent;
import robocode.util.Utils;

public class Funnelweb extends AdvancedRobot
{
	private static final double	WZ		= 18.0;
	private static final double	WZ_W	= 800 - 2 * WZ;
	private static final double	WZ_H	= 600 - 2 * WZ;
	private final static double	DIST	= 160;
	private final static double	PI_360	= Math.PI * 2.0;
	private final static double	PI_180	= Math.PI;
	private final static double	PI_90	= Math.PI / 2.0;
	private final static double	PI_30	= Math.PI / 6.0;

	private static double		dxx;
	private static double		kx;

	private static double		eBearing;
	private static int			DIR		= 100;

	public Funnelweb()
	{}

	@Override
	public void run()
	{
		setAllColors(Color.CYAN);

		dxx = 18; //  18 - 782 oscillate between  ;

		setAdjustGunForRobotTurn(true);
		setAdjustRadarForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
		setTurnRadarRightRadians(Double.POSITIVE_INFINITY);
	}

	@Override
	public void onStatus(StatusEvent e)
	{
		// 0 = cos(18.0 * pi/800 + k);
		// acos(0) = 18.0 * pi/800 + k; acos(0) = pi/2
		// pi/2 - 18.0 * pi/800 = k;
		// pi*(0.5 - 0.0225) = k
		// pi * 0.4775 = k;
		// k = 1.500110492089126;

		//kx = PI_180 * (0.5 - 200.0 / 800.0);
//		double ky = PI_180 * (0.5 - 200.0 / 600.0);
//
//		// kx {-pi/2.0 ... +pi/2.0} it would be wise to take 90% because the range is calculated on 0 and not an 18  
//		kx = Utils.normalRelativeAngle(kx += 0.031415926535898 - PI_90);
//
//		double dx = Math.cos(getX() * PI_180 / 800 + kx);
//		double dy = Math.cos(getY() * PI_180 / 600 + ky);
//
//		PaintHelper.drawPoint(new Point2D.Double((0.5 - (kx / PI_180)) * 800, 200.0), Color.red, getGraphics(), 4);
//
//		System.out.format("[%04d] dx=%3.5f dy=%3.5f \n", getTime(), dx, dy);
//
//		double angle;
//		setTurnRightRadians(Utils.normalRelativeAngle(angle = (Math.atan2(dx, dy) - getHeadingRadians())));
//		setAhead(100 * Math.cos(angle));

	}

	@Override
	public void onScannedRobot(ScannedRobotEvent e)
	{
		setTurnRadarLeftRadians(getRadarTurnRemainingRadians());
		setTurnRightRadians(Math.cos(e.getBearingRadians()));
		setAhead(DIR);

		double absBearing = e.getBearingRadians() + getHeadingRadians();
		double bDiff = eBearing - (eBearing = e.getBearingRadians());

		double bPower = 600 / e.getDistance();

		double bSpeed = Rules.getBulletSpeed(bPower);
		double tick = e.getDistance() / bSpeed;

		System.out.format("[%04d] bearDiff=%3.5f (%3.5f) (%3.5f) \n", getTime(), Math.toDegrees(bDiff),
				Math.toDegrees(bDiff * tick), tick);
		setTurnGunRightRadians(Utils.normalRelativeAngle(absBearing + Math.signum(e.getVelocity()) * bDiff * tick
				- getGunHeadingRadians()));
		setFire(bPower);
	}

	@Override
	public void onHitWall(HitWallEvent e)
	{
		DIR = -DIR;
		System.out.format("[%04d] Boiiiing! (%3.5f) \n", getTime(), e.getBearing());
	}

//	@Override
//	public void onPaint(Graphics2D g)
//	{}

}
