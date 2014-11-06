package view;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.TreeSet;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

import terrain.TerrainCache;

public class InfiniScroller extends JFrame {
	private Point topLeft;
	private TerrainCache cache;
	private JPanel drawPanel;
	private boolean showGrid;
	//private int width, height;
	private TreeSet<Character> moveSet;
	
	private class InfiniPanel extends JPanel {
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			int scaledChunkSize = TerrainCache.CHUNK_SIZE * TerrainCache.SCALE;
			
			int gridWidth = InfiniScroller.this.getWidth() / scaledChunkSize;
			int gridHeight = InfiniScroller.this.getHeight() / scaledChunkSize;
			
			Point topLeftChunk = new Point(topLeft.x / scaledChunkSize, topLeft.y / scaledChunkSize);

			
			int dx = (topLeftChunk.x * scaledChunkSize) - topLeft.x;
			int dy = (topLeftChunk.y * scaledChunkSize) - topLeft.y;
			
			//System.out.printf("(%d, %d) (%d, %d) %d %d %d %d\n", topLeft.x, topLeft.y, topLeftChunk.x, topLeftChunk.y, gridWidth, gridHeight, dx, dy);
			
			BufferedImage image;
			Point cell;
			int xPos, yPos;
			for (int x = -2; x <= gridWidth + 1; x++)
				for (int y = -2; y <= gridHeight + 1; y++){
					cell = new Point(topLeftChunk.x + x, topLeftChunk.y + y);
					
					xPos = x * scaledChunkSize + dx;
					yPos = y * scaledChunkSize + dy;
					
					image = cache.getImage(cell);
					if (image != null) {
						g.drawImage(image, xPos, yPos, null);
						if (showGrid){
							g.setColor(Color.WHITE);
							g.drawRect(xPos, yPos, scaledChunkSize, scaledChunkSize);
						}
					}
					else {
						g.setColor(Color.BLACK);
						g.fillRect(xPos, yPos, scaledChunkSize, scaledChunkSize);
					}
				}
		}
	}
	
	private class WASDListener extends KeyAdapter {
		public void keyPressed(KeyEvent e){
			if (e.getKeyChar() == 'g')
				showGrid = !showGrid;
/*			else if (e.getKeyChar() == 'e') {
				if (cache.setScale(TerrainCache.SCALE * 2)) {
					topLeft.x += InfiniScroller.this.getWidth() / (TerrainCache.SCALE / 2);
					topLeft.y += InfiniScroller.this.getHeight() / (TerrainCache.SCALE / 2);
				}
			}
			else if (e.getKeyChar() == 'q') {
				if (cache.setScale(TerrainCache.SCALE / 2)) {
					topLeft.x -= InfiniScroller.this.getWidth() / (2 * TerrainCache.SCALE);
					topLeft.y -= InfiniScroller.this.getHeight() / (2 * TerrainCache.SCALE);
				}
			}*/
			else if (!moveSet.contains(e.getKeyChar()))
				moveSet.add(e.getKeyChar());
		}
		
		public void keyReleased(KeyEvent  e){
			moveSet.remove(e.getKeyChar());
		}
	}
	
	public InfiniScroller(int width, int height){
		super("Infini-Scroller");
		
		moveSet = new TreeSet<Character>();
		showGrid = false;
		
		this.setSize(width, height);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		topLeft = new Point(0, 0);
		cache = new TerrainCache(512, 1.5f, 9, System.currentTimeMillis());
		drawPanel = new InfiniPanel();
		this.add(drawPanel);
		
		Timer t = new Timer(33, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				for (char c : moveSet){
					switch (c) {
						case 'w': 	topLeft.y-=5;
									break;
						case 's':	topLeft.y+=5;
									break;
						case 'a':	topLeft.x-=5;
									break;
						case 'd':	topLeft.x+=5;
									break;
					}
				}
				//System.out.println(topLeft);
				drawPanel.repaint();
			}
		});
		
		this.addKeyListener(new WASDListener());
		
		this.setVisible(true);
		t.start();
	}
	
	public static void main(String[] args) {
		new InfiniScroller(1024, 768);
	}
}