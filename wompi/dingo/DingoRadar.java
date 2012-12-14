package wompi.dingo;

import robocode.AdvancedRobot;
import robocode.util.Utils;

public class DingoRadar
{
	private final static double	LOCK_OFFSET	= 1.9;

	private double				slipDir;
	private long				lastScan;

	public void onRun(AdvancedRobot bot, double absBearing)
	{
		double rTurn;
		long sDelta = bot.getTime() - lastScan;
		if (sDelta == 0)
		{
			rTurn = Utils.normalRelativeAngle(absBearing - bot.getRadarHeadingRadians()) * LOCK_OFFSET;
			slipDir = Math.signum(rTurn);
		}
		else
		{
			rTurn = Double.POSITIVE_INFINITY * ((slipDir == 0) ? 1 : slipDir); // TODO: zero slipDir is sitting duck, maybe 1 is not good
			System.out.format("[%04d] scan = %d \n", bot.getTime(), sDelta);
		}

		bot.setTurnRadarRightRadians(rTurn);
	}

	public void onScannedRobot(long time)
	{
		lastScan = time;
	}
}
