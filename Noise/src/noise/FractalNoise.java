package noise;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class FractalNoise {
	private static Random rand = new Random();
	public static long seed = System.currentTimeMillis();
	
	public static float noise2(int x, int y){ // 2D noise function, returns a float [0.0, 1.0]
		rand.setSeed(seed);
		rand.setSeed(rand.nextLong() * x + rand.nextLong());
		rand.setSeed(rand.nextLong() * y + rand.nextLong());
		return rand.nextFloat();
	}
	
	public static float fracNoise2(int x, int y, float persistence, int octaves, long seedVal){ // fractalNoise, returns a float [0.0, 1.0] ... kinda
		int freq;
		float amp, totalAmp = 0.0f, value = 0.0f;
		
		float dx, dy, fx, fy;
		int ix, iy;
		
		for (int i = 0; i < octaves; i++){
			seed = seedVal * i;
			freq = 2 << i;
			amp = (float)Math.pow(persistence, i);
			totalAmp += amp;
			
			dx = x / ((float)freq);
			dy = y / ((float)freq);
			ix = (int)Math.floor(dx);
			iy = (int)Math.floor(dy);
			fx = dx - ix;
			fy = dy - iy;
			
			value += cosInterp(cosInterp(noise2(ix, iy), noise2(ix+1, iy), fx), cosInterp(noise2(ix, iy+1), noise2(ix+1, iy+1), fx), fy) * amp;
		}
		
		return value / totalAmp;
	}
	
	// get a 2d array of fractal noise centered at the origin point
	public static float[][] noiseArr2d(int originX, int originY, int width, int height, float persistence, int octaves, long seedVal){
		float[][] result = new float[width][height];
		for (int x = 0; x < width; x++)
			for (int y = 0; y < height; y++)
				result[x][y] = fracNoise2(x + originX, y + originY, persistence, octaves, seedVal);
		return result;
	}
	
	// interpolation based on cosine, smoother than vanilla Linear interpoalation
	public static float cosInterp(float a, float b, float t){
		return LERP(a,b,(float) ((1 - Math.cos(t * Math.PI))/2.0));
	}
	
	// linear interpolation
	public static float LERP(float a, float b, float t){
		return (float) (((1.0-t) * a) + (t * b));
	}
	
	// returns a point representing the range of values in the array (used in scaling)
	public static Point2D range(float[][] arr){
		float max = 0f, min = 0f; double val = 0;
		int num = 0;
		for (int x = 0; x < arr.length; x++)
			for (int y = 0; y < arr[0].length; y++){
				if (x == 0 && y == 0){
					min = arr[x][y]; max = min;
				}
				if (min > arr[x][y])
					min = arr[x][y];
				if (max < arr[x][y])
					max = arr[x][y];
				val += arr[x][y];
				num++;
			}
		return new Point2D.Double(min, max);
	}
	
	// scales array such that the minimum value goes to 0.0, and the max to 1.0
	public static void scale(float[][] result, float min, float max){
		int width = result.length;
		int height = result[0].length;
		float nmax = max-min;
		
		for (int x = 0; x < width; x++)
			for (int y = 0; y < height; y++){
				result[x][y] -= min;
				result[x][y] /= nmax;
			}
	}
	
	// this is where the magic happens
	public static void main(String[] args){
		int WIDTH = 1600, HEIGHT = 900, FACTOR = 1;
		long seed = System.currentTimeMillis();
		
		float[][] noise = noiseArr2d(-320, -240, WIDTH, HEIGHT, 1.5f, 8, System.currentTimeMillis());
		Point2D p = range(noise);
		scale(noise, (float)p.getX(), (float)p.getY());
		
		final BufferedImage img = new BufferedImage(FACTOR * WIDTH, FACTOR * HEIGHT, BufferedImage.TYPE_INT_RGB);
		Graphics g = img.getGraphics();
		for (int x = 0; x < WIDTH; x++){
			for (int y = 0; y < HEIGHT; y++){
				int val = (int)(255*noise[x][y]);
				
				/* UN-comment this line to see B&W clouds*/
				//g.setColor(new Color(val, val, val));
				
				/* and comment out this line */
				g.setColor((
						val < 148 ? new Color(0, 0, 128) :
							val < 152 ? Color.BLUE : 
								val < 156 ? new Color (255, 255, 64) :
									val < 174 ? new Color(32, 140, 32) : 
										val < 200 ? new Color(17, 70, 17) :
													val < 218 ? new Color(96, 96, 96):
															val < 232 ? new Color(128, 128, 128): new Color(224, 224, 224)));
				
				g.fillRect(FACTOR * x, FACTOR * y, FACTOR, FACTOR);
				
			};
		}
		
		// blech swing
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(800,600);
		JPanel panel = new JPanel(){
			public void paintComponent(Graphics g){
				super.paintComponent(g);
				g.drawImage(img, 0, 0, null);
			}
		};
		panel.setPreferredSize(new Dimension(img.getWidth(), img.getHeight()));
		frame.add(new JScrollPane(panel));
		frame.setVisible(true);
	}
}
