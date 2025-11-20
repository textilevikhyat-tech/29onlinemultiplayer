package com.shaheed.online29server;

import java.util.*;

public class BotAI {

    private final Random rnd = new Random();

    public int estimateBid(List<String> hand) {
        int score = 0;
        for (String c: hand) {
            String r = c.length()==2?c.substring(0,1):c.substring(0,c.length()-1);
            if (r.equals("J")) score += 3;
            if (r.equals("9")) score += 2;
            if (r.equals("A") || r.equals("10")) score += 1;
        }
        if (score < 5) return 0; // pass
        if (score < 8) return 16 + rnd.nextInt(3);
        if (score < 11) return 19 + rnd.nextInt(4);
        return 23 + rnd.nextInt(6);
    }

    public String chooseCard(List<String> hand, List<String> playable, List<String> trick, String trump) {
        if (playable == null || playable.isEmpty()) return null;
        // simple heuristics: follow suit; if can win, win; else lowest
        playable.sort(Comparator.comparingInt(this::cardStrength)); // low->high
        if (trick==null || trick.isEmpty()) {
            // lead with medium-high
            return playable.get(Math.max(0, playable.size()/2));
        } else {
            String lead = trick.get(0);
            String leadSuit = getSuit(lead);
            List<String> follow = new ArrayList<>();
            for (String c: playable) if (getSuit(c).equals(leadSuit)) follow.add(c);
            if (!follow.isEmpty()) {
                // try to beat top
                int top = -1;
                for (String c: trick) top = Math.max(top, cardStrength(c));
                for (String c: follow) if (cardStrength(c) > top) return c;
                return follow.get(0);
            }
            // no follow suit: play trump if any
            for (String c: playable) if (getSuit(c).equals(trump)) return c;
            return playable.get(0);
        }
    }

    private String getSuit(String card) { return card.substring(card.length()-1); }
    private int cardStrength(String c) {
        String r = c.length()==2?c.substring(0,1):c.substring(0,c.length()-1);
        switch (r) { case "A": return 8; case "K": return 7; case "Q": return 6; case "J": return 5; case "10": return 4; case "9": return 3; case "8": return 2; case "7": return 1; default: return 0; }
    }
}
