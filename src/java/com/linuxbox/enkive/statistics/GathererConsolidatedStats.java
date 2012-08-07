package com.linuxbox.enkive.statistics;

import java.util.Date;
import java.util.Map;

public class GathererConsolidatedStats {
    /*
     * note: this is a class within a class; it means that each instance of ConsolidatedStats
     * is tied to an "enclosing" instance of GathererConsolidatedStats; if it ever makes
     * sense to use ConsolidatedStats independent of GathererConsolidatedStats, then
     * you can move the class so it's at the same level as GathererConsolidatedStats.
     * However I'm guessing this shouldn't be done, as then you lose the gatherer name
     * and timestamps.
     */
    class ConsolidatedStats {
            String name; // e.g., freeM
            Map<String, Double> summaries; // e.g., "min", "max", ec.
    }

    String gatherer;
    Date startTime; // or should it be Calendar if we have time zone info?
    Date endTime;
    Map<String, ConsolidatedStats> stats;
}