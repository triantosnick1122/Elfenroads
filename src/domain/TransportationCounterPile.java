package domain;

import commands.DrawCounterCommand;
import enums.CounterType;
import loginwindow.MainFrame;
import networking.GameState;
import panel.GameScreen;
import utils.GameRuleUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.*;

/**
 * The face-down pile of transportation counters
 */
public class TransportationCounterPile extends Deck <TransportationCounter> {

    private JLabel deckImage;

    public TransportationCounterPile(String sessionID) {

        super(sessionID);

        for (CounterType type : CounterType.values()) {
            for (int i = 0; i < 8; i++) {
                components.add(new TransportationCounter(type, MainFrame.instance.getWidth()*67/1440, MainFrame.instance.getHeight()*60/900));
            }
        }

        // initialize the deck image
        ImageIcon imageIcon = new ImageIcon("./assets/sprites/M08.png");
        Image image = imageIcon.getImage();
        Image imageResized = image.getScaledInstance(GameScreen.width*67/1440, GameScreen.height*60/900,  java.awt.Image.SCALE_SMOOTH);
        this.deckImage = new JLabel(new ImageIcon(imageResized));

        initializeMouseListener();

        shuffle();
    }

    public JLabel getImage() {
        return this.deckImage;
    }

    private void initializeMouseListener() {
        this.deckImage.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (GameRuleUtils.isDrawCountersPhase()) {
                    TransportationCounter drawn = GameState.instance().getCounterPile().draw(); // draw a counter
                    GameManager.getInstance().getThisPlayer().getHand().addUnit(drawn); // add to player's hand
                    GameScreen.getInstance().updateAll(); // update GUI

                    // tell the other peers to remove the counter from the pile
                    try {
                        GameManager.getInstance().getComs().sendGameCommandToAllPlayers(new DrawCounterCommand(1, Optional.empty()));
                    } catch (IOException err) {
                        System.out.println("Error: there was a problem sending the DrawCounterCommand to the other peers.");
                    }

                    GameManager.getInstance().endTurn();
            }}});
    }
}
