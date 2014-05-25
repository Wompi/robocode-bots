package wompi.target;

import java.awt.Graphics2D;
import java.util.HashMap;

import robocode.AdvancedRobot;
import robocode.ScannedRobotEvent;
import robocode.StatusEvent;

public class SimpleTargetHandler extends ATargetHandler
{
	private final HashMap<String, ATarget>	myTargets;

	private StatusEvent						myStatus;

	public SimpleTargetHandler()
	{
		myTargets = new HashMap<String, ATarget>();
	}

	@Override
	public void onInit(AdvancedRobot bot)
	{
		super.onInit(bot);

		for (ATarget target : myTargets.values())
		{
			target.onInit(bot);
		}
	}

	@Override
	public void onStatus(StatusEvent e)
	{
		myStatus = e;

		for (ATarget target : myTargets.values())
		{
			target.onStatus(e);
		}
	}

	@Override
	public void onScannedRobot(ScannedRobotEvent e)
	{
		ATarget target = myTargets.get(e.getName());
		if (target == null)
		{
			myTargets.put(e.getName(), target = new AreaTarget(myStatus));
			target.onInit(myBot);
		}
		target.onScannedRobot(e);
	}

	@Override
	public void onPaint(Graphics2D g)
	{
		for (ATarget target : myTargets.values())
		{
			target.onPaint(g);
		}
	}
}
