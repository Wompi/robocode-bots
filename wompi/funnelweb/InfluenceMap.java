package wompi.funnelweb;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;

public class InfluenceMap
{
	private static final int						BORDER		= 20;
	private static final int						GRID_X		= 1000 - 2 * BORDER;
	private static final int						GRID_Y		= 1000 - 2 * BORDER;

	private static final int						GRID_STEP	= 8;

	private static final int						GRID_X_MAX	= GRID_X / GRID_STEP;
	private static final int						GRID_Y_MAX	= GRID_Y / GRID_STEP;

	private static final InfluenceNode[][]			GRID		= new InfluenceNode[GRID_X_MAX + 1][GRID_Y_MAX + 1];

	public static HashMap<Integer, InfluenceOwner>	GRID_OWNER	= new HashMap<Integer, InfluenceOwner>();

	//@formatter:off
	private static final int ALPHA = 50; 
	private static Color[]							cField		=														
		{ 	getAlphaColor(Color.RED,ALPHA), 
			getAlphaColor(Color.BLUE,ALPHA), 
			getAlphaColor(Color.CYAN,ALPHA), 
			getAlphaColor(Color.GRAY,ALPHA), 
			getAlphaColor(Color.GREEN,ALPHA), 
			getAlphaColor(Color.MAGENTA,ALPHA), 
			getAlphaColor(Color.ORANGE,ALPHA), 
			getAlphaColor(Color.PINK,ALPHA), 
			getAlphaColor(Color.YELLOW,ALPHA), 
			getAlphaColor(Color.WHITE,ALPHA), 
			getAlphaColor(Color.DARK_GRAY,ALPHA) 
		};
	//@formatter:on
	public InfluenceMap()
	{

	}

	private static Color getAlphaColor(Color c, int alpha)
	{
		return new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
	}

	public void init()
	{
		// create all nodes
		for (int y = 0; y <= GRID_Y_MAX; y++)
		{
			for (int x = 0; x <= GRID_X_MAX; x++)
			{
				InfluenceNode node = new InfluenceNode();
				node.nodeX = x * GRID_STEP;
				node.nodeY = y * GRID_STEP;

				GRID[x][y] = node;
			}
		}

		// register the borders for each node
		int x = -1;
		int y = -1;
		try
		{
			for (y = 0; y <= GRID_Y_MAX; y++)
			{
				for (x = 0; x <= GRID_X_MAX; x++)
				{
					if (x < GRID_X_MAX)
					{
						GRID[x][y].borderNodes.add(GRID[x + 1][y]);
					}
					if (x > 0)
					{
						GRID[x][y].borderNodes.add(GRID[x - 1][y]);
					}

					if (y < GRID_Y_MAX)
					{
						GRID[x][y].borderNodes.add(GRID[x][y + 1]);
					}
					if (y > 0)
					{
						GRID[x][y].borderNodes.add(GRID[x][y - 1]);
					}
				}
			}
		}
		catch (Exception e1)
		{
			System.out.format("x=%d y=%d \n", x, y);
		}
	}

	public void registerNodeOwner(int enemyID, Color enemyColor, double energy, double enemyX, double enemyY)
	{
		InfluenceOwner owner = GRID_OWNER.get(enemyID);
		if (owner == null)
		{
			owner = new InfluenceOwner();
			owner.ownerID = enemyID;
			owner.ownerColor = cField[enemyID];
			GRID_OWNER.put(enemyID, owner);
		}

		int xIndex = (int) Math.round((enemyX - BORDER) / GRID_STEP);
		int yIndex = (int) Math.round((enemyY - BORDER) / GRID_STEP);

		// just to adjust if the border is not the real border of our bots
		xIndex = Math.max(0, Math.min(xIndex, GRID_X_MAX));
		yIndex = Math.max(0, Math.min(yIndex, GRID_Y_MAX));

		owner.ownerX = xIndex;
		owner.ownerY = yIndex;
		owner.ownerEnergy = energy;
		//	System.out.format("xIndex=%d yIndex=%d max=%d \n", xIndex, yIndex, GRID_X_MAX);
	}

	public void onRobotDeath(int enemyID)
	{
		GRID_OWNER.remove(enemyID);
	}

	public void calcuateInfluence()
	{
		// reset last influence settings
		int nodeCount = 0;

		for (int y = 0; y <= GRID_Y_MAX; y++)
		{
			for (int x = 0; x <= GRID_X_MAX; x++)
			{
				InfluenceNode node = GRID[x][y];
				node.nodeColor = Color.WHITE;
				node.nodeOwner = -1;
				node.isClosed = false;
				nodeCount++;
			}
		}

		double ownerEnergySum = 0;
		for (InfluenceOwner owner : GRID_OWNER.values())
		{
			ownerEnergySum += owner.ownerEnergy;
		}

		for (InfluenceOwner owner : GRID_OWNER.values())
		{
			owner.ownerDanger = 1;
			//owner.ownerDanger = owner.ownerEnergy / ownerEnergySum;
			owner.gridDist = 0;
			owner.isBorderComplete = false;
		}

		for (InfluenceOwner owner : GRID_OWNER.values())
		{
			InfluenceNode centerNode = GRID[owner.ownerX][owner.ownerY];
			centerNode.nodeOwner = owner.ownerID;
			centerNode.nodeColor = owner.ownerColor;
			owner.ownedBorder = new ArrayList<InfluenceNode>();
			owner.ownedBorder.add(centerNode);
//			System.out.format("[%d] xCenter=%d yCenter=%d \n", owner.ownerID, owner.ownedBorder.get(0).nodeX,
//					owner.ownedBorder.get(0).nodeY);
		}

		int borderCount = GRID_OWNER.values().size();
		int breakCount = 0;
		while (borderCount < nodeCount && GRID_OWNER.size() > 0)
		{
			for (InfluenceOwner owner : GRID_OWNER.values())
			{
				if (!owner.isBorderComplete) // maintain this somehow
				{
					double nextGridDist = owner.ownerDanger * GRID_STEP + owner.gridDist;
					if (nextGridDist >= GRID_STEP)
					{
						owner.gridDist -= GRID_STEP;
						ArrayList<InfluenceNode> lastBorder = owner.ownedBorder;
						owner.ownedBorder = new ArrayList<InfluenceNode>();
						boolean hasStillBorders = false;
						for (InfluenceNode lastBorderNode : lastBorder)
						{
							if (!lastBorderNode.isClosed)
							{
								boolean hasNewBorders = false;
								for (InfluenceNode newBorderNode : lastBorderNode.borderNodes)
								{
									if (newBorderNode.nodeOwner == -1)
									{
										newBorderNode.nodeOwner = owner.ownerID;
										newBorderNode.nodeColor = owner.ownerColor;
										owner.ownedBorder.add(newBorderNode);
										hasNewBorders = true;
										hasStillBorders = true;
										borderCount++;

										if (lastBorderNode.borderNodes.size() < 4)
										{
											owner.ownedBorder.add(lastBorderNode);
										}

									}
									else
									{
										if (newBorderNode.nodeOwner != owner.ownerID)
										//|| newBorderNode.borderNodes.size() < 4)
										{
											owner.ownedBorder.add(lastBorderNode);
										}
									}
								}
								if (!hasNewBorders)
								{
									lastBorderNode.isClosed = true;
									owner.ownedBorder.add(lastBorderNode);
								}
							}
							else
							{
								owner.ownedBorder.add(lastBorderNode);
							}
						}

						if (!hasStillBorders)
						{
							owner.isBorderComplete = true;
						}
					}
					else
					{
						owner.gridDist = nextGridDist;
					}
				}
			}

			// just an emergency break if somthing went wrong
			breakCount++;

			if (breakCount == nodeCount + 10)
			{
				System.out.format("Emergency Break %d %d %d check the loop\n", borderCount, breakCount, nodeCount);
				break;
			}
		}
	}

	public void onPaint(Graphics2D g)
	{
		Stroke old = g.getStroke();
		g.setStroke(new BasicStroke(1));
		for (InfluenceOwner owner : GRID_OWNER.values())
		{
			ArrayList<InfluenceNode> borders = new ArrayList<InfluenceNode>(owner.ownedBorder);

			if (borders.size() == 0) continue;

			//System.out.format("BORDER[%d] borderSize=%d \n", owner.ownerID, owner.ownedBorder.size());

			InfluenceNode current = borders.get(0);
			Polygon aPath = new Polygon();

			int emergencyBreak = borders.size() + 10;

			while (!borders.isEmpty())
			{
				double minDist = Double.MAX_VALUE;
				int index = -1;
				for (int i = 0; i < borders.size(); i++)
				{
					InfluenceNode node = borders.get(i);
					double d = Point2D.distance(node.nodeX, node.nodeY, current.nodeX, current.nodeY);

					if (d < minDist)
					{
						minDist = d;
						index = i;
					}
				}
				current = borders.remove(index);
				aPath.addPoint(current.nodeX + BORDER, current.nodeY + BORDER);

				if (--emergencyBreak < 0)
				{
					System.out.format("Emergency Break %d... check the paint infuence loop\n", emergencyBreak);
					break;
				}

			}

			aPath.addPoint(aPath.xpoints[0], aPath.ypoints[0]);
			g.setColor(owner.ownerColor);
			g.fillPolygon(aPath);
			//g.drawPolygon(aPath);
		}
		g.setStroke(old);

	}
}

class InfluenceOwner
{
	protected int						ownerID;
	protected int						ownerX;
	protected int						ownerY;
	protected Color						ownerColor;

	protected double					ownerEnergy;
	protected double					ownerDanger;
	protected double					gridDist;

	protected ArrayList<InfluenceNode>	ownedBorder	= new ArrayList<InfluenceNode>();
	protected boolean					isBorderComplete;
}

class InfluenceNode
{
	protected ArrayList<InfluenceNode>	borderNodes	= new ArrayList<InfluenceNode>();

	protected int						nodeOwner	= -1;

	// debug maybe
	protected int						nodeX;
	protected int						nodeY;

	protected Color						nodeColor	= Color.WHITE;
	protected boolean					isClosed;

}
