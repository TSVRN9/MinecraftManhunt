package me.tsvrn9.minecraftmanhunt.features;

import me.tsvrn9.minecraftmanhunt.MinecraftManhunt;
import me.tsvrn9.minecraftmanhunt.configuration.ConfigValue;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.stream.Stream;

public class OPHunterGear implements Feature {
    @ConfigValue("armor")
    private List<ItemStack> opArmor = Stream.of(
                    Material.DIAMOND_BOOTS,
                    Material.DIAMOND_LEGGINGS,
                    Material.DIAMOND_CHESTPLATE,
                    Material.DIAMOND_HELMET
            )
            .map(ItemStack::new)
            .peek(i -> {
                i.addUnsafeEnchantment(Enchantment.PROTECTION, 4);
                i.addUnsafeEnchantment(Enchantment.UNBREAKING, 3);
                i.addUnsafeEnchantment(Enchantment.VANISHING_CURSE, 1);
            })
            .toList();

    @ConfigValue("items")
    private List<ItemStack> items = defaultItems();

    private static List<ItemStack> defaultItems() {
        ItemStack axe = new ItemStack(Material.DIAMOND_AXE);
        axe.addUnsafeEnchantment(Enchantment.EFFICIENCY, 1);
        axe.addUnsafeEnchantment(Enchantment.UNBREAKING, 3);
        axe.addUnsafeEnchantment(Enchantment.VANISHING_CURSE, 1);
        return List.of(axe);
    }

    @Override
    public void onEnable(Plugin plugin) {
        MinecraftManhunt.hunterArmor.addAll(this.opArmor);
        MinecraftManhunt.hunterItems.addAll(this.items);
    }

    @Override
    public void onDisable(Plugin plugin) {
        MinecraftManhunt.hunterArmor.removeAll(this.opArmor); // should be fine
        MinecraftManhunt.hunterItems.removeAll(this.items);
    }

    @Override
    public String getPath() {
        return "give_op_hunter_gear";
    }
}
