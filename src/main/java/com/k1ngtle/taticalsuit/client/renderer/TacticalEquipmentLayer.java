package com.k1ngtle.taticalsuit.client.renderer;

import com.k1ngtle.taticalsuit.capability.EquipmentSlotType;
import com.k1ngtle.taticalsuit.capability.TacticalEquipmentProvider;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class TacticalEquipmentLayer<T extends LivingEntity, M extends HumanoidModel<T>> extends RenderLayer<T, M> {

    private final HumanoidModel<T> innerArmorModel;
    private final HumanoidModel<T> outerArmorModel;

    public TacticalEquipmentLayer(RenderLayerParent<T, M> parent, EntityModelSet modelSet) {
        super(parent);
        // We pre-bake the vanilla armor models to use as base templates for rendering custom clothing
        this.innerArmorModel = new HumanoidModel<>(modelSet.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR));
        this.outerArmorModel = new HumanoidModel<>(modelSet.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR));
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T entity, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        
        entity.getCapability(TacticalEquipmentProvider.CAPABILITY).ifPresent(cap -> {
            
            // Left Side - Uniform/Armor
            renderSlot(poseStack, buffer, entity, packedLight, cap.getStackInSlot(EquipmentSlotType.SHIRT.getIndex()), EquipmentSlotType.SHIRT);
            renderSlot(poseStack, buffer, entity, packedLight, cap.getStackInSlot(EquipmentSlotType.PANTS.getIndex()), EquipmentSlotType.PANTS);
            renderSlot(poseStack, buffer, entity, packedLight, cap.getStackInSlot(EquipmentSlotType.GLOVES.getIndex()), EquipmentSlotType.GLOVES);
            renderSlot(poseStack, buffer, entity, packedLight, cap.getStackInSlot(EquipmentSlotType.BOOTS.getIndex()), EquipmentSlotType.BOOTS);

            // Right Side - Accessories
            renderSlot(poseStack, buffer, entity, packedLight, cap.getStackInSlot(EquipmentSlotType.BELT.getIndex()), EquipmentSlotType.BELT);
            renderSlot(poseStack, buffer, entity, packedLight, cap.getStackInSlot(EquipmentSlotType.TATTOO.getIndex()), EquipmentSlotType.TATTOO);
            renderSlot(poseStack, buffer, entity, packedLight, cap.getStackInSlot(EquipmentSlotType.EYEWEAR.getIndex()), EquipmentSlotType.EYEWEAR);
            renderSlot(poseStack, buffer, entity, packedLight, cap.getStackInSlot(EquipmentSlotType.WATCH.getIndex()), EquipmentSlotType.WATCH);
        });
    }

    private void renderSlot(PoseStack poseStack, MultiBufferSource buffer, T entity, int packedLight, ItemStack stack, EquipmentSlotType tacticalSlot) {
        if (stack.isEmpty()) return;

        if (stack.getItem() instanceof ArmorItem armorItem) {
            // If the item is an Armor piece, render it wrapped around the player's body!
            EquipmentSlot vanillaSlotTarget = getArmorSlotMapping(tacticalSlot);
            HumanoidModel<T> baseModel = (vanillaSlotTarget == EquipmentSlot.LEGS) ? innerArmorModel : outerArmorModel;
            renderArmorPiece(poseStack, buffer, entity, stack, vanillaSlotTarget, tacticalSlot, packedLight, baseModel);
        } else {
            // If it's a standard Item (like a custom 3D model Watch or Glasses), pin it to a bone!
            switch (tacticalSlot) {
                case EYEWEAR:
                    renderItemAttached(poseStack, buffer, stack, packedLight, this.getParentModel().head, 0.0f, -0.25f, -0.28f, 0f, 180f, 0f, 0.65f);
                    break;
                case WATCH:
                    // Pin to the left wrist
                    renderItemAttached(poseStack, buffer, stack, packedLight, this.getParentModel().leftArm, 0.05f, 0.65f, 0.0f, 0f, -90f, 0f, 0.4f);
                    break;
                case BELT:
                    renderItemAttached(poseStack, buffer, stack, packedLight, this.getParentModel().body, 0.0f, 0.65f, -0.15f, 0f, 180f, 0f, 0.75f);
                    break;
                default:
                    break;
            }
        }
    }

    private EquipmentSlot getArmorSlotMapping(EquipmentSlotType tacticalSlot) {
        return switch (tacticalSlot) {
            case SHIRT, GLOVES, TATTOO, WATCH -> EquipmentSlot.CHEST;
            case PANTS, BELT -> EquipmentSlot.LEGS;
            case BOOTS -> EquipmentSlot.FEET;
            case EYEWEAR -> EquipmentSlot.HEAD;
        };
    }

    private void renderArmorPiece(PoseStack poseStack, MultiBufferSource bufferSource, T entity, ItemStack itemStack, EquipmentSlot slot, EquipmentSlotType tacticalSlot, int packedLight, HumanoidModel<T> defaultModel) {
        
        // Fetch custom models (supports GeckoLib natively!)
        HumanoidModel<?> model = (HumanoidModel<?>) net.minecraftforge.client.ForgeHooksClient.getArmorModel(entity, itemStack, slot, defaultModel);
        this.getParentModel().copyPropertiesTo((HumanoidModel<T>) model);
        
        // Define which body parts to show based on the custom slot
        setPartVisibility(model, slot, tacticalSlot);

        ArmorItem armorItem = (ArmorItem) itemStack.getItem();

        // Fetch custom textures and colors
        String materialName = armorItem.getMaterial().getName();
        String domain = "minecraft";
        int idx = materialName.indexOf(':');
        if (idx != -1) {
            domain = materialName.substring(0, idx);
            materialName = materialName.substring(idx + 1);
        }
        String defaultTexture = String.format("%s:textures/models/armor/%s_layer_%d.png", domain, materialName, (slot == EquipmentSlot.LEGS ? 2 : 1));
        
        String textureStr = net.minecraftforge.client.ForgeHooksClient.getArmorTexture(entity, itemStack, defaultTexture, slot, null);
        ResourceLocation texture = new ResourceLocation(textureStr);
        
        float r = 1.0F, g = 1.0F, b = 1.0F;
        if (armorItem instanceof net.minecraft.world.item.DyeableArmorItem dyedArmor) {
            int color = dyedArmor.getColor(itemStack);
            r = (float)(color >> 16 & 255) / 255.0F;
            g = (float)(color >> 8 & 255) / 255.0F;
            b = (float)(color & 255) / 255.0F;
        }

        // Send to renderer
        VertexConsumer vertexconsumer = ItemRenderer.getArmorFoilBuffer(bufferSource, RenderType.armorCutoutNoCull(texture), false, itemStack.hasFoil());
        model.renderToBuffer(poseStack, vertexconsumer, packedLight, OverlayTexture.NO_OVERLAY, r, g, b, 1.0F);
    }

    private void setPartVisibility(HumanoidModel<?> model, EquipmentSlot slot, EquipmentSlotType tacticalSlot) {
        model.setAllVisible(false);
        
        // Custom visibilities so a shirt doesn't accidentally render leggings, etc.
        if (tacticalSlot == EquipmentSlotType.GLOVES) {
            model.leftArm.visible = true;
            model.rightArm.visible = true;
        } else if (tacticalSlot == EquipmentSlotType.BELT) {
            model.body.visible = true;
        } else if (tacticalSlot == EquipmentSlotType.TATTOO) {
            model.setAllVisible(true);
            model.hat.visible = false;
        } else {
            // Standard mappings
            switch (slot) {
                case HEAD:
                    model.head.visible = true;
                    model.hat.visible = true;
                    break;
                case CHEST:
                    model.body.visible = true;
                    model.rightArm.visible = true;
                    model.leftArm.visible = true;
                    break;
                case LEGS:
                    model.body.visible = true;
                    model.rightLeg.visible = true;
                    model.leftLeg.visible = true;
                    break;
                case FEET:
                    model.rightLeg.visible = true;
                    model.leftLeg.visible = true;
                    break;
            }
        }
    }

    private void renderItemAttached(PoseStack poseStack, MultiBufferSource buffer, ItemStack stack, int packedLight, ModelPart bone, float x, float y, float z, float rotX, float rotY, float rotZ, float scale) {
        poseStack.pushPose();
        bone.translateAndRotate(poseStack); // Lock to the animation of the specific limb (e.g. arm swinging)
        
        poseStack.translate(x, y, z);
        poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(rotX));
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(rotY));
        poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(rotZ));
        poseStack.scale(scale, scale, scale);
        
        Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED, packedLight, OverlayTexture.NO_OVERLAY, poseStack, buffer, null, 0);
        poseStack.popPose();
    }
}