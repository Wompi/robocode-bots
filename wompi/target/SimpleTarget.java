package wompi.target;

import java.awt.Graphics2D;

import robocode.ScannedRobotEvent;

public class SimpleTarget extends ATarget
{

	@Override
	void onScannedRobot(ScannedRobotEvent e)
	{
		myScanns.add(new TargetScan(e));
	}

	@Override
	void onPaint(Graphics2D g)
	{
		// TODO Auto-generated method stub

	}

}
