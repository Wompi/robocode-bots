package wompi.radar;

import robocode.AdvancedRobot;
import robocode.HitRobotEvent;
import robocode.RobotStatus;
import robocode.ScannedRobotEvent;
import robocode.StatusEvent;
import robocode.util.Utils;
import wompi.robotcontrol.IRobotTurn;

public class RadarSingleLock extends ARadar
{
	private final static double	LOCK_OFFSET	= 1.9;

	private double				slipDir;

	private IRadar				myRadarBot;
	private IRobotTurn			myTurnBot;
	private final ARadar		nextRadar;

	private RobotStatus			myStatus;

	public RadarSingleLock(ARadar radar)
	{
		nextRadar = radar;
	}

	@Override
	public void onInit(AdvancedRobot bot)
	{
		bot.setAdjustGunForRobotTurn(true);
		bot.setAdjustRadarForGunTurn(true);
		bot.setAdjustRadarForRobotTurn(true);

		myRadarBot = (IRadar) bot;
		myTurnBot = (IRobotTurn) bot;
	}

	@Override
	public void onStatus(StatusEvent e)
	{
		myStatus = e.getStatus();
	}

	@Override
	public void onRun()
	{}

	@Override
	public void onScannedRobot(ScannedRobotEvent e)
	{
		double rTurn;
		long sDelta = myStatus.getTime() - e.getTime();
		if (sDelta == 0)
		{
			double absBearing = myStatus.getHeadingRadians() + e.getBearingRadians();
			rTurn = Utils.normalRelativeAngle(absBearing - myStatus.getRadarHeadingRadians()) * LOCK_OFFSET;
			slipDir = Math.signum(rTurn);
		}
		else
		{
			rTurn = Double.POSITIVE_INFINITY * ((slipDir == 0) ? 1 : slipDir); // TODO: zero slipDir is sitting duck, maybe 1 is not good
			//System.out.format("[%04d] scan = %d \n", myStatus.getTime(), sDelta);
		}

		myTurnBot.setRadarTurnAmount(rTurn, getName());
	}

	@Override
	public void onHitRobot(HitRobotEvent e)
	{
		// TODO: what to do on ram - turn radar to nearest bot?
	}

	@Override
	public String getName()
	{
		// TODO Auto-generated method stub
		return "RadarSingleLock";
	}
}
