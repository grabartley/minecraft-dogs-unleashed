package com.grahambartley.dogsunleashed.item;

import net.minecraft.item.Item;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

public class RopeToyItem extends Item implements GeoItem {

  private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

  public RopeToyItem(final Settings settings) {
    super(settings);
  }

  @Override
  public void registerControllers(final AnimatableManager.ControllerRegistrar controllers) {}

  @Override
  public AnimatableInstanceCache getAnimatableInstanceCache() {
    return this.cache;
  }
}
