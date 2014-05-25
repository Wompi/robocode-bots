package wompi.radar;

import java.util.HashSet;

import robocode.AdvancedRobot;
import robocode.HitRobotEvent;
import robocode.RobotStatus;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.StatusEvent;
import robocode.util.Utils;
import wompi.robotcontrol.IRobotTurn;

public class RadarStartSearch extends ARadar
{

	private IRadar					myRadarBot;
	private IRobotTurn				myTurnBot;

	private RobotStatus				myStatus;
	private boolean					isFound;

	private double					sDir;

	private final ARadar			nextRadar;

	private final HashSet<String>	rEnemys;

	private double					startRadar;

	// some init stuff
	private double					BATTLE_FIELD_H;
	private double					BATTLE_FIELD_W;
	private int						enemyCount;

	public RadarStartSearch(ARadar nRadar)
	{
		nextRadar = nRadar;
		rEnemys = new HashSet<String>();
	}

	@Override
	public void onInit(AdvancedRobot bot)
	{
		bot.setAdjustGunForRobotTurn(false);
		bot.setAdjustRadarForGunTurn(false);
		bot.setAdjustRadarForRobotTurn(false);

		myRadarBot = (IRadar) bot;
		myTurnBot = (IRobotTurn) bot;

		BATTLE_FIELD_H = bot.getBattleFieldHeight();
		BATTLE_FIELD_W = bot.getBattleFieldWidth();
		enemyCount = bot.getOthers();

		double cx = BATTLE_FIELD_W / 2.0;
		double cy = BATTLE_FIELD_H / 2.0;
		double bAngle = Math.atan2(cx - myStatus.getX(), cy - myStatus.getY());
		sDir = Math.signum(Utils.normalRelativeAngle(bAngle - myStatus.getRadarHeadingRadians()));

		startRadar = bot.getRadarHeadingRadians();
		System.out.format("[%03d] init %3.5f\n", myStatus.getTime(), Math.toDegrees(startRadar));

	}

	private void checkFound()
	{
		// TODO: if one bot sits outside the scan range this will never terminate .. take this into
		// account some sort of turnTick == 8 or so but better would be to check if we have turned one full
		// circle - tick handling is the key
		// remember: he can only sit on the opposite diagonal edge all other edges should be scanned - this makes a good 
		// point to just make an dummy entry to the left over enemy
		if (rEnemys.size() == enemyCount)
		{
			myRadarBot.setRadar(nextRadar);
			System.out.format("[%03d] Found all %d Enemys\n", myStatus.getTime(), enemyCount);
		}
		else
		{
			System.out.format("[%03d] Found %d Enemys so far\n", myStatus.getTime(), rEnemys.size());
		}
	}

	@Override
	public void onRun()
	{}

	@Override
	public void onScannedRobot(ScannedRobotEvent e)
	{
		rEnemys.add(e.getName());
		checkFound();
	}

	@Override
	public void onStatus(StatusEvent e)
	{
		myStatus = e.getStatus();

		if (myTurnBot != null) // the first call to init is made after onStatus
		{
			myTurnBot.setRadarTurnAmount(Rules.RADAR_TURN_RATE_RADIANS * sDir, getName());
			myTurnBot.setGunTurnAmount(Rules.GUN_TURN_RATE_RADIANS * sDir, getName());
			myTurnBot.setBodyTurnAmount(Rules.MAX_TURN_RATE_RADIANS * sDir, getName());
		}

		double deltaRadar = Utils.normalRelativeAngle(startRadar - myStatus.getRadarHeadingRadians());
		System.out.format("[%03d] onStatus\n", myStatus.getTime());

		System.out.format("[%03d] deltaRadar=%3.5f \n", myStatus.getTime(), Math.toDegrees(deltaRadar));
		startRadar = myStatus.getRadarHeadingRadians();
	}

	@Override
	public void onHitRobot(HitRobotEvent e)
	{
		rEnemys.add(e.getName());
		checkFound();
	}

	@Override
	public String getName()
	{
		return "RadarStartSearch";
	}
}
