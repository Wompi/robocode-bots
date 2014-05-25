package wompi.paint;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;

import wompi.move.MoveArea;

public class PaintMoveArea
{

	public void onPaint(Graphics2D g, MoveArea moveArea)
	{
		Stroke old = g.getStroke();
		g.setStroke(new BasicStroke(1));
		g.setColor(Color.GREEN);
		g.draw(moveArea.getArea());
		g.setColor(new Color(Color.LIGHT_GRAY.getColorSpace(), Color.LIGHT_GRAY.getColorComponents(null), 0.2f));
		g.fill(moveArea.getArea());
		g.setStroke(old);
	}
}
