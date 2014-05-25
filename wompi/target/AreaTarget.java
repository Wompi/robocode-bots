package wompi.target;

import java.awt.Graphics2D;

import robocode.AdvancedRobot;
import robocode.RobotStatus;
import robocode.ScannedRobotEvent;
import robocode.StatusEvent;
import wompi.move.MoveArea;
import wompi.paint.PaintMoveArea;
import wompi.radar.misc.BlindRadarArea;
import wompi.target.helper.StatsVelocity;

public class AreaTarget extends ATarget
{
	private RobotStatus				myStatus;

	private final MoveArea			myMoveArea;

	private final StatsVelocity		myMaxVeocity;

	private final PaintMoveArea		myPaintArea;

	private final BlindRadarArea	myBlindRadar;

	public AreaTarget(StatusEvent e)
	{
		myMoveArea = new MoveArea();
		myMoveArea.onStatus(e);
		myPaintArea = new PaintMoveArea();
		myMaxVeocity = new StatsVelocity();
		myBlindRadar = new BlindRadarArea();
		myStatus = e.getStatus();
	}

	@Override
	public void onInit(AdvancedRobot bot)
	{
		super.onInit(bot);
		myBlindRadar.onInit(bot);
		myMoveArea.onInit(bot);
	}

	@Override
	public void onStatus(StatusEvent e)
	{
		myStatus = e.getStatus();
		myMoveArea.onStatus(e);
	}

	@Override
	public void onScannedRobot(ScannedRobotEvent e)
	{
		double aBear = myStatus.getHeadingRadians() + e.getBearingRadians();
		double myX = myStatus.getX() + Math.sin(aBear) * e.getDistance();
		double myY = myStatus.getY() + Math.cos(aBear) * e.getDistance();

		myMaxVeocity.onScannedRobot(e);
		System.out.format("Forward: %3.5f Backward: %3.5f %s\n", myMaxVeocity.forwardVelocity,
				myMaxVeocity.backwardVelocity, e.getName());

		myMoveArea.onScannedRobot(e, myMaxVeocity);

		myBlindRadar.onScannedRobot(e, myX, myY);
	}

	@Override
	public void onPaint(Graphics2D g)
	{
		myPaintArea.onPaint(g, myMoveArea);
		myBlindRadar.onPaint(g);
	}

}
