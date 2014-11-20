package objects;
import sprites.FrogSprite;

// TODO 10: Look at Frog. How could this design be improved (think Factory)
public class Frog extends SpriteObject{
	public Frog(int x, int y){
		super(new FrogSprite(), x, y, 100);
	}
}
