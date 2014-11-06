package terrain;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.imageio.ImageIO;

public enum Terrain {
	GRASS(1, "grass", ' '),
	DEEP(2, "deep", '='),
	SHALLOW(3, "shallow", '~'),
	TREE(4, "tree", 't'),
	ROCK(5, "rock", 'r'),
	SNOW(6, "snow", 's'),
	SHORE(7, "shore", 'd');
	
	public final int value;
	public final String str;
	public final char c;
	
	Terrain(int value, String str, char c){
		this.value = value;
		this.str = str;
		this.c = c;
	}
	
	public static HashMap<String, BufferedImage> terrainImages = makeImages();
	
	private static HashMap<String, BufferedImage> makeImages(){
		HashMap<String, BufferedImage> imgMap = new HashMap<String, BufferedImage>();
		BufferedImage img;
		for (Terrain t: Terrain.values()){
			try{
				img = ImageIO.read(new File("images/" + t.str + ".png"));
			}catch(IOException e){System.err.printf("File \'%s.png\' could not be loaded\n", t.str); img = null;}
			imgMap.put(t.str, img);
		}
		return imgMap;
	}
}
