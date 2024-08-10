package net.rober.mixin;

import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiButton;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiElement;
import com.replaymod.pathing.properties.TimestampProperty;
import com.replaymod.replay.gui.screen.GuiReplayViewer;
import com.replaymod.replaystudio.pathing.impl.KeyframeImpl;
import com.replaymod.replaystudio.pathing.impl.PathImpl;
import com.replaymod.replaystudio.pathing.path.Keyframe;
import com.replaymod.replaystudio.pathing.path.Path;
import com.replaymod.replaystudio.pathing.path.PathSegment;
import com.replaymod.replaystudio.pathing.path.Timeline;
import com.replaymod.replaystudio.pathing.serialize.TimelineSerialization;
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

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
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
                    ZipReplayFile replayfile = new ZipReplayFile(new ReplayStudio(),entry.file);
					SPTimeline spTimeline = new SPTimeline(replayfile.getTimelines(new SPTimeline()).get(""));
					spTimeline.addTimeKeyframe(123,123);
					replayfile.writeTimelines(spTimeline,new HashMap<>(Map.of("",spTimeline.getTimeline())));
					TimelineSerialization serializer = new TimelineSerialization(spTimeline,replayfile);
					serializer.save(new HashMap<>(Map.of("",spTimeline.getTimeline())));
					replayfile.saveTo(new File(entry.file.getPath()+".asd.mcpr"));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
			/*HashMap<String, Integer> replaysDurations = new HashMap<>();
			int durationsSum = 0;
            for (GuiReplayViewer.GuiReplayEntry entry : list.getSelected()) {
                try {
                    ZipReplayFile replayFile = new ZipReplayFile(new ReplayStudio(),entry.file);
					int replayDuration = replayFile.getMetaData().getDuration();
					replaysDurations.put(entry.file.getName(),replayDuration);
					durationsSum+=replayDuration;

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
			for(GuiReplayViewer.GuiReplayEntry entry : list.getSelected()){
                try {
                    ZipReplayFile replayFile = new ZipReplayFile(new ReplayStudio(),entry.file);
					Timeline timeline = replayFile.getTimelines(new SPTimeline()).get("");
					SPTimeline spTimeline = new SPTimeline(timeline);
					Collection<Keyframe> keyframes = spTimeline.getPath(SPTimeline.SPPath.POSITION).getKeyframes();
					Keyframe lastKeyframe = null;
					for (Keyframe keyframe : keyframes) {
						lastKeyframe = keyframe;
					}
					if(lastKeyframe!=null){
						ReplayInterpolator.LOGGER.info(lastKeyframe.getTime());
					}
					else {
						throw new RuntimeException("There isn't any camera keyframes to get the duration in the first replay");
					}
					replayFile.writeTimelines(SPTimeline);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }*/


				}).setSize(150,20).setLabel("Interpolator");
		GuiElement[] content = new GuiElement[]{(((GuiReplayViewer) (Object) this)).loadButton, interpolateButton};
		args.set(1,content);
	}
}