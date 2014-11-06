package test;

import java.awt.Point;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class FractalNoise {
	private long seed;	
	private Random rand;
	private HashMap<Point, Float> cache;
	
	public FractalNoise() {
		this.seed = System.currentTimeMillis();
		rand = new Random(seed);
		cache = new HashMap<Point, Float>();
	}
	
	public FractalNoise(long seed) {
		this.seed = seed;
		rand = new Random(seed);
		cache = new HashMap<Point, Float>();
	}
	
	public void setSeed(long seed) {
		this.seed = seed;
		cache.clear();
	}
	
	public static float LERP(float a, float b, float x) {
		return a * (1.0f - x) + b * x;
	}
	
	private float noise2(int x, int y) {
		Point p = new Point(x, y);
		Float val = cache.get(p);
		
		if (val == null) {
			rand.setSeed(seed);
			rand.setSeed(x * rand.nextLong() + rand.nextLong());
			rand.setSeed(y * rand.nextLong() + rand.nextLong());
			val = rand.nextFloat();
			cache.put(p, val);
		}
		return val.floatValue();
	}
	
	private float smoothNoise2(int x, int y){
		float corners = (noise2(x-1, y-1) + noise2(x+1, y-1) + noise2(x+1, y+1) + noise2(x-1, y+1)) / 16.0f;
		float sides = (noise2(x, y-1) + noise2(x, y+1) + noise2(x-1, y) + noise2(x+1, y)) / 8.0f;
		float center = noise2(x, y) / 4.0f;
		return corners + sides + center;
	}
	
	private float interpNoise(float x, float y) {
		int intX = (x < 0 ? (int)(x-1) : (int)x);
		int intY = (y < 0 ? (int)(y-1) : (int)y);
		
		float dx = x - intX;
		float dy = y - intY;
		
		float top = LERP(smoothNoise2(intX, intY), smoothNoise2(intX+1, intY), dx);
		float bottom = LERP(smoothNoise2(intX, intY+1), smoothNoise2(intX+1, intY+1), dx);
		return LERP(top, bottom, dy);
	}
	
	public float fractalNoise2(int x, int y, float persistence, int octaves){
		float freq;
		float amp = 1.0f, totalAmp = 0.0f;
		float total = 0.0f;
		
		for (int i = 0; i < octaves - 1; i++){
			freq = 1 << i;
			totalAmp += amp;
			
			total += interpNoise(x / freq, y / freq) * amp;
			
			amp *= persistence;
		}
		
		return total / totalAmp;
	}
	
	public float[][] fracNoise2D(int dx, int dy, int size, float persistence, int octaves){
		float[][] values = new float[size][size];
		for (int x = 0; x < size; x++)
			for (int y = 0; y < size; y++)
				values[x][y] = fractalNoise2(x + dx, y + dy, persistence, octaves);
		return values;
	}
}
