package wompi.teststuff;

import java.awt.geom.Point2D;

public class RozuSim
{
	public double	v, h;

	public Point2D.Double predictPosition(int direction, Point2D.Double predictedPosition)
	{
		//		double predictedVelocity = v;
		//		double predictedHeading = h;
		//		double maxTurning, moveAngle, moveDir;
		//
		//		moveAngle = wallSmoothing(predictedPosition, absoluteBearing(surfWave.fireLocation, predictedPosition) + (direction * (Math.PI / 2)),
		//				direction) - predictedHeading;
		//		moveDir = 1;
		//
		//		if (Math.cos(moveAngle) < 0)
		//		{
		//			moveAngle += Math.PI;
		//			moveDir = -1;
		//		}
		//
		//		moveAngle = Utils.normalRelativeAngle(moveAngle);
		//
		//		// maxTurning is built in like this, you can't turn more then this in one tick
		//		maxTurning = Math.PI / 720d * (40d - 3d * Math.abs(predictedVelocity));
		//		predictedHeading = Utils.normalRelativeAngle(predictedHeading + limit(-maxTurning, moveAngle, maxTurning));
		//
		//		// this one is nice ;). if predictedVelocity and moveDir have
		//		// different signs you want to breack down
		//		// otherwise you want to accelerate (look at the factor "2")
		//		predictedVelocity += (predictedVelocity * moveDir < 0 ? 2 * moveDir : moveDir);
		//		predictedVelocity = limit(-8, predictedVelocity, 8);
		//
		//		// calculate the new predicted position
		//		predictedPosition = project(predictedPosition, predictedHeading, predictedVelocity);
		//
		return predictedPosition;
	}

	private static double limit(double min, double value, double max)
	{
		return Math.max(min, Math.min(value, max));
	}

}
