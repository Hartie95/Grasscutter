package emu.grasscutter.game.dungeons.challenge;

public record ChallengeScoreInfo(int succScore, int failScore) {
    public int get(boolean isSucc){
        return isSucc ? succScore : failScore;
    }
}
