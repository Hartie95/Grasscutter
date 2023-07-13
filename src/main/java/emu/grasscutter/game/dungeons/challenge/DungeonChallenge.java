package emu.grasscutter.game.dungeons.challenge;

import emu.grasscutter.game.dungeons.enums.DungeonPassConditionType;
import emu.grasscutter.game.dungeons.challenge.trigger.ChallengeTrigger;
import emu.grasscutter.game.world.Scene;
import emu.grasscutter.scripts.data.SceneGroup;

import java.util.List;

public class DungeonChallenge extends WorldChallenge {

    /**
     * has more challenge
     */
    private boolean stage;

    // header: currentChallengeIndex, currentChallengeId, fatherChallengeIndex
    public DungeonChallenge(Scene scene, SceneGroup group,
                            ChallengeInfo header,
                            List<Integer> paramList,
                            List<ChallengeTrigger> challengeTriggers,
                            ChallengeScoreInfo scoreInfo) {
        super(scene, group, header, paramList, challengeTriggers, scoreInfo);
    }

    public boolean isStage() {
        return stage;
    }

    public void setStage(boolean stage) {
        this.stage = stage;
    }

    @Override
    public void done() {
        super.done();
        getScene().triggerDungeonEvent(DungeonPassConditionType.DUNGEON_COND_FINISH_CHALLENGE, getInfo().challengeId(), getInfo().challengeIndex());
        if (this.isSuccess()) {
            // Settle
            settle();
        }
    }

    private void settle() {
        if (!stage) {
            var scene = this.getScene();
            /*if(this.isSuccess()){
                scene.getDungeonManager().finishDungeon();
            } else {
                scene.getDungeonManager().failDungeon();
            }*/
        }
    }

}
