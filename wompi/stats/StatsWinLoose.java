package wompi.stats;

import java.util.Arrays;

import robocode.AdvancedRobot;
import robocode.DeathEvent;
import robocode.WinEvent;

public class StatsWinLoose
{
	private int[]			survivalStats;
	private int[]			lostSurvivalPoints;

	private int				sumLostPoints;

	private AdvancedRobot	myBot;

	public void onInit(AdvancedRobot bot)
	{
		if (survivalStats == null)
		{
			survivalStats = new int[bot.getOthers() + 1];
			lostSurvivalPoints = new int[bot.getOthers() + 1];
			sumLostPoints = 0;
		}
		myBot = bot;
		//int round = myBot.getRoundNum() + 1;
	}

	public void onDeath(DeathEvent e)
	{
		survivalStats[myBot.getOthers()]++;

		int lostPoints = (myBot.getOthers()) * 50 + 90;
		sumLostPoints += lostPoints;
		lostSurvivalPoints[myBot.getOthers()] += lostPoints;
		printStats();
	}

	public void onWin(WinEvent e)
	{
		survivalStats[0]++;
		printStats();
	}

	private void printStats()
	{
		System.out.format("Survival: %s\n", Arrays.toString(survivalStats));
		System.out.format("LostPoints[%d]: %s\n", sumLostPoints, Arrays.toString(lostSurvivalPoints));
	}
}
