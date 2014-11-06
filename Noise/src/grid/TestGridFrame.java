package grid;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.Timer;

public class TestGridFrame extends JFrame {
	private static final long serialVersionUID = 5348261894139701331L;
	
	private GridChunk HEYYOUGUYS;
	private JPanel imagePanel, minimap;
	private JScrollPane scroll;
	private boolean mapChanged = true;
	
	public TestGridFrame(){
		super();
		this.setLayout(new BorderLayout());
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(800,600);
		
		HEYYOUGUYS = new GridChunk();
		imagePanel = new JPanel(){
			private static final long serialVersionUID = 6607918962680992630L;

			public void paintComponent(Graphics g){
				super.paintComponent(g);
				//System.out.println("img draw");
				HEYYOUGUYS.isoDraw(g);
			}
		};
		imagePanel.setPreferredSize(HEYYOUGUYS.getMapDimension());
		imagePanel.addMouseListener(new newMap());
		
		scroll = new JScrollPane(imagePanel);
		
/*		minimap = new MinimapPanel();
		minimap.setSize(new Dimension(300, 300));
		minimap.setOpaque(false);*/
		this.add(scroll, BorderLayout.CENTER);
		//this.setGlassPane(minimap);
		//this.getGlassPane().setVisible(true);
		
		this.addKeyListener(new MoveViewportListener());
		
		this.setVisible(true);
		scroll.getViewport().setViewPosition(new Point((imagePanel.getPreferredSize().width - scroll.getViewport().getWidth())/2,
				(imagePanel.getPreferredSize().height - scroll.getViewport().getHeight())/2));
	}
	
	private class newMap implements MouseListener{

		@Override
		public void mouseClicked(MouseEvent e) {
			// TODO Auto-generated method stub
			if (e.getButton() == MouseEvent.BUTTON3){
				HEYYOUGUYS = new GridChunk();
				mapChanged = true;
				repaint();
			}	
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseExited(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mousePressed(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}	
	}
	
	private class MinimapPanel extends JPanel {
		/**
		 * 
		 */
		private BufferedImage img;
		private static final long serialVersionUID = 713281910218889460L;
		private final int factor1 = GridChunk.MINIMAP_FACTOR * Terrain.TILE_SIZE, factor2 = GridChunk.CHUNK_SIZE/GridChunk.MINIMAP_FACTOR + 4;

		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			//System.out.println("mm draw");
			Graphics2D g2d = (Graphics2D)g;
			Composite original = g2d.getComposite();
			Composite translucent = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);
			g2d.setComposite(translucent);

			if (img == null || mapChanged){
				img = HEYYOUGUYS.getMiniMap();
				if (img != null)
					mapChanged = false;
			}
			if (img != null){
				g2d.setColor(Color.LIGHT_GRAY);
				g2d.fillRect(15, 15, factor2, factor2);
				g2d.drawImage(img, 17, 17, null);
			}
			g2d.setComposite(original);
			g2d.setColor(Color.white);
			Rectangle r = scroll.getViewport().getViewRect();
			r.x/=factor1; r.y/=factor1; 
			r.width/=factor1; r.height/=factor1;
			g2d.drawRect(17 + r.x, 17 + r.y, r.width, r.height);	
		}
	}
	
	private class MoveViewportListener implements KeyListener, ActionListener{
		private Timer t;
		private Set<Character> pressed;
		private int delta = 2 * Terrain.TILE_SIZE;
		
		public MoveViewportListener(){
			super();
			t = new Timer(10, this);
			pressed = new TreeSet<Character>();
		}
		
		@Override
		public void keyPressed(KeyEvent arg0) {
			pressed.add(arg0.getKeyChar());
			if(!t.isRunning())
				t.start();
		}

		@Override
		public void keyReleased(KeyEvent arg0) {
			pressed.remove(Character.valueOf(arg0.getKeyChar()));
			if (pressed.size() == 0)
				t.stop();
		}

		@Override
		public void keyTyped(KeyEvent arg0) {
			// TODO Auto-generated method stub
			if (arg0.getKeyChar() == 'm')
				minimap.setVisible(!minimap.isVisible());
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			// TODO Auto-generated method stub
			//System.out.println("move");
			JViewport viewport = scroll.getViewport();
			Point p = viewport.getViewPosition();
			
			for (char direction : pressed)
				switch(direction){
					case 'w': p.y -= delta; break;
					case 's': p.y += delta; break;
					case 'a': p.x -= delta; break;
					case 'd': p.x += delta; break;
					default: break;
				}
			
			p.x = (p.x < 0 ? 0 : p.x); p.y = (p.y < 0 ? 0 : p.y);
			
			if (p.x + viewport.getWidth() > imagePanel.getWidth())
				p.x -= (p.x + viewport.getWidth() - imagePanel.getWidth());
			
			if (p.y + viewport.getHeight() > imagePanel.getHeight())
				p.y -= (p.y + viewport.getHeight() - imagePanel.getHeight());
			
			viewport.setViewPosition(p);
		}
	}
	
	public static void main(String[] args){
		new TestGridFrame();
	}
}
