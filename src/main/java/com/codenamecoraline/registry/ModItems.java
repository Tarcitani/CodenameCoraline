package com.codenamecoraline.registry;

import com.codenamecoraline.CodenameCoraline;
import com.codenamecoraline.item.BestiaryItem;
import com.codenamecoraline.item.CatacombLilyItem;
import com.codenamecoraline.item.CrimsonOathGreatswordItem;
import com.codenamecoraline.item.CursedSpindleItem;
import com.codenamecoraline.item.TenThreadsStaffItem;
import com.codenamecoraline.item.DeadrootClubItem;
import com.codenamecoraline.item.HollowTrunkIdolItem;
import com.codenamecoraline.item.MistFlaskItem;
import com.codenamecoraline.item.RenegadeGreataxeItem;
import com.codenamecoraline.item.SilentBellItem;
import com.codenamecoraline.item.StainedOathCrestItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.equipment.Equippable;
import java.util.List;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Items registered by this mod. */
public final class ModItems {
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(CodenameCoraline.MOD_ID);

    /** Renegade steel: the greataxe the Collector of the Last Gift carried. */
    private static final ToolMaterial RENEGADE_STEEL = new ToolMaterial(
            BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 2200, 8.0F, 4.0F, 16, ItemTags.NETHERITE_TOOL_MATERIALS);

    /** Deadroot: a heavy club of dead wood. */
    private static final ToolMaterial DEADROOT = new ToolMaterial(
            BlockTags.INCORRECT_FOR_IRON_TOOL, 720, 5.0F, 2.0F, 10, ItemTags.LOGS);

    /** Obsidian steel edged with gold: the Crimson Oath Greatsword (10 attack damage in all, a heavy netherite-grade blade). */
    private static final ToolMaterial CRIMSON_OATH = new ToolMaterial(
            BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 2100, 8.0F, 4.0F, 15, ItemTags.NETHERITE_TOOL_MATERIALS);

    /** Items that repair the Staff of the Ten Threads (see data/codenamecoraline/tags/item/puppet_remnants.json). */
    public static final TagKey<Item> PUPPET_REMNANTS = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(CodenameCoraline.MOD_ID, "puppet_remnants"));

    /** Silver thread and carved wood: the Staff of the Ten Threads (7 attack damage in all). */
    private static final ToolMaterial TEN_THREADS = new ToolMaterial(
            BlockTags.INCORRECT_FOR_IRON_TOOL, 1561, 6.0F, 2.0F, 14, PUPPET_REMNANTS);

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

    public static final DeferredItem<Item> LITHOPHAGE_SPAWN_EGG = ITEMS.registerItem("lithophage_spawn_egg", SpawnEggItem::new,
            () -> new Item.Properties().spawnEgg(ModEntities.LITHOPHAGE.get()));
    public static final DeferredItem<Item> ANCIENT_CLAY_FRAGMENT = ITEMS.registerSimpleItem("ancient_clay_fragment");
    public static final DeferredItem<Item> MINERAL_AMBER_CORE = ITEMS.registerSimpleItem("mineral_amber_core");
    /** Sprite item rendered for the Lithophage's AmberShard projectile. */
    public static final DeferredItem<Item> AMBER_SHARD = ITEMS.registerSimpleItem("amber_shard");

    public static final DeferredItem<Item> LAST_GIFT_COLLECTOR_SPAWN_EGG = ITEMS.registerItem("last_gift_collector_spawn_egg", SpawnEggItem::new,
            () -> new Item.Properties().spawnEgg(ModEntities.LAST_GIFT_COLLECTOR.get()));
    public static final DeferredItem<Item> SILENT_BELL = ITEMS.registerItem("silent_bell", SilentBellItem::new,
            properties -> properties.stacksTo(1));
    public static final DeferredItem<Item> CRACKED_BELL = ITEMS.registerSimpleItem("cracked_bell",
            () -> new Item.Properties().stacksTo(16));
    public static final DeferredItem<Item> RENEGADE_GREATAXE = ITEMS.registerItem("renegade_greataxe", RenegadeGreataxeItem::new,
            properties -> properties.axe(RENEGADE_STEEL, 7.0F, -3.2F).rarity(Rarity.EPIC)
                    .component(DataComponents.LORE, new ItemLore(List.of(
                            Component.translatable("item.codenamecoraline.renegade_greataxe.lore1").withStyle(ChatFormatting.AQUA),
                            Component.translatable("item.codenamecoraline.renegade_greataxe.lore2").withStyle(ChatFormatting.GRAY)))));
    /** The Collector's face, used only as the icon of his story advancements (not in the creative tab). */
    public static final DeferredItem<Item> COLLECTOR_PORTRAIT = ITEMS.registerSimpleItem("collector_portrait",
            () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<Item> HOLLOW_TRUNK_IDOL = ITEMS.registerItem("hollow_trunk_idol", HollowTrunkIdolItem::new,
            properties -> properties.stacksTo(1));
    public static final DeferredItem<Item> DEADROOT_CLUB = ITEMS.registerItem("deadroot_club", DeadrootClubItem::new,
            properties -> properties.sword(DEADROOT, 8.0F, -3.0F).rarity(Rarity.RARE)
                    .component(DataComponents.LORE, new ItemLore(List.of(
                            Component.translatable("item.codenamecoraline.deadroot_club.lore1").withStyle(ChatFormatting.AQUA),
                            Component.translatable("item.codenamecoraline.deadroot_club.lore2").withStyle(ChatFormatting.GRAY)))));
    public static final DeferredItem<Item> CANOPY_RESIN = ITEMS.registerSimpleItem("canopy_resin");
    public static final DeferredItem<Item> HEARTWOOD_SPLINTER = ITEMS.registerSimpleItem("heartwood_splinter");
    /** The Canopy Breaker's face, used only as the icon of its advancements. */
    public static final DeferredItem<Item> CANOPY_BREAKER_PORTRAIT = ITEMS.registerSimpleItem("canopy_breaker_portrait",
            () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<Item> BARK_MACAQUE_SPAWN_EGG = ITEMS.registerItem("bark_macaque_spawn_egg", SpawnEggItem::new,
            () -> new Item.Properties().spawnEgg(ModEntities.BARK_MACAQUE.get()));
    public static final DeferredItem<Item> CANOPY_BREAKER_SPAWN_EGG = ITEMS.registerItem("canopy_breaker_spawn_egg", SpawnEggItem::new,
            () -> new Item.Properties().spawnEgg(ModEntities.CANOPY_BREAKER.get()));
    public static final DeferredItem<Item> FROSTBOUND_THREAD = ITEMS.registerSimpleItem("frostbound_thread");
    public static final DeferredItem<Item> SOLSTICE_SEAL = ITEMS.registerSimpleItem("solstice_seal",
            () -> new Item.Properties().stacksTo(16));

    public static final DeferredItem<Item> CRIMSON_COMMANDER_SPAWN_EGG = ITEMS.registerItem("crimson_commander_spawn_egg", SpawnEggItem::new,
            () -> new Item.Properties().spawnEgg(ModEntities.CRIMSON_COMMANDER.get()));
    public static final DeferredItem<Item> CRIMSON_ASH_VANGUARD_SPAWN_EGG = ITEMS.registerItem("crimson_ash_vanguard_spawn_egg", SpawnEggItem::new,
            () -> new Item.Properties().spawnEgg(ModEntities.CRIMSON_ASH_VANGUARD.get()));
    public static final DeferredItem<Item> CRIMSON_PLATE_SHARD = ITEMS.registerSimpleItem("crimson_plate_shard");
    /** Calls the Crimson Commander on deepslate bricks; also part of his greatsword recipe. */
    public static final DeferredItem<Item> STAINED_OATH_CREST = ITEMS.registerItem("stained_oath_crest", StainedOathCrestItem::new,
            properties -> properties.stacksTo(16).rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> CRIMSON_OATH_GREATSWORD = ITEMS.registerItem("crimson_oath_greatsword", CrimsonOathGreatswordItem::new,
            properties -> properties.sword(CRIMSON_OATH, 5.0F, -3.0F).rarity(Rarity.EPIC).fireResistant()
                    .component(DataComponents.LORE, new ItemLore(List.of(
                            Component.translatable("item.codenamecoraline.crimson_oath_greatsword.lore1").withStyle(ChatFormatting.RED),
                            Component.translatable("item.codenamecoraline.crimson_oath_greatsword.lore2").withStyle(ChatFormatting.GRAY)))));
    /** The Crimson Commander helm, used only as the icon of his story advancements. */
    public static final DeferredItem<Item> CRIMSON_COMMANDER_PORTRAIT = ITEMS.registerSimpleItem("crimson_commander_portrait",
            () -> new Item.Properties().stacksTo(1));

    public static final DeferredItem<Item> TEN_THREADS_WITCH_SPAWN_EGG = ITEMS.registerItem("ten_threads_witch_spawn_egg", SpawnEggItem::new,
            () -> new Item.Properties().spawnEgg(ModEntities.TEN_THREADS_WITCH.get()));
    /** Summons the Witch of Ten Threads on a solid block in the old forests. */
    public static final DeferredItem<Item> CURSED_SPINDLE = ITEMS.registerItem("cursed_spindle", CursedSpindleItem::new,
            properties -> properties.stacksTo(16).rarity(Rarity.UNCOMMON));
    /** What a defeated puppet leaves: repairs the staff, a trophy and decorative ingredient. */
    public static final DeferredItem<Item> PUPPET_REMNANT = ITEMS.registerSimpleItem("puppet_remnant");
    public static final DeferredItem<Item> MATRIARCH_THIMBLE = ITEMS.registerSimpleItem("matriarch_thimble",
            () -> new Item.Properties().rarity(Rarity.RARE));
    public static final DeferredItem<Item> TEN_THREADS_STAFF = ITEMS.registerItem("ten_threads_staff", TenThreadsStaffItem::new,
            properties -> properties.sword(TEN_THREADS, 4.0F, -2.8F).rarity(Rarity.EPIC)
                    .component(DataComponents.LORE, new ItemLore(List.of(
                            Component.translatable("item.codenamecoraline.ten_threads_staff.lore1").withStyle(ChatFormatting.LIGHT_PURPLE),
                            Component.translatable("item.codenamecoraline.ten_threads_staff.lore2").withStyle(ChatFormatting.GRAY)))));
    /** The Matriarch's face, used only as the icon of her advancements. */
    public static final DeferredItem<Item> TEN_THREADS_WITCH_PORTRAIT = ITEMS.registerSimpleItem("ten_threads_witch_portrait",
            () -> new Item.Properties().stacksTo(1));

    public static final DeferredItem<Item> BABY_DRAGON_SPAWN_EGG = ITEMS.registerItem("baby_dragon_spawn_egg", SpawnEggItem::new,
            () -> new Item.Properties().spawnEgg(ModEntities.BABY_DRAGON.get()));
    /** Render-only sprite for the companion's ember. */
    public static final DeferredItem<Item> DRAGON_EMBER = ITEMS.registerSimpleItem("dragon_ember");

    public static final DeferredItem<Item> FEY_RAVEN_SPAWN_EGG = ITEMS.registerItem("fey_raven_spawn_egg", SpawnEggItem::new,
            () -> new Item.Properties().spawnEgg(ModEntities.FEY_RAVEN.get()));

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
