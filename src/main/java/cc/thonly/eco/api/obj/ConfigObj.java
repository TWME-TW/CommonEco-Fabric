package cc.thonly.eco.api.obj;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.lang.reflect.Field;
import java.util.Map;

public class ConfigObj {
    public static double eco_block_ratio = 1.0;
    public static Item select_item = Items.WOODEN_HOE;

    public static void read(Map<String, Object> config) {
        eco_block_ratio = (double) config.getOrDefault("eco_block_ratio","1.0");
        select_item = Registries.ITEM.get(Identifier.of((String) config.getOrDefault("select_item","minecraft:wooden_hoe")));
        modifyReflection();
    }
    public static void modifyReflection() {
        try {
            Class<?> serverMainClass = Class.forName("com.github.zly2006.enclosure.ServerMain");
            Field operationItemField = serverMainClass.getDeclaredField("operationItem");
            operationItemField.setAccessible(true);
            Item newItem = select_item;
            operationItemField.set(null, newItem);

            Item modifiedItem = (Item) operationItemField.get(null);
            System.out.println("Modified operationItem: " + modifiedItem);
        } catch (ClassNotFoundException e) {
        } catch (NoSuchFieldException e) {
            System.err.println("Field not found: " + e.getMessage());
        } catch (IllegalAccessException e) {
            System.err.println("Unable to access field: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
