package terrain;

import grid.Terrain;
import grid.Terrain.Terrain;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.image.BufferedImage;


public class TerrainChunk {
	public static final int CHUNK_SIZE = 64;
	
	private TerrainCell[][] map;
	private Point chunkPos;
	private transient BufferedImage background;
	private boolean rendering;
	
	public TerrainChunk(Point pos, float[][] noise) {
		map = new TerrainCell[CHUNK_SIZE][CHUNK_SIZE];
		int dx = pos.x * CHUNK_SIZE;
		int dy = pos.y * CHUNK_SIZE;
		for (int x = 0; x < CHUNK_SIZE; x++)
			for (int y = 0; y < CHUNK_SIZE; y++)
			{
				int height = 1024 * noise[x][y];
				map[x][y] = new TerrainCell(new Point(dx + x, dy + y), 
						(height < 500 ? Color.blue.darker() : 
							height < 524 ? Color.blue :
								height < 532 ? Color.yellow :
									height < 536 ? Color.green : 
										height < 560 ? Color.green.darker() : 
											height < 584 ? Color.green.darker().darker() : 
												height < 608 ? Color.green.darker().darker().darker() : 
													height < 616 ? new Color(96,96,96) : 
														height < 628 ? new Color(160, 160, 160) : 
															height < 652 ? new Color(200,200,200):
																Color.white));
			}
		
		chunkPos = pos;
		redraw();
	}
	
	public BufferedImage getBackground(){
		if (background == null)
			redraw();
		return background;
	}

	public Dimension getMapDimension(){
		return new Dimension(TerrainCell.ISO_SIZE * (CHUNK_SIZE+1), (TerrainCell.ISO_SIZE * (CHUNK_SIZE+1))/2 + TerrainCell.ISO_SIZE/2);
	}
	
	private void redraw() {
		if (!rendering)
			new Thread(new ChunkRenderer()).start();
		rendering = true;
	}
	
	private class ChunkRenderer implements Runnable {
		public void run() {
			int dx = TerrainCell.ISO_SIZE/2, dy = dx/2;
			int width = TerrainCell.ISO_SIZE * (CHUNK_SIZE+1);
			int height = (int)Math.ceil(width/2) + dx;
			if (background == null) 
					background = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2d = (Graphics2D)background.getGraphics();
			g2d.setColor(Color.black);
			g2d.fillRect(0, 0, width, height);
			g2d.setColor(Color.green);
			
			BufferedImage tile;
			Polygon py;
			
			g2d.setColor(Color.black);
			
			for (int x = 0; x < CHUNK_SIZE; x++){
				Point p = new Point(width/2 + (x-1)*dx, (x+1)*dy);
				py = new Polygon();
				py.addPoint(p.x, p.y + 3*dy);
				py.addPoint(p.x + dx, p.y + 2*dy);
				py.addPoint(p.x + 2*dx, p.y + 3*dy);
				py.addPoint(p.x + dx, p.y + 4*dy);
				py.addPoint(p.x, p.y + 3*dy);
				
				for (int y = 0; y < CHUNK_SIZE; y++){

					tile = Terrain.terrainImages.get(map[x][y].terrain.str);
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
		float [][] 
	}
}
