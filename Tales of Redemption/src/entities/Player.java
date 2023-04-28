package entities;

import static utilz.Constants.PlayerConstants.*;
import static utilz.Constants.Directions.*;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;

import utilz.LoadSave;

public class Player extends Entity {

	private BufferedImage[][] animations;
	private int aniTick, aniIndex, aniSpeed = 15;
	private int playerAction = IDLE;
	private boolean isMoving = false, isAttacking = false;
	private boolean left, up, right, down;
	private float playerSpeed = 2.0f;
	
	public Player(float x, float y, int width, int height) {
		super(x, y, width, height);
		loadAnimations();
	}
	
	public void update() {
		updatePos();
		updateAnimationTick();
		setAnimation();
	}
	
	public void render(Graphics g) {
		g.drawImage(animations[playerAction][aniIndex], (int)x, (int)y, width, height, null);

	}
	
	
	private void updateAnimationTick() {
		aniTick++;
		if (aniTick >= aniSpeed) {
			aniTick = 0;
			aniIndex++;
			
			if (aniIndex >= getSpriteAmount(playerAction)) {
				aniIndex = 0;
				isAttacking = false;
			}
		}
		
	}

	private void setAnimation() {
		int startAni = playerAction;
		
		if (isMoving) {
			playerAction = RUNNING;
		} else {
			playerAction = IDLE;
		}
		
		if(isAttacking) {
			playerAction = ATTACK_1;
		}
		
		if (startAni != playerAction) {
			resetAniTick();
		}
		
	}
	
	private void resetAniTick() {
		aniTick = 0;
		aniIndex = 0;
	}

	private void updatePos() {
		
		isMoving = false;
		
		if(left && !right) {
			x -= playerSpeed;
			isMoving = true;
			
		} else if (right && !left) {
			x += playerSpeed;
			isMoving = true;
		}
		
		if(up && !down) {
			y -= playerSpeed;
			isMoving = true;

		} else if (down && !up) {
			y += playerSpeed;
			isMoving = true;

		}
		
	}
	
	private void loadAnimations() {

		BufferedImage img = LoadSave.GetSpriteAtlas(LoadSave.PLAYER_ATLAS);
		
		animations = new BufferedImage[9][6];
		
		for (int j = 0; j < animations.length; j++) {
			for (int i = 0; i < animations[j].length; i++) {
				animations[j][i] = img.getSubimage(i*64, j*40, 64, 40);
			}
		}

	}
	
	public void resetDirBooleans() {
		left = false;
		up = false;
		right = false;
		down = false;
	}

	public void setAttacking(boolean isAttacking) {
		this.isAttacking = isAttacking;
	}
	
	public boolean isLeft() {
		return left;
	}

	public void setLeft(boolean left) {
		this.left = left;
	}

	public boolean isUp() {
		return up;
	}

	public void setUp(boolean up) {
		this.up = up;
	}

	public boolean isRight() {
		return right;
	}

	public void setRight(boolean right) {
		this.right = right;
	}

	public boolean isDown() {
		return down;
	}

	public void setDown(boolean down) {
		this.down = down;
	}
	
	
}
