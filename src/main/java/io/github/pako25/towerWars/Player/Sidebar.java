package io.github.pako25.towerWars.Player;

import io.github.pako25.towerWars.Arena.Track;
import io.github.pako25.towerWars.GameManagment.Game;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.HashMap;
import java.util.List;

public class Sidebar {
    private final Game game;
    private final TWPlayer twPlayer;
    private Scoreboard scoreboard;
    private int lastCoin = 75;
    private int lastIncome = 0;
    private int lastNextIncome = 5;
    private int lastTimer = 0;

    private HashMap<Location, TrackLivesAndScoreboardIndexHolder> lastTrackLivesAndIndexes = new HashMap<>();

    public Sidebar(Game game, TWPlayer twPlayer) {
        this.game = game;
        this.twPlayer = twPlayer;
        showSidebar();
    }

    public void showSidebar() {
        Player player = twPlayer.getPlayer();

        ScoreboardManager manager = Bukkit.getScoreboardManager();
        scoreboard = manager.getNewScoreboard();

        Objective objective = scoreboard.registerNewObjective(
                String.valueOf(player.getUniqueId()),
                Criteria.DUMMY,
                Component.text("TOWERWARS", NamedTextColor.YELLOW)
        );
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        objective.getScore(timerBuilder(lastTimer)).setScore(14);
        objective.getScore(goldBuilder(lastCoin)).setScore(5);
        objective.getScore(incomeBuilder(lastIncome)).setScore(4);
        objective.getScore(nextIncomeBuilder(lastNextIncome)).setScore(3);

        objective.getScore("").setScore(6);
        List<Track> trackList = game.getTrackList();
        for (int i = 0; i < trackList.size(); i++) {
            Track track = trackList.get(i);
            int index = 7 + i;
            objective.getScore(trackBuilder(track, track.getLives())).setScore(index);
            lastTrackLivesAndIndexes.put(track.getTrackSpawn(), new TrackLivesAndScoreboardIndexHolder(track.getLives(), index));
        }
        objective.getScore(" ").setScore(8 + trackList.size());

        player.setScoreboard(scoreboard);
    }

    public void updateSidebar(int coin, int income, int nextIncome, int timer, boolean tracksChanged) {
        if (scoreboard == null) return;

        Objective objective = scoreboard.getObjective(DisplaySlot.SIDEBAR);
        if (objective == null) return;

        if (tracksChanged) {
            List<Track> trackList = game.getTrackList();
            for (Track track : trackList) {
                TrackLivesAndScoreboardIndexHolder record = lastTrackLivesAndIndexes.get(track.getTrackSpawn());
                if (track.getLives() != record.lives) {
                    scoreboard.resetScores(trackBuilder(track, record.lives));
                    objective.getScore(trackBuilder(track, track.getLives())).setScore(record.index);
                    record.lives = track.getLives();
                }
            }
            return;
        }

        if (coin != lastCoin) {
            scoreboard.resetScores(goldBuilder(lastCoin));
            objective.getScore(goldBuilder(coin)).setScore(5);
            lastCoin = coin;
        }
        if (income != lastIncome) {
            scoreboard.resetScores(incomeBuilder(lastIncome));
            objective.getScore(incomeBuilder(income)).setScore(4);
            lastIncome = income;
        }
        if (nextIncome != lastNextIncome) {
            scoreboard.resetScores(nextIncomeBuilder(lastNextIncome));
            objective.getScore(nextIncomeBuilder(nextIncome)).setScore(3);
            lastNextIncome = nextIncome;
        }
        if (timer != lastTimer) {
            scoreboard.resetScores(timerBuilder(lastTimer));
            objective.getScore(timerBuilder(timer)).setScore(14);
            lastTimer = timer;
        }
    }

    public void delete() {
        scoreboard.getScoresFor(twPlayer.getPlayer());
        scoreboard.clearSlot(DisplaySlot.SIDEBAR);
        this.scoreboard = null;
    }

    private String goldBuilder(int gold) {
        Component component = Component.text("Gold: ", NamedTextColor.GOLD).append(Component.text(gold, NamedTextColor.YELLOW));
        return LegacyComponentSerializer.legacySection().serialize(component);
    }
    private String incomeBuilder(int income) {
        Component component = Component.text("Income: ", NamedTextColor.GOLD).append(Component.text(income, NamedTextColor.YELLOW));
        return LegacyComponentSerializer.legacySection().serialize(component);
    }
    private String nextIncomeBuilder(int nextIncome) {
        Component component = Component.text("Next income: ", NamedTextColor.GOLD).append(Component.text(nextIncome, NamedTextColor.YELLOW));
        return LegacyComponentSerializer.legacySection().serialize(component);
    }
    private String timerBuilder(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        Component component = Component.text("Timer: ", NamedTextColor.WHITE).append(Component.text(minutes + ":" + (seconds >= 10 ? seconds : "0" + seconds), NamedTextColor.GREEN));
        return LegacyComponentSerializer.legacySection().serialize(component);
    }
    private String trackBuilder(Track track, int lives) {
        String name = track.getColor().toString().toUpperCase();
        if (name.equals("GOLD")) name = "ORANGE";
        NamedTextColor color = track.getColor();
        Component component = Component.text(name, color).append(Component.text(": " + lives, NamedTextColor.WHITE));
        if (track.getUUID().equals(twPlayer.getTrack().getUUID())) {
            component = component.append(Component.text(" (YOU)", NamedTextColor.GRAY));
        }
        return LegacyComponentSerializer.legacySection().serialize(component);
    }
}

class TrackLivesAndScoreboardIndexHolder {
    public int lives;
    public int index;

    public TrackLivesAndScoreboardIndexHolder(int lives, int index) {
        this.lives = lives;
        this.index = index;
    }
}