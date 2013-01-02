package wompi.radar;

import robocode.AdvancedRobot;
import robocode.HitRobotEvent;
import robocode.RobotStatus;
import robocode.ScannedRobotEvent;
import robocode.StatusEvent;
import robocode.util.Utils;

public class RadarSingleLock extends ARadar
{
	private final static double	LOCK_OFFSET	= 1.9;

	private double				slipDir;

	private AdvancedRobot		myBot;
	private RobotStatus			myStatus;
	private ScannedRobotEvent	myRobotStatus;

	@Override
	public void onInit(AdvancedRobot bot)
	{
		myBot = bot;
		myBot.setAdjustRadarForGunTurn(true);
		myBot.setAdjustRadarForRobotTurn(true);
	}

	@Override
	public void onStatus(StatusEvent e)
	{
		myStatus = e.getStatus();
	}

	@Override
	public void onRun()
	{
		double rTurn;
		long sDelta = myStatus.getTime() - myRobotStatus.getTime();
		if (sDelta == 0)
		{
			double absBearing = myStatus.getHeadingRadians() + myRobotStatus.getBearingRadians();
			rTurn = Utils.normalRelativeAngle(absBearing - myStatus.getRadarHeadingRadians()) * LOCK_OFFSET;
			slipDir = Math.signum(rTurn);
		}
		else
		{
			rTurn = Double.POSITIVE_INFINITY * ((slipDir == 0) ? 1 : slipDir); // TODO: zero slipDir is sitting duck, maybe 1 is not good
			//System.out.format("[%04d] scan = %d \n", myStatus.getTime(), sDelta);
		}

		myBot.setTurnRadarRightRadians(rTurn);
	}

	@Override
	public void onScannedRobot(ScannedRobotEvent e)
	{
		myRobotStatus = e;
	}

	@Override
	public void onHitRobot(HitRobotEvent e)
	{
		// TODO: what to do on ram - turn radar to nearest bot?
	}
}
