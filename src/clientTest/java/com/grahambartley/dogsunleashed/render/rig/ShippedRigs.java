package com.grahambartley.dogsunleashed.render.rig;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import javax.imageio.ImageIO;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.keyframe.BoneAnimation;
import software.bernie.geckolib.animation.keyframe.Keyframe;
import software.bernie.geckolib.animation.keyframe.KeyframeStack;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.loading.json.raw.Model;
import software.bernie.geckolib.loading.json.typeadapter.KeyFramesAdapter;
import software.bernie.geckolib.loading.math.MathValue;
import software.bernie.geckolib.loading.object.BakedAnimations;
import software.bernie.geckolib.loading.object.BakedModelFactory;
import software.bernie.geckolib.loading.object.GeometryTree;

public final class ShippedRigs {

  private static final String ASSETS = "/assets/dogs-unleashed/";

  private ShippedRigs() {}

  public static BakedGeoModel model(final String name) {
    return modelAt("geo/" + name + ".geo.json");
  }

  public static BakedGeoModel modelAt(final String path) {
    final Model raw = KeyFramesAdapter.GEO_GSON.fromJson(json(path), Model.class);
    final BakedGeoModel baked =
        BakedModelFactory.DEFAULT_FACTORY.constructGeoModel(GeometryTree.fromModel(raw));
    saveRestPose(baked.topLevelBones());
    return baked;
  }

  public static void pose(final BakedGeoModel model, final String rig, final String animationName) {
    final BakedAnimations animations =
        KeyFramesAdapter.GEO_GSON.fromJson(
            json("animations/" + rig + ".animation.json").getAsJsonObject("animations"),
            BakedAnimations.class);
    final Animation animation = animations.getAnimation(animationName);
    for (final BoneAnimation boneAnimation : animation.boneAnimations()) {
      final GeoBone bone = model.getBone(boneAnimation.boneName()).orElseThrow();
      final KeyframeStack<Keyframe<MathValue>> rotation = boneAnimation.rotationKeyFrames();
      bone.setRotX(heldFrame(rotation.xKeyframes(), 0) + bone.getInitialSnapshot().getRotX());
      bone.setRotY(heldFrame(rotation.yKeyframes(), 0) + bone.getInitialSnapshot().getRotY());
      bone.setRotZ(heldFrame(rotation.zKeyframes(), 0) + bone.getInitialSnapshot().getRotZ());
      final KeyframeStack<Keyframe<MathValue>> position = boneAnimation.positionKeyFrames();
      bone.setPosX(heldFrame(position.xKeyframes(), 0));
      bone.setPosY(heldFrame(position.yKeyframes(), 0));
      bone.setPosZ(heldFrame(position.zKeyframes(), 0));
      final KeyframeStack<Keyframe<MathValue>> scale = boneAnimation.scaleKeyFrames();
      bone.setScaleX(heldFrame(scale.xKeyframes(), 1));
      bone.setScaleY(heldFrame(scale.yKeyframes(), 1));
      bone.setScaleZ(heldFrame(scale.zKeyframes(), 1));
    }
  }

  public static List<String> animatedBones(final String path, final String animationName) {
    final BakedAnimations animations =
        KeyFramesAdapter.GEO_GSON.fromJson(
            json(path).getAsJsonObject("animations"), BakedAnimations.class);
    return Arrays.stream(animations.getAnimation(animationName).boneAnimations())
        .map(BoneAnimation::boneName)
        .toList();
  }

  static CollarBandLocator.CollarMask collarMask(final String rig) {
    try (InputStream in = stream("textures/entity/" + rig + "_collar.png")) {
      final BufferedImage image = ImageIO.read(in);
      return new CollarBandLocator.CollarMask() {
        @Override
        public int width() {
          return image.getWidth();
        }

        @Override
        public int height() {
          return image.getHeight();
        }

        @Override
        public boolean painted(final int x, final int y) {
          return (image.getRGB(x, y) >>> 24) != 0;
        }
      };
    } catch (final IOException exception) {
      throw new UncheckedIOException(exception);
    }
  }

  private static float heldFrame(final List<Keyframe<MathValue>> frames, final float none) {
    return frames.isEmpty() ? none : (float) frames.get(frames.size() - 1).endValue().get();
  }

  private static void saveRestPose(final List<GeoBone> bones) {
    for (final GeoBone bone : bones) {
      bone.saveInitialSnapshot();
      saveRestPose(bone.getChildBones());
    }
  }

  private static JsonObject json(final String path) {
    try (InputStream in = stream(path)) {
      return JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8))
          .getAsJsonObject();
    } catch (final IOException exception) {
      throw new UncheckedIOException(exception);
    }
  }

  private static InputStream stream(final String path) {
    final InputStream in = ShippedRigs.class.getResourceAsStream(ASSETS + path);
    if (in == null) {
      throw new IllegalArgumentException("missing asset " + path);
    }
    return in;
  }
}
