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
package wompi.echidna.stats;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;

import wompi.paint.PaintHelper;

/**
 * Diese klasse soll das relative movement ueber einen bestimmten zeitraum visualisieren.
 * Dazu wird die position alle 10 ticks bestimmt und in ein feld eingeordnet. Jedes Feld wird dann
 * um eins erhoeht und am ende sollte dann (hoffentlich) das relative verhalten fuer 10 ticks sichtbar werden
 * - sitting duck sollte dann nur ein feld haben welches extrem hochgezaehlt wird
 * -
 * mit ein wenig glueck kann man aus diesem feld dann irgednwas extrahieren was einem schusswinkel gleicht
 * 
 * @author wompi
 */
public class TargetReleativeMovement
{
	public static long	INTERVAL	= 20;								// time window for measures
	static int			FIELD_SIZE	= 15;
	static int			DEF_SIZE	= 36;
	static int			DEF_X_INDEX	= (FIELD_SIZE - 1) / 2;
	static int			DEF_Y_INDEX	= (FIELD_SIZE - 1) / 2;

	// field
	// x - target
	// row-column dist = 40 (robot width)

	// 0 1 2 3 4 5 6 7 8 9 A
	// 0 |-|-|-|-|-|-|-|-|-|-|-|
	// 1 |-|-|-|-|-|-|-|-|-|-|-|
	// 2 |-|-|-|-|-|-|-|-|-|-|-|
	// 3 |-|-|-|-|-|-|-|-|-|-|-|
	// 4 |-|-|-|-|-|-|-|-|-|-|-|
	// 5 |-|-|-|-|-|X|-|-|-|-|-|
	// 6 |-|-|-|-|-|-|-|-|-|-|-|
	// 7 |-|-|-|-|-|-|-|-|-|-|-|
	// 8 |-|-|-|-|-|-|-|-|-|-|-|
	// 9 |-|-|-|-|-|-|-|-|-|-|-|
	// A |-|-|-|-|-|-|-|-|-|-|-|

	// Beispiel [4:5]: dy = 20..40 dx= -20...+20
	// dy = 0.5...1.0 dx = -0.5..0.5 dx/40 dy/40

	int					field[][]	= new int[FIELD_SIZE][FIELD_SIZE];

	public void registerNewMove(Point2D newPos, Point2D oldPos)
	{
		double dx = newPos.getX() - oldPos.getX();
		double dy = newPos.getY() - oldPos.getY();

		int xIndex = DEF_X_INDEX + (int) Math.round(dx / DEF_SIZE);
		int yIndex = DEF_Y_INDEX - (int) Math.round(dy / DEF_SIZE);
		try
		{
			field[xIndex][yIndex] += 1;
			System.out.format("index[%d:%d] delta:[%3.2f:%3.2f]\n", xIndex, yIndex, dx, dy);
		}
		catch (ArrayIndexOutOfBoundsException e)
		{
			System.out.format("Last scan was to long ago...this would be [%d][%d]\n", xIndex, yIndex);
		}
	}

	public Point2D getMaxPoints(double bulletTime)
	{
		return new Point2D.Double();
	}

	public void printField()
	{
		for (int y = 0; y < FIELD_SIZE; y++)
		{
			for (int x = 0; x < FIELD_SIZE; x++)
			{
				if (x == DEF_X_INDEX && y == DEF_Y_INDEX)
				{
					System.out.format("[%3d] ", field[x][y]);
				}
				else
				{
					System.out.format("%3d ", field[x][y]);
				}
			}
			System.out.format("\n");
		}
		System.out.format("\n");
	}

	public void onPaint(Graphics2D g, Point2D pos)
	{
		int max = 0;

		for (int i = 0; i < FIELD_SIZE; i++)
		{
			for (int j = 0; j < FIELD_SIZE; j++)
			{
				max = Math.max(max, field[i][j]);
				// if (field[i][j] < min) min = field[i][j];
			}
		}

		for (int y = 0; y < FIELD_SIZE; y++)
		{
			for (int x = 0; x < FIELD_SIZE; x++)
			{
				double xi = (x - DEF_X_INDEX) * DEF_SIZE;
				double yi = (y - DEF_Y_INDEX) * DEF_SIZE;
				int col = 0;
				if (field[x][y] > 0) col = 255 * field[x][y] / max;
				// System.out.format("[%3.2f:%3.2f]",x,y);
				if (col > 0) PaintHelper.drawPoint(new Point2D.Double(pos.getX() + xi, pos.getY() - yi), new Color(col, 0, 0, 200), g, 36);
				// PaintHelper.drawPoint(new Point2D.Double(x ,y), Color.YELLOW, g,1);
			}
			// System.out.format("\n");
		}
	}

}
