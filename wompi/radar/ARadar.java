package wompi.radar;

import robocode.AdvancedRobot;
import robocode.HitRobotEvent;
import robocode.ScannedRobotEvent;
import robocode.StatusEvent;

public abstract class ARadar
{
	public abstract void onInit(AdvancedRobot bot);

	public abstract void onStatus(StatusEvent e);

	public abstract void onRun();

	public abstract void onScannedRobot(ScannedRobotEvent e);

	public abstract void onHitRobot(HitRobotEvent e);
}
