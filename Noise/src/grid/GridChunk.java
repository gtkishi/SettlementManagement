package grid;

import grid.Terrain.TerrainType;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import noise.FractalNoise;

public class GridChunk {
	public static final int CHUNK_SIZE = 256;

	private GridCell[][] map;
	private BufferedImage background, miniMap;
	//private Point chunkLoc;
	public static final int MINIMAP_FACTOR = (int) Math.floor(Math.max(1, CHUNK_SIZE/192));
	
	public GridChunk(){
		// initialize map (all grass)
		newMap();
		//addTerrain(new Random(System.currentTimeMillis()), TerrainType.TREE, 35, 15, 10, 50, false);
	}
		
	public BufferedImage getMiniMap(){
		return miniMap;
	}
	
	public Dimension getMapDimension(){
		return new Dimension(Terrain.ISO_SIZE * (CHUNK_SIZE+1), (Terrain.ISO_SIZE * (CHUNK_SIZE+1))/2 + Terrain.ISO_SIZE/2);
	}
	
	public void addTerrain(Random rand, TerrainType type, int maxPercent, int minPercent, int seedTiles, int spreadPercent, boolean smooth){
		double percent = (minPercent + rand.nextInt(maxPercent - minPercent + 1))/ 100.0;
		
		int tiles = (int)(percent * CHUNK_SIZE * CHUNK_SIZE);
		//System.out.printf("%d%% %s: %d seed tile(s)\n", (int)(percent*100), type.str, seedTiles);
		
		List<Point> added = new ArrayList<Point>();
		Point p;
		for (int x = 0; x < seedTiles && tiles > 0; x++) {
			do{
				p = new Point(rand.nextInt(CHUNK_SIZE), rand.nextInt(CHUNK_SIZE));
			} while (added.contains(p) || map[p.x][p.y].terrain != TerrainType.GRASS);
			added.add(p); map[p.x][p.y].terrain = type;
			tiles--;
		}
		
		while (tiles > 0){
			if (added.size() == 0 || rand.nextInt(1000) < spreadPercent){
				p = addSeed(rand, added, type);
				tiles--;	
			}
			else {
				GridCell cell = null;
				do{
					if (added.size() == 0){
						p = addSeed(rand, added, type);
						tiles--;
						break;
					}
					
					p = added.get(rand.nextInt(added.size()));
					cell = map[p.x][p.y];
					List<GridCell> neighbors = cell.neighbors();
					if (cell.neighborsOfType(TerrainType.GRASS) < 1){
						added.remove(p);
						cell = null;
					}
					else
						cell = neighbors.get(rand.nextInt(neighbors.size()));
				}while(cell == null|| cell.terrain != TerrainType.GRASS);
				p = cell.chunkPos;
			}
			map[p.x][p.y].terrain = type;
			added.add(p);
			tiles--;
		}
		
		if (smooth)
			for (int x = 0; x < CHUNK_SIZE; x++)
				for (int y = 0; y < CHUNK_SIZE; y++){
					if (map[x][y].terrain == TerrainType.GRASS && map[x][y].neighborsOfType(TerrainType.GRASS) < 2)
						map[x][y].terrain = type;
					if (map[x][y].terrain == type && map[x][y].neighborsOfType(type) < 2)
						map[x][y].terrain = TerrainType.GRASS;
				}
	}
	
	public Point addSeed(Random rand, List<Point> added, TerrainType type){
		Point p;
		do{
			p = new Point(rand.nextInt(CHUNK_SIZE), rand.nextInt(CHUNK_SIZE));
		} while (added.contains(p) || map[p.x][p.y].terrain != TerrainType.GRASS);
		added.add(p); map[p.x][p.y].terrain = type;
		return p;
	}
	

	public String toString(){
		StringBuilder s = new StringBuilder();
		
		for (int y = 0; y <= 3 * CHUNK_SIZE + 1; y++)
			s.append('-');
		s.append('\n');
		for (int x = 0; x < CHUNK_SIZE; x++)
			for (int y = 0; y < CHUNK_SIZE; y++){
				if (y == 0)
					s.append('|');
				s.append(' ');
				s.append(map[x][y].getChar());
				s.append(' ');
				if (y + 1 == CHUNK_SIZE){
					s.append('|'); s.append('\n');
				}
			}
		
		for (int y = 0; y <= 3 * CHUNK_SIZE + 1; y++)
			s.append('-');

		return s.toString();
	}
	
	/*public void simpleDraw(Graphics g){
		if (background != null){
			g.drawImage(background, 0, 0, null);
			return;
		}
		background = new BufferedImage(Terrain.TILE_SIZE * CHUNK_SIZE, Terrain.TILE_SIZE * CHUNK_SIZE, BufferedImage.TYPE_INT_RGB);
		miniMap = new BufferedImage(CHUNK_SIZE/MINIMAP_FACTOR, CHUNK_SIZE/MINIMAP_FACTOR,BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = (Graphics2D)background.getGraphics(), mg = (Graphics2D)miniMap.getGraphics();
		
		BufferedImage tile;
		for (int x = 0; x < CHUNK_SIZE; x++){
			for (int y = 0; y < CHUNK_SIZE; y++){
				tile = Terrain.terrainImages.get(map[x][y].terrain.str);
				if (tile == null){
					g2d.setColor(Color.PINK);
					g2d.fillRect(x*Terrain.TILE_SIZE, y*Terrain.TILE_SIZE, Terrain.TILE_SIZE, Terrain.TILE_SIZE);
				}
				else
					g2d.drawImage(tile, x*Terrain.TILE_SIZE, y*Terrain.TILE_SIZE, null);
				g2d.setColor(Color.BLACK);
				g2d.drawRect(x*Terrain.TILE_SIZE, y*Terrain.TILE_SIZE, Terrain.TILE_SIZE, Terrain.TILE_SIZE);
				if (x % MINIMAP_FACTOR == 0 && y % MINIMAP_FACTOR == 0){
					switch(map[x][y].terrain){
					case GRASS:	mg.setColor(Color.green); break;
					case TREE:	mg.setColor(new Color(17, 139, 17)); break;
					case WATER:	mg.setColor(Color.blue); break;
					case ROCK:	mg.setColor(Color.darkGray); break;
					default:	mg.setColor(Color.BLACK);
					}
					mg.fillRect(x/MINIMAP_FACTOR, y/MINIMAP_FACTOR, 1,1);
				}
			}
		}
		g.drawImage(background, 0, 0, null);
	}*/
	
	public void isoDraw(Graphics g){
		if (background != null){
			g.drawImage(background, 0, 0, null);
			return;
		}
		
		int dx = Terrain.ISO_SIZE/2, dy = dx/2;
		//System.out.printf("%s,%s\n",dx, dy);
		
		int width = Terrain.ISO_SIZE * (CHUNK_SIZE+1);
		int height = (int)Math.ceil(width/2) + dx;
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
				if (map[x][y].terrain == TerrainType.GRASS || 
						map[x][y].terrain == TerrainType.SHALLOW || 
							map[x][y].terrain == TerrainType.SHORE || map[x][y].terrain == TerrainType.DEEP)
					g2d.drawPolygon(py);
				p.translate(-dx, dy);
				py.translate(-dx, dy);
			}
		}
		g.drawImage(background, 0, 0, null);
		//System.out.println("drew new map");
	/*	minimap = background.*/
	}

	public void newMap() {

		// initialize map (all grass)
		//System.out.println("new map");
		map = GridCell.fromNoise(FractalNoise.noiseArr2d(-CHUNK_SIZE / 2, - CHUNK_SIZE / 2, CHUNK_SIZE, CHUNK_SIZE, 1.4f, 8, System.currentTimeMillis()));
/*		for (int x = 0; x < CHUNK_SIZE; x++)
			for (int y = 0; y < CHUNK_SIZE; y++)
				map[x][y] = new GridCell(new Point(x, y));*/
		
		// setup graph in the chunk
		for (int x = 0; x < CHUNK_SIZE; x++)
			for (int y = 0; y < CHUNK_SIZE; y++){
				map[x][y].north = (y > 0 ? map[x][y-1] : null);
				map[x][y].south = (y + 1 < CHUNK_SIZE ? map[x][y+1] : null);
				map[x][y].east = (x > 0 ? map[x-1][y] : null);
				map[x][y].west = (x + 1 < CHUNK_SIZE ? map[x+1][y] : null);
			}
		background = null;
	}
}
