package com.kazumiii.gachacrate.crates;

/**
 * A running crate-opening animation (menu or in-world).
 * {@link #finish()} must hand out the reward exactly once, however it's called.
 */
public interface OpeningSession {

    void start();

    /** Ends the animation now and gives the reward. Safe to call more than once. */
    void finish();
}
