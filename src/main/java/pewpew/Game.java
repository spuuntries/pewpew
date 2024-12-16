package pewpew;

import java.awt.Dimension;

import javax.swing.JFrame;

public class Game {
	public static void main(String[] args) {
		JFrame frame = new JFrame("WMNCB");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		GamePanel gamePanel = new GamePanel();
		frame.getContentPane().add(gamePanel);

		// Set minimum size
		frame.setMinimumSize(new Dimension(640, 360)); // Half of base resolution

		// Make frame resizable
		frame.setResizable(true);

		// Set preferred size
		frame.setPreferredSize(new Dimension(1280, 720));

		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
}
