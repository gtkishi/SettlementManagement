package terrain;

import java.awt.Point;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;

import noise.FractalNoise;
import noise.FractalNoise.ChunkLoadedListener;

public class ChunkLoader implements Runnable{
	private Point pos;
	private int dx, dy, octaves;
	private float persistence;
	private long seed;
	private ChunkLoadedListener callback;
	private TerrainCache cache;
	
	public ChunkLoader(Point p, float persistence, int octaves, long seed, ChunkLoadedListener listener, TerrainCache cache){
		this.pos = p;
		this.dx = p.x * TerrainChunk.CHUNK_SIZE; 
		this.dy = p.y * TerrainChunk.CHUNK_SIZE;
		this.octaves = octaves;
		this.persistence = persistence;
		this.seed = seed;
		this.callback = listener;
		this.cache = cache;
	}
	
	public void run() {
		File f = new File(TerrainCache.fileString(seed, pos));
		TerrainChunk chunk = null;
		if (f.exists()) {
			try {
				ObjectInputStream input = new ObjectInputStream(new FileInputStream(f));
				chunk = (TerrainChunk)input.readObject();
				input.close();
				System.out.printf("Loaded chunk at (%d, %d) from \'%s\'\n", pos.x ,pos.y, f.getName());
			} catch(Exception e){
				f.delete();
			}
		}
			
		if (chunk == null)
			callback.chunkLoaded(FractalNoise.fracNoise2D(dx, dy, TerrainChunk.CHUNK_SIZE, persistence, octaves, seed));
		else
			cache.put(pos, chunk);
	}
}
