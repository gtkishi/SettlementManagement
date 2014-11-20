package terrain;

import java.awt.Point;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class TerrainCell implements Serializable{
	public Point chunkPos;
	public TerrainCell north, south, east, west;

	public Terrain terrain;
	public String filename;
	
	public TerrainCell(Point pos){
		chunkPos = pos;
		terrain = Terrain.GRASS;
		filename = terrain.getFilename();
	}
	
	public TerrainCell(Point pos, Terrain type)
	{
		chunkPos = pos;
		terrain = type;
		filename = terrain.getFilename();
	}
	
	public char getChar(){
		return terrain.c;
	}
	
	public String toString(){
		return "" + getChar();
	}
	
	public List<TerrainCell> neighbors(){
		LinkedList<TerrainCell> list = new LinkedList<TerrainCell>();
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
	
	public int neighborsOfType(Terrain type){
		int x = 0;
		for (TerrainCell cell : neighbors())
			x = (cell.terrain.value == type.value ? x + 1 : x);
		return x;
	}
	
	public boolean surrounded(Terrain type){
		for (TerrainCell cell : neighbors())
			if (cell.terrain.value != type.value)
				return false;
		return true;
	}
}
