package de.corvin.snake;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferStrategy;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SnakeGame implements Runnable {

    private static SnakeGame instance;

    private Thread thread;

    private JFrame frame;
    private Canvas canvas;
    private BufferStrategy bufferStrategy;
    private Graphics g;
    private KeyManager keyManager;

    private boolean running, gameOver, flicker = true, showGrid = false;

    private final int WIDTH = 320, HEIGHT = 320;
    private final int PIXEL = 20, GRID_SIZE = 16;
    private final int FPS = 60;
    private int trail = 0, ticks = 0;
    private List<Vec2> history = new ArrayList<>();

    private Vec2 player, vel, velQueue, food;
    private int points = 0;

    public static void main(String[] args) {
        instance = new SnakeGame();
    }

    public SnakeGame() {
        frame = new JFrame("Snake!");
        frame.setSize(WIDTH, HEIGHT);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setAutoRequestFocus(true);
        frame.setVisible(true);
        frame.setResizable(false);
        keyManager = new KeyManager();
        frame.addKeyListener(keyManager);
        frame.setIconImage(new ImageIcon(getClass().getClassLoader().getResource("snek.png")).getImage());

        canvas = new Canvas();
        canvas.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        canvas.setMinimumSize(new Dimension(WIDTH, HEIGHT));
        canvas.setMaximumSize(new Dimension(WIDTH, HEIGHT));
        canvas.setFocusable(false);
        canvas.setVisible(true);

        frame.add(canvas);
        frame.pack();

        startGame();

        thread = new Thread(this);
        running = true;
        thread.start();
    }

    /**
     * Called every frame to update values
     */
    private void update() {
        if(player.x % PIXEL == 0 && player.y % PIXEL == 0) {
            vel.set(velQueue.mul(2));

            history.add(new Vec2(player.x, player.y));
            if(history.size() > trail) history.remove(0);
        }
        player.add(vel);
        if(player.y < 0 || player.y > HEIGHT-PIXEL || player.x < 0 || player.x > WIDTH-PIXEL) gameOver();

        if(player.hits(food)) {
            points++;
            System.out.println(points+"/"+trail);
            generateNewFood();
            trail++;
        }

        for(int i = 1; i < history.size(); i++) {
            if(player.hits(history.get(i))) gameOver();
        }

        if(ticks % (FPS/2) == 0) {
            flicker = !flicker;
        }

    }

    /**
     * Called every frame to draw object on the screen
     */
    private void draw() {
        bufferStrategy = canvas.getBufferStrategy();
        if(bufferStrategy == null) {
            canvas.createBufferStrategy(3);
            return;
        }
        g = bufferStrategy.getDrawGraphics();
        // Clear
        g.clearRect(0, 0, WIDTH, HEIGHT);

        // Draw
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, WIDTH, HEIGHT);
        if(showGrid) {
            g.setColor(new Color(183, 181, 170));
            for(int i = 0; i < GRID_SIZE; i++) {
                g.drawLine(i*PIXEL, 0, i*PIXEL,HEIGHT);
                g.drawLine(0, i*PIXEL, WIDTH, i*PIXEL);
            }
        }
        if(!gameOver) {

            g.setColor(new Color(168, 30, 30));
            g.fillRect(food.x, food.y, PIXEL, PIXEL);

            g.setColor(new Color(244, 217, 66));
            g.fillRect(player.x, player.y, PIXEL, PIXEL);
            for (Vec2 v : history) {
                g.fillRect(v.x, v.y, PIXEL, PIXEL);
            }
        } else {
            g.setColor(Color.red);
            g.setFont(new Font("Arial", 0, 20));
            int offset = g.getFontMetrics().stringWidth("Game Over")/2;
            g.drawString("Game Over", WIDTH/2-offset, HEIGHT/2+5);
            g.setColor(Color.white);
            g.setFont(new Font("Arial", 0, 15));
            offset = g.getFontMetrics().stringWidth("Points: "+points)/2;
            g.drawString("Points: "+points, WIDTH/2-offset, HEIGHT/2+20);
            if(flicker) {
                offset = g.getFontMetrics().stringWidth("- Press Space -")/2;
                g.drawString("- Press Space -", WIDTH/2-offset, HEIGHT/2+40);
            }

            int[][] sprite =   {{0, 1, 1, 1, 0},
                                {1, 1, 1, 1, 1},
                                {1, 0, 1, 0, 1},
                                {1, 1, 1, 1, 1},
                                {0, 1, 0, 1, 0},};
            drawSprite(g, sprite, WIDTH/2-50, 30);
        }

        // Finish
        bufferStrategy.show();
        g.dispose();
    }

    @Override
    public void run() {
        double timePerTick = 1000000000 / FPS;
        double delta = 0;
        long now;
        long lastTime = System.nanoTime();
        long timer = 0;

        while(running) {
            now = System.nanoTime();
            delta += (now - lastTime) / timePerTick;
            timer += now - lastTime;
            lastTime = now;

            if(delta >= 1) {
                update();
                draw();

                ticks++;
                delta--;
            }

            if(timer >= 1000000000) {
                //System.out.println("Ticks and Frames per second: "+ticks);
                ticks = 0;
                timer = 0;
            }
        }
    }

    /**
     * Initializes default values to start/restart the game
     */
    private void startGame() {
        gameOver = false;
        player = new Vec2(WIDTH/2, HEIGHT/2);
        vel = new Vec2(0, -1);
        velQueue = new Vec2(0, -1);
        history.clear();
        trail = 0;
        points = 0;
        generateNewFood();
    }

    /**
     * Enables game-over mode
     */
    private void gameOver() {
        gameOver = true;
    }

    /**
     * Randomly spawns new food on the grid
     */
    private void generateNewFood() {
        Random r = new Random();
        food = new Vec2(r.nextInt(GRID_SIZE)*PIXEL, r.nextInt(GRID_SIZE)*PIXEL);
        for(Vec2 vec2 : history) if(food.equal(vec2)) generateNewFood();
    }

    /**
     * Draws int-array sprites
     * @param g Graphics object to draw with
     * @param sprite sprite as int array (1=white)
     * @param x x position
     * @param y y position
     */
    private void drawSprite(Graphics g, int[][] sprite, int x, int y) {
        g.setColor(Color.white);
        for(int i = 0; i < sprite.length; i++) {
            for(int j = 0; j < sprite.length; j++) {
                if(sprite[i][j] == 1) g.fillRect(x+j*PIXEL, y+i*PIXEL, PIXEL, PIXEL);
            }
        }
    }

    private class KeyManager implements KeyListener {

        @Override
        public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_W: case KeyEvent.VK_UP:
                        if(!vel.norm().equal(0, 1)) velQueue.set(0, -1);
                        break;
                    case KeyEvent.VK_A: case KeyEvent.VK_LEFT:
                        if(!vel.norm().equal(1, 0)) velQueue.set(-1, 0);
                        break;
                    case KeyEvent.VK_S: case KeyEvent.VK_DOWN:
                        if(!vel.norm().equal(0, -1)) velQueue.set(0, 1);
                        break;
                    case KeyEvent.VK_D: case KeyEvent.VK_RIGHT:
                        if(!vel.norm().equal(-1, 0)) velQueue.set(1, 0);
                        break;
                    case KeyEvent.VK_SPACE: case KeyEvent.VK_ENTER:
                        if(gameOver) startGame();
                        break;
                    case KeyEvent.VK_G:
                        showGrid = !showGrid;
                        break;
                    case KeyEvent.VK_ESCAPE:
                        System.exit(0);
                        break;
                }
        }

        @Override
        public void keyReleased(KeyEvent e) { }
        @Override
        public void keyTyped(KeyEvent e) { }
    }

}
