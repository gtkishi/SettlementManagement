package noise;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextField;

import terrain.Terrain;

public class FractalNoise {
	public interface ChunkLoadedListener {
		public void chunkLoaded(float[][] chunk);
		public void chunkLoaded(float[][] data, float[][] data2);
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
		return fracNoise2DCache(dx, dy, size, persistence, octaves, seed, cache);
	}
	
	public static float[][] fracNoise2DCache(int dx, int dy, int size, float persistence, int octaves, long seed, HashMap<Point, Float> cache){
		float[][] values = new float[size][size];
		for (int x = 0; x < size; x++)
			for (int y = 0; y < size; y++)
				values[x][y] = fractalNoise2(x + dx, y + dy, persistence, octaves, seed, cache);
		return values;
	}
	
	/**
	 * Generates a 2D array of 2 element fractal noise starting at the given point
	 * 
	 * @param dx	starting x location
	 * @param dy	starting y location
	 * @param size	width and height of array
	 * @param persis1	indicates how much weight each subsequent layer has on the first noise value
	 * * @param persis1	indicates how much weight each subsequent layer has on the second noise value
	 * @param octaves	number of layers to generate
	 * @param seed	seed to use for random generation
	 * @return	a 2D array of noise
	 */
	public static Point2D[][] fracPointNoise2D(int dx, int dy, int size, float persis1, float persis2, int octaves, long seed){
		HashMap<Point, Float> cache = new HashMap<Point, Float>();
		HashMap<Point, Float> cache2 = new HashMap<Point, Float>();
		
		long seed2 = new Random(seed).nextLong();
		
		Point2D[][] values = new Point2D.Float[size][size];
		for (int x = 0; x < size; x++)
			for (int y = 0; y < size; y++) {
				float first = fractalNoise2(x + dx, y + dy, persis1, octaves, seed, cache);
				float second = fractalNoise2(x + dx, y + dy, persis2, octaves, seed2, cache2);
				values[x][y] = new Point2D.Float(first, second);
			}
		return values;
	}
	
	public static void main2(String[] args){
		int size = 512;
		int scale = 2;
		
		HashMap<Point, Float> pointCache = new HashMap<Point, Float>();
		BufferedImage image = new BufferedImage(size * scale, size * scale, BufferedImage.TYPE_INT_ARGB);
		Graphics g =  image.getGraphics();
		Color dark = Color.green.darker().darker().darker();
		float[][] data = fracNoise2D(0, 0, size, 1.2f, 6, System.currentTimeMillis());
		float[][] data2 = fracNoise2D(0, 0, size, 1.5f, 9, System.currentTimeMillis());
		for (int x = 0; x < size; x++)
			for (int y = 0; y < size; y++){
				int height = (int)(1024 * data2[x][y]);
				int value = (int)(256 * data[x][y]);
				Color c = (	height < 500 ? Color.blue.darker() : 
					height < 524 ? Color.blue :
						height < 532 ? Color.yellow :
							height < 536 ? Color.green : 
								height < 608 && value < 128 ? Color.green : 
									height < 608 && value >= 128 ? dark :
										height < 616 ? new Color(96,96,96) : 
											height < 628 ? new Color(160, 160, 160) : 
												height < 652 ? new Color(200,200,200):
													Color.white);
				g.setColor(c);
				g.fillRect(x*scale, y*scale, scale, scale);
			}
		
		JPanel panel = new JPanel(){
			public void paintComponent(Graphics g){
				super.paintComponent(g);
				g.drawImage(image, 0, 0, null);
			}
		};
		
		JFrame frame = new JFrame();
		frame.add(panel);
		frame.setSize(800, 600);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		frame.setVisible(true);
	}
	
	public static void main(String[] args){
		int[] topLeft = {0, 255, 0};
		int[] topRight = {0, 0, 255};
		int[] botLeft = {255, 0, 0};
		int[] botRight = {0, 0, 0};
		int size = 1024;
		int scale = 1;
		
		BufferedImage image = new BufferedImage(size * scale, size * scale, BufferedImage.TYPE_INT_ARGB);
		Graphics gr =  image.getGraphics();
		
		for (int x = 0; x < size; x++)
			for (int y = 0; y < size; y++){
				float dx = ((float)x) / size;
				float dy = ((float)y) / size;
				
				float r = LERP(LERP(topLeft[0], botLeft[0], dy), LERP(topRight[0], botRight[0], dy), dx);
				float g = LERP(LERP(topLeft[1], botLeft[1], dy), LERP(topRight[1], botRight[1], dy), dx);
				float b = LERP(LERP(topLeft[2], botLeft[2], dy), LERP(topRight[2], botRight[2], dy), dx);
				
				gr.setColor(new Color(r/256.0f, g/256.0f, b/256.0f));
				
				gr.fillRect(x*scale, y*scale, scale, scale);
			}
		
		JPanel panel = new JPanel(){
			public void paintComponent(Graphics g){
				super.paintComponent(g);
				g.drawImage(image, 0, 0, null);
			}
		};
		
		JFrame frame = new JFrame();
		frame.add(panel);
		frame.setSize(800, 600);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		frame.setVisible(true);
	}
}
