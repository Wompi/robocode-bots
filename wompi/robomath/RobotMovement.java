package wompi.robomath;

import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;

import robocode.Rules;

public class RobotMovement
{
	public static Shape stripOutsidePoints(Shape s, Rectangle2D bField)
	{
		PathIterator ite = s.getPathIterator(null);
		Polygon result = new Polygon();
		while (!ite.isDone())
		{
			double coord[] = new double[6];
			ite.next();
			int type = ite.currentSegment(coord);
			if (type == PathIterator.SEG_LINETO)
			{
				if (!bField.contains(coord[0], coord[1]))
				{
					coord[0] = Math.max(bField.getMinX(), Math.min(coord[0], bField.getMaxX()));
					coord[1] = Math.max(bField.getMinY(), Math.min(coord[1], bField.getMaxY()));
				}

				System.out.format("x=%3.5f y=%3.5f type=%d \n", coord[0], coord[1], type);
				result.addPoint((int) coord[0], (int) coord[1]);
			}
		}
		return result;
	}

	// taken from voidious for testing stuff 
	public static double getNextVelocity(double velocity, double distance, double maxVelocity)
	{
		if (distance < 0)
		{
			// If the distance is negative, then change it to be positive
			// and change the sign of the input velocity and the result
			return -getNextVelocity(-velocity, -distance, maxVelocity);
		}

		final double goalVel;
		if (distance == Double.POSITIVE_INFINITY)
		{
			goalVel = maxVelocity;
		}
		else
		{
			goalVel = Math.min(getMaxVelocity(distance), maxVelocity);
		}

		if (velocity >= 0)
		{
			return RobotMath.limit(velocity - Rules.DECELERATION, goalVel, velocity + Rules.ACCELERATION);
		}
		else
		{
			return RobotMath.limit(velocity - Rules.ACCELERATION, goalVel, velocity + maxDecel(-velocity));
		}
	}

	private static double getMaxVelocity(double distance)
	{
		final double decelTime = Math.max(1,
				Math.ceil((Math.sqrt((4 * 2 / Rules.DECELERATION) * distance + 1) - 1) / 2));
		// sum of 0..decelTime, solving for decelTime using quadratic formula

		final double decelDist = (decelTime / 2.0) * (decelTime - 1) // sum of 0..(decelTime-1)
				* Rules.DECELERATION;

		return ((decelTime - 1) * Rules.DECELERATION) + ((distance - decelDist) / decelTime);
	}

	private static double maxDecel(double velocity)
	{
		velocity = Math.abs(velocity);
		if (velocity > Rules.DECELERATION)
		{
			return Rules.DECELERATION;
		}
		else
		{
			double tickFractionDecel = velocity / Rules.DECELERATION;
			double tickFractionAccel = 1 - tickFractionDecel;
			return (tickFractionDecel * Rules.DECELERATION) + (tickFractionAccel * Rules.ACCELERATION);
		}
	}
}
