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
package wompi.quokka;

import robocode.Condition;

public class QuokkaEnemy
{
	double	pendingBulletPower;
	String	myName;
}

class Wave extends Condition
{
	String	enemyName;
	double	power;
	long	guessedFlighTime;

	@Override
	public boolean test()
	{
		guessedFlighTime--;
		return (guessedFlighTime <= 0);
	}

}
