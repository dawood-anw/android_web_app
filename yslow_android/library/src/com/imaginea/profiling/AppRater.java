/*******************************************************************************
 * Copyright 2014 Asha
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *       http://www.apache.org/licenses/LICENSE-2.0
 * Owner : Asha - initial API and implementation
 * Project Name : Lib_PerformanceMonitoring
 * FileName :AppRater
 ******************************************************************************/
package com.imaginea.profiling;

import java.util.HashMap;
import java.util.Map;

/**
 * The Class AppRater.
 */
public class AppRater {

    /** The Constant GRADE_A. */
    private final int GRADE_A = 90;

    /** The Constant GRADE_B. */
    private final int GRADE_B = 80;

    /** The Constant GRADE_C. */
    private final int GRADE_C = 70;

    /** The Constant GRADE_A. */
    private final int SCORE_A = 100;

    /** The Constant GRADE_B. */
    private final int SCORE_B = 75;

    /** The Constant GRADE_C. */
    private final int SCORE_C = 50;

    /** The Constant GRADE_D. */
    private final int SCORE_D = 25;

    /** The Constant VERY_BAD_LAUNCHTIME. */
    private final int VERY_BAD_LAUNCHTIME = 600;

    /** The Constant BAD_LAUNCHTIME. */
    private static final int BAD_LAUNCHTIME = 400;

    /** The Constant FAIR_LAUNCHTIME. */
    private final int FAIR_LAUNCHTIME = 250;

    /** The Constant OVERDRAW_COUNT_POOR. */
    private final String OVERDRAW_COUNT_POOR = "4.";

    /** The Constant OVERDRAW_COUNT_FAIR. */
    private final String OVERDRAW_COUNT_FAIR = "3.";

    /** The Constant OVERDRAW_COUNT_GOOD. */
    private final String OVERDRAW_COUNT_GOOD = "2.";

    /** The Constant OVERDRAW_COUNT_EXCELLENT. */
    private final String OVERDRAW_COUNT_EXCELLENT = "1.";

    /** The Constant GC_CALLS_VERYBAD. */
    private final int GC_CALLS_VERYBAD = 30;

    /** The Constant GC_CALLS_BAD. */
    private final int GC_CALLS_BAD = 20;

    /** The Constant GC_CALLS_FAIR. */
    private final int GC_CALLS_FAIR = 10;

    /** The Constant NUMBER_OF_RULES. */
    private final int NUMBER_OF_RULES = 4;

    /** The grade launch time. */
    private int mGradeLaunchTime = 0;

    /** The grade activity stack. */
    private int mGradeActivityStack = 0;

    /** The grade over draw. */
    private int mGradeOverDraw = 0;

    /** The grade gc. */
    private int mGradeGC = 0;

    /** The m activity grade map. */
    public Map<String, String> activitiesGrade = new HashMap<String, String>();

    private static AppRater sAppRater;

    private AppRater() {
    }

    public static AppRater getInstance() {
        if (sAppRater == null) {
            sAppRater = new AppRater();
        }

        return sAppRater;
    }

    /**
     * Rate launch time.
     * 
     * @param launchTime
     *            the launch time
     * @return the int
     */
    private int rateLaunchTime(final long launchTime) {
        if (launchTime > VERY_BAD_LAUNCHTIME) {
            return SCORE_D;
        } else if (launchTime <= VERY_BAD_LAUNCHTIME
                && launchTime >= BAD_LAUNCHTIME) {
            return SCORE_C;
        } else if (launchTime <= BAD_LAUNCHTIME
                && launchTime >= FAIR_LAUNCHTIME) {
            return SCORE_B;
        } else {
            return SCORE_A;
        }

    }

    /**
     * Rate overdraws.
     * 
     * @param numberOfOverDraws
     *            the number of over draws
     * @return the int
     */
    private int rateOverdraws(final String numberOfOverDraws) {
        if (numberOfOverDraws.contains(OVERDRAW_COUNT_POOR)) {
            return SCORE_D;
        } else if (numberOfOverDraws.contains(OVERDRAW_COUNT_FAIR)) {
            return SCORE_C;
        } else if (numberOfOverDraws.contains(OVERDRAW_COUNT_GOOD)) {
            return SCORE_B;
        } else if (numberOfOverDraws.contains(OVERDRAW_COUNT_EXCELLENT)) {
            return SCORE_A;
        } else {
            return SCORE_A;
        }
    }

    /**
     * Rate activity stack.
     * 
     * @param numberOfActivitiesInStack
     *            the number of activities in stack
     * @return the int
     */
    private int rateActivityStack(final int numberOfActivitiesInStack) {
        if (numberOfActivitiesInStack == 0 || numberOfActivitiesInStack == 1) {
            return SCORE_A;
        } else {
            return SCORE_D;
        }
    }

    /**
     * Rate gc calls.
     * 
     * @param numberOfGC
     *            the number of gc
     * @return the int
     */
    private int rateGCCalls(final int numberOfGC) {
        if (numberOfGC > GC_CALLS_VERYBAD) {
            return SCORE_D;
        } else if (numberOfGC <= GC_CALLS_VERYBAD && numberOfGC >= GC_CALLS_BAD) {
            return SCORE_C;
        } else if (numberOfGC < GC_CALLS_BAD && numberOfGC >= GC_CALLS_FAIR) {
            return SCORE_B;
        } else {
            return SCORE_A;
        }
    }

    /**
     * Rate activity.
     * 
     * @param activityName
     *            the activity name
     * @param launchTime
     *            the launch time
     * @param activitiesInStack
     *            the activities in stack
     * @param overdraws
     *            the overdraws
     * @param noOfGC
     *            the no of gc
     * @return the int
     */
    private String rateActivity(final String activityName,
            final long launchTime, final int activitiesInStack,
            final String overdraws, final int noOfGC) {
        // launch time grade
        final int launchTimeGrade = rateLaunchTime(launchTime);
        // Activity stack grade
        final int activitiesInStackGrade = rateActivityStack(activitiesInStack);
        // Overdraw Grade
        final int overDrawGrade = rateOverdraws(overdraws);
        // GC grade
        final int gcCallsGrade = rateGCCalls(noOfGC);
        // calculate grade
        final int grade = (launchTimeGrade + activitiesInStackGrade
                + overDrawGrade + gcCallsGrade)
                / NUMBER_OF_RULES;

        if (grade >= GRADE_A) {
            return "A";
        } else if (grade < GRADE_A && grade >= GRADE_B) {
            return "B";
        } else if (grade < GRADE_B && grade >= GRADE_C) {
            return "C";
        } else {
            return "D";
        }

    }

    /**
     * Calculate grades.
     * 
     * @param activityName
     *            the activity name
     * @param launchTime
     *            the launch time
     * @param numberOfActivitiesInStack
     *            the number of activities in stack
     * @param noOfOverDraws
     *            the no of over draws
     * @param noOfGC
     *            the no of gc
     */
    public void calculateGrades(final String activityName,
            final long launchTime, final int numberOfActivitiesInStack,
            final String noOfOverDraws, final int noOfGC) {

        final String rank = rateActivity(activityName, launchTime,
                numberOfActivitiesInStack, noOfOverDraws, noOfGC);

        activitiesGrade.put(activityName, rank);
        /* Calculate Launch time Rating */
        mGradeLaunchTime = mGradeLaunchTime + rateLaunchTime(launchTime);
        /* Calculate GC Calls Rating */
        mGradeGC = mGradeGC + rateGCCalls(noOfGC);
        /* Calculate Overdraw Rating */
        mGradeOverDraw = mGradeOverDraw + rateOverdraws(noOfOverDraws);
        /* Calculate Activity Stack Rating */
        mGradeActivityStack = mGradeActivityStack
                + rateActivityStack(numberOfActivitiesInStack);
    }

    /**
     * Gets the rank.
     * 
     * @param grade
     *            the grade
     * @return the rank
     */
    private String getRating(final int grade) {
        String rank = null;
        if (grade > GRADE_A) {
            rank = "A";
        } else if (grade <= GRADE_A && grade >= GRADE_B) {
            rank = "B";
        } else if (grade < GRADE_B && grade >= GRADE_C) {
            rank = "C";
        } else {
            rank = "D";
        }
        return rank;
    }

    /**
     * Gets the launch time score.
     * 
     * @return the launch time score
     */
    public int getLaunchTimeScore() {
        final int launchTimeGrade = mGradeLaunchTime / activitiesGrade.size();
        return launchTimeGrade;
    }

    /**
     * Gets the GC calls score.
     * 
     * @return the GC calls score
     */
    public int getGCCallsScore() {
        final int gcGrade = mGradeGC / activitiesGrade.size();
        return gcGrade;
    }

    /**
     * Gets the activity stack score.
     * 
     * @return the activity stack score
     */
    public int getActivityStackScore() {
        final int activityStackGrade = mGradeActivityStack
                / activitiesGrade.size();
        return activityStackGrade;
    }

    /**
     * Gets the over draw score.
     * 
     * @return the over draw score
     */
    public int getOverDrawScore() {
        int overdrawGrade = mGradeOverDraw / activitiesGrade.size();

        if (overdrawGrade == -1) {
            overdrawGrade = 100;
        }
        return overdrawGrade;
    }

    /**
     * Gets the application score.
     * 
     * @return the application score
     */
    public int getApplicationScore() {
        final int finalScore = (getLaunchTimeScore() + getGCCallsScore()
                + getOverDrawScore() + getActivityStackScore())
                / NUMBER_OF_RULES;
        return finalScore;

    }

    /**
     * Gets the application rating.
     * 
     * @return the application rating
     */
    public String getApplicationRating() {
        final int finalScore = (getLaunchTimeScore() + getGCCallsScore()
                + getOverDrawScore() + getActivityStackScore())
                / NUMBER_OF_RULES;
        final String rank = getRating(finalScore);
        return rank;
    }

    /**
     * Gets the s activities grade.
     * 
     * @return the s activities grade
     */
    public Map<String, String> getsActivitiesGrade() {
        return activitiesGrade;
    }

}
