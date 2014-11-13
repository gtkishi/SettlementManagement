package terrain;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Timer;

import noise.FractalNoise.ChunkLoadedListener;

public class TerrainCache {
	private LinkedHashMap<Point, TerrainChunk> chunks;
	private HashMap<Point, Boolean> loading;
	
	private int octaves, capacity;
	private float persistence;
	private long seed;
	
	private class OnLoadListener implements ChunkLoadedListener {
		private Point loc;
		
		public OnLoadListener(Point toLoad){
			this.loc = toLoad;
		}
		
		@Override
		public void chunkLoaded(float[][] chunk) {
			put(loc, new TerrainChunk(loc, chunk));
		}
	}
	
	private class Archiver implements Runnable {
		private TerrainChunk chunk;
		
		public Archiver(TerrainChunk chunk){
			this.chunk = chunk;
		}
		
		public void run() {
			File f = new File(fileString(seed, chunk.getPos()));
			if (f.exists())
				return;
			
			try {
				ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(f));
				output.writeObject(chunk);
				output.close();
				System.out.printf("Archived chunk at (%d, %d) to \'%s\'\n", chunk.getPos().x, chunk.getPos().y, f.getName());
			} catch(Exception e) {
				e.printStackTrace();
				System.err.println("Error! Could not archive chunk at " + chunk.getPos().x + ", " + chunk.getPos().y);
			}
		}
	}
	
	public TerrainCache(int octaves, float persistence, long seed, int capacity)
	{
		this.octaves = octaves;
		this.persistence = persistence;
		this.seed = seed;
		this.capacity = capacity;
		
		chunks = new LinkedHashMap<Point, TerrainChunk>() {
			@Override
			public boolean removeEldestEntry(Entry<Point, TerrainChunk> eldest){
				if (this.size() > capacity) {
					new Thread(new Archiver(eldest.getValue())).start();
					return true;
				}
				return false;
			}
		};
		loading = new HashMap<Point, Boolean>();
		clearCache();
	}
	
	public void clearCache(){
		File dir = new File("cache");
		if (!dir.exists())
			dir.mkdir();
		for (File f : dir.listFiles())
			if (!f.getName().matches("^" + seed + ".*")) {
				System.out.println("Deleting " + f.getName());
				f.delete();
			}
	}
	
	public TerrainChunk get(Point p) {
		TerrainChunk c = chunks.get(p);
		if (c == null && !loading.containsKey(p)) {
			new Thread(new ChunkLoader(p, persistence, octaves, seed, new OnLoadListener(p), this)).start();
			loading.put(p, true);
		}
		return c;
	}
	
	public void put(Point loc, TerrainChunk c)
	{
		chunks.put(loc, c);
		loading.remove(loc);
	}
	
	public static String fileString(long seed, Point chunkPos){
		return "cache/" + seed + "-" + chunkPos.x + "-" + chunkPos.y + ".chunk";
	}
	
	public static void main(String[] args){
		TerrainCache cache = new TerrainCache(9, 1.5f, System.currentTimeMillis(), 256);
		Dimension d = TerrainChunk.getMapDimension();
		int width = d.width, w2 = width / 2;
		int height = d.height, h2 = height / 2;
		int mid = ((3 * width) / 2) - w2;
				
		JFrame frame = new JFrame();
		JPanel panel = new JPanel(){
			public void paintComponent(Graphics g){
				Point p;
				g.setColor(Color.black);
				g.fillRect(0, 0, this.getWidth(), this.getHeight());
				for (int x = 0; x < 3; x++){
					for (int y = 0; y < 3; y++)
					{
						p = new Point(x, y);
						int xpos = mid + (w2 * (x - y - 1));
						int ypos = (h2 - Terrain.ISO_SIZE/4) * (x + y);
						TerrainChunk chunk = cache.get(p);
						if (chunk!= null)
							g.drawImage(chunk.getBackground(), xpos, ypos, null);
					}
				}
			}
		};
		panel.setPreferredSize(new Dimension(3 * width, 3 * height));
		
		frame.add(new JScrollPane(panel));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(800, 600);
		frame.setVisible(true);
		
		new Timer(60, new ActionListener(){
			public void actionPerformed(ActionEvent e){
				panel.repaint();
			}
		}).start();
	}
}
