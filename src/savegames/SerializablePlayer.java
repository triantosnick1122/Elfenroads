package savegames;

import com.sun.jdi.connect.Transport;
import domain.*;
import enums.Colour;
import enums.GameVariant;
import networking.GameState;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SerializablePlayer implements Serializable {

    private String name;

    private String currentTownName;
    private ArrayList<String> visitedTownNames;
    private String destinationTownName;

    private Colour color;

    private ArrayList<SerializableCardUnit> cards;
    private ArrayList<SerializableCounterUnit> counters; // in Elfengold, the obstacles will be in here
    private SerializableObstacle obstacle; // could be null (optional is not serializable)

    public SerializablePlayer (Player original)
    {
        name = original.getName();
        // will turn a Player object into a serializable version which can be saved to a file
        currentTownName = original.getCurrentTownName();
        visitedTownNames = getVisitedTownNames(original);
        // save destination town name if that is the variant we are playing
        if (GameState.instance().getGameVariant() == GameVariant.ELFENLAND_DESTINATION)
        {
            destinationTownName = original.getDestinationTown().getName();
        }
        color = original.getColour();

        // add the counters, cards, and obstacles
        addCounters(original);
        addCards(original);
        addObstacle(original);

    }

    private static ArrayList<String> getVisitedTownNames (Player original)
    {
        Set<Town> visited = original.getTownsVisited();
        ArrayList<String> visitedNames = new ArrayList<String>();

        for (Town cur : visited)
        {
            visitedNames.add(cur.getName());
        }

        return visitedNames;
    }

    private void addCounters(Player original)
    {
        List<CounterUnit> origCounters = original.getHand().getCounters();
        counters = new ArrayList<>();

        for (CounterUnit cur : origCounters)
        {
            if (cur instanceof TransportationCounter)
            {
                TransportationCounter curDowncasted = (TransportationCounter) cur;
                counters.add(new SerializableTransportationCounter(curDowncasted));
            }
            else // for Elfengold, cur could also be an obstacle
            {
                Obstacle curDowncasted = (Obstacle) cur;
                counters.add(new SerializableObstacle(curDowncasted));
            }
        }

    }

    private void addObstacle(Player original)
    {
        // for Elfenland, check if the player has an obstacle

        if (original.hasObstacle())
        {
            obstacle = new SerializableObstacle(original.getHand().getObstacle());
        }

    }

    private void addCards(Player original)
    {
        List <CardUnit> origCards = original.getHand().getCards();
        cards = new ArrayList<>();

        for (CardUnit cur : origCards)
        {
            if (cur instanceof TravelCard)
            {
                TravelCard curDowncasted = (TravelCard) cur;
                cards.add(new SerializableTravelCard(curDowncasted));
            }
            // do we do anything with gold cards?
        }
    }

    public String getCurrentTownName() {
        return currentTownName;
    }

    public ArrayList<String> getVisitedTownNames() {
        return visitedTownNames;
    }

    public String getDestinationTownName() {
        return destinationTownName;
    }

    public Colour getColor() {
        return color;
    }

    public ArrayList<SerializableCardUnit> getCards() {
        return cards;
    }

    public ArrayList<SerializableCounterUnit> getCounters() {
        return counters;
    }

    public SerializableObstacle getObstacle() {
        return obstacle;
    }

    public String getName() {
        return name;
    }
}
