package grid;

import grid.Terrain.TerrainType;

import java.awt.Color;
import java.awt.Point;
import java.util.LinkedList;
import java.util.List;

public class GridCell {
	public Point chunkPos;
	public GridCell north, south, east, west;

	public TerrainType terrain;
	
	public GridCell(Point pos){
		chunkPos = pos;
		terrain = TerrainType.GRASS;
	}
	
	public char getChar(){
		return terrain.c;
	}
	
	public String toString(){
		return "" + getChar();
	}
	
	public List<GridCell> neighbors(){
		LinkedList<GridCell> list = new LinkedList<GridCell>();
		if (north != null)
			list.add(north);
		if (south != null)
			list.add(south);
		if (east != null)
			list.add(east);
		if (west != null)
			list.add(west);
		return list;
	}
	
	public int neighborsOfType(TerrainType type){
		int x = 0;
		for (GridCell cell : neighbors())
			x = (cell.terrain.value == type.value ? x + 1 : x);
		return x;
	}
	
	public boolean surrounded(TerrainType type){
		for (GridCell cell : neighbors())
			if (cell.terrain.value != type.value)
				return false;
		return true;
	}

	public static GridCell[][] fromNoise(float[][] noise) {
		int len = noise.length;
		GridCell[][] cells = new GridCell[len][len];
		for (int x = 0; x < len; x++)
			for (int y = 0 ; y < len; y++){
				cells[x][y] = new GridCell(new Point(x,y));
				int val = (int)(255 * noise[x][y]);
				cells[x][y].terrain = (val < 112 ? TerrainType.DEEP :
					val < 116 ?  TerrainType.SHALLOW : 
						val < 120 ? TerrainType.SHORE : // TerrainType.GRASS);
							val < 142? TerrainType.GRASS : 
								val < 166 ? TerrainType.TREE :
									val < 214 ? TerrainType.ROCK: TerrainType.SNOW);
				cells[x][y].chunkPos = new Point(x,y);
			}
		return cells;
	}
}
