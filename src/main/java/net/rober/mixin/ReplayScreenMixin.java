package net.rober.mixin;

import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiButton;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiElement;
import com.replaymod.pathing.properties.TimestampProperty;
import com.replaymod.replay.gui.screen.GuiReplayViewer;
import com.replaymod.replaystudio.pathing.impl.KeyframeImpl;
import com.replaymod.replaystudio.pathing.impl.PathImpl;
import com.replaymod.replaystudio.pathing.path.Path;
import com.replaymod.replaystudio.pathing.path.PathSegment;
import com.replaymod.replaystudio.pathing.path.Timeline;
import com.replaymod.replaystudio.replay.ReplayMetaData;
import com.replaymod.replaystudio.replay.ZipReplayFile;
import com.replaymod.replaystudio.studio.ReplayStudio;
import com.replaymod.simplepathing.SPTimeline;
import net.rober.ReplayInterpolator;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.io.IOException;
import java.util.List;
import java.util.Map;


@Mixin(GuiReplayViewer.class)
public class ReplayScreenMixin {

	@Shadow @Final public GuiReplayViewer.GuiReplayList list;

	@ModifyArgs(method = "<init>", at = @At(value="INVOKE",target = "Lcom/replaymod/lib/de/johni0702/minecraft/gui/container/GuiPanel;addElements(Lcom/replaymod/lib/de/johni0702/minecraft/gui/layout/LayoutData;[Lcom/replaymod/lib/de/johni0702/minecraft/gui/element/GuiElement;)Lcom/replaymod/lib/de/johni0702/minecraft/gui/container/AbstractGuiContainer;", ordinal = 0))
	private void addButtonMixin(Args args) {
		final GuiButton interpolateButton = new GuiButton().onClick(()-> {
            for (GuiReplayViewer.GuiReplayEntry entry : list.getSelected()) {
                try {
                    ZipReplayFile replay = new ZipReplayFile(new ReplayStudio(),entry.file);
					ReplayInterpolator.LOGGER.info(replay.getMetaData().getDuration());
					//SPTimeline SPTimeline = new SPTimeline();
					//if(replay.getTimelines(new SPTimeline()).keySet().isEmpty())replay.getTimelines(SPTimeline).put("",SPTimeline.createTimeline());
					SPTimeline SPtimeline = new SPTimeline(replay.getTimelines(new SPTimeline()).get(""));
					SPtimeline.addTimeKeyframe(123,123);
					ReplayMetaData metaData = replay.getMetaData();
					metaData.getDuration();
					

                } catch (Exception e) {
					e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }

				}).setSize(150,20).setLabel("Interpolator");
		GuiElement[] content = new GuiElement[]{(((GuiReplayViewer) (Object) this)).loadButton, interpolateButton};
		args.set(1,content);
	}
}