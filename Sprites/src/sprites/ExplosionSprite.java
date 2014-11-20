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
	private boolean isDead = false;
	
	public ExplosionSprite(){
		//TODO 4: Implement
		if (sheet == null)
			try{
				sheet = ImageIO.read(new File("images/explosion-sprite.png"));
			}catch(Exception e){e.printStackTrace();};
	}
	
	@Override
	public Image getImage() {
		//TODO 5: implement getImage()
		int row = frame / 5, col = frame % 5;
		if (frame == MAX_FRAMES){
			isDead = true;
			return null;
		}
		else
			frame++;
		return sheet.getSubimage(col * width, row * height, width, height);
	}

	//TODO 6: implement remaining methods
	public int getHeight() { return height; }
	public int getWidth() { return width; }
	public boolean isFinished() { return isDead; }
	
	public void reset(){
		isDead = false;
		super.frame = 0;
	}
}
