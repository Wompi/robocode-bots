package wompi.numbat.target;

import robocode.Rules;

public class NumbatBulletTracker
{
	double[]		myDistPower;

	long			myNextZeroGunHeat;
	public double	myLastFirePower;
	long			myLastShoot;
	private boolean	hasFired;

	public NumbatBulletTracker()
	{
		myDistPower = new double[13];
	}

	public void registerTrack(double distance, double energyDiff, long time)
	{
		if (energyDiff <= 3.0 && energyDiff >= .1 && time >= 30)
		{
			myLastFirePower = energyDiff;
			hasFired = true;
			myLastShoot = time;
			myNextZeroGunHeat = time + (long) (Rules.getGunHeat(myLastFirePower) / 0.1);
		}
		else
		{
			hasFired = false;
		}
	}

	public boolean hasFired(long scanDiff)
	{
		return hasFired && scanDiff <= 5;
	}

	public long nextZeroHeat()
	{
		return myNextZeroGunHeat;
	}
}
