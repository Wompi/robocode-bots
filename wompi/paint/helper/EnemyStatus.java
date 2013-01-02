package wompi.paint.helper;

import robocode.RobotStatus;
import robocode.ScannedRobotEvent;

public class EnemyStatus
{
	private final ScannedRobotEvent	myRobotStatus;
	private final RobotStatus		myScanStatus;
	private final double			x;
	private final double			y;

	public EnemyStatus(ScannedRobotEvent e, RobotStatus s)
	{
		myRobotStatus = e;
		myScanStatus = s;

		x = s.getX() + Math.sin(s.getHeadingRadians() + e.getBearingRadians()) * e.getDistance();
		y = s.getY() + Math.cos(s.getHeadingRadians() + e.getBearingRadians()) * e.getDistance();
	}

	public double getX()
	{
		return x;
	}

	public double getY()
	{
		return y;
	}
}