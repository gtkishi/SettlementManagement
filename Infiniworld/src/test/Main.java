package test;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class Main {
	
	private static final int WIDTH = 32;
	private static final int SIZE = 32;
	private static final int SCALE = 1;
	
	public static void main(String[] args) {
		float[][][][] values = new float[WIDTH][WIDTH][][];
		
		int toGen = WIDTH * WIDTH * SIZE * SIZE;
		long time = System.currentTimeMillis();
		System.out.println("Generating Noise");
		for (int x = 0; x < WIDTH; x++)
			for(int y = 0; y < WIDTH; y++)
				values[x][y] = new FractalNoise(time).fracNoise2D(x * SIZE, y * SIZE, SIZE, 1.5f, 8);
		double seconds = (System.currentTimeMillis() - time) / 1000.0;
		System.out.println("... Done!");
		System.out.printf("%d tiles generated in %.3fs (%.2f tiles/s)\n", toGen, seconds, (toGen)/seconds);
		
		final BufferedImage image = new BufferedImage(WIDTH * SIZE * SCALE, WIDTH * SIZE * SCALE, BufferedImage.TYPE_INT_RGB);
		
		Color c;
		Graphics g = image.getGraphics();
		
		for (int x1 = 0; x1 < WIDTH; x1++)
		for (int y1 = 0; y1 < WIDTH; y1++)
		for (int x = 0; x < SIZE; x++)
			for (int y = 0; y < SIZE; y++){
				int height = (int) (1024 * values[x1][y1][x][y]);
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
				g.fillRect(x1*SIZE + x * SCALE, y1*SIZE + y * SCALE, SCALE, SCALE);
			}
		
		JPanel panel = new JPanel() {
			public void paintComponent(Graphics g){
				super.paintComponent(g);
				g.drawImage(image, 0, 0, null);
			}
		};
		
		panel.setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
		
		JFrame frame = new JFrame();
		frame.setSize(1024, 768);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new JScrollPane(panel));
		frame.setVisible(true);
	}
}
