import java.awt.*;
import java.util.Random;

public class PowerUp {
    public int x, y;
    public int width = 20, height = 20;
    public boolean active = true;
    public String type; // "extraLife", "paddleExpand"

    public PowerUp(int x, int y) {
        this.x = x;
        this.y = y;
        // Randomly assign type
        this.type = new Random().nextBoolean() ? "extraLife" : "paddleExpand";
    }

    public void draw(Graphics g) {
        if(!active) return;
        if(type.equals("extraLife")) g.setColor(Color.PINK);
        else if(type.equals("paddleExpand")) g.setColor(Color.CYAN);
        g.fillOval(x, y, width, height);
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    public void fall() {
        if(active) y += 2; // falling speed
    }
}
