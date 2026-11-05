package com.codenamecoraline.entity.babydragon;

import com.codenamecoraline.CodenameCoraline;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.Permissions;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/** An explicit single entity selector avoids touching a different nearby companion. */
@EventBusSubscriber(modid = CodenameCoraline.MOD_ID)
public final class DragonCommands {
    private static final SimpleCommandExceptionType ERROR = new SimpleCommandExceptionType(Component.translatable("command.codenamecoraline.dragon.invalid"));
    @SubscribeEvent public static void register(RegisterCommandsEvent event) {
        var target = Commands.argument("dragon", EntityArgument.entity());
        var stage = Commands.literal("stage");
        for (DragonStage s : DragonStage.values()) stage.then(Commands.literal(s.name().toLowerCase(java.util.Locale.ROOT)).executes(ctx -> {
            BabyDragon dragon = selected(ctx);
            if (!dragon.setDragonStage(s, false)) throw ERROR.create();
            ctx.getSource().sendSuccess(() -> Component.translatable("command.codenamecoraline.dragon.stage", s.title()), true); return 1;
        }));
        var order = Commands.literal("command");
        for (BabyDragon.Command c : BabyDragon.Command.values()) order.then(Commands.literal(c.name().toLowerCase(java.util.Locale.ROOT)).executes(ctx -> {
            BabyDragon dragon = selected(ctx);
            if (!dragon.issueCommand(c, null)) throw ERROR.create();
            return info(ctx);
        }));
        target.then(stage).then(order).then(Commands.literal("xp").then(Commands.literal("add").then(Commands.argument("amount", IntegerArgumentType.integer(1, 2000)).executes(ctx -> {
            selected(ctx).addDragonXp(IntegerArgumentType.getInteger(ctx, "amount")); return info(ctx);
        })))).then(Commands.literal("info").executes(DragonCommands::info));
        event.getDispatcher().register(Commands.literal("dragon").requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)).then(target));
    }
    private static BabyDragon selected(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        var entity = EntityArgument.getEntity(ctx, "dragon");
        if (!(entity instanceof BabyDragon dragon) || !dragon.isAlive() || dragon.level() != ctx.getSource().getLevel()) throw ERROR.create();
        return dragon;
    }
    private static int info(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        BabyDragon d = selected(ctx);
        ctx.getSource().sendSuccess(() -> Component.translatable("command.codenamecoraline.dragon.info", d.getName(), d.getUUID().toString(), d.getDragonStage().title(), d.getDragonXp(), (int)d.getHealth(), (int)d.getMaxHealth()), false); return 1;
    }
    private DragonCommands() {}
}
