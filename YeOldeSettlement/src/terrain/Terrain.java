package terrain;

import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Random;
import java.util.function.Function;

import javax.imageio.ImageIO;

public enum Terrain implements Serializable{
	GRASS(1, "grass", '-'),
	DEEP(2, "deep", '%'),
	SHALLOW(3, "shallow", '~'),
	TREE(4, "forest", 't', 2),
	ROCK(5, "rock", '^', 2),
	SNOW(6, "snow", '*'),
	SHORE(7, "shore", ' ');
	
	public final int value;
	public final String str;
	public final char c;
	public final int count;
	
	Terrain(int value, String str, char c){
		this.value = value;
		this.str = str;
		this.c = c;
		count = 1;
	}
	
	Terrain(int value, String str, char c, int count){
		this.value = value;
		this.str = str;
		this.c = c;
		this.count = count;
	}
	
	public static final int ISO_SIZE = 48;
	public static final int HALF_ISO = ISO_SIZE / 2;
	public static HashMap<String, BufferedImage> terrainImages = makeImages();
	
	public String getFilename(){
		if (count == 1)
			return str + ".png";
		return str + "" + new Random().nextInt(count) + ".png";
	}
	
	public static Point2D orthoToIso(Point2D p){
		double x, y;
		
		x = (p.getX() + 2 * p.getY()) / ISO_SIZE;
		y = (2 * p.getY() - p.getX()) / ISO_SIZE;
		
		return new Point2D.Double(x, y);
	}
	
	public static Point2D isoToOrtho(Point2D p){
		double x, y;
	
		x = (p.getX() - p.getY()) * HALF_ISO;
		y = (p.getX() + p.getY()) * (HALF_ISO / 2);
		
		return new Point2D.Double(x, y);
	}
	
	private static HashMap<String, BufferedImage> makeImages(){
		HashMap<String, BufferedImage> imgMap = new HashMap<String, BufferedImage>();
		BufferedImage img;
/*		for (Terrain t: Terrain.values()){
			try{
				img = ImageIO.read(new File("images/" + t.str + ".png"));
			}catch(IOException e){System.err.printf("File \'%s.png\' could not be loaded\n", t.str); img = null;}
			imgMap.put(t.str, img);
		}*/
		File dir = new File("images/" + ISO_SIZE);
		for (File f: dir.listFiles()){
			try{
				img = ImageIO.read(f);
			}
			catch(IOException e){System.err.printf("File \'$s\' cannot be loaded\n", f.getName()); img = null;}
			imgMap.put(f.getName(), img);
		}
		return imgMap;
	}
	
	public static void main(String[] args){
		System.out.println(orthoToIso(new Point2D.Double(0, 512)));
	}
}
