package com.shaheed.online29.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameEngine {
    private List<String> deck = new ArrayList<>();

    public GameEngine(){
        initDeck();
    }

    private void initDeck(){
        String[] suits = {"H","S","D","C"};
        String[] ranks = {"A","2","3","4","5","6","7","8","9","10","J","Q","K"};
        deck.clear();
        for(String s: suits) for(String r: ranks) deck.add(r+s);
    }

    public void shuffle(){ Collections.shuffle(deck); }

    public List<String> dealHand(int n){
        List<String> hand = new ArrayList<>();
        for(int i=0;i<n && !deck.isEmpty();i++) hand.add(deck.remove(0));
        return hand;
    }
}
