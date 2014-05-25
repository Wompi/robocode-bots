package wompi.radar;

import robocode.AdvancedRobot;
import robocode.HitRobotEvent;
import robocode.ScannedRobotEvent;
import robocode.StatusEvent;

public class RadarDistanceWeighted extends ARadar
{

	@Override
	public void onInit(AdvancedRobot bot)
	{}

	@Override
	public void onStatus(StatusEvent e)
	{}

	@Override
	public void onRun()
	{}

	@Override
	public void onScannedRobot(ScannedRobotEvent e)
	{}

	@Override
	public void onHitRobot(HitRobotEvent e)
	{}

	@Override
	public String getName()
	{
		return "Distance Weighted";
	}

}
