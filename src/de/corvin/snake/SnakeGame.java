package de.corvin.snake;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferStrategy;
import java.util.*;
import java.util.List;

import static javax.swing.JFrame.EXIT_ON_CLOSE;

/**
 * Created by Corvin on 13.05.2017.
 */
public class SnakeGame implements Runnable {

    private static SnakeGame instance;

    private Thread thread;

    private JFrame frame;
    private Canvas canvas;
    private BufferStrategy bufferStrategy;
    private Graphics g;
    private KeyManager keyManager;

    private boolean running, gameOver, flicker = true;

    private final int width = 160, height = 160;
    private int trail = 0, ticks = 0;
    private List<Vec2> history = new ArrayList<>();

    private Vec2 player, vel, velQueue, food;
    private int points = 0;

    public static void main(String[] args) {
        instance = new SnakeGame();
    }

    public SnakeGame() {
        frame = new JFrame("Snake!");
        frame.setSize(width, height);
        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setAutoRequestFocus(true);
        frame.setVisible(true);
        keyManager = new KeyManager();
        frame.addKeyListener(keyManager);

        canvas = new Canvas();
        canvas.setPreferredSize(new Dimension(width, height));
        canvas.setMinimumSize(new Dimension(width, height));
        canvas.setMaximumSize(new Dimension(width, height));
        canvas.setFocusable(false);

        frame.add(canvas);
        frame.pack();

        startGame();

        thread = new Thread(this);
        running = true;
        thread.start();
    }

    private void update() {
        if(player.x % 10 == 0 && player.y % 10 == 0) {
            vel.set(velQueue);

            history.add(new Vec2(player.x, player.y));
            if(history.size() > trail) history.remove(0);
        }
        player.add(vel);
        if(player.y < 0 || player.y > height-10 || player.x < 0 || player.x > width-10) gameOver();

        if(player.hits(food, 10)) {
            points++;
            System.out.println(points+"/"+trail);
            generateNewFood();
            trail++;
        }

        for(int i = 1; i < history.size(); i++) {
            if(player.hits(history.get(i), 10)) gameOver();
        }

        if(ticks % 30 == 0) {
            flicker = !flicker;
        }

    }

    private void gameOver() {
        gameOver = true;
    }

    private void startGame() {
        gameOver = false;
        player = new Vec2(width/2, height/2);
        vel = new Vec2(0, 0);
        velQueue = new Vec2(0, 0);
        history.clear();
        trail = 0;
        points = 0;
        generateNewFood();
    }

    private void generateNewFood() {
        Random r = new Random();
        food = new Vec2(r.nextInt(16)*10, r.nextInt(height/16)*10);
        for(Vec2 vec2 : history) if(food.equal(vec2)) generateNewFood();
    }

    private void draw() {
        bufferStrategy = canvas.getBufferStrategy();
        if(bufferStrategy == null) {
            canvas.createBufferStrategy(3);
            return;
        }
        g = bufferStrategy.getDrawGraphics();
        // Clear
        g.clearRect(0, 0, width, height);

        // Draw
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, width, height);

        if(!gameOver) {
            g.setColor(Color.yellow);
            g.fillRect(player.x, player.y, 10, 10);
            for (Vec2 v : history) g.fillRect(v.x, v.y, 10, 10);

            g.setColor(Color.green);
            g.fillRect(food.x, food.y, 10, 10);
        } else {
            g.setColor(Color.red);
            g.setFont(new Font("Arial", 0, 20));
            // Game Over = 9 = 90
            g.drawString("Game Over", width/2-50, height/2+5);
            g.setColor(Color.white);
            g.setFont(new Font("Arial", 0, 15));
            g.drawString("Points: "+points, width/2-25, height/2+20);
            if(flicker) {
                g.drawString("-Space-", width/2-25, height/2+40);
            }
        }

        // Finish
        bufferStrategy.show();
        g.dispose();
    }

    @Override
    public void run() {
        int fps = 60;
        double timePerTick = 1000000000 / fps;
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

    private class KeyManager implements KeyListener {

        @Override
        public void keyTyped(KeyEvent e) {

        }

        @Override
        public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_W:
                        if(!vel.equal(0, 1)) velQueue.set(0, -1);
                        break;
                    case KeyEvent.VK_A:
                        if(!vel.equal(1, 0)) velQueue.set(-1, 0);
                        break;
                    case KeyEvent.VK_S:
                        if(!vel.equal(0, -1)) velQueue.set(0, 1);
                        break;
                    case KeyEvent.VK_D:
                        if(!vel.equal(-1, 0)) velQueue.set(1, 0);
                        break;
                    case KeyEvent.VK_SPACE:
                        if(gameOver) startGame();
                        break;
                }
        }

        @Override
        public void keyReleased(KeyEvent e) {

        }
    }

}
