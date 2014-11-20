package view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.awt.image.BufferedImage;
import java.util.TreeSet;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

import terrain.Terrain;
import terrain.TerrainCache;
import terrain.TerrainChunk;

public class Infinipanel extends JPanel{
	private TerrainCache cache;
	private Point center;
	private Point offset;
	private Point mousePos;
	private Dimension chunkDim;
	public TreeSet<Character> pressed;
	
	private class MouseListener extends MouseAdapter {
		public void mouseExited(MouseEvent e) {
			mousePos = null;
		}
		
		public void mouseMoved(MouseEvent e){
			mousePos = e.getPoint();
		}
	}
	
	public Infinipanel() {
		cache = new TerrainCache(9, 1.5f, System.currentTimeMillis(), 64);
		chunkDim = TerrainChunk.getMapDimension();
		center = new Point(0, 0);
		offset = new Point(chunkDim.width / 2, chunkDim.height / 2);
		pressed = new TreeSet<Character>();
		
		this.setPreferredSize(chunkDim);
		this.addMouseMotionListener(new MouseListener());
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.setColor(Color.black);
		g.fillRect(0, 0, this.getWidth(), this.getHeight());

		Point c;
		for (int x = -1; x < 2; x++)
			for (int y = -1; y < 2; y++){
				c = new Point(center.x + x, center.y + y);
				TerrainChunk chunk = cache.get(c);
				if (chunk!= null) {
					BufferedImage image = chunk.getBackground();
					int xPos = getWidth()/2 - offset.x + (x - y) * (chunkDim.width / 2);
					int yPos = getHeight()/2 - offset.y + (x + y) * (chunkDim.height / 2 - Terrain.ISO_SIZE/4);
					
					g.drawImage(image, xPos, yPos, null);
				}
			}
		Point2D curr = currentTile();
		Point2D rel = relativeTile();
		
		g.setColor(Color.yellow);
		if (mousePos != null){
			Point m = new Point(mousePos.x - getWidth()/2, mousePos.y - getHeight()/2);
			Point2D relMouseTile = Terrain.orthoToIso(new Point2D.Double(m.x + offset.x - chunkDim.width/2, m.y + offset.y));
			Point2D absMouseTile = new Point2D.Double(relMouseTile.getX() + TerrainChunk.CHUNK_SIZE * center.x, relMouseTile.getY() + TerrainChunk.CHUNK_SIZE * center.y);
			g.drawString(String.format("(%d, %d)", (int)Math.floor(absMouseTile.getX()), (int)Math.floor(absMouseTile.getY())), this.getWidth() - 75, this.getHeight() - 10);

			Point p = getOrthoOffsets(relMouseTile);
			int dx = Terrain.ISO_SIZE/2, dy = dx/2;
			
			Polygon py = new Polygon();
			py.addPoint(p.x, p.y + 3*dy);
			py.addPoint(p.x + dx, p.y + 2*dy);
			py.addPoint(p.x + 2*dx, p.y + 3*dy);
			py.addPoint(p.x + dx, p.y + 4*dy);
			py.addPoint(p.x, p.y + 3*dy);
			
			g.drawPolygon(py);
		}
	}
	
	private Point getOrthoOffsets(Point2D isoPos){
		Point2D t1 = Terrain.isoToOrtho(relativeTile());
		Point2D t2 = Terrain.isoToOrtho(new Point2D.Double(Math.floor(isoPos.getX()), Math.floor(isoPos.getY())));

		return new Point(getWidth()/2 + (int)(t2.getX() - t1.getX()) - Terrain.HALF_ISO, getHeight()/2 + (int)(t2.getY() - t1.getY()) - Terrain.HALF_ISO);
	}
	
	private Point2D currentTile(){
		Point2D curr = new Point2D.Double(offset.x - chunkDim.width/2, offset.y);
		Point2D projected = Terrain.orthoToIso(curr);
		return new Point2D.Double(projected.getX() + TerrainChunk.CHUNK_SIZE * center.x, projected.getY() + TerrainChunk.CHUNK_SIZE * center.y);
	}
	
	private Point2D relativeTile(){
		Point2D curr = new Point2D.Double(offset.x - chunkDim.width/2, offset.y);
		return Terrain.orthoToIso(curr);
	}
	
	public void move(int dx, int dy){
		offset.translate(dx, dy);
	}
	
	public void update(){
		int d = 2;
		for (char c : pressed){
			switch(c){
				case 'w':	move(0, -d);
							break;
				case 'a':	move(-d, 0);
							break;
				case 's':	move(0, d);
							break;
				case 'd':	move(d, 0);
							break;
			}
		}
		
		Point2D curr = currentTile();
		Point newChunk = new Point((int)Math.floor(curr.getX()/TerrainChunk.CHUNK_SIZE), (int)Math.floor(curr.getY()/TerrainChunk.CHUNK_SIZE));
		
		// re-center when currChunk != center, determine new offsets
		if (!newChunk.equals(center)) {
			Point2D orthoCenter = Terrain.isoToOrtho(new Point2D.Double(center.getX() * TerrainChunk.CHUNK_SIZE, center.getY() * TerrainChunk.CHUNK_SIZE));
			Point2D orthoNew = Terrain.isoToOrtho(new Point2D.Double(newChunk.getX() * TerrainChunk.CHUNK_SIZE, newChunk.getY() * TerrainChunk.CHUNK_SIZE));
			Point2D orthoOff = new Point2D.Double(orthoCenter.getX() + offset.x, orthoCenter.getY() + offset.y);
			Point2D newOff = new Point2D.Double(orthoOff.getX() - orthoNew.getX(), orthoOff.getY() - orthoNew.getY());
			offset.x = (int)newOff.getX();
			offset.y = (int)newOff.getY();
			center = newChunk;
		}
		
		repaint();
	}
	
	public static void main(String[] args){
		JFrame frame = new JFrame();
		Infinipanel panel = new Infinipanel();
		frame.setSize(800, 600);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(panel);
		frame.setVisible(true);
		
		new Timer(10, (e -> panel.update())).start();
		frame.addKeyListener(new KeyAdapter(){
			public void keyPressed(KeyEvent arg0) {
				panel.pressed.add(arg0.getKeyChar());
			}
			
			public void keyReleased(KeyEvent arg0){
				panel.pressed.remove(arg0.getKeyChar());
			}
		});
		Thread.currentThread().setPriority(Thread.NORM_PRIORITY + 1);
	}
}
