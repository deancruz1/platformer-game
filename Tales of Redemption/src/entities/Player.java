package entities;

import static utilz.Constants.PlayerConstants.*;
import static utilz.HelpMethods.*;
import static utilz.Constants.*;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import gamestates.Playing;
import main.Game;
import utilz.LoadSave;

public class Player extends Entity {

	private BufferedImage[][] animations;
	private boolean isMoving = false, isAttacking = false;
	private boolean left, right, jump;
	private int[][] lvlData;
	private float xDrawOffset = 21 * Game.SCALE;
	private float yDrawOffset = 4 * Game.SCALE;
	
	// jumping/gravity related
	private float jumpSpeed = -2.25f * Game.SCALE;
	private float fallSpeedAfterCollision = 0.5f * Game.SCALE;
	
	// status bar UI
	private BufferedImage statusBarImg;
	
	private int statusBarWidth = (int) (192 * Game.SCALE);
	private int statusBarHeight = (int) (58 * Game.SCALE);
	private int statusBarX = (int) (10 * Game.SCALE);
	private int statusBarY = (int) (10 * Game.SCALE);

	private int healthBarWidth = (int) (150 * Game.SCALE);
	private int healthBarHeight = (int) (4 * Game.SCALE);
	private int healthBarXStart = (int) (34 * Game.SCALE);
	private int healthBarYStart = (int) (14 * Game.SCALE);
	
	private int healthWidth = healthBarWidth;

	private int flipX = 0;
	private int flipW = 1;
	
	private boolean attackChecked;
	private Playing playing;
	
	private int tileY = 0;
	
	public Player(float x, float y, int width, int height, Playing playing) {
		super(x, y, width, height);
		this.playing = playing;
		this.state = IDLE;
		this.maxHealth = 100;
		this.currentHealth = 35;
		this.walkSpeed = Game.SCALE * 1.0f;
		loadAnimations();
		initHitbox(20, 27);
		initAttackBox();
	}
	
	public void setSpawn(Point spawn) {
		this.x = spawn.x;
		this.y = spawn.y;
		hitbox.x = x;
		hitbox.y = y;
	}
	
	private void initAttackBox() {
		attackBox = new Rectangle2D.Float(x, y, (int) (20 * Game.SCALE), (int) (20 * Game.SCALE));
		
	}

	public void update() {
		updateHealthBar();

		if (currentHealth <= 0) {
			if (state != DEAD) {
				state = DEAD;
				aniTick = 0;
				aniIndex = 0;
				playing.setPlayerDying(true);
				
			} else if (aniIndex == getSpriteAmount(DEAD) - 1 && aniTick >= ANI_SPEED - 1) {
				playing.setGameOver(true);
				
			} else
				updateAnimationTick();
			
			return;
		}
		
		updateAttackBox();
		
		updatePos();
		if (isMoving) {
			checkPotionTouched();
			checkSpikesTouched();
			tileY = (int) (hitbox.y / Game.TILES_SIZE);
		}
		
		if (isAttacking)
			checkAttack();
		updateAnimationTick();
		setAnimation();
	}
	
	private void checkSpikesTouched() {
		playing.checkSpikesTouched(this);
		
	}

	private void checkPotionTouched() {
		playing.checkPotionTouched(hitbox);
		
	}

	private void checkAttack() {
		if (attackChecked || aniIndex != 1)
			return;
		attackChecked = true;
		playing.checkEnemyHit(attackBox);
		playing.checkObjectHit(attackBox);
	}

	private void updateAttackBox() {
		if (right) {
			attackBox.x = hitbox.x + hitbox.width + (int) (10 * Game.SCALE);
		} else if (left) {
			attackBox.x = hitbox.x - hitbox.width - (int) (10 * Game.SCALE);
		}
		attackBox.y = hitbox.y + (int) (10 * Game.SCALE);
	}

	private void updateHealthBar() {
		healthWidth = (int) ((currentHealth / (float) (maxHealth)) * healthBarWidth);
		
	}

	public void render(Graphics g, int lvlOffset) {
		g.drawImage(animations[state][aniIndex],
				(int)(hitbox.x - xDrawOffset) - lvlOffset + flipX,
				(int)(hitbox.y - yDrawOffset),
				width * flipW, height, null);
//		drawHitbox(g, lvlOffset);
//		drawAttackBox(g, lvlOffset);
		drawUI(g);
	}
	
	private void drawUI(Graphics g) {
		g.drawImage(statusBarImg, statusBarX, statusBarY, statusBarWidth, statusBarHeight, null);
		g.setColor(Color.red);
		g.fillRect(healthBarXStart + statusBarX, healthBarYStart + statusBarY, healthWidth, healthBarHeight);
	}

	private void updateAnimationTick() {
		aniTick++;
		if (aniTick >= ANI_SPEED) {
			aniTick = 0;
			aniIndex++;
			
			if (aniIndex >= getSpriteAmount(state)) {
				aniIndex = 0;
				isAttacking = false;
				attackChecked = false;
			}
		}
		
	}

	private void setAnimation() {
		int startAni = state;
		
		if (isMoving) {
			state = RUNNING;
		} else {
			state = IDLE;
		}
		
		if (inAir) {
			if (airSpeed < 0) {
				state = JUMP;
			} else {
				state = FALLING;
			}
		}
		
		if(isAttacking) {
			state = ATTACK;
			if (startAni != ATTACK) {
				aniIndex = 1;
				aniTick = 0;
				return;
			}		
		}
		
		if (startAni != state) {
			resetAniTick();
		}
		
	}
	
	private void resetAniTick() {
		aniTick = 0;
		aniIndex = 0;
	}

	private void updatePos() {
		isMoving = false;
		
		if (jump) {
			jump();
		}
		
		if (!inAir)
			if((!left && !right) || (right && left))
				return;
		
		float xSpeed = 0;
		
		if (left) {
			xSpeed -= walkSpeed;
			flipX = width;
			flipW = -1;
		}
		
		if (right) {
			xSpeed += walkSpeed;
			flipX = 0;
			flipW = 1;
		}
		
		if (!inAir) {
			if (!isEntityOnFloor(hitbox, lvlData)) {
				inAir = true;
			}
		}
	
		if (inAir) {
			
			if (canMoveHere(hitbox.x, hitbox.y + airSpeed, hitbox.width, hitbox.height, lvlData)) {
				hitbox.y += airSpeed;
				airSpeed += GRAVITY;
				updateXPos(xSpeed);
				
			} else {
				hitbox.y = getEntityYPosUnderRoofOrAboveFloor(hitbox, airSpeed);
				if (airSpeed > 0) {
					resetInAir();
					
				} else {
					airSpeed = fallSpeedAfterCollision;
					
				}
				
				updateXPos(xSpeed);
				
			}
			
		} else {
			updateXPos(xSpeed);
		}
		
		isMoving = true;
	}
	
	private void jump() {
		if (inAir) {
			return;
		} else {
			inAir = true;
			airSpeed = jumpSpeed;
		}
	}

	private void resetInAir() {
		inAir = false;
		airSpeed = 0;
	}

	private void updateXPos(float xSpeed) {
		if (canMoveHere(hitbox.x + xSpeed, hitbox.y, hitbox.width, hitbox.height, lvlData)) {
		hitbox.x += xSpeed;
		} else {
			hitbox.x = getEntityXPosNextToWall(hitbox, xSpeed);
		}
	}
	
	public void changeHealth(int value) {
		currentHealth += value;
		
		if (currentHealth <= 0) {
			currentHealth = 0;
			// gameOver();
		} else if (currentHealth >= maxHealth)
			currentHealth = maxHealth;
	}

	public void kill() {
		currentHealth = 0;
		
	}
	
	
	private void loadAnimations() {

		BufferedImage img = LoadSave.GetSpriteAtlas(LoadSave.PLAYER_ATLAS);
		
		animations = new BufferedImage[7][8];
		
		for (int j = 0; j < animations.length; j++) 
			for (int i = 0; i < animations[j].length; i++) 
				animations[j][i] = img.getSubimage(i*64, j*40, 64, 40);
			
		statusBarImg = LoadSave.GetSpriteAtlas(LoadSave.STATUS_BAR);

	}
	
	public void loadLvlData(int[][] lvlData) {
		this.lvlData = lvlData;
		if (!isEntityOnFloor(hitbox, lvlData)) {
			inAir = true;
		}
	}
	
	public void resetDirBooleans() {
		left = false;
		right = false;
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

	public boolean isRight() {
		return right;
	}

	public void setRight(boolean right) {
		this.right = right;
	}

	public void setJump(boolean jump) {
		this.jump = jump;
	}

	public void resetAll() {
		resetDirBooleans();
		inAir = false;
		isAttacking = false;
		isMoving = false;
		state = IDLE;
		currentHealth = maxHealth;
		hitbox.x = x;
		hitbox.y = y;
		
		if (!isEntityOnFloor(hitbox, lvlData))
			inAir = true;
	}

	public void changePower(int value) {
		System.out.println("Added power");
		
	}

	public int getTileY() {
		return tileY;
	}
	
}
