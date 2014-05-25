package wompi.target.helper;

import java.util.ArrayList;

import robocode.ScannedRobotEvent;

public class StatsVelocity
{
	public double				forwardVelocity;
	public double				backwardVelocity;

	public ArrayList<Long>		myTimes;
	public ArrayList<Double>	myVelocities;
	public ArrayList<Double>	myVelocityDirections;
	public ArrayList<Double>	myVelocityChanges;

	public StatsVelocity()
	{
		myTimes = new ArrayList<Long>();
		myVelocities = new ArrayList<Double>();
		myVelocityDirections = new ArrayList<Double>();
		myVelocityChanges = new ArrayList<Double>();

		myTimes.add(0L);
		myVelocities.add(0.0);
		myVelocityChanges.add(0.0);
		myVelocityDirections.add(0.0);
	}

	public void onScannedRobot(ScannedRobotEvent e)
	{
		myTimes.add(e.getTime());
		double last = myVelocities.get(myVelocities.size() - 1);
		myVelocities.add(e.getVelocity());
		myVelocityDirections.add(Math.signum(e.getVelocity()));
		myVelocityChanges.add(last - e.getVelocity());

		forwardVelocity = Math.max(forwardVelocity, e.getVelocity());
		backwardVelocity = Math.min(backwardVelocity, e.getVelocity());
	}

	public double getMaxVelocity()
	{
		// TODO: this is sightly wrong for zero velocity - maybe take acceleration or deceleration into account
		if (myVelocities.get(myVelocities.size() - 1) > 0)
		{
			//System.out.format("Forward: %3.5f \n", forwardVelocity);
			return forwardVelocity;
		}
		else
		{
			//System.out.format("Bacward: %3.5f \n", backwardVelocity);
			return backwardVelocity;
		}
	}
}
