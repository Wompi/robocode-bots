package wompi.paint;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;

import robocode.AdvancedRobot;
import robocode.RobotStatus;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.StatusEvent;
import wompi.robomath.RobotMath;
import wompi.robomath.WallHeadingDistance;

public class PaintMaxEscapeAngle
{
	private final WallHeadingDistance	myWallDistance;
	private RobotStatus					myStatus;
	private ScannedRobotEvent			myRobotEvent;
	private RobotStatus					myScanStatus;
	private double						bSpeed;

	public PaintMaxEscapeAngle()
	{
		myWallDistance = new WallHeadingDistance();
		bSpeed = Double.NaN;
	}

	public void onInit(AdvancedRobot bot, double fieldBorder)
	{
		myWallDistance.onInit(bot, fieldBorder);
	}

	public void onStatus(StatusEvent e)
	{
		myStatus = e.getStatus();
	}

	public void setBulletSpeed(double bulletPower)
	{
		bSpeed = Rules.getBulletSpeed(bulletPower);
	}

	public void onScannedRobot(ScannedRobotEvent e)
	{
		myRobotEvent = e;
		myScanStatus = myStatus;
		myWallDistance.setStartPoint(myStatus.getX(), myStatus.getY());
	}

	public void onPaint(Graphics2D g)
	{
		if (Double.isNaN(bSpeed))
		{
			System.out.format("ERROR: bSpeed is not set in PaintMaxEscapeAngle\n");
			return;
		}
		double maxEscAngle = RobotMath.getSimpleEscapeAngle(bSpeed);

		double absBearing = myScanStatus.getHeadingRadians() + myRobotEvent.getBearingRadians();
		Point2D start = new Point2D.Double(myScanStatus.getX(), myScanStatus.getY());

		myWallDistance.setHeading(absBearing + maxEscAngle);
		Point2D end0 = RobotMath.calculatePolarPoint(myWallDistance.getForwardHeading(),
				myWallDistance.getForwardDistance(), start);
		myWallDistance.setHeading(absBearing - maxEscAngle);
		Point2D end1 = RobotMath.calculatePolarPoint(myWallDistance.getForwardHeading(),
				myWallDistance.getForwardDistance(), start);

		PaintHelper.drawLine(start, end0, g, Color.BLUE);
		PaintHelper.drawLine(start, end1, g, Color.BLUE);
	}
}
