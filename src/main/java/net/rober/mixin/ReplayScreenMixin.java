package net.rober.mixin;

import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiButton;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiElement;
import com.replaymod.replay.gui.screen.GuiReplayViewer;
import com.replaymod.replaystudio.pathing.path.Keyframe;
import com.replaymod.replaystudio.pathing.path.Timeline;
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
import java.util.*;


@Mixin(GuiReplayViewer.class)
public class ReplayScreenMixin {

	@Shadow @Final public GuiReplayViewer.GuiReplayList list;

	@ModifyArgs(method = "<init>", at = @At(value="INVOKE",target = "Lcom/replaymod/lib/de/johni0702/minecraft/gui/container/GuiPanel;addElements(Lcom/replaymod/lib/de/johni0702/minecraft/gui/layout/LayoutData;[Lcom/replaymod/lib/de/johni0702/minecraft/gui/element/GuiElement;)Lcom/replaymod/lib/de/johni0702/minecraft/gui/container/AbstractGuiContainer;", ordinal = 0))
	private void addButtonMixin(Args args) {
		final GuiButton interpolateButton = new GuiButton().onClick(()-> {
			List<GuiReplayViewer.GuiReplayEntry> selected = list.getSelected();
			HashMap<String, Long> replaysDurations = new HashMap<>();
			int durationsSum = 0;
            for (GuiReplayViewer.GuiReplayEntry entry : selected) {
                try {
                    ZipReplayFile replayFile = new ZipReplayFile(new ReplayStudio(),entry.file);
					long replayDuration = ((long) replayFile.getMetaData().getDuration())-3000L;
					replaysDurations.put(entry.file.getName(),replayDuration);
					durationsSum+=replayDuration;

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
			long finalDuration = 10000; //Initialize to don't mess up
			try {
				GuiReplayViewer.GuiReplayEntry entry = list.getSelected().get(0);
				ZipReplayFile replayFile = new ZipReplayFile(new ReplayStudio(),entry.file);
				ReplayInterpolator.LOGGER.info(replayFile.getMetaData().getDuration());
				Timeline timeline = replayFile.getTimelines(new SPTimeline()).get("");
				SPTimeline spTimeline = new SPTimeline(timeline);
				Collection<Keyframe> keyframes = spTimeline.getPath(SPTimeline.SPPath.POSITION).getKeyframes();

				Keyframe lastKeyframe = null;
				for (Keyframe keyframe : keyframes) {
					lastKeyframe = keyframe;
				}
				if(lastKeyframe!=null){
					finalDuration = lastKeyframe.getTime();
				}
				else {
					throw new RuntimeException("There isn't any camera keyframes to get the duration in the first replay");
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			long timePassed = 0;
			boolean isFirst = true;
			Timeline cameraTimeline = new SPTimeline().createTimeline();
			for (GuiReplayViewer.GuiReplayEntry entry : selected) {
                try {
                    ZipReplayFile replay = new ZipReplayFile(new ReplayStudio(),entry.file);
					long replayDuration = replaysDurations.get(entry.file.getName());
					double timelapseDuration = (double) replayDuration / (double )durationsSum * finalDuration;
					SPTimeline spTimeline = new SPTimeline();
					if(isFirst) {
						cameraTimeline = replay.getTimelines(new SPTimeline()).get("");
						isFirst=false;

                    }
                    spTimeline = new SPTimeline(cameraTimeline);
                    spTimeline.addTimeKeyframe(timePassed,3000);
					timePassed+= (long) timelapseDuration;
					spTimeline.addTimeKeyframe(timePassed, (int) (replayDuration+3000));
					replay.writeTimelines(spTimeline,new HashMap<>(Map.of("",cameraTimeline)));
					replay.save();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
			}
			
				}).setSize(150,20).setLabel("Interpolator");
		GuiElement[] content = new GuiElement[]{(((GuiReplayViewer) (Object) this)).loadButton, interpolateButton};
		args.set(1,content);
	}
}