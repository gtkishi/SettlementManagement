package controller;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

import objects.Explosion;
import objects.Frog;
import objects.SpriteObject;


public class Runner {
	private TreeSet<Character> keySet;
	private SpriteObject frog;
	private List<SpriteObject> splosions;
	private JFrame frame;
	private JPanel panel;
	private Timer animTimer;
	
	// TODO 7: run the runner and click to see explosions!
	// TODO 15: run this and move the frog around!
	
	public Runner() {
		keySet = new TreeSet<Character>();
		splosions = new LinkedList<SpriteObject>();
		
		// TODO 13: uncomment these two lines
		frog = new Frog(400, 300);
		frog.start();
		
		// creates the panel that actually draws the sprites
		panel = new JPanel(){
			public void paintComponent(Graphics g){
				super.paintComponent(g);
				
				// TODO 14: uncomment the next line
				frog.draw(g);
				for (SpriteObject explosion : splosions)
					explosion.draw(g);
			}
		};
		panel.setPreferredSize(new Dimension(800, 600));
		
		// creates the timer for animating the panel
		animTimer = new Timer(15, new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (frog != null){ // move the frog according to what keys are being pressed
					if (keySet.contains('w'))
						frog.moveUp();
					else if (keySet.contains('a'))
						frog.moveLeft();
					else if (keySet.contains('s'))
						frog.moveDown();
					else if (keySet.contains('d'))
						frog.moveRight();
					else
						frog.moveStop();
				}
				
				// clean up finished explosions
				try{
					splosions.stream().filter(e -> e.getSprite().isFinished()).forEach(s -> splosions.remove(s));
					/*LinkedList<SpriteObject> dead = new LinkedList<SpriteObject>();
					for (SpriteObject s : splosions)
						if (s.getSprite().isFinished()){
							dead.add(s);
							s.stop();
						}
					
					for (SpriteObject s: dead)
						splosions.remove(s);*/
					
				} catch(Exception e){}
				
				// repaint the panel
				panel.repaint();
			}
			
		});
		
		frame = new JFrame();
		frame.add(panel);
		
		frame.addKeyListener(new KeyAdapter(){
			@Override
			public void keyPressed(KeyEvent arg0) {
				keySet.add(arg0.getKeyChar());
				
				if (arg0.getKeyChar() == 'b' && frog != null){ // add an explosion at the frog's position
					Explosion explosion = new Explosion(frog.position.x, frog.position.y);
					splosions.add(explosion);
					explosion.start();
				}
			}
			
			@Override
			public void keyReleased(KeyEvent arg0) {keySet.remove(arg0.getKeyChar());}
		});
		
		frame.addMouseListener(new MouseAdapter(){
			@Override
			public void mousePressed(MouseEvent arg0) { // add an explosion where the user clicked
				Explosion e = new Explosion(0, 0);
				e.setPosition(arg0.getPoint().x, arg0.getPoint().y - e.getSprite().getHeight()/2);
				splosions.add(e);
				e.start();
			}
		});
		
		frame.setSize(800, 600);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);

		animTimer.start();
	}
	
	public static void main(String[] args){
		new Runner();
	}
}
