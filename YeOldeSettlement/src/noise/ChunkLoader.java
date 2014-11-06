package noise;

import noise.FractalNoise.ChunkLoadedListener;

public class ChunkLoader implements Runnable{
	private int dx, dy, size, octaves;
	private float persistence;
	private long seed;
	private ChunkLoadedListener callback;
	
	public ChunkLoader(int dx, int dy, int size, float persistence, int octaves, long seed, ChunkLoadedListener listener){
		this.dx = dx; this.dy = dy;
		this.size = size; this.octaves = octaves;
		this.persistence = persistence;
		this.seed = seed;
		this.callback = listener;
	}
	
	public void run() {
		callback.chunkLoaded(FractalNoise.fracNoise2D(dx, dy, size, persistence, octaves, seed));
	}
}
