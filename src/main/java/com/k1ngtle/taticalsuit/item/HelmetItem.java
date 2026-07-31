package com.k1ngtle.taticalsuit.item;

import com.k1ngtle.taticalsuit.client.model.HelmetModel;
import com.k1ngtle.taticalsuit.client.renderer.HelmetRenderer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;

public class HelmetItem extends ArmorItem implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public HelmetItem(ArmorMaterial material, Type type, Properties properties) {
        super(material, type, properties);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private HelmetRenderer armorRenderer;
            private GeoItemRenderer<HelmetItem> itemRenderer;

            @Override
            public HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
                if (this.armorRenderer == null) {
                    this.armorRenderer = new HelmetRenderer();
                }
                this.armorRenderer.prepForRender(livingEntity, itemStack, equipmentSlot, original);
                return this.armorRenderer;
            }

            // THIS is what allows the 3D model to render in your hand and inventory GUI!
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.itemRenderer == null) {
                    this.itemRenderer = new GeoItemRenderer<>(new HelmetModel());
                }
                return this.itemRenderer;
            }
        });
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}