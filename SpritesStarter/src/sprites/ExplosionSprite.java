package sprites;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

// TODO 3: Talk about the Explosion Sprite
public class ExplosionSprite extends Sprite{
	private static BufferedImage sheet; // sprite sheet, why is this static?
	private static int width = 96, height = 96; // frame width & height
	private static int MAX_FRAMES = 15; // magic number
	private boolean isDead;
	
	public ExplosionSprite(){
		//TODO 4: Implement
		super();
		
		if (sheet == null){
			try{
				sheet = ImageIO.read(new File("images/explosion-sprite.png"));
			}catch(Exception e){e.printStackTrace();}
		}
		isDead = false;
	}
	
	@Override
	public Image getImage() {
		//TODO 5: implement getImage()
		if (frame < MAX_FRAMES){
			int row = frame / 5;
			int col = frame % 5;
			
			Image frameImg = sheet.getSubimage(col * width, row * height, width, height);
			
			frame++;
			return frameImg;
		}
		else {
			isDead = true;
			return null;
		}
	}

	//TODO 6: implement remaining methods
	@Override
	public int getWidth() {
		// TODO Auto-generated method stub
		return width;
	}

	@Override
	public int getHeight() {
		// TODO Auto-generated method stub
		return height;
	}
}
