package terrain;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.Serializable;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Timer;

import noise.FractalNoise;


public class TerrainChunk implements Serializable{
	public static final int CHUNK_SIZE = 96;
	
	private TerrainCell[][] map;
	private Point chunkPos;
	private transient BufferedImage background;
	private boolean rendering;
	
	public TerrainChunk(Point pos, float[][] noise) {
		map = new TerrainCell[CHUNK_SIZE][CHUNK_SIZE];
		chunkPos = pos;
		setNoise(noise);
	}
	
	public TerrainChunk(Point pos, float[][] noise, float[][] data){
		map = new TerrainCell[CHUNK_SIZE][CHUNK_SIZE];
		chunkPos = pos;
		setNoise2(noise, data);
	}
	
	public BufferedImage getBackground(){
		if (background == null)
			redraw();
		
		if (!rendering)
			return background;
		
		return null;
	}

	public static Dimension getMapDimension(){
		return new Dimension(Terrain.ISO_SIZE * CHUNK_SIZE, (Terrain.HALF_ISO * (CHUNK_SIZE + 1)));
	}
	
	public Point getPos(){return chunkPos;}
	
	public void setNoise(float[][] noise){
		int dx = chunkPos.x * CHUNK_SIZE;
		int dy = chunkPos.y * CHUNK_SIZE;
		for (int x = 0; x < CHUNK_SIZE; x++)
			for (int y = 0; y < CHUNK_SIZE; y++)
			{
				int height = (int)(1024 * noise[x][y]);
				map[x][y] = new TerrainCell(new Point(dx + x, dy + y), 
						(height < 500 ? Terrain.DEEP: 
							height < 524 ? Terrain.SHALLOW :
								height < 532 ? Terrain.SHORE :
									height < 560 ? Terrain.GRASS : 
										height < 608 ? Terrain.TREE : 
											height < 628 ? Terrain.ROCK : 
												Terrain.SNOW));
		}
		redraw();
	}
	
	public void setNoise2(float[][] noise, float[][] data){
		int dx = chunkPos.x * CHUNK_SIZE;
		int dy = chunkPos.y * CHUNK_SIZE;
		for (int x = 0; x < CHUNK_SIZE; x++){
			for (int y = 0; y < CHUNK_SIZE; y++)
			{
				int height = (int)(1024 * noise[x][y]);
				int value = (int)(256 * data[x][y]);
				map[x][y] = new TerrainCell(new Point(dx + x, dy + y), 
						(height < 500 ? Terrain.DEEP: 
							height < 524 ? Terrain.SHALLOW :
								height < 532 ? Terrain.SHORE :
									height < 540 ? Terrain.GRASS :
										height < 608 && value < 128 ? Terrain.GRASS :
											height < 608 ? Terrain.TREE : 
												height < 768 ? Terrain.ROCK : 
													Terrain.SNOW));
			}
			Thread.yield();
		}
		redraw();
	}
	
	private void redraw() {
		if (!rendering)
			new Thread(new ChunkRenderer()).start();
		rendering = true;
	}
	
	private class ChunkRenderer implements Runnable {
		public void run() {
			System.out.printf("Rendering chunk at (%d, %d)\n", chunkPos.x, chunkPos.y);
			int dx = Terrain.ISO_SIZE/2, dy = dx/2;
			Dimension d = getMapDimension();
			int width = d.width;
			int height = d.height;
			if (background == null) 
					background = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2d = (Graphics2D)background.getGraphics();
			g2d.setColor(new Color(0.0f, 0.0f, 0.0f, 0.0f));
			g2d.fillRect(0, 0, width, height);
			
			BufferedImage tile;
			Polygon py;
			
			g2d.setColor(Color.BLACK);
			
			for (int x = 0; x < CHUNK_SIZE; x++){
				Point p = new Point(width/2 + (x-1)*dx, x * dy);
				py = new Polygon();
				py.addPoint(p.x, p.y + 3*dy);
				py.addPoint(p.x + dx, p.y + 2*dy);
				py.addPoint(p.x + 2*dx, p.y + 3*dy);
				py.addPoint(p.x + dx, p.y + 4*dy);
				py.addPoint(p.x, p.y + 3*dy);
				
				for (int y = 0; y < CHUNK_SIZE; y++){
					tile = Terrain.terrainImages.get(map[x][y].filename);
					g2d.drawImage(tile, p.x, p.y, null);
					if (map[x][y].terrain == Terrain.GRASS || 
							map[x][y].terrain == Terrain.SHALLOW || 
								map[x][y].terrain == Terrain.SHORE || map[x][y].terrain == Terrain.DEEP)
						g2d.drawPolygon(py);
					p.translate(-dx, dy);
					py.translate(-dx, dy);
				}
			}
			rendering = false;
		}
	}
	
	public static void main(String[] args) {
		float [][] noise = FractalNoise.fracNoise2D(0, 0, CHUNK_SIZE, 1.5f, 9, System.currentTimeMillis());
		TerrainChunk chunk = new TerrainChunk(new Point(0, 0), noise);
		System.out.print(chunk);
		JPanel panel = new JPanel(){
			public void paintComponent(Graphics g){
				super.paintComponent(g);
				g.setColor(Color.black);
				g.fillRect(0, 0, this.getWidth(), this.getHeight());
				g.drawImage(chunk.getBackground(), 0, 0, null);
			}
		};
		panel.setPreferredSize(getMapDimension());
		panel.setBackground(Color.black);
		
		JFrame frame = new JFrame();
		frame.setSize(800, 600);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new JScrollPane(panel));
		
		panel.addMouseListener(new MouseAdapter(){
			public void mousePressed(MouseEvent e){
				chunk.setNoise(FractalNoise.fracNoise2D(0, 0, CHUNK_SIZE, 1.5f, 9, System.currentTimeMillis()));
				System.out.print(chunk);
			}
		});
		
		frame.setVisible(true);
		
		new Timer(30, new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				frame.repaint();
			}
			
		}).start();
	}
	
	public String toString() {
		StringBuilder s = new StringBuilder();
		for (int y = 0; y < CHUNK_SIZE; y++) {
			for (int x = 0; x <CHUNK_SIZE; x++) {
				s.append(map[x][y].getChar());
				s.append(' ');
			}
			s.append('\n');
		}
		return s.toString();
	}
}
