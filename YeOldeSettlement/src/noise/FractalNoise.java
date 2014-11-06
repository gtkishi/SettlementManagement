package noise;

import java.awt.Point;
import java.util.HashMap;
import java.util.Random;

public class FractalNoise {
	public interface ChunkLoadedListener {
		public void chunkLoaded(float[][] chunk);
	}
	
	/**
	 * Linearly interpolates between values a and b
	 * @param a	first value
	 * @param b	second value
	 * @param x	value from 0.0 to 1.0 used to interpolate between a (0.0) and b (1.0)
	 * @return
	 */
	public static float LERP(float a, float b, float x) {
		return a * (1.0f - x) + b * x;
	}
	
	/**
	 * Generates the gaussian noise for a given point and seed
	 * @param x	x coordinate of the point
	 * @param y	y coordinate of the point
	 * @param seed	seed to use for generation
	 * @param cache	cache of already generated points
	 * @return	the value of the noise at this point
	 */
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
	
	/**
	 * Generates the smoothed noise for a point using a simplistic gaussian kernel
	 * @param x	x coordinate of the point
	 * @param y	y coordinate of the point
	 * @param seed	seed to use for generation
	 * @param cache	cache of already generated points
	 * @return	the value of the smoothed noise at this point
	 */
	private static float smoothNoise2(int x, int y, long seed, HashMap<Point, Float> cache){
		float corners = (noise2(x-1, y-1, seed, cache) + noise2(x+1, y-1, seed, cache) + noise2(x+1, y+1, seed, cache) + noise2(x-1, y+1, seed, cache)) / 16.0f;
		float sides = (noise2(x, y-1, seed, cache) + noise2(x, y+1, seed, cache) + noise2(x-1, y, seed, cache) + noise2(x+1, y, seed, cache)) / 8.0f;
		float center = noise2(x, y, seed, cache) / 4.0f;
		return corners + sides + center;
	}
	
	/**
	 * Returns the interpolated smoothed gaussian noise for given points
	 * @param x	x coordinate of the point
	 * @param y	y coordinate of the point
	 * @param seed	seed to use for generation
	 * @param cache	cache of already generated points
	 * @return	the value of the interpolated smoothed noise at this point
	 */
	private static float interpNoise(float x, float y, long seed, HashMap<Point, Float> cache) {
		int intX = (x < 0 ? (int)(x-1) : (int)x);
		int intY = (y < 0 ? (int)(y-1) : (int)y);
		
		float dx = x - intX;
		float dy = y - intY;
		
		float top = LERP(smoothNoise2(intX, intY, seed, cache), smoothNoise2(intX+1, intY, seed, cache), dx);
		float bottom = LERP(smoothNoise2(intX, intY+1, seed, cache), smoothNoise2(intX+1, intY+1, seed, cache), dx);
		return LERP(top, bottom, dy);
	}
	
	/**
	 * Generates a noise value for a given point by using Fractal Brownian motion.
	 * 
	 * Several layers of noise are generated and averaged together to generate smooth
	 * interesting noise at the point level
	 * 
	 * @param x	x position of a point to generate the noise value for
	 * @param y	y position of a point to generate the noise value for
	 * @param persistence	indicates how much weight each subsequent layer has on the final noise value
	 * @param octaves	number of layers to generate
	 * @param seed	seed to use for random generation
	 * @param cache	cache to use for storing generated values
	 * @return	a float value for the fractal noise
	 */
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
	
	/**
	 * Generates a 2D array of fractal noise starting at the given point
	 * 
	 * @param dx	starting x location
	 * @param dy	starting y location
	 * @param size	width and height of array
	 * @param persistence	indicates how much weight each subsequent layer has on the final noise value
	 * @param octaves	number of layers to generate
	 * @param seed	seed to use for random generation
	 * @return	a 2D array of noise
	 */
	public static float[][] fracNoise2D(int dx, int dy, int size, float persistence, int octaves, long seed){
		HashMap<Point, Float> cache = new HashMap<Point, Float>();
		
		float[][] values = new float[size][size];
		for (int x = 0; x < size; x++)
			for (int y = 0; y < size; y++)
				values[x][y] = fractalNoise2(x + dx, y + dy, persistence, octaves, seed, cache);
		return values;
	}
	
	/**
	 * Loads a chunk of noise asynchronously
	 * @param dx	starting x location
	 * @param dy	starting y location
	 * @param size	width and height of array
	 * @param persistence	indicates how much weight each subsequent layer has on the final noise value
	 * @param octaves	number of layers to generate
	 * @param seed	seed to use for random generation
	 * @param listener	callback listener
	 */
	public static void loadChunk(int dx, int dy, int size, float persistence, int octaves, long seed, ChunkLoadedListener listener) {
		new Thread(new ChunkLoader(dx, dy, size, persistence, octaves, seed, listener)).start();
	}
}
