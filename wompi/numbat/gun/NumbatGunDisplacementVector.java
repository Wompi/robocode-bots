package wompi.numbat.gun;

import robocode.RobotStatus;
import robocode.ScannedRobotEvent;
import wompi.numbat.gun.fire.ANumbatFire;
import wompi.numbat.target.ITargetManager;

public class NumbatGunDisplacementVector extends ANumbatGun
{

	@Override
	void setGun(RobotStatus status, ITargetManager targetMan, ANumbatFire fire)
	{

	}

	@Override
	public void onScannedRobot(ScannedRobotEvent e, RobotStatus myBotStatus, ITargetManager targetMan)
	{

	}

	@Override
	String getName()
	{
		return "Displacement Vector";
	}

	@Override
	boolean checkActivateRule(RobotStatus status, ITargetManager targetMan)
	{
		return false;
	}

}
