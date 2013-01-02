package wompi.paint;

import robocode.RobotStatus;

public class PaintFunctions
{
	private static final int	ROUND_OFFSET	= 10000;	// this should make the turn numbers unique (maybe 10k is to less)

	public static long getAbsoutTime(RobotStatus s)
	{
		int round = s.getRoundNum();
		long time = s.getTime();
		return round * ROUND_OFFSET + time;
	}

}
