package skylands.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import skylands.logic.IslandStuck;
import skylands.logic.Skylands;
import skylands.util.SkylandsTexts;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class DeleteCommand {

	static void init(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(literal("sl").then(literal("delete").requires(Permissions.require("skylands.delete", true)).executes(context -> {
			var player = context.getSource().getPlayer();
			if(player != null) DeleteCommand.warn(player);
			return 1;
		}).then(argument("confirmation", word()).executes(context -> {
			var player = context.getSource().getPlayer();
			String confirmWord = StringArgumentType.getString(context, "confirmation");
			if(player != null) DeleteCommand.run(player, confirmWord);
			return 1;
		}))));
	}

	static void run(ServerPlayerEntity player, String confirmWord) {

		if(confirmWord.equals("CONFIRM")) {
			IslandStuck islands = Skylands.instance.islands;

			islands.get(player).ifPresentOrElse(island -> {
				var created = island.created;
				var now = Instant.now();
				var seconds = ChronoUnit.SECONDS.between(created, now);

				if(seconds >= Skylands.config.islandDeletionCooldown || Permissions.check(player, "skylands.delete.bypass", 2)) {
					islands.delete(player);
					player.sendMessage(SkylandsTexts.prefixed("message.skylands.island_delete.success"));
				}
				else {
					player.sendMessage(SkylandsTexts.prefixed("message.skylands.island_delete.too_often"));
				}

			}, () -> {
				player.sendMessage(SkylandsTexts.prefixed("message.skylands.island_delete.fail"));
			});
		}
		else {
			player.sendMessage(SkylandsTexts.prefixed("message.skylands.island_delete.warning"));
		}
	}

	static void warn(ServerPlayerEntity player) {
		IslandStuck islands = Skylands.instance.islands;

		islands.get(player).ifPresentOrElse(island -> {
			var created = island.created;
			var now = Instant.now();
			var seconds = ChronoUnit.SECONDS.between(created, now);

			if(seconds >= Skylands.config.islandDeletionCooldown || Permissions.check(player, "skylands.delete.bypass", 2)) {
				player.sendMessage(SkylandsTexts.prefixed("message.skylands.island_delete.warning"));
			}
			else {
				player.sendMessage(SkylandsTexts.prefixed("message.skylands.island_delete.too_often"));
			}

		}, () -> {
			player.sendMessage(SkylandsTexts.prefixed("message.skylands.island_delete.fail"));
		});
	}
}
