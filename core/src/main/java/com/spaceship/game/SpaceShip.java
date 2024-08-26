package com.spaceship.game;

import com.badlogic.gdx.ApplicationAdapter;
// import com.badlogic.gdx.Gdx;
// import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;
import java.util.Iterator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.Color;




/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class SpaceShip extends ApplicationAdapter {
    private SpriteBatch batch;
    private Texture image, tNave, tMissile, tEnemy;
    private Sprite nave, missile;
    private float posX, posY, velocity, xMissile, yMissile;
    private boolean attack, gameover;
    private Array<Rectangle> enemies;
    private long lastEnemyTime;
    private int score, power, numEnemies;
    
    private FreeTypeFontGenerator generator;
    private FreeTypeFontGenerator.FreeTypeFontParameter parameter;
    private BitmapFont bitmap;

    @Override
    public void create() { // Construtor
        batch = new SpriteBatch();
        image = new Texture("bg.png");
        tNave = new Texture("spaceship.png");
        nave = new Sprite(tNave);
        posX = 0;
        posY = 0;
        velocity = 10;
        
        tMissile = new Texture("missile.png");
        missile = new Sprite(tMissile);
        xMissile = posX;
        yMissile = posY;
        attack = false;
        
        tEnemy = new Texture("enemy.png");
        enemies = new Array<Rectangle>();
        lastEnemyTime = 0;
        
        score = 0;
        power = 3;
        numEnemies = 799999999;
        
        generator= new FreeTypeFontGenerator(Gdx.files.internal("font.ttf"));
        parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        
        parameter.size = 30;
        parameter.borderWidth = 1;
        parameter.borderColor = Color.BLACK;
        parameter.color = Color.WHITE;
        bitmap = generator.generateFont(parameter);
        
        gameover = false;
    }

    @Override
    public void render() { // Graficos ou Desenho/Imagens
        
        this.moveNave();
        this.moveMissile();
        this.moveEnemies();
        ScreenUtils.clear(1, 0, 0, 1);
        batch.begin();
        batch.draw(image, 0, 0);
        
        if(!gameover) {
            if (attack) {
            batch.draw(missile, xMissile + nave.getWidth() / 2, yMissile + nave.getHeight() / 2 - 12); 
            }
            batch.draw(nave, posX, posY);

            for(Rectangle enemy : enemies) {
                batch.draw(tEnemy, enemy.x, enemy.y);
            }
            bitmap.draw(batch, "Score: " + score, 20, Gdx.graphics.getHeight() - 20);
            bitmap.draw(batch, "Power: " + power, Gdx.graphics.getWidth() - 150, Gdx.graphics.getHeight() - 20);
        } else {
            bitmap.draw(batch, "Score: " + score, 20, Gdx.graphics.getHeight() - 20);
            bitmap.draw(batch, "GameOver", Gdx.graphics.getWidth() - 150, Gdx.graphics.getHeight() - 20);
        }
        
        if (Gdx.input.isKeyPressed(Input.Keys.ENTER) && gameover) {
            score = 0;
            power = 3;
            posX = 0;
            posY = 0;
            gameover = false;
            Rectangle enemy = new Rectangle(Gdx.graphics.getWidth(), MathUtils.random(0, Gdx.graphics.getHeight() - tEnemy.getHeight()), tEnemy.getWidth(), tEnemy.getHeight());
        }
        
        batch.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
        image.dispose();
        tNave.dispose();
    }
    
    private void moveNave() {
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) {
            if(posX < Gdx.graphics.getWidth() - nave.getWidth()) {
                posX += velocity;
            }
        }
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)) {
            if(posX > 0) {
                posX -= velocity;
            }
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP) || Gdx.input.isKeyPressed(Input.Keys.W)) {
            if(posY < Gdx.graphics.getHeight() - nave.getHeight()) {
                posY += velocity;
            }
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN) || Gdx.input.isKeyPressed(Input.Keys.S)) {
            if(posY > 0) {
                posY -= velocity;
            }
        }
    }
    
    private void moveMissile() {
        if (Gdx.input.isKeyPressed(Input.Keys.SPACE) && !attack) {
            attack = true;
        } 
        
        if (attack) {
            if (xMissile < Gdx.graphics.getWidth()) {
                xMissile += 20;  
            } else {
                xMissile = posX;
                attack = false;
            }
        } else {
            xMissile = posX;
            yMissile = posY; 
        }
        
    }
    
    private void spawnEnemies() {
        Rectangle enemy = new Rectangle(Gdx.graphics.getWidth(), MathUtils.random(0, Gdx.graphics.getHeight() - tEnemy.getHeight()), tEnemy.getWidth(), tEnemy.getHeight());
        enemies.add(enemy);
        lastEnemyTime = TimeUtils.nanoTime();
    }
    
    private void moveEnemies() {
        if (TimeUtils.nanoTime() - lastEnemyTime > numEnemies) {
            this.spawnEnemies();
        }
        
        for(Iterator<Rectangle> iter = enemies.iterator(); iter.hasNext();) {
            Rectangle enemy = iter.next();
            enemy.x -= 400 * Gdx.graphics.getDeltaTime();
            
            // Colisao com o Missel
            if(collide(enemy.x, enemy.y, enemy.width, enemy.height, xMissile, yMissile, missile.getWidth(), missile.getHeight()) && attack) {
                ++score;
                if (score % 10 == 0) {
                    numEnemies -= 400;
                    velocity += 2;
                }
                attack = false;
                iter.remove();
            // Colisao com o Player
            } else if (collide(enemy.x, enemy.y, enemy.width, enemy.height, posX, posY, nave.getWidth(), nave.getHeight()) && !gameover) {
                --power;
                if (power <= 0) {
                    gameover = true;
                }
                iter.remove();
            }
                
            if (enemy.x + tEnemy.getWidth() < 0) {
                iter.remove();
            }
        }
    }
    
    private boolean collide(float x1, float y1, float w1, float h1, float x2, float y2, float w2, float h2) {
        if(x1 + w1 > x2 && x1 < x2 + w2 && y1 + h1 > y2 && y1 < y2 + h2) {
            return true;
        }
        return false;
    }
}
