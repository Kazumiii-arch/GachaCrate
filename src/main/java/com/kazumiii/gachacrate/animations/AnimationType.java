// ========================================================================
// File: AnimationType.java
// Location: /src/main/java/com/kazumiii/gachacrate/animations/AnimationType.java
// ========================================================================
package com.kazumiii.gachacrate.animations;

public enum AnimationType {
    REEL("Reel"),
    ROULETTE("Roulette");

    private final String displayName;

    AnimationType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
