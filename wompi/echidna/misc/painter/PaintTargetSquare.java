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
package wompi.echidna.misc.painter;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;

public class PaintTargetSquare
{
	public static void drawTargetSquare(Graphics2D g, double eHeading, double eX, double eY, Color myColor)
	{
		double rDim = 20.0;      // robot width

		AffineTransform transform = new AffineTransform();
		Rectangle rectangle = new Rectangle(0, 0, 40, 40);
		Polygon poly = new Polygon();
		poly.addPoint(0, 0);
		poly.addPoint(0, 40);
		poly.addPoint(40, 40);
		poly.addPoint(40, 0);
		poly.addPoint(0, 0);

		transform.translate(eX - rDim, eY - rDim);
		transform.rotate(-eHeading, rectangle.getCenterX(), rectangle.getCenterY());

		Shape s1 = transform.createTransformedShape(rectangle);
		Shape s2 = transform.createTransformedShape(poly);

		g.setColor(myColor);
		g.fill(s1);
		g.setColor(Color.GREEN);
		g.draw(s2);
	}
}
