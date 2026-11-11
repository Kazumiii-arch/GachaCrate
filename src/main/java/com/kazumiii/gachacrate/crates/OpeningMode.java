package com.kazumiii.gachacrate.crates;

public enum OpeningMode {
    PHYSICAL_BLOCKS("Physical Block"),
    KEY_IN_HAND("Key in Hand");

    private final String displayName;

    OpeningMode(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
