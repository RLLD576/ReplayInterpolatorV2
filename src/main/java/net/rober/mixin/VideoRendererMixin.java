package net.rober.mixin;

import com.replaymod.pathing.properties.TimestampProperty;
import com.replaymod.render.rendering.*;
import com.replaymod.replaystudio.pathing.path.Keyframe;
import com.replaymod.replaystudio.pathing.path.Path;
import com.replaymod.replaystudio.pathing.path.Timeline;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(VideoRenderer.class)
public class VideoRendererMixin {
    @Unique
    private long videoEndTime;

    @Final
    @Shadow(remap = false)
    private Timeline timeline;

    @Shadow(remap = false)
    private int totalFrames;

    @Shadow(remap = false)
    private int fps;

    @Inject(method= "setup()V", at=@At("HEAD"), remap = false)
    private void setLocals(CallbackInfo ci){
        long min = Long.MAX_VALUE;
        long max = Long.MIN_VALUE;
        for(Path path : timeline.getPaths()){
            if(path.getKeyframes().iterator().next().getProperties().iterator().next().equals(TimestampProperty.PROPERTY)) {
                for (Keyframe keyframe:path.getKeyframes()){
                    long n = keyframe.getTime();
                    if(n<min)min=n;
                    if(n>max)max=n;
                }
            }
        }
        videoEndTime=max;

    }
    @Redirect(method="setup()V", at= @At(value = "FIELD", target = "Lcom/replaymod/render/rendering/VideoRenderer;totalFrames:I", opcode = Opcodes.PUTFIELD, remap = false), remap = false)
    private void changeTotalFrames(VideoRenderer instance, int value){
        totalFrames=(int)((videoEndTime/*-videoStartTime*/) * (long)fps / 1000L);
    }
}
