package commands;

import domain.*;
import enums.MagicSpellType;
import enums.RegionType;
import gamemanager.GameManager;
import gamescreen.GameScreen;
import networking.GameState;

import java.util.List;

public class ExchangeCommand implements GameCommand {

    private final String start1;
    private final String destination1;
    private final RegionType regionType1;
    private final String start2;
    private final String destination2;
    private final RegionType regionType2;
    private final String senderName;
    private final boolean isSecret;

    public ExchangeCommand(Road r1, Road r2, boolean isSecret) {
        GameMap map = GameMap.getInstance();
        start1 = map.getRoadSource(r1).getName();
        destination1 = map.getRoadTarget(r1).getName();
        regionType1 = r1.getRegionType();
        start2 = map.getRoadSource(r2).getName();
        destination2 = map.getRoadTarget(r2).getName();
        regionType2 = r2.getRegionType();
        senderName = GameManager.getInstance().getThisPlayer().getName();
        this.isSecret = isSecret;
    }

    @Override
    public void execute() {
        GameMap map = GameMap.getInstance();
        Town startTown1 = map.getTown(start1);
        Town destinationTown1 = map.getTown(destination1);
        Road r1 = map.getRoadBetween(startTown1, destinationTown1, regionType1);
        Town startTown2 = map.getTown(start2);
        Town destinationTown2 = map.getTown(destination2);
        Road r2 = map.getRoadBetween(startTown2, destinationTown2, regionType2);
        r1.exchangeWith(r2);

        // remove the exchange counter from the sending player's hand
        List<CounterUnit> senderHand = GameState.instance().getPlayerByName(senderName).getHand().getCounters();
        CounterUnit toRemove = null;
        for (CounterUnit c : senderHand) {
            if (c.getType() == MagicSpellType.EXCHANGE && c.isSecret() == isSecret) {
                toRemove = c;
            }
        }
        assert toRemove != null; // The counter should be in the sending player's hand
        senderHand.remove(toRemove);
        toRemove.setOwned(false);

        // add the exchange back to the counter pile
        GameState.instance().getCounterPile().addDrawable(toRemove);
        GameScreen.getInstance().updateAll();
    }
}
