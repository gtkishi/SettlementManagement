package noise;

import java.awt.Point;
import java.util.HashMap;
import java.util.Random;

public class FractalNoise {
	public interface ChunkLoadedListener {
		public void chunkLoaded(float[][] chunk);
	}
	
	public static float LERP(float a, float b, float x) {
		return a * (1.0f - x) + b * x;
	}
	
	private static float noise2(int x, int y, long seed, HashMap<Point, Float> cache) {
		Point p = new Point(x, y);
		Float val = cache.get(p);
		
		if (val == null) {
			Random rand = new Random(seed);
			rand.setSeed(x * rand.nextLong() + rand.nextLong());
			rand.setSeed(y * rand.nextLong() + rand.nextLong());
			val = rand.nextFloat();
			cache.put(p, val);
		}
		return val.floatValue();
	}
	
	private static float smoothNoise2(int x, int y, long seed, HashMap<Point, Float> cache){
		float corners = (noise2(x-1, y-1, seed, cache) + noise2(x+1, y-1, seed, cache) + noise2(x+1, y+1, seed, cache) + noise2(x-1, y+1, seed, cache)) / 16.0f;
		float sides = (noise2(x, y-1, seed, cache) + noise2(x, y+1, seed, cache) + noise2(x-1, y, seed, cache) + noise2(x+1, y, seed, cache)) / 8.0f;
		float center = noise2(x, y, seed, cache) / 4.0f;
		return corners + sides + center;
	}
	
	private static float interpNoise(float x, float y, long seed, HashMap<Point, Float> cache) {
		int intX = (x < 0 ? (int)(x-1) : (int)x);
		int intY = (y < 0 ? (int)(y-1) : (int)y);
		
		float dx = x - intX;
		float dy = y - intY;
		
		float top = LERP(smoothNoise2(intX, intY, seed, cache), smoothNoise2(intX+1, intY, seed, cache), dx);
		float bottom = LERP(smoothNoise2(intX, intY+1, seed, cache), smoothNoise2(intX+1, intY+1, seed, cache), dx);
		return LERP(top, bottom, dy);
	}
	
	public static float fractalNoise2(int x, int y, float persistence, int octaves, long seed, HashMap<Point, Float> cache){
		float freq;
		float amp = 1.0f, totalAmp = 0.0f;
		float total = 0.0f;
		
		for (int i = 0; i < octaves - 1; i++){
			freq = 1 << i;
			totalAmp += amp;
			
			total += interpNoise(x / freq, y / freq, seed, cache) * amp;
			
			amp *= persistence;
		}
		
		return total / totalAmp;
	}
	
	public static float[][] fracNoise2D(int dx, int dy, int size, float persistence, int octaves, long seed){
		HashMap<Point, Float> cache = new HashMap<Point, Float>();
		
		float[][] values = new float[size][size];
		for (int x = 0; x < size; x++)
			for (int y = 0; y < size; y++)
				values[x][y] = fractalNoise2(x + dx, y + dy, persistence, octaves, seed, cache);
		return values;
	}
	
	public static void loadChunk(int dx, int dy, int size, float persistence, int octaves, long seed, ChunkLoadedListener listener) {
		new Thread(new ChunkLoader(dx, dy, size, persistence, octaves, seed, listener)).start();
	}
}
