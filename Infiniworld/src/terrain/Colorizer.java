package terrain;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.LinkedHashMap;

public class Colorizer implements Runnable{
	public static BufferedImage imageFromNoise(float[][] chunk, int scale){
		BufferedImage image = new BufferedImage(chunk.length * scale, chunk[0].length * scale, BufferedImage.TYPE_INT_RGB);
		
		Color c;
		Graphics g = image.getGraphics();
		for (int x = 0; x < chunk.length; x++)
			for (int y = 0; y < chunk[0].length; y++){
				int height = (int) (1024 * chunk[x][y]);
				c = (height < 500 ? Color.blue.darker() : 
					height < 524 ? Color.blue :
						height < 532 ? Color.yellow :
							height < 536 ? Color.green : 
								height < 560 ? Color.green.darker() : 
									height < 584 ? Color.green.darker().darker() : 
										height < 608 ? Color.green.darker().darker().darker() : 
											height < 616 ? new Color(96,96,96) : 
												height < 628 ? new Color(160, 160, 160) : 
													height < 652 ? new Color(200,200,200):
														Color.white);
				g.setColor(c);
				g.fillRect(x * scale, y * scale, scale, scale);
			}
		
		return image;
	}
	
	private Point loc;
	private LinkedHashMap<Point, BufferedImage> cache;
	private float[][] data;
	private int scale;
	public Colorizer(Point loc, LinkedHashMap<Point, BufferedImage> cache, float[][] data, int scale){
		this.loc = loc;
		this.cache = cache;
		this.data = data;
		this.scale = scale;
	}
	
	@Override
	public void run() {
		cache.put(loc, imageFromNoise(data, scale));
	}
}
