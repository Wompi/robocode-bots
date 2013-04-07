package wompi;

import robocode.JuniorRobot;

public class Bilby_Junior extends JuniorRobot
{
	@Override
	public void run()
	{
		turnGunLeft(20);
	}

	@Override
	public void onScannedRobot()
	{
		System.out.format("Scan!\n");
	}
}
