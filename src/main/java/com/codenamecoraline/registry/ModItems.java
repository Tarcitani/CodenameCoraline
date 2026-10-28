package com.codenamecoraline.registry;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.item.BestiaryItem;
import com.codenamecoraline.item.CatacombLilyItem;
import com.codenamecoraline.item.MistFlaskItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.equipment.Equippable;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Items registered by this mod. */
public final class ModItems {
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(CodenameCoraline.MOD_ID);

    /** Rusted iron: a worse iron. Used by the weapons a Redcap drops. */
    private static final ToolMaterial RUSTED_IRON = new ToolMaterial(
            BlockTags.INCORRECT_FOR_IRON_TOOL, 140, 4.0F, 1.0F, 8, ItemTags.IRON_TOOL_MATERIALS);

    /**
     * One egg for every Redcap: the variant (spear, dagger or claws) is drawn at random when it spawns.
     * The properties are built lazily because they need the entity type, which is not registered yet
     * while this class is initialised.
     */
    public static final DeferredItem<Item> REDCAP_SPAWN_EGG = ITEMS.registerItem("redcap_spawn_egg", SpawnEggItem::new,
            () -> new Item.Properties().spawnEgg(ModEntities.REDCAP.get()));

    /** The animated pebble a claws Redcap throws. Only its texture is used: it is not obtainable in the creative tab. */
    public static final DeferredItem<Item> REDCAP_PEBBLE = ITEMS.registerSimpleItem("redcap_pebble");

    // Parts of the mob: ingredients for potions that are not designed yet.
    public static final DeferredItem<Item> REDCAP_NAIL = ITEMS.registerSimpleItem("redcap_nail");
    public static final DeferredItem<Item> REDCAP_TOOTH = ITEMS.registerSimpleItem("redcap_tooth");

    // Weapons a Redcap carried. They drop with random wear (see Redcap.dropCustomDeathLoot).
    public static final DeferredItem<Item> REDCAP_DAGGER = ITEMS.registerSimpleItem("redcap_dagger",
            () -> new Item.Properties().sword(RUSTED_IRON, 2.0F, -1.6F));
    public static final DeferredItem<Item> REDCAP_PIKESTAFF = ITEMS.registerSimpleItem("redcap_pikestaff",
            () -> new Item.Properties().spear(RUSTED_IRON, 0.95F, 0.95F, 0.6F, 2.5F, 11.0F, 6.75F, 5.1F, 11.25F, 4.6F));

    /**
     * Worn on the head, but not as armor: without an equipment asset the game draws the item's own model on the
     * head (display context "head", see items/redcap_cap.json), which lets the cap be a real hood with a floppy
     * tip instead of a flat armor layer. Redcaps take the wearer for one of their own (see Redcap.isFriendTo).
     */
    public static final DeferredItem<Item> REDCAP_CAP = ITEMS.registerSimpleItem("redcap_cap",
            () -> new Item.Properties().component(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.HEAD).build()));

    /** The bestiary book: lore, attack patterns and habitats of every mob of this mod. */
    public static final DeferredItem<Item> BESTIARY = ITEMS.registerItem("bestiary", BestiaryItem::new,
            properties -> properties.stacksTo(1));

    public static final DeferredItem<Item> BASILISK_SPAWN_EGG = ITEMS.registerItem("basilisk_spawn_egg", SpawnEggItem::new,
            () -> new Item.Properties().spawnEgg(ModEntities.BASILISK.get()));
    public static final DeferredItem<Item> BASILISK_LEG = ITEMS.registerSimpleItem("basilisk_leg");
    public static final DeferredItem<Item> BASILISK_EYE = ITEMS.registerSimpleItem("basilisk_eye");
    public static final DeferredItem<Item> BASILISK_TAIL = ITEMS.registerSimpleItem("basilisk_tail");

    // Retired rewards stay registered to preserve items in existing worlds; no drops or recipes.
    public static final DeferredItem<Item> CATACOMB_LILY = ITEMS.registerItem("catacomb_lily", CatacombLilyItem::new);
    public static final DeferredItem<Item> ROTTEN_BUTTERFLY = ITEMS.registerSimpleItem("rotten_butterfly");
    public static final DeferredItem<Item> BASILISK_EYE_CYST = ITEMS.registerSimpleItem("basilisk_eye_cyst");
    public static final DeferredItem<Item> MIST_FLASK = ITEMS.registerItem("mist_flask", MistFlaskItem::new,
            properties -> properties.stacksTo(16));
    public static final DeferredItem<Item> BASILISK_WARD = ITEMS.registerSimpleItem("basilisk_ward",
            () -> new Item.Properties().stacksTo(1));

    private ModItems() {}

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
