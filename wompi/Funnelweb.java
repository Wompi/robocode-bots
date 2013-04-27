package wompi;

import robocode.AdvancedRobot;
import robocode.HitWallEvent;
import robocode.StatusEvent;

public class Funnelweb extends AdvancedRobot
{
	private static double	INF	= Double.POSITIVE_INFINITY;

	@Override
	public void onStatus(StatusEvent e)
	{
		setAhead(INF);
	}

	@Override
	public void onHitWall(HitWallEvent e)
	{
		setTurnRightRadians(Math.cos(e.getBearingRadians()));
	}
}