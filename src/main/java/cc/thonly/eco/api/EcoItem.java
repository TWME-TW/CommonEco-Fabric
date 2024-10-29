package cc.thonly.eco.api;

import net.minecraft.item.Item;

public class EcoItem {
    protected Item item;
    protected double value;
    public EcoItem(Item item, double value) {
        this.item = item;
        this.value = value;
    }

    public Item getItem() {
        return this.item;
    }

    public double getValue() {
        return this.value;
    }
}
