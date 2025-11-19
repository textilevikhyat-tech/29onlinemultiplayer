package com.shaheed.online29;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.*;

/**
 * Updated GameActivity:
 * - Play With Bots (local game)
 * - Quick Match (tries server endpoint defined in Constants.SERVER_URL -> if fails, fallback to bots)
 * - Simple UI updates to show player names and card placeholders
 *
 * NOTE:
 * - Replace card image creation logic with your project's drawable mapping.
 * - If you have a server & WebSocket, replace local engine with server-driven flow.
 */
public class GameActivity extends Activity {

    private TextView tvStatus;
    private LinearLayout bottomCards, topCards, leftCards, rightCards, playedCards;
    private TextView topName, leftName, rightName, bottomName;
    private Button btnVsBots, btnQuickMatch, btnPlay, btnPass;
    private ImageView tableBg;

    private final Handler uiHandler = new Handler();
    private final Random rnd = new Random();

    // Local game model (very simplified)
    private List<String> deck;
    private Map<String, List<String>> hands = new HashMap<>();
    private List<String> trick = new ArrayList<>();
    private List<PlayerModel> players = new ArrayList<>(); // 4 players
    private int currentTurnIndex = 0; // 0..3 -> which player to play
    private boolean inGame = false;
    private BotAI botAI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        bindViews();
        setupListeners();

        // show username if available in Constants
        TextView usernameTv = findViewById(R.id.gametextViewusername);
        if (usernameTv != null) {
            usernameTv.setText(Constants.username + " (" + Constants.actionUserId + ")");
        }

        botAI = new BotAI("local-bot");

        // if launched with SOLO_BOTS param -> start local bot game immediately
        Intent i = getIntent();
        String roomId = i.getStringExtra("roomId");
        if ("SOLO_BOTS".equals(roomId)) {
            startLocalBotGame();
        }
    }

    private void bindViews() {
        tvStatus = findViewById(R.id.tv_status);
        bottomCards = findViewById(R.id.bottom_cards);
        topCards = findViewById(R.id.top_cards);
        leftCards = findViewById(R.id.left_cards);
        rightCards = findViewById(R.id.right_cards);
        playedCards = findViewById(R.id.played_cards);

        topName = findViewById(R.id.top_name);
        leftName = findViewById(R.id.left_name);
        rightName = findViewById(R.id.right_name);
        bottomName = findViewById(R.id.bottom_name);

        btnVsBots = findViewById(R.id.btn_vs_bots);
        btnQuickMatch = findViewById(R.id.btn_quick_match);
        btnPlay = findViewById(R.id.btn_play);
        btnPass = findViewById(R.id.btn_pass);
        tableBg = findViewById(R.id.table_bg);
    }

    private void setupListeners() {
        btnVsBots.setOnClickListener(v -> startLocalBotGame());
        btnQuickMatch.setOnClickListener(v -> attemptQuickMatch());
        btnPlay.setOnClickListener(v -> onPlayClicked());
        btnPass.setOnClickListener(v -> onPassClicked());
    }

    // -----------------------------
    // Quick Match: try server -> fallback to local bots
    // -----------------------------
    private void attemptQuickMatch() {
        tvStatus.setText("Searching for match...");
        // If Constants.SERVER_URL is set and reachable you should call your server endpoint here.
        // For now: fallback to local matchmaking after 3 seconds (simulate search).
        uiHandler.postDelayed(() -> {
            // if server integration not present, auto-fill with bots and start
            tvStatus.setText("No remote match found — joining with bots.");
            startLocalBotGame();
        }, 3000);
    }

    // -----------------------------
    // Local bot game startup
    // -----------------------------
    private void startLocalBotGame() {
        if (inGame) return;
        inGame = true;
        tvStatus.setText("Starting local game vs bots...");

        // create players (bottom is human)
        players.clear();
        players.add(new PlayerModel(Constants.username != null ? Constants.username : "You", false));
        players.add(new PlayerModel("Bot_Left", true));
        players.add(new PlayerModel("Bot_Top", true));
        players.add(new PlayerModel("Bot_Right", true));

        // update names
        bottomName.setText(players.get(0).name);
        leftName.setText(players.get(1).name);
        topName.setText(players.get(2).name);
        rightName.setText(players.get(3).name);

        // create and shuffle deck
        deck = createDeck();
        Collections.shuffle(deck);

        // deal 8 cards each (29 game deals differ; adapt as needed)
        hands.clear();
        for (int i = 0; i < 4; i++) {
            List<String> h = new ArrayList<>();
            for (int c = 0; c < 8; c++) {
                if (!deck.isEmpty()) h.add(deck.remove(0));
            }
            hands.put(players.get(i).id, h);
        }

        // clear UI
        renderHandsUI();

        // start from player 0
        currentTurnIndex = 0;
        uiHandler.postDelayed(this::runTurn, 800);
    }

    private List<String> createDeck() {
        // minimal 29-like deck: cards 7..A of four suits
        String[] ranks = {"7","8","9","10","J","Q","K","A"};
        String[] suits = {"H","D","S","C"};
        List<String> d = new ArrayList<>();
        for (String s : suits) for (String r : ranks) d.add(r + s);
        return d;
    }

    // -----------------------------
    // Game loop (very simplified)
    // -----------------------------
    private void runTurn() {
        if (!inGame) return;
        PlayerModel current = players.get(currentTurnIndex);
        tvStatus.setText("Turn: " + current.name);
        if (current.isBot) {
            // bot chooses and plays
            List<String> hand = hands.get(current.id);
            List<String> playable = new ArrayList<>(hand); // naive: all playable
            String chosen = new BotAI(current.id).chooseCard(hand, playable, trick, null);
            if (chosen == null && !hand.isEmpty()) chosen = hand.get(0);
            performPlay(current, chosen);
            // next turn
            currentTurnIndex = (currentTurnIndex + 1) % players.size();
            uiHandler.postDelayed(this::runTurn, 800 + rnd.nextInt(900));
        } else {
            // human: wait for player input (click on card)
            tvStatus.setText("Your turn - tap a card to play");
            enablePlayerInteraction(true);
        }
    }

    private void onPlayClicked() {
        // If user pressed Play button (alternative to card tap), play first card
        if (!inGame) return;
        PlayerModel human = players.get(0);
        List<String> hand = hands.get(human.id);
        if (hand == null || hand.isEmpty()) {
            Toast.makeText(this, "No cards to play", Toast.LENGTH_SHORT).show();
            return;
        }
        String card = hand.get(0);
        performPlay(human, card);
        enablePlayerInteraction(false);
        currentTurnIndex = (currentTurnIndex + 1) % players.size();
        uiHandler.postDelayed(this::runTurn, 600);
    }

    private void onPassClicked() {
        Toast.makeText(this, "You passed", Toast.LENGTH_SHORT).show();
        // simple pass -> go next
        currentTurnIndex = (currentTurnIndex + 1) % players.size();
        uiHandler.postDelayed(this::runTurn, 300);
    }

    private void performPlay(PlayerModel player, String card) {
        if (player == null || card == null) return;
        List<String> hand = hands.get(player.id);
        if (hand != null) hand.remove(card);
        trick.add(card);
        addPlayedCardToUI(player.name, card);
        renderHandsUI();
        // if trick complete (4 plays) -> clear trick and continue
        if (trick.size() >= players.size()) {
            // naive winner selection: highest card strength
            String winnerId = determineTrickWinner();
            tvStatus.setText("Trick won by " + winnerId);
            trick.clear();
            playedCards.removeAllViews();
            // set currentTurnIndex to winner
            int winnerIndex = 0;
            for (int i=0;i<players.size();i++) if (players.get(i).name.equals(winnerId)) { winnerIndex = i; break;}
            currentTurnIndex = winnerIndex;
            uiHandler.postDelayed(this::runTurn, 900);
            return;
        }
    }

    private String determineTrickWinner() {
        // very naive: highest rank wins (no suit/trump logic)
        String topCard = null;
        int topStrength = -1;
        int offset = 0;
        for (String c : trick) {
            int s = cardStrength(c);
            if (s > topStrength) {
                topStrength = s;
                topCard = c;
                // winner is player at (currentTurnIndex - (trick.size()-1) + offset)
            }
            offset++;
        }
        // find owner by checking who had played which (we didn't track owner per trick in this simple impl)
        // for demo, declare player with highest remaining cards length as winner (hack)
        int bestIdx = 0;
        int max = -1;
        for (int i=0;i<players.size();i++) {
            int sz = hands.get(players.get(i).id).size();
            if (sz > max) { max = sz; bestIdx = i; }
        }
        return players.get(bestIdx).name;
    }

    private int cardStrength(String c) {
        if (c == null) return 0;
        String r = c.length() == 2 ? c.substring(0,1) : c.substring(0, c.length()-1);
        switch (r) {
            case "A": return 14;
            case "K": return 13;
            case "Q": return 12;
            case "J": return 11;
            case "10": return 10;
            case "9": return 9;
            case "8": return 8;
            case "7": return 7;
            default: return 5;
        }
    }

    // -----------------------------
    // UI helpers
    // -----------------------------
    private void renderHandsUI() {
        bottomCards.removeAllViews();
        topCards.removeAllViews();
        leftCards.removeAllViews();
        rightCards.removeAllViews();

        // bottom (human)
        List<String> humanHand = hands.get(players.get(0).id);
        if (humanHand != null) {
            for (String card : humanHand) {
                View cardView = createCardView(card);
                // card click -> play
                cardView.setOnClickListener(v -> {
                    if (currentTurnIndex == 0 && inGame) {
                        performPlay(players.get(0), card);
                        enablePlayerInteraction(false);
                        currentTurnIndex = (currentTurnIndex + 1) % players.size();
                        uiHandler.postDelayed(this::runTurn, 600);
                    } else {
                        Toast.makeText(GameActivity.this, "Not your turn", Toast.LENGTH_SHORT).show();
                    }
                });
                bottomCards.addView(cardView);
            }
        }

        // bots: show back-of-card or small placeholder count
        addBotPlaceholders(leftCards, hands.get(players.get(1).id));
        addBotPlaceholders(topCards, hands.get(players.get(2).id));
        addBotPlaceholders(rightCards, hands.get(players.get(3).id));
    }

    private void addBotPlaceholders(LinearLayout container, List<String> hand) {
        container.removeAllViews();
        if (hand == null) return;
        int count = Math.max(0, hand.size());
        for (int i=0;i<count;i++) {
            ImageView iv = new ImageView(this);
            iv.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(36), dpToPx(54)));
            iv.setImageResource(android.R.drawable.sym_def_app_icon); // placeholder - replace with back-of-card drawable
            container.addView(iv);
        }
    }

    private View createCardView(String card) {
        ImageView iv = new ImageView(this);
        iv.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(56), dpToPx(80)));
        // replace with actual mapping: card -> drawable resource
        // example: R.drawable.card_ah for Ace of hearts
        iv.setImageResource(android.R.drawable.alert_dark_frame); // placeholder; replace with real card image
        iv.setPadding(4,4,4,4);
        return iv;
    }

    private void addPlayedCardToUI(String playerName, String card) {
        TextView tv = new TextView(this);
        tv.setText(playerName + ": " + card);
        tv.setTextColor(getResources().getColor(android.R.color.white));
        playedCards.addView(tv);
    }

    private void enablePlayerInteraction(boolean enabled) {
        btnPlay.setEnabled(enabled);
        btnPass.setEnabled(enabled);
        // allow clicking cards only when enabled (createCardView uses click)
    }

    private int dpToPx(int dp) {
        float scale = getResources().getDisplayMetrics().density;
        return Math.round(dp * scale);
    }

    // -----------------------------
    // Simple internal model classes
    // -----------------------------
    private static class PlayerModel {
        String id;
        String name;
        boolean isBot;
        PlayerModel(String name, boolean bot) {
            this.id = UUID.randomUUID().toString();
            this.name = name;
            this.isBot = bot;
        }
    }
}
