package wompi.paint;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;

import robocode.ScannedRobotEvent;
import wompi.robomath.RobotMath;

public class PaintEnemyMaxEscapeAngle extends PaintMaxEscapeAngle
{

	@Override
	public void onScannedRobot(ScannedRobotEvent e)
	{
		myRobotEvent = e;
		myScanStatus = myStatus;
		double absBearing = myStatus.getHeadingRadians() + e.getBearingRadians();
		double x = myStatus.getX() + Math.sin(absBearing) * e.getDistance();
		double y = myStatus.getY() + Math.cos(absBearing) * e.getDistance();
		myWallDistance.setStartPoint(x, y);

	}

	@Override
	public void onPaint(Graphics2D g)
	{
		if (Double.isNaN(bSpeed))
		{
			System.out.format("ERROR: bSpeed is not set in PaintMaxEscapeAngle\n");
			return;
		}
		double maxEscAngle = RobotMath.getSimpleEscapeAngle(bSpeed);

		double absBearing = myScanStatus.getHeadingRadians() + myRobotEvent.getBearingRadians() - Math.PI;
		Point2D enemy = myWallDistance.getStartPoint();
		Point2D me = new Point2D.Double(myScanStatus.getX(), myScanStatus.getY());

		myWallDistance.setHeading(absBearing + maxEscAngle);
		Point2D end0 = RobotMath.calculatePolarPoint(myWallDistance.getForwardHeading(),
				myWallDistance.getForwardDistance(), enemy);
		myWallDistance.setHeading(absBearing - maxEscAngle);
		Point2D end1 = RobotMath.calculatePolarPoint(myWallDistance.getForwardHeading(),
				myWallDistance.getForwardDistance(), enemy);

		double distToPerp = Math.sin(maxEscAngle) * myRobotEvent.getDistance();
		double angleToPerp = (Math.PI / 2.0) - maxEscAngle;
		double eAbsBearing = absBearing - Math.PI;

		Point2D perpPointCW = RobotMath.calculatePolarPoint(eAbsBearing + angleToPerp, distToPerp, me);
		Point2D perpPointCCW = RobotMath.calculatePolarPoint(eAbsBearing - angleToPerp, distToPerp, me);

		PaintHelper.drawLine(me, perpPointCW, g, Color.GREEN);
		PaintHelper.drawLine(me, perpPointCCW, g, Color.GREEN);

		PaintHelper.drawLine(enemy, me, g, Color.DARK_GRAY);
		PaintHelper.drawLine(enemy, end0, g, Color.GREEN);
		PaintHelper.drawLine(enemy, end1, g, Color.GREEN);
	}

}
