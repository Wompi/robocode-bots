package wompi.numbat.debug;

import java.awt.Color;

import robocode.AdvancedRobot;

public class DebugColorChanger
{
	public static void setColor(AdvancedRobot myBot)
	{
		if (myBot.getGunHeat() < 0.8) myBot.setAllColors(Color.RED);
		else myBot.setAllColors(Color.WHITE);

	}
}
