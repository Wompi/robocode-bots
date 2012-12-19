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
	final WallHeadingDistance	myWallDistance;
	RobotStatus					myStatus;
	ScannedRobotEvent			myRobotEvent;
	RobotStatus					myScanStatus;
	double						bSpeed;

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
		Point2D me = myWallDistance.getStartPoint();
		double x = myStatus.getX() + Math.sin(absBearing) * myRobotEvent.getDistance();
		double y = myStatus.getY() + Math.cos(absBearing) * myRobotEvent.getDistance();
		Point2D enemy = new Point2D.Double(x, y);

		myWallDistance.setHeading(absBearing + maxEscAngle);
		Point2D end0 = RobotMath.calculatePolarPoint(myWallDistance.getForwardHeading(),
				myWallDistance.getForwardDistance(), me);
		myWallDistance.setHeading(absBearing - maxEscAngle);
		Point2D end1 = RobotMath.calculatePolarPoint(myWallDistance.getForwardHeading(),
				myWallDistance.getForwardDistance(), me);

		double distToPerp = Math.sin(maxEscAngle) * myRobotEvent.getDistance();
		double angleToPerp = (Math.PI / 2.0) - maxEscAngle;
		double eAbsBearing = absBearing - Math.PI;

		Point2D perpPointCW = RobotMath.calculatePolarPoint(eAbsBearing + angleToPerp, distToPerp, enemy);
		Point2D perpPointCCW = RobotMath.calculatePolarPoint(eAbsBearing - angleToPerp, distToPerp, enemy);

		PaintHelper.drawLine(enemy, perpPointCW, g, Color.BLUE);
		PaintHelper.drawLine(enemy, perpPointCCW, g, Color.BLUE);
		PaintHelper.drawLine(me, enemy, g, Color.DARK_GRAY);
		PaintHelper.drawLine(me, end0, g, Color.BLUE);
		PaintHelper.drawLine(me, end1, g, Color.BLUE);
	}
}
