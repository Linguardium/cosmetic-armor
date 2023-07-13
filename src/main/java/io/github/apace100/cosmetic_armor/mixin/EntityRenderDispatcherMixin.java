package io.github.apace100.cosmetic_armor.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.apace100.cosmetic_armor.CosmeticArmor;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.HashMap;
import java.util.Map;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {

    @WrapOperation(at=@At(value="INVOKE", target="Lnet/minecraft/client/render/entity/EntityRenderer;render(Lnet/minecraft/entity/Entity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"),method="render")
    private void cosmeticArmorItemReplacement(EntityRenderer instance, Entity entity, float yaw, float tickDelta, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int light, Operation<Void> original) {
        // We lose functionality here by not rendering changes in all LivingEntities, only players
        // TODO: branch player handling and LivingEntity handling?
        if (!(entity instanceof AbstractClientPlayerEntity abstractClientPlayerEntity)) {
            original.call(instance, entity, yaw, tickDelta, matrixStack, vertexConsumerProvider, light);
            return;
        }
        HashMap<Integer,ItemStack> realEquipment = new HashMap<>();
        // Hardcoded PlayerInventory instance.
        // getArmorItems returns an iterable and is not guaranteed to be an inventory, it could just be a copy of all items.
        DefaultedList<ItemStack> inventory = abstractClientPlayerEntity.getInventory().armor;

        // Modify armor slots with cosmetics
        for(int i = 0; i < 4; i++) {
            EquipmentSlot slot = EquipmentSlot.fromTypeIndex(EquipmentSlot.Type.ARMOR, i);
            int equipSlot = slot.getEntitySlotId();
            ItemStack cosmeticStack = CosmeticArmor.getCosmeticArmor(abstractClientPlayerEntity, slot);
            if (!cosmeticStack.isEmpty()) {
                ItemStack equippedStack = inventory.get(equipSlot);
                if (equippedStack.isEmpty() || !equippedStack.isIn(CosmeticArmor.ALWAYS_VISIBLE)) {
                    // We use inventory.set instead of equipStack because equipStack calls onEquipStack which we don't want.
                    // We are modifying only for the rendering and no other effects should take place
                    // To keep this simple, instead of just cancelling out of the onEquipStack with a boolean, we just set the inventory slot directly
                    realEquipment.put(equipSlot, inventory.set(equipSlot, cosmeticStack));
                }
            }
        }

        //WrapOperation passes in an operation that calls the original method. we call that here, to render the entity now that the inventory has been modified
        original.call(instance, entity, yaw, tickDelta, matrixStack, vertexConsumerProvider, light);

        //Restore items in inventory slot
        for (Map.Entry<Integer,ItemStack> entry: realEquipment.entrySet()) {
            inventory.set(entry.getKey(),entry.getValue());
        }
    }
}
