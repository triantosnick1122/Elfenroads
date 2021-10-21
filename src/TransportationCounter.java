import javax.swing.*;
import java.awt.*;

public class TransportationCounter {

    public enum CounterType {GIANTPIG, ELFCYCLE, MAGICCLOUD, UNICORN, TROLLWAGON, DRAGON}

    // another rudimentary class to go along with Deck

    // will represent, for now, a Transportation Counter. It will contain information about how to display said counter.

    private CounterType type;
    private String imageFilepath;
    private ImageIcon image;

    public TransportationCounter (CounterType pType)
    {
         this.type = pType;

         // find the picture of the card based on what type it is
        // since the images are named similarly and ordered the same way as they are in the enum declaration, we can get the filepath just by using the type.

        int imageNumber = this.type.ordinal() + 1; // since the images start from M01, not M00
        this.imageFilepath = ("M0" + imageNumber);
        image = new ImageIcon (this.imageFilepath);
        Image toResize = image.getImage();
        Image resized = toResize.getScaledInstance(90, 130,  java.awt.Image.SCALE_SMOOTH);
        image = new ImageIcon(resized);
    }

    private ImageIcon getImage() {return this.image;}

    public CounterType getType() {return this.type;}

    public String getImageFilepath() {return this.imageFilepath;}







}
