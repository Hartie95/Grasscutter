package emu.grasscutter.game.entity.gadget;

import java.util.Arrays;
import java.util.Optional;

import emu.grasscutter.game.entity.EntityGadget;
import emu.grasscutter.game.entity.gadget.worktop.WorktopWorktopOptionHandler;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.net.proto.GadgetInteractReqOuterClass.GadgetInteractReq;
import emu.grasscutter.net.proto.SceneGadgetInfoOuterClass.SceneGadgetInfo;
import emu.grasscutter.net.proto.SelectWorktopOptionReqOuterClass.SelectWorktopOptionReq;
import emu.grasscutter.net.proto.WorktopInfoOuterClass.WorktopInfo;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import lombok.Getter;

public class GadgetWorktop extends GadgetContent {
    @Getter private final IntSet worktopOptions = new IntOpenHashSet();
    private WorktopWorktopOptionHandler handler;

    public GadgetWorktop(EntityGadget gadget) {
        super(gadget);
    }

//    public IntSet getWorktopOptions() {
//        if (this.worktopOptions == null) {
//            this.worktopOptions = new IntOpenHashSet();
//        }
//        return this.worktopOptions;
//    }

    public void addWorktopOptions(int[] options) {
        Arrays.stream(options).forEach(getWorktopOptions()::add);
    }

    public void removeWorktopOption(int option) {
        getWorktopOptions().remove(option);
    }

    public boolean onInteract(Player player, GadgetInteractReq req) {
        return false;
    }

    public void onBuildProto(SceneGadgetInfo.Builder gadgetInfo) {
        gadgetInfo.setWorktop(WorktopInfo.newBuilder()
            .addAllOptionList(getWorktopOptions())
            .build());
    }

    public void setOnSelectWorktopOptionEvent(WorktopWorktopOptionHandler handler) {
        this.handler = handler;
    }
    public boolean onSelectWorktopOption(SelectWorktopOptionReq req) {
        Optional.ofNullable(this.handler)
            .ifPresent(h -> h.onSelectWorktopOption(this, req.getOptionId()));
        return false;
    }

}
