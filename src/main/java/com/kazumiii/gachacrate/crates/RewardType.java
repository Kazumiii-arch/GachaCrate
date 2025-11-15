package com.kazumiii.gachacrate.crate;

public enum RewardType {
    ITEM,       // Gives an ItemStack
    MONEY,      // Gives money (requires vault / eco give command)
    COMMAND,    // Executes commands
    BUNDLE,     // A bundle of multiple rewards
    NOTHING     // For filler rewards in animation or failed rolls
}
