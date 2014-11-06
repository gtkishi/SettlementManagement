package terrain;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import javax.swing.JFrame;
import javax.swing.JPanel;

import noise.FractalNoise;
import noise.FractalNoise.ChunkLoadedListener;

public class TerrainCache {
	private class OnLoadListener implements ChunkLoadedListener {
		private Point loc;
		
		public OnLoadListener(Point toLoad){
			this.loc = toLoad;
		}
		
		@Override
		public void chunkLoaded(float[][] chunk) {
			dataCache.put(loc, chunk);
			BufferedImage image = Colorizer.imageFromNoise(chunk, SCALE);
			imageCache.put(loc, image);
			loading.remove(loc);
		}
		
	}
	
	public static final int CHUNK_SIZE = 128;
	public static int SCALE = 1;
	
	private LinkedHashMap<Point, float[][]> dataCache;
	private LinkedHashMap<Point, BufferedImage> imageCache;
	private HashMap<Point, Boolean> loading;
	private int capacity;
	
	private int octaves;
	private float persistence;
	private long seed;
	
	public TerrainCache(int capac, float persistence, int octaves, long seed) {
		this.capacity = capac;
		this.octaves = octaves;
		this.persistence = persistence;
		this.seed = seed;
		
		dataCache = new LinkedHashMap<Point, float[][]>() {
			private int SIZE = 4 * capacity;
			
			@Override
			public boolean removeEldestEntry(Entry eldest){
				return this.size() > SIZE;
			}
		};
		
		imageCache = new LinkedHashMap<Point, BufferedImage>() {
			private int SIZE = capacity;
			
			@Override
			public boolean removeEldestEntry(Entry eldest){
				return this.size() > SIZE;
			}
		};
		
		loading = new HashMap<Point, Boolean>();
	}
	
	public BufferedImage getImage(Point loc) {
		return (loadImage(loc) ? imageCache.get(loc) : null);
	}
	
	public boolean loadImage(Point loc){
		if (!imageCache.containsKey(loc)) {
			if (!dataCache.containsKey(loc)) {
				if (!loading.containsKey(loc)) {
					loading.put(loc, true);
					FractalNoise.loadChunk(loc.x * CHUNK_SIZE, loc.y * CHUNK_SIZE, CHUNK_SIZE, persistence, octaves, seed, new OnLoadListener(loc));
				}
				return false;
			}
			new Thread(new Colorizer(loc, imageCache, dataCache.get(loc), SCALE)).start();
			return false;
		}
		return true;
	}
	
	public boolean setScale(int scale) {
		if (scale > 0 && scale < 10) {
			SCALE = scale;
			imageCache.clear();
			return true;
		}
		return false;
	}
}
