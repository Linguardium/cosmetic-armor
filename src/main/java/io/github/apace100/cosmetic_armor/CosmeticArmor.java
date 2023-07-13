package io.github.apace100.cosmetic_armor;

import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.SlotType;
import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketsApi;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;

import java.util.List;
import java.util.Optional;

import static net.minecraft.util.registry.Registry.ITEM_KEY;

public class CosmeticArmor implements ModInitializer {

	public static final String MODID = "cosmetic-armor";

	public static final TagKey<Item> BLACKLIST = TagKey.of(ITEM_KEY, id("blacklist"));
	public static final TagKey<Item> ALWAYS_VISIBLE = TagKey.of(ITEM_KEY, id("always_visible"));

	@Override
	public void onInitialize() {
		for(int i = 0; i < 4; i++) {
			EquipmentSlot slot = EquipmentSlot.fromTypeIndex(EquipmentSlot.Type.ARMOR, i);
			TrinketsApi.registerTrinketPredicate(id(slot.getName()), (stack, slotReference, entity) -> {
				if(stack.isIn(BLACKLIST)) {
					return TriState.FALSE;
				}
				if(MobEntity.getPreferredEquipmentSlot(stack) == slot) {
					return TriState.TRUE;
				}
				return TriState.DEFAULT;
			});
		}
	}

	public static ItemStack getCosmeticArmor(LivingEntity entity, EquipmentSlot slot) {
		Optional<TrinketComponent> component = TrinketsApi.getTrinketComponent(entity);
		if(component.isPresent()) {
			List<Pair<SlotReference, ItemStack>> list = component.get().getEquipped(stack -> MobEntity.getPreferredEquipmentSlot(stack) == slot);
			for(Pair<SlotReference, ItemStack> equipped : list) {
				SlotType slotType = equipped.getLeft().inventory().getSlotType();
				if(!slotType.getName().equals("cosmetic")) {
					continue;
				}
				if(!slotType.getGroup().equalsIgnoreCase(slot.getName())) {
					continue;
				}
				return equipped.getRight();
			}
		}
		return ItemStack.EMPTY;
	}

	private static Identifier id(String path) {
		return new Identifier(MODID, path);
	}
}
