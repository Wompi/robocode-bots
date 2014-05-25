package wompi.target;

import java.awt.Graphics2D;
import java.util.ArrayList;

import robocode.AdvancedRobot;
import robocode.ScannedRobotEvent;
import robocode.StatusEvent;

public abstract class ATarget
{
	protected final ArrayList<TargetScan>	myScanns	= new ArrayList<TargetScan>();
	protected AdvancedRobot					myBot;

	public void onInit(AdvancedRobot bot)
	{
		myBot = bot;
	}

	abstract public void onStatus(StatusEvent e);

	abstract public void onScannedRobot(ScannedRobotEvent e);

	abstract public void onPaint(Graphics2D g);
}
