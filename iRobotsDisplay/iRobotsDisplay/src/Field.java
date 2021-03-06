import java.awt.Color;
import java.awt.Graphics;
import java.awt.Polygon;

import javax.swing.JPanel;


public class Field extends JPanel
{
	private Polygon poly;
	
	public Field()
	{
		initComponents();
	}
	
	public void initComponents()
	{
		this.setBackground(Color.WHITE);
		poly = new Polygon();
		poly.addPoint(100, 100);
		poly.addPoint(150, 150);
		poly.addPoint(50,  150);
	}
	
	@Override
	public void paintComponent( Graphics g ) 
	{
        super.paintComponent(g);
        g.setColor(Color.BLUE);
        g.drawRect(100, 100, 100, 100);
        g.drawPolygon(poly);
	}
}
