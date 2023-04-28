package main;

import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;

import javax.swing.JFrame;

public class GameWindow {
	
	private JFrame jframe;
	
	public GameWindow(GamePanel gamePanel) {
		jframe = new JFrame();
		jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jframe.add(gamePanel);
		jframe.setResizable(false);
		jframe.pack();
		jframe.setVisible(true);
		jframe.setLocationRelativeTo(null); //sets screen to start in the middle
		jframe.addWindowFocusListener(new WindowFocusListener() {

			public void windowGainedFocus(WindowEvent e) {

			}

			public void windowLostFocus(WindowEvent e) {
				gamePanel.getGame().windowFocusLost();
			}
			
		});

	}
}

