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

public class DiamondSquares {
	private static Random rand = new Random();
	public static long seed = System.currentTimeMillis();
	
	public static float[][] DSGen(int length, long seedVal){
		seed = seedVal;
	
		int power = (int)Math.ceil(Math.log(length)/Math.log(2));
		int size = (int)(Math.pow(2, power)) + 1;
		
		//System.out.println(power + " : " + size);
		float[][] values = new float[size][size];
		
		// seed corner values
		values[0][0] = rand.nextFloat();
		values[0][size-1] = rand.nextFloat();
		values[size-1][0] = rand.nextFloat();
		values[size-1][size-1] = rand.nextFloat();
		
		// begin diamond/square iteration
		DSIter(values, size, 0.5f);
		
		// get result from noise array and return
		float[][] result = new float[length][length];
		for (int x = 0; x < length-1; x++)
			for (int y = 0; y < length; y++)
				result[x][y] = values[x][y];
		return result;
	}

	private static void DSIter(float[][] values, int size, float roughness) {
		int sideLen = size - 1;
		int halfLen;
		
		while(sideLen > 1){
			float avg;
			halfLen = sideLen / 2;
			
			// squares
			for (int x = 0; x < size - 1; x += sideLen)
				for (int y = 0; y < size - 1; y += sideLen){
					avg = values[x][y] + values[x + sideLen][y] + values[x][y + sideLen] + values[x + sideLen][y + sideLen];
					avg /= 4.0f;
					
					// midpoint = mean of corners + random displacement
					avg += 2 * roughness * rand.nextFloat() - roughness;
					values[x + halfLen][y + halfLen] = (avg > 1.0f ? 1.0f : avg < 0.0f ? 0.0f : avg);
				}
			
			// diamonds
			for (int x = 0; x < size - 1; x+= halfLen)
				for (int y = (x + halfLen) % sideLen; y < size; y += sideLen){
					float W, E, S, N;
					W = values[(x - halfLen+size)%size][y];
					E = values[(x + halfLen) % size][y];
					avg = W + E;
					int added = 2;
					if (y - halfLen >= 0){
						avg += values[x][y - halfLen];
						added++;
					}
					if (y + halfLen < size){
						avg += values[x][y + halfLen];
						added++;
					}
					avg /= added;
					
					// diamond point = mean of cardinal neighbors + random displacement
					avg += 2 * roughness * rand.nextFloat() - roughness;
					values[x][y] = (avg > 1.0f ? 1.0f : avg < 0.0f ? 0.0f : avg);
				}
			
			sideLen /= 2;
			roughness *= .6;
		}
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
	
	public static void main(String[] args){
		int WIDTH = 1024, HEIGHT = 1024, FACTOR = 1;
		float[][] noise = DSGen(WIDTH, System.currentTimeMillis());
		
		Point2D p = range(noise);
		scale(noise, (float)p.getX(), (float)p.getY());
		
		final BufferedImage img = new BufferedImage(FACTOR * WIDTH,  FACTOR * WIDTH, BufferedImage.TYPE_INT_RGB);
		Graphics g = img.getGraphics();
		for (int x = 0; x < WIDTH; x++){
			for (int y = 0; y < WIDTH; y++){
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
		
		// swing me right round...
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(1024, 1024);
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
