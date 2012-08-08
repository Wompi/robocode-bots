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
package wompi.numbat.gun.misc;

public class NumbatST_dH_V_A extends ANumbatSTCode
{
	private final int		ENCODE_PARAMS		= 3;
	private final int		DELTA_HEADING_INDEX	= 20;
	private final int		VELOCITY_INDEX		= 8;
	private final double	HEAD_FACTOR			= 2.0;
	private final double	VELO_FACTOR			= 1.0;

	/**
	 * parameter:
	 * 0 - deltaHeading
	 * 1 - velocity
	 * 2 - lastVelocity
	 */
	@Override
	public int encode(double... encodeParam)
	{
		if (encodeParam.length != ENCODE_PARAMS) throw new IllegalArgumentException("wrong encode parameter");
		double deltaHeading = encodeParam[0];
		double velocity = encodeParam[1];
		double lastVelo = encodeParam[2];

		int hint = (int) Math.rint(Math.toDegrees(deltaHeading * HEAD_FACTOR)) + DELTA_HEADING_INDEX;
		int vint = (int) (Math.rint(velocity * VELO_FACTOR)) + VELOCITY_INDEX;
		// int aInt = (int)(Math.rint(lastVelo - velocity)) + 2;

		int head = (hint) << 8;
		int v = (head + (vint));
		// int step = v + aInt;
		int step = v;
		return step;
	}

	/**
	 * parameter:
	 * 0 - deltaHeading
	 * 1 - velocity
	 * 2 - acceleration
	 */
	@Override
	public double[] decode(int tick)
	{
		double[] result = new double[3];
		int pHeadChange = tick >> 8;
		result[0] = Math.toRadians((pHeadChange - DELTA_HEADING_INDEX) / HEAD_FACTOR); // delta heading

		int vkey = (tick) - (pHeadChange << 8);
		result[1] = (vkey - VELOCITY_INDEX) / VELO_FACTOR; // velocity

		return result;

	}
}
