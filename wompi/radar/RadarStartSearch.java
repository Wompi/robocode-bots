package wompi.radar;

import robocode.AdvancedRobot;
import robocode.HitRobotEvent;
import robocode.RobotStatus;
import robocode.ScannedRobotEvent;
import robocode.StatusEvent;
import robocode.util.Utils;

public class RadarStartSearch extends ARadar
{
	private static final double	INF	= Double.MAX_VALUE;

	private AdvancedRobot		myBot;
	private RobotStatus			myStatus;
	private boolean				isFound;

	private double				sDir;

	private final ARadar		myRadar;

	public RadarStartSearch(ARadar nextRadar)
	{
		myRadar = nextRadar;
	}

	@Override
	public void onInit(AdvancedRobot bot)
	{
		myBot = bot;
		myBot.setAdjustGunForRobotTurn(false);
		myBot.setAdjustRadarForGunTurn(false);
		myBot.setAdjustRadarForRobotTurn(false);

		double cx = bot.getBattleFieldWidth() / 2.0;
		double cy = bot.getBattleFieldHeight() / 2.0;
		double bAngle = Math.atan2(cx - myStatus.getX(), cy - myStatus.getY());
		sDir = Math.signum(Utils.normalRelativeAngle(bAngle - myStatus.getRadarHeadingRadians()));
	}

	private boolean isFound()
	{
		// TODO: for melee start search this should be a function of time and getOthers
		return isFound;
	}

	@Override
	public void onRun()
	{
		if (isFound())
		{
			myRadar.onRun();
		}
		else
		{
			myBot.setTurnRightRadians(INF * sDir);
			myBot.setTurnGunRightRadians(INF * sDir);
			myBot.setTurnRadarRightRadians(INF * sDir);
		}
	}

	@Override
	public void onScannedRobot(ScannedRobotEvent e)
	{
		if (!isFound())
		{
			System.out.format("[%04d] enemy found! \n", myStatus.getTime());
			myRadar.onInit(myBot);
			isFound = true;
		}
		myRadar.onScannedRobot(e);
	}

	@Override
	public void onStatus(StatusEvent e)
	{
		if (!isFound())
		{
			myStatus = e.getStatus();
		}
		myRadar.onStatus(e);
	}

	@Override
	public void onHitRobot(HitRobotEvent e)
	{
		if (!isFound())
		{
			System.out.format("[%04d] enemy found through ramming! \n", myStatus.getTime());
			myRadar.onInit(myBot);
			isFound = true;
		}
		myRadar.onHitRobot(e);
	}
}
