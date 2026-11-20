package com.kazumiii.gachacrate.history;

import java.util.UUID;

/**
 * One reward someone won. Stored as a single text line so the files stay small and readable.
 */
public final class WinRecord {

    private final long time;
    private final UUID playerId;
    private final String playerName;
    private final String crateId;
    private final UUID rewardId;
    private final String rarityId;
    private final String rewardName;

    public WinRecord(long time, UUID playerId, String playerName, String crateId, UUID rewardId,
                     String rarityId, String rewardName) {
        this.time = time;
        this.playerId = playerId;
        this.playerName = playerName;
        this.crateId = crateId;
        this.rewardId = rewardId;
        this.rarityId = rarityId;
        this.rewardName = rewardName;
    }

    public long getTime() { return time; }
    public UUID getPlayerId() { return playerId; }
    public String getPlayerName() { return playerName; }
    public String getCrateId() { return crateId; }
    public UUID getRewardId() { return rewardId; }
    public String getRarityId() { return rarityId; }
    public String getRewardName() { return rewardName; }

    /** "time;player-uuid;player-name;crate;reward-uuid;rarity;reward name". The name goes last because it may contain ';'. */
    public String serialize() {
        return time + ";" + playerId + ";" + clean(playerName) + ";" + clean(crateId) + ";" + rewardId + ";"
                + clean(rarityId) + ";" + (rewardName == null ? "" : rewardName);
    }

    private static String clean(String value) {
        return value == null ? "" : value.replace(";", "");
    }

    /** Returns null for broken lines. */
    public static WinRecord parse(String line) {
        if (line == null) return null;
        String[] parts = line.split(";", 7);
        if (parts.length < 7) return null;
        try {
            return new WinRecord(Long.parseLong(parts[0]), UUID.fromString(parts[1]), parts[2], parts[3],
                    UUID.fromString(parts[4]), parts[5], parts[6]);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /** "5m ago", "3h ago", "2d ago". */
    public String ago() {
        long seconds = Math.max(0, (System.currentTimeMillis() - time) / 1000);
        if (seconds < 60) return "just now";
        long minutes = seconds / 60;
        if (minutes < 60) return minutes + "m ago";
        long hours = minutes / 60;
        if (hours < 24) return hours + "h ago";
        return (hours / 24) + "d ago";
    }
}
