package com.f2pstarhunter.starhook.model;

public enum MessageType {
    SPOTTED,      // Tx Wxxx location
    DEPLETED,     // Wxxx dust
    DISAPPEARED,   // Wxxx poof
    SCOUTING_CLAIMED,
    SCOUTING_COMPLETED,
    WAVE_END,
}