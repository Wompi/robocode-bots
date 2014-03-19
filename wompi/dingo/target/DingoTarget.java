package wompi.dingo.target;

import java.util.ArrayList;

import robocode.ScannedRobotEvent;
import robocode.StatusEvent;

public class DingoTarget
{
	private final ArrayList<Double>	eVelocities	= new ArrayList<Double>();
	private final ArrayList<Long>	eScanTimes	= new ArrayList<Long>();
	private final ArrayList<Double>	eEnergy		= new ArrayList<Double>();
	private final ArrayList<Double>	eDistance	= new ArrayList<Double>();

	public void onStatus(StatusEvent e)
	{

	}

	public void onScannedRobot(ScannedRobotEvent e)
	{
		eScanTimes.add(e.getTime());
		eVelocities.add(e.getVelocity());
		eEnergy.add(e.getEnergy());
		eDistance.add(e.getDistance());
	}
}
