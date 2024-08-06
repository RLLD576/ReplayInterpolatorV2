package net.rober.mixin;

import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiButton;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiElement;
import com.replaymod.replay.gui.screen.GuiReplayViewer;
import com.replaymod.replaystudio.pathing.path.Keyframe;
import com.replaymod.replaystudio.pathing.path.Path;
import com.replaymod.replaystudio.pathing.path.Timeline;
import com.replaymod.replaystudio.replay.ZipReplayFile;
import com.replaymod.replaystudio.studio.ReplayStudio;
import com.replaymod.replaystudio.util.Property;
import com.replaymod.simplepathing.SPTimeline;
import net.rober.ReplayInterpolator;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.io.IOException;
import java.util.Iterator;
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
					Map<String, Timeline> map = replay.getTimelines(new SPTimeline());
					ReplayInterpolator.LOGGER.info(map.entrySet().size());
					map.keySet().forEach((String s)-> {
						ReplayInterpolator.LOGGER.info(s);
                        for (Path path : map.get(s).getPaths()) {
                            path.getKeyframes().iterator().forEachRemaining((Keyframe keyframe)-> {ReplayInterpolator.LOGGER.info(keyframe.getTime());
							keyframe.getProperties().iterator().forEachRemaining((Property prop)-> {});
							});
                        }
					});

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
				}).setSize(73,20).setLabel("Interpolator");
		GuiElement[] content = new GuiElement[]{(((GuiReplayViewer) (Object) this)).loadButton, interpolateButton};
		args.set(1,content);
	}
}