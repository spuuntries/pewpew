package pewpew;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

public class SpriteSheet {
	private BufferedImage spriteSheet;
	private int spriteWidth;
	private int spriteHeight;

	public SpriteSheet(String path, int spriteWidth, int spriteHeight) {
		try {
			spriteSheet = ImageIO.read(getClass().getResource(path));
			this.spriteWidth = spriteWidth;
			this.spriteHeight = spriteHeight;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public BufferedImage[] getSprites(int row, int startCol, int count) {
		if (spriteSheet == null)
			return new BufferedImage[0];

		BufferedImage[] sprites = new BufferedImage[count];
		for (int i = 0; i < count; i++) {
			sprites[i] = spriteSheet.getSubimage((startCol + i) * spriteWidth, row * spriteHeight, spriteWidth,
					spriteHeight);
		}
		return sprites;
	}
}
