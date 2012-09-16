package wompi;

import java.awt.geom.Point2D;

import robocode.AdvancedRobot;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.util.Utils;
import wompi.teststuff.NatSim;
import wompi.teststuff.NatSim.PredictionStatus;

public class LocationBot extends AdvancedRobot
{
	private static final boolean	PRINT_DEBUG_INFO	= true;
	private static final boolean	DISCARD_ROBOT_HITS	= true;
	private static final boolean	DISCARD_WALL_HITS	= false;

	private boolean					hitRobot;
	private boolean					hitWall;

	@Override
	public void run()
	{
		for (int x = 0; x < 100; x++)
		{
			hitRobot = false;
			hitWall = false;

			String debug = "";
			String test = "";

			setMaxVelocity(8);
			do
			{
				double goAngle = Math.atan2(500 - getX(), 500 - getY());

				setBackAsFront(this, goAngle);
				execute();
			}
			while (Point2D.distanceSq(getX(), getY(), 500, 500) > 0.1);

			System.out.format("[%d] lets go\n", getTime());
			turnRightRadians(Math.random() * 2 * Math.PI);
			double distance = (300 + (Math.random() * 1000)) * ((Math.random() > 0.5) ? 1 : -1);
			double turn = (Math.random() * 4 * Math.PI) - (2 * Math.PI);
			double maxVelocity;
			if (Math.random() < 0.5)
			{
				maxVelocity = 8;
			}
			else
			{
				maxVelocity = Math.random() * 7.5 + 0.5;
			}

			if (PRINT_DEBUG_INFO)
			{
				debug += String.format("[%d] Start state: \n", getTime());
				debug += String.format("[%d]   Location: %10.4f : %10.4f\n", getTime(), getX(), getY());
				debug += String.format("[%d]   Heading: %10.4f\n", getTime(), getHeadingRadians());
				debug += String.format("[%d]   Velocity: %8.3f \n", getTime(), getVelocity());
				debug += String.format("[%d]   Distance: %10.4f\n", getTime(), distance);
				debug += String.format("[%d]   Turn: %6.4f\n", getTime(), turn);
				debug += String.format("[%d]   Max Veocity: %8.3f\n", getTime(), maxVelocity);
			}

			test += String.format("[%d] %10.10f, %10.10f, %10.10f, %10.10f L, %10.10f, %10.10f, %10.10f, ", getTime(), getX(), getY(),
					getHeadingRadians(), getVelocity(), distance, turn, maxVelocity);

			System.out.format("[%d] Start: %s\n", getTime(), test);

			System.out.format("double angle = %10.10f;\n", turn);
			System.out.format("NanoSimWompi.x = %10.10f;\n", getX());
			System.out.format("NanoSimWompi.y = %10.10f;\n", getY());
			System.out.format("NanoSimWompi.h = %10.10f;\n", getHeadingRadians());
			System.out.format("NanoSimWompi.v = %10.10f;\n", getVelocity());
			System.out.format("double maxv = %10.10f;\n", maxVelocity);

			setMaxVelocity(maxVelocity);
			setAhead(distance);
			setTurnRightRadians(turn);
			do
			{
				System.out.format("[%d] %10.4f, %10.4f, %10.4f, %10.4f \n", getTime(), getX(), getY(), getHeadingRadians(), getVelocity());
				execute();
			}
			while (Math.abs(getDistanceRemaining()) > 0.000001);

			if (PRINT_DEBUG_INFO)
			{
				debug += String.format("[%d] End time: \n", getTime());
				debug += String.format("[%d]   Location: %10.4f : %10.4f\n", getTime(), getX(), getY());
				debug += String.format("[%d]   Heading: %10.4f \n", getTime(), getHeadingRadians());
			}

			test += String.format("[%d] L, %10.4f, %10.4f , %10.4f \n", getTime(), getX(), getY(), getHeadingRadians());

			if ((!hitRobot || !DISCARD_ROBOT_HITS) && (!hitWall || !DISCARD_WALL_HITS))
			{
				if (PRINT_DEBUG_INFO)
				{
					System.out.format(debug);
				}
				System.out.format("testPrediction(%s);", test);
				if (PRINT_DEBUG_INFO)
				{
					System.out.println("***************************************");
				}
			}
		}
	}

	@Override
	public void onHitRobot(HitRobotEvent e)
	{
		if (!hitRobot)
		{
			System.out.println("HIT A ROBOT");
			hitRobot = true;
		}
	}

	@Override
	public void onHitWall(HitWallEvent e)
	{
		if (!hitWall)
		{
			System.out.println("HIT A WALL");
			hitWall = true;
		}
	}

	public static void setBackAsFront(AdvancedRobot robot, double goAngle)
	{
		double angle = Utils.normalRelativeAngle(goAngle - robot.getHeadingRadians());

		if (Math.abs(angle) > (Math.PI / 2))
		{
			if (angle < 0)
			{
				robot.setTurnRightRadians(Math.PI + angle);
			}
			else
			{
				robot.setTurnLeftRadians(Math.PI - angle);
			}
			robot.setBack(100);
		}
		else
		{
			if (angle < 0)
			{
				robot.setTurnLeftRadians(-1 * angle);
			}
			else
			{
				robot.setTurnRightRadians(angle);
			}
			robot.setAhead(100);
		}
	}

	private void testPrediction(double startX, double startY, double startHeading, double startVelocity, long startTime, double distance,
			double turn, double maxVelocity, long endTime, double endX, double endY, double endHeading)
	{
		PredictionStatus startState = new PredictionStatus(startX, startY, startHeading, startVelocity, startTime);
		long ticks = endTime - startTime;
		PredictionStatus endState = NatSim.predict(startState, endHeading, maxVelocity);

		if (Math.abs(endX - endState.x) > 0.01) System.out.format("[%d] wrong x\n", endTime);
		if (Math.abs(endY - endState.x) > 0.01) System.out.format("[%d] wrong y\n", endTime);

		//		assertEquals(Utils.normalAbsoluteAngle(endHeading), Utils.normalAbsoluteAngle(endState.heading), 0.01);
	}
}
