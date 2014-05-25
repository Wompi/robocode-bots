package wompi.move;

import robocode.Rules;

public class MoveMath
{
	//@formatter:off
	private static final int [][]			VELOCITY_LOOKUP	= 
	{ // velo / dist_to_max / turns
			{0 	, 36 ,  8 },
			{1 	, 35 ,  7 },
			{2 	, 33 ,  6 },
			{3 	, 30 ,  5 },
			{4 	, 26 ,  4 },
			{5 	, 21 ,  3 },
			{6 	, 15 ,  2 },
			{7 	,  8 ,  1 },
			{8 	,  8 ,  1 }
	};
		//@formatter:on

	public static int getAccelerationDist(double velocity, double maxVelocity)
	{
		//System.out.format("Get accelerationDist for %d (%3.5f) \n", (int) Math.round(velocity), velocity);
		return VELOCITY_LOOKUP[(int) Math.round(velocity)][1];
	}

	public static int getAccelerationTurns(double velocity, double maxVelocity)
	{
		//System.out.format("Get accelerationTurn for %d (%3.5f) \n", (int) Math.round(velocity), velocity);
		return VELOCITY_LOOKUP[(int) Math.round(velocity)][2];
	}

	public static double getVelocityDist(double velo, double maxVelo)
	{
		double dist = 0;
		while (velo <= maxVelo)
		{
			velo = Math.min(velo + Rules.ACCELERATION, maxVelo);
			dist += velo;
		}
		return dist;
	}
}
