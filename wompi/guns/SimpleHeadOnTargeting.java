package wompi.guns;

import robocode.AdvancedRobot;
import robocode.RobotStatus;
import robocode.ScannedRobotEvent;
import robocode.StatusEvent;
import robocode.util.Utils;

public class SimpleHeadOnTargeting
{
	private AdvancedRobot	myBot;
	private RobotStatus		myStatus;

	public SimpleHeadOnTargeting()
	{

	}

	public void onInit(AdvancedRobot bot)
	{
		myBot = bot;
		myBot.setAdjustGunForRobotTurn(true);
	}

	public void onStatus(StatusEvent e)
	{
		myStatus = e.getStatus();
	}

	public void onScannedRobot(ScannedRobotEvent e)
	{
		double absHeading = myStatus.getHeadingRadians() + e.getHeadingRadians();
		myBot.setTurnGunRightRadians(Utils.normalRelativeAngle(absHeading - myStatus.getGunHeadingRadians()));
	}

	public void onRun()
	{

	}
}
