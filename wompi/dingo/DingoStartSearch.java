package wompi.dingo;

import robocode.AdvancedRobot;
import robocode.util.Utils;

public class DingoStartSearch
{
	private static final double	INF	= Double.MAX_VALUE;

	private AdvancedRobot		myBot;
	private boolean				isFound;

	private double				sDir;

	public void onInit(AdvancedRobot bot)
	{
		isFound = false;
		myBot = bot;
		myBot.setAdjustGunForRobotTurn(false);
		myBot.setAdjustRadarForGunTurn(false);
		myBot.setAdjustRadarForRobotTurn(false);

		double cx = bot.getBattleFieldWidth() / 2.0;
		double cy = bot.getBattleFieldHeight() / 2.0;
		double bAngle = Math.atan2(cx - myBot.getX(), cy - myBot.getY());
		sDir = Math.signum(Utils.normalRelativeAngle(bAngle - bot.getRadarHeadingRadians()));
	}

	public void onRun()
	{
		myBot.setTurnRightRadians(INF * sDir);
		myBot.setTurnGunRightRadians(INF * sDir);
		myBot.setTurnRadarRightRadians(INF * sDir);
	}

	public void onScannedRobot()
	{
		if (!isFound) System.out.format("[%04d] enemy found! \n", myBot.getTime());
		isFound = true;

	}

	public boolean isFound()
	{
		return isFound;
	}
}
