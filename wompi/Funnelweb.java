package wompi;

import java.awt.Color;

import robocode.AdvancedRobot;
import robocode.ScannedRobotEvent;

public class Funnelweb extends AdvancedRobot
{
	final static double	INF		= Double.POSITIVE_INFINITY;

	final static double	PI_360	= Math.PI * 2;
	final static double	PI_90	= Math.PI / 2;
	final static double	PI_180	= Math.PI;
	final static double	PI_270	= Math.PI * 3 / 2;

	@Override
	public void run()
	{
		setAllColors(Color.GREEN);
		setTurnRadarRightRadians(INF);
		setAdjustGunForRobotTurn(true);

	}

	@Override
	public void onScannedRobot(ScannedRobotEvent e)
	{

		double absBear = e.getBearingRadians() + getHeadingRadians();
		double dist = e.getDistance();

		setTurnRightRadians(Math.cos(e.getBearingRadians() - (e.getDistance() - 160) * (getVelocity() / 2500)));
		setAhead(100 * dir);

		setAhead(direction *= ((chancesOfReversing.charAt(deathCount
				+ (100 + (int) (enemyEnergy - (enemyEnergy = e.getEnergy())))) * Math.random()) - 1));

	}

}