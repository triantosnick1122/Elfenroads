package domain;

import javax.swing.*;
import java.awt.*;

public abstract class CounterUnit extends Drawable{

    private Road placedOn;

    CounterUnit(int resizeWidth, int resizeHeight, int imageNumber) {

        // find the picture of the card based on what type it is
        // since the images are named similarly and ordered the same way as they are in the enum declaration,
        // we can get the filepath just by using the type
        super("./assets/sprites/M0" + imageNumber + ".png");
        // String filepath = ("./assets/sprites/M0" + imageNumber + ".png");
        Image toResize = icon.getImage();
        Image resized = toResize.getScaledInstance(resizeWidth, resizeHeight,  java.awt.Image.SCALE_SMOOTH);
        display = new JLabel(new ImageIcon(resized));
    }

    public Road getPlacedOn() {
        return placedOn;
    }

    public void setPlacedOn(Road placedOn) {
        this.placedOn = placedOn;
    }
}
