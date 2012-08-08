/*******************************************************************************
 * Copyright (c)  2012  Wompi 
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the ZLIB
 * which accompanies this distribution, and is available at
 * http://robowiki.net/wiki/ZLIB
 * 
 * Contributors:
 *     Wompi - initial API and implementation
 ******************************************************************************/
package wompi.echidna.radar;

import robocode.AdvancedRobot;
import robocode.CustomEvent;
import wompi.echidna.misc.ACondition;
import wompi.echidna.radar.conditions.RadarRuleGunLock;
import wompi.echidna.radar.conditions.RadarRuleStartSearch;

public class RadarConditioned extends ARadar
{
	ACondition	myRule;

	public RadarConditioned(AdvancedRobot robot)
	{
		super(robot);
	}

	@Override
	public void init()
	{
		myRule = new RadarRuleStartSearch(myRobot);
		myRule.followRule = new RadarRuleGunLock(myRobot);
		myRule.registerCondition();
	}

	@Override
	public void run()
	{
		myRule.processCommand();
	}

	@Override
	public void onCustomEvent(CustomEvent e)
	{
		ACondition condition = ((ACondition) (e.getCondition()));

		System.out.format("[%d] Radar: %s finished\n", myRobot.getTime(), condition.getConditionName());

		condition.unregisterCondition();
		if (condition.followRule != null)
		{
			myRule = condition.followRule;
			myRule.registerCondition();
		}
	}
}
