package emu.grasscutter.game.dungeons.challenge.factory;

import emu.grasscutter.game.dungeons.challenge.ChallengeInfo;
import emu.grasscutter.game.dungeons.challenge.WorldChallenge;
import emu.grasscutter.game.dungeons.challenge.enums.ChallengeType;
import emu.grasscutter.game.dungeons.challenge.trigger.ElementReactionTrigger;
import emu.grasscutter.game.entity.EntityMonster;
import emu.grasscutter.game.props.ElementReactionType;
import emu.grasscutter.game.world.Scene;
import emu.grasscutter.scripts.data.SceneGroup;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.stream.IntStream;

import static emu.grasscutter.game.dungeons.challenge.enums.ChallengeType.*;

@Getter
@Setter
public class ElementReactionCountChallengeFactoryHandler implements ChallengeFactoryHandler{
    private ChallengeType challengeType;
    private final List<ElementReactionType> swirlReaction = List.of(
        ElementReactionType.SwirlFire, ElementReactionType.SwirlWater,
        ElementReactionType.SwirlElectric, ElementReactionType.SwirlIce);
    private final List<ElementReactionType> crystalliseReaction = List.of(
        ElementReactionType.CrystalliseFire, ElementReactionType.CrystalliseWater,
        ElementReactionType.CrystalliseElectric, ElementReactionType.CrystalliseIce);

    @Override
    public boolean isThisType(ChallengeType challengeType) {
        if (challengeType == CHALLENGE_ELEMENT_REACTION_COUNT
            || challengeType == CHALLENGE_SWIRL_ELEMENT_REACTION_COUNT
            || challengeType == CHALLENGE_CRYSTAL_ELEMENT_REACTION_COUNT) {
            setChallengeType(challengeType);
            return true;
        }
        return false;
    }

    /**
     * Get reaction type by integer for this challenge
     * @param oneHotReactionType: one hot encoded list to select targeted reaction type
     */
    private List<ElementReactionType> getReactionType(List<Integer> oneHotReactionType) {
        if (getChallengeType() == CHALLENGE_ELEMENT_REACTION_COUNT) {
            return List.of(ElementReactionType.getTypeByValue(oneHotReactionType.get(0)));
        }

        List<ElementReactionType> reactionTypes = getChallengeType() == CHALLENGE_SWIRL_ELEMENT_REACTION_COUNT ?
            getSwirlReaction() : getCrystalliseReaction();

        return IntStream.range(0, reactionTypes.size())
            .filter(i -> oneHotReactionType.get(i) == 1)
            .mapToObj(reactionTypes::get)
            .toList();
    }

    /**
     * Build a new challenge
     * @param params: 1) [reactionType, goal, unused1, unused2, successCount, failCount] or
     *              2) [Fire, Water, Electric, Ice, goalCount, successCount, failCount],
     *              first four parameters are one hot encoded
     */
    @Override
    public WorldChallenge build(ChallengeInfo header, List<Integer> params, Scene scene, SceneGroup group) {
        // normal reaction if size == 6, crystallise or swirl if more than 6
        int goalCount = params.size() > 6 ? params.get(4) : params.get(1);

        return new WorldChallenge(
            scene, group,
            header,
            List.of(goalCount), // parameters
            List.of(new ElementReactionTrigger(1, goalCount, getReactionType(params.subList(0, 4)), EntityMonster.class, true)),
            params.get(params.size() - 2), params.get(params.size() - 1) // success count, fail count
        );
    }
}
