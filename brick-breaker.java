import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.sound.sampled.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Random;

public class BrickBreaker extends JPanel implements KeyListener, ActionListener {
    private boolean play = false;
    private int score = 0;
    private int totalBricks;
    private int level = 1;
    private int lives = 3;

    private Timer timer;
    private int delay = 8;
    private int playerX = 310;
    private int playerWidth = 100;
    private int ballPosX = 120;
    private int ballPosY = 350;
    private int ballXDir = -1;
    private int ballYDir = -2;

    private MapGenerator map;
    private ArrayList<PowerUp> powerUps;

    public BrickBreaker() {
        map = new MapGenerator(level, 7); // Level 1, 7 columns
        totalBricks = map.getTotalBricks();
        powerUps = new ArrayList<>();
        addKeyListener(this);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
        timer = new Timer(delay, this);
        timer.start();
    }

    // Sound helper
    private void playSound(String filename) {
        try {
            File f = new File("assets/" + filename);
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(f);
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.start();
        } catch (Exception e) {
            System.out.println("Sound error: " + e.getMessage());
        }
    }

    public void paint(Graphics g) {
        // background
        g.setColor(Color.black);
        g.fillRect(1,1,692,592);

        // drawing map
        map.draw((Graphics2D)g);

        // borders
        g.setColor(Color.yellow);
        g.fillRect(0,0,3,592);
        g.fillRect(0,0,692,3);
        g.fillRect(691,0,3,592);

        // score and lives
        g.setColor(Color.white);
        g.setFont(new Font("serif",Font.BOLD,25));
        g.drawString("Score: "+score, 500,30);
        g.drawString("Lives: "+lives, 50,30);

        // paddle
        g.setColor(Color.green);
        g.fillRect(playerX,550,playerWidth,8);

        // ball
        g.setColor(Color.yellow);
        g.fillOval(ballPosX, ballPosY, 20, 20);

        // power-ups
        for(PowerUp p : powerUps) p.draw(g);

        // Game Over
        if(lives <=0) {
            play = false;
            ballXDir = 0;
            ballYDir = 0;
            g.setColor(Color.RED);
            g.setFont(new Font("serif",Font.BOLD,30));
            g.drawString("Game Over! Score: "+score, 150,300);
            g.setFont(new Font("serif",Font.BOLD,20));
            g.drawString("Press Enter to Restart", 200,350);
        }

        g.dispose();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        timer.start();
        if(play) {
            // Ball-paddle collision
            if(new Rectangle(ballPosX, ballPosY,20,20).intersects(new Rectangle(playerX,550,playerWidth,8))) {
                ballYDir = -ballYDir;
                playSound("hit.wav");
            }

            // Ball-brick collision
            A: for(int i=0;i<map.map.length;i++) {
                for(int j=0;j<map.map[0].length;j++) {
                    if(map.map[i][j]>0) {
                        int brickX = j*map.brickWidth + 80;
                        int brickY = i*map.brickHeight + 50;
                        int brickW = map.brickWidth;
                        int brickH = map.brickHeight;

                        Rectangle ballRect = new Rectangle(ballPosX, ballPosY,20,20);
                        Rectangle brickRect = new Rectangle(brickX,brickY,brickW,brickH);

                        if(ballRect.intersects(brickRect)) {
                            map.setBrickValue(0,i,j);
                            totalBricks--;
                            score +=5;
                            playSound("brick.wav");

                            // Chance to spawn power-up
                            if(new Random().nextInt(5)==0) {
                                powerUps.add(new PowerUp(brickX+brickW/2, brickY+brickH/2));
                            }

                            if(ballPosX +19 <= brickRect.x || ballPosX+1 >= brickRect.x+brickRect.width)
                                ballXDir = -ballXDir;
                            else
                                ballYDir = -ballYDir;
                            break A;
                        }
                    }
                }
            }

            // Ball movement
            ballPosX += ballXDir;
            ballPosY += ballYDir;

            if(ballPosX <0) ballXDir = -ballXDir;
            if(ballPosY <0) ballYDir = -ballYDir;
            if(ballPosX >670) ballXDir = -ballXDir;

            // Ball missed paddle
            if(ballPosY >570) {
                lives--;
                if(lives >0) {
                    ballPosX = 120;
                    ballPosY = 350;
                    ballXDir = -1;
                    ballYDir = -2;
                    playerX = 310;
                }
            }

            // Power-ups falling
            for(PowerUp p : powerUps) {
                p.fall();
                if(new Rectangle(playerX,550,playerWidth,8).intersects(p.getBounds()) && p.active) {
                    p.active = false;
                    if(p.type.equals("extraLife")) lives++;
                    if(p.type.equals("paddleExpand")) playerWidth += 30;
                    playSound("powerup.wav");
                }
            }

            // Next level
            if(totalBricks <=0) {
                level++;
                map = new MapGenerator(level+2,7); // Increase rows per level
                totalBricks = map.getTotalBricks();
                ballPosX = 120;
                ballPosY = 350;
                ballXDir = -1;
                ballYDir = -2;
                playerX = 310;
            }
        }

        repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if(e.getKeyCode() == KeyEvent.VK_RIGHT) {
            if(playerX >=600) playerX = 600;
            else playerX += 20;
        }

        if(e.getKeyCode() == KeyEvent.VK_LEFT) {
            if(playerX <=10) playerX = 10;
            else playerX -= 20;
        }

        if(e.getKeyCode() == KeyEvent.VK_ENTER) {
            if(!play) {
                play = true;
                ballPosX = 120;
                ballPosY = 350;
                ballXDir = -1;
                ballYDir = -2;
                playerX = 310;
                playerWidth = 100;
                score =0;
                lives=3;
                level=1;
                map = new MapGenerator(3,7);
                powerUps.clear();
                totalBricks = map.getTotalBricks();
                repaint();
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e){}
    @Override
    public void keyTyped(KeyEvent e){}

    public static void main(String[] args) {
        JFrame obj = new JFrame();
        BrickBreaker game = new BrickBreaker();
        obj.setBounds(10,10,700,600);
        obj.setTitle("Brick Breaker");
        obj.setResizable(false);
        obj.setVisible(true);
        obj.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        obj.add(game);
    }
}
