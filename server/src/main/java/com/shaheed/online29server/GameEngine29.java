package com.shaheed.online29server;

import java.util.*;

/**
 * A simplified but functional 29 game engine.
 * - 32-card deck: 7,8,9,10,J,Q,K,A in 4 suits (H,D,S,C)
 * - 4 players, 8 cards each
 * - bidding simplified: players can pass or bid 16..28; highest bidder selects trump and leads
 * - trick play: must follow suit if possible; highest card of lead suit wins, trump overrides
 * - scoring: J=3, 9=2, A=1, 10=1
 */
public class GameEngine29 {

    private final Room room;
    private final List<Player> playersOrder = new ArrayList<>();
    private final Map<String, List<String>> hands = new HashMap<>();
    private final List<String> deck = new ArrayList<>();
    private int currentPlayerIndex = 0;
    private final List<String> currentTrick = new ArrayList<>();
    private final List<String> trickPlayers = new ArrayList<>();
    private String trump = null;
    private String bidderId = null;
    private int bidValue = 0;
    private final Map<String,Integer> points = new HashMap<>();

    public GameEngine29(Room room) {
        this.room = room;
    }

    public void startRound(List<Player> players) {
        playersOrder.clear();
        playersOrder.addAll(players);
        initDeck();
        Collections.shuffle(deck);
        hands.clear();
        for (Player p : playersOrder) {
            List<String> h = new ArrayList<>();
            for (int i=0;i<8;i++) h.add(deck.remove(0));
            hands.put(p.getId(), h);
            points.put(p.getId(), 0);
        }
        // initial simple bidding: auto-select highest estimator or assign first as bidder with 16
        // For now set default trump to suit of first card of bidder candidate later
        bidderId = playersOrder.get(0).getId();
        trump = getSuit(hands.get(bidderId).get(0));
        currentPlayerIndex = 0;
    }

    public void handleAction(Player p, Map<String,Object> action) {
        String a = (String) action.get("action");
        if ("play".equals(a)) {
            String card = (String) action.get("card");
            playCard(p, card);
        } else if ("bid".equals(a)) {
            int b = ((Number) action.getOrDefault("bid",0)).intValue();
            placeBid(p, b);
        }
    }

    private void placeBid(Player p, int b) {
        if (b > bidValue) {
            bidValue = b;
            bidderId = p.getId();
            room.broadcast(Map.of("type","bid","playerId",p.getId(),"bid",b));
        } else {
            // pass or invalid; ignore for now
        }
    }

    private void playCard(Player p, String card) {
        List<String> hand = hands.get(p.getId());
        if (hand == null || !hand.contains(card)) return;
        // check turn
        if (!playersOrder.get(currentPlayerIndex).getId().equals(p.getId())) return;
        // follow suit if possible
        if (!currentTrick.isEmpty()) {
            String leadSuit = getSuit(currentTrick.get(0));
            boolean hasLead = hand.stream().anyMatch(c->getSuit(c).equals(leadSuit));
            if (hasLead && !getSuit(card).equals(leadSuit)) return; // illegal
        }
        hand.remove(card);
        currentTrick.add(card);
        trickPlayers.add(p.getId());
        room.broadcast(Map.of("type","card_played","playerId",p.getId(),"card",card));
        currentPlayerIndex = (currentPlayerIndex + 1) % playersOrder.size();
        if (currentTrick.size() == playersOrder.size()) resolveTrick();
        else room.broadcastState();
    }

    private void resolveTrick() {
        // determine winner
        String leadSuit = getSuit(currentTrick.get(0));
        String winnerId = trickPlayers.get(0);
        String winningCard = currentTrick.get(0);
        for (int i=1;i<currentTrick.size();i++) {
            String c = currentTrick.get(i);
            String pid = trickPlayers.get(i);
            if (getSuit(c).equals(getSuit(winningCard))) {
                if (cardRank(c) > cardRank(winningCard)) {
                    winningCard = c; winnerId = pid;
                }
            } else {
                if (getSuit(c).equals(trump) && !getSuit(winningCard).equals(trump)) {
                    winningCard = c; winnerId = pid;
                }
            }
        }
        // count trick points
        int trickPoints = 0;
        for (String c : currentTrick) trickPoints += cardPoints(c);
        points.put(winnerId, points.getOrDefault(winnerId,0) + trickPoints);

        // notify
        room.broadcast(Map.of("type","trick_resolved","winnerId",winnerId,"card",winningCard,"points",trickPoints));

        // clear trick
        currentTrick.clear();
        trickPlayers.clear();

        // set current player to winner index
        for (int i=0;i<playersOrder.size();i++) {
            if (playersOrder.get(i).getId().equals(winnerId)) {
                currentPlayerIndex = i; break;
            }
        }

        // check round end
        boolean anyLeft = hands.values().stream().anyMatch(l->!l.isEmpty());
        if (!anyLeft) finalizeRound();
        else room.broadcastState();
    }

    private void finalizeRound() {
        // compute team points: (0+2) vs (1+3)
        int t0=0,t1=0;
        for (int i=0;i<playersOrder.size();i++) {
            String pid = playersOrder.get(i).getId();
            int pts = points.getOrDefault(pid,0);
            if (i%2==0) t0 += pts; else t1 += pts;
        }
        boolean bidderTeam = false;
        // determine bidder team (if bidder is at even index)
        for (int i=0;i<playersOrder.size();i++) {
            if (playersOrder.get(i).getId().equals(bidderId)) { bidderTeam = (i%2==0); break; }
        }
        int bidderPoints = bidderTeam ? t0 : t1;
        boolean bidderMade = bidderPoints >= bidValue;
        room.broadcast(Map.of("type","round_end","bidderId",bidderId,"bidValue",bidValue,"bidderPoints",bidderPoints,"bidderMade",bidderMade,"team0",t0,"team1",t1));
        // next round optional: for now keep same players and deal again after short delay
        startRound(playersOrder);
        room.broadcastState();
    }

    public Map<String,Object> getPublicState() {
        Map<String,Object> s = new HashMap<>();
        s.put("trump", trump);
        s.put("bidValue", bidValue);
        s.put("bidderId", bidderId);
        s.put("currentPlayerId", playersOrder.size()>0?playersOrder.get(currentPlayerIndex).getId():null);
        s.put("currentTrick", new ArrayList<>(currentTrick));
        return s;
    }

    public int getHandSize(String playerId) {
        List<String> h = hands.get(playerId);
        return h==null?0:h.size();
    }

    public List<String> getHandForPlayer(String playerId) {
        List<String> h = hands.get(playerId);
        return h==null?Collections.emptyList():new ArrayList<>(h);
    }

    // utilities
    private void initDeck() {
        deck.clear();
        String[] ranks = {"7","8","9","10","J","Q","K","A"};
        String[] suits = {"H","D","S","C"};
        for (String s : suits) for (String r : ranks) deck.add(r+s);
    }
    private String getSuit(String card) { return card.substring(card.length()-1); }
    private int cardRank(String c) {
        String r = c.length()==2?c.substring(0,1):c.substring(0,c.length()-1);
        switch (r) { case "A": return 8; case "K": return 7; case "Q": return 6; case "J": return 5; case "10": return 4; case "9": return 3; case "8": return 2; case "7": return 1; default: return 0; }
    }
    private int cardPoints(String c) {
        String r = c.length()==2?c.substring(0,1):c.substring(0,c.length()-1);
        switch (r) { case "J": return 3; case "9": return 2; case "A": return 1; case "10": return 1; default: return 0; }
    }
}
