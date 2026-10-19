package com.grahambartley.dogsunleashed.gametest;

import com.grahambartley.dogsunleashed.config.DogsUnleashedConfig;
import com.grahambartley.dogsunleashed.config.SoundSettings;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.network.ServerConfigPayloads.EditServerConfigC2SPayload;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.DecoderException;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

public final class ServerConfigPayloadGameTest implements FabricGameTest {

  private static final String ARENA = "dogs-unleashed:dog_arena";

  private static RegistryByteBuf newBuf(final TestContext context) {
    return new RegistryByteBuf(Unpooled.buffer(), context.getWorld().getRegistryManager());
  }

  private static RegistryByteBuf editPacketWithVolumes(
      final TestContext context, final float bark, final float howl) {
    final RegistryByteBuf buf = newBuf(context);
    EditServerConfigC2SPayload.CODEC.encode(
        buf, new EditServerConfigC2SPayload(DogsUnleashedConfig.defaults()));
    final RegistryByteBuf defaultVolumes = newBuf(context);
    defaultVolumes.writeFloat(SoundSettings.DEFAULT_BARK_VOLUME);
    defaultVolumes.writeFloat(SoundSettings.DEFAULT_HOWL_VOLUME);
    final int offset = ByteBufUtil.indexOf(defaultVolumes, buf);
    context.assertTrue(offset >= 0, "The edit packet must carry the default volumes");
    buf.setFloat(offset, bark);
    buf.setFloat(offset + Float.BYTES, howl);
    return buf;
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void editPacketWithNonFiniteVolumesFallsBackToDefaults(final TestContext context) {
    final RegistryByteBuf buf = editPacketWithVolumes(context, Float.NaN, Float.POSITIVE_INFINITY);

    final SoundSettings sound = EditServerConfigC2SPayload.CODEC.decode(buf).config().sound();

    context.assertTrue(
        SoundSettings.DEFAULTS.equals(sound),
        "A NaN or infinite volume must be refused, but decoded " + sound);
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void editPacketWithMoreBreedsThanExistIsRefused(final TestContext context) {
    final RegistryByteBuf buf = newBuf(context);
    buf.writeBoolean(true);
    buf.writeVarInt(100);
    buf.writeVarInt(UnleashedDogBreed.values().length + 1);

    boolean refused = false;
    try {
      EditServerConfigC2SPayload.CODEC.decode(buf);
    } catch (final DecoderException expected) {
      refused = true;
    }

    context.assertTrue(refused, "A breed rate map larger than the breed list must be refused");
    context.complete();
  }
}
