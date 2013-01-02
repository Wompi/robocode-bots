package wompi.numbat.move;

import robocode.AdvancedRobot;
import robocode.RobotStatus;
import wompi.numbat.target.ITargetManager;
import wompi.numbat.target.NumbatTarget;

public class NumbatMoveChase extends ANumbatMove
{

	private double	moveTurn;

	@Override
	void setMove(RobotStatus status, ITargetManager targetMan)
	{
		NumbatTarget target = targetMan.getMoveTarget();

		moveTurn = target.getAbsoluteBearing(status);
		moveTurn -= status.getHeadingRadians();
	}

	@Override
	String getName()
	{
		return "Chase Move";
	}

	@Override
	protected void excecute(AdvancedRobot myBot)
	{
		myBot.setTurnRightRadians(Math.tan(moveTurn));
		myBot.setAhead(1000 * Math.cos(moveTurn));
	}

	@Override
	boolean checkActivateRule(RobotStatus status, ITargetManager targetMan)
	{
		boolean r1 = status.getOthers() == 1;
		boolean r2 = targetMan.getGunTarget() != null;
		return r1 && r2;// || r3;
	}
}
