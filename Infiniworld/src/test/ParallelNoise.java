package test;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class ParallelNoise{
	private static final int SCALE = 1;
	private static final int WIDTH = 8;
	private static final int SIZE = 128;

	private JFrame frame;
	private JPanel canvas;
	private final BufferedImage image;
	private Integer runningThreads = 0;
	private int total;
	private long time;
	
	private class NoiseAsync implements Runnable{
		private int x, y;
		private int dx, dy, size, octaves;
		private long seed;
		private float persistence;
		
		public NoiseAsync(int x, int y, int size, float persistence, int octaves, long seed){
			this.x = x;
			this.y = y;
			this.dx = x * size;
			this.dy = y * size;
			this.size = size;
			this.persistence = persistence;
			this.octaves = octaves;
			this.seed = seed;
			
			synchronized(runningThreads){
				total++;
				runningThreads++;
			}
		}
		
		@Override
		public void run() {
			float[][] chunk = new FractalNoise(seed).fracNoise2D(dx, dy, size, persistence, octaves);

			//data[x][y] = chunk;
			Color c;
			synchronized (image){
				Graphics g = image.getGraphics();
				for (int x = 0; x < SIZE; x++)
					for (int y = 0; y < SIZE; y++){
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
						g.fillRect((dx + x) * SCALE, (dy + y) * SCALE, SCALE, SCALE);
					}
				frame.repaint();
			}
			
			synchronized(runningThreads){
				runningThreads--;
				if (runningThreads == 0 && total == WIDTH * WIDTH){
					int toGen = WIDTH * WIDTH * SIZE * SIZE;
					double seconds = (System.currentTimeMillis() - time) / 1000.0;
					System.out.printf("%d tiles generated in %.3fs (%.2f tiles/s)\n", toGen, seconds, (toGen)/seconds);
					frame.setVisible(true);
				}
			}
		}
	}
	
	@SuppressWarnings("serial")
	public ParallelNoise() {
		//data = new float[WIDTH][WIDTH][][];
		
		image = new BufferedImage(WIDTH*SIZE*SCALE, WIDTH*SIZE*SCALE, BufferedImage.TYPE_INT_RGB);
		
		canvas = new JPanel() {
			public void paintComponent(Graphics g){
				super.paintComponent(g);
				g.drawImage(image, 0, 0, null);
			}
		};
		canvas.setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
		
		frame = new JFrame("Parallel Noise");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(1024, 1024);
		frame.add(new JScrollPane(canvas));
		frame.setVisible(true);
		
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		time = System.currentTimeMillis();
		for (int x = 0; x < WIDTH; x++)
			for (int y = 0; y < WIDTH; y++)
				new Thread(new NoiseAsync(x, y, SIZE, 1.5f, 8, time)).start();
	}
	
	public static void main(String[] args) {
		new ParallelNoise();
	}
}
