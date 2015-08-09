/**
 * BetonQuest Classic Conversations - add-on for BetonQuest
 * Copyright (C) 2015  Jakub "Co0sh" Sapalski
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package pl.betoncraft.bqcc;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import pl.betoncraft.betonquest.BetonQuest;
import pl.betoncraft.betonquest.config.Config;
import pl.betoncraft.betonquest.conversation.Conversation;
import pl.betoncraft.betonquest.conversation.ConversationIO;
import pl.betoncraft.betonquest.utils.Debug;
import pl.betoncraft.betonquest.utils.PlayerConverter;

/**
 * Simple chat-based conversation output with modifiable output syntax
 * 
 * @author Jakub Sapalski
 */
public class ClassicConv extends JavaPlugin {

	private static ClassicConv instance;

	@Override
	public void onEnable() {
		instance = this;
		saveDefaultConfig();
		BetonQuest.getInstance().registerConversationIO("classic", ClassicConvIO.class);
		Debug.info("BetonQuest Classic Conversations add-on enabled!");
	}

	public static class ClassicConvIO implements ConversationIO, Listener {

		private int i; // counts options
		private HashMap<Integer, String> options;
		private String npcText;

		private Conversation conv;
		private Player player;
		private String npcName;

		private String npcFormat;
		private String optionFormat;
		private String answerFormat;

		public ClassicConvIO(Conversation conv, String playerID, String npcName) {
			this.conv = conv;
			this.player = PlayerConverter.getPlayer(playerID);
			this.npcName = npcName;
			npcFormat = instance.getConfig().getString("npc-prefix");
			optionFormat = instance.getConfig().getString("option-prefix");
			answerFormat = instance.getConfig().getString("answer-prefix");
			options = new HashMap<>();
			Bukkit.getPluginManager().registerEvents(this, BetonQuest.getInstance());
		}

		@Override
		public void setNPCResponse(String response) {
			this.npcText = response;
		}

		@Override
		public void addPlayerOption(String option) {
			i++;
			options.put(i, option);
		}

		@Override
		public void clear() {
			i = 0;
			options.clear();
			npcText = null;
		}

		@Override
		public void display() {
			if (npcText == null && options.isEmpty()) {
				end();
				return;
			}
			player.sendMessage(replace(npcFormat + npcText));
			for (int j = 1; j <= options.size(); j++) {
				player.sendMessage(replace(optionFormat.replace("%number%", String.valueOf(j)) + options.get(j)));
			}
		}

		@Override
		public void end() {
			HandlerList.unregisterAll(this);
		}

		@EventHandler(priority = EventPriority.LOWEST)
		public void onReply(AsyncPlayerChatEvent event) {
			if (event.isCancelled())
				return;
			if (!event.getPlayer().equals(player))
				return;
			String message = event.getMessage().trim();
			for (int i : options.keySet()) {
				if (message.equals(Integer.toString(i))) {
					player.sendMessage(replace(answerFormat + options.get(i)));
					conv.passPlayerAnswer(i);
					event.setCancelled(true);
					return;
				}
			}
			new BukkitRunnable() {
				@Override
				public void run() {
					display();
				}
			}.runTask(BetonQuest.getInstance());
		}

		@EventHandler
		public void onWalkAway(PlayerMoveEvent event) {
			if (!event.getPlayer().equals(player)) {
				return;
			}
			if (!event.getTo().getWorld().equals(conv.getLocation().getWorld()) || event.getTo().distance(conv.getLocation()) > Integer.valueOf(Config.getString("config.max_npc_distance"))) {
				if (conv.isMovementBlock()) {
					moveBack(event);
				} else {
					conv.endConversation();
				}
			}
			return;
		}

		private void moveBack(PlayerMoveEvent event) {
			if (!event.getTo().getWorld().equals(conv.getLocation().getWorld()) || event.getTo().distance(conv.getLocation()) > Integer.valueOf(Config.getString("config.max_npc_distance")) * 2) {
				event.getPlayer().teleport(conv.getLocation());
				return;
			}
			float yaw = event.getTo().getYaw();
			float pitch = event.getTo().getPitch();
			Vector vector = new Vector(conv.getLocation().getX() - event.getTo().getX(), conv.getLocation().getY() - event.getTo().getY(), conv.getLocation().getZ() - event.getTo().getZ());
			vector = vector.multiply(1 / vector.length());
			Location newLocation = event.getTo().clone();
			newLocation.add(vector);
			newLocation.setPitch(pitch);
			newLocation.setYaw(yaw);
			event.getPlayer().teleport(newLocation);
			if (Config.getString("config.notify_pullback").equalsIgnoreCase("true")) {
				Config.sendMessage(PlayerConverter.getID(event.getPlayer()), "pullback");
			}
		}
		
		private String replace(String string) {
			return string.replace("%player%", player.getName()).replace("%quester%", npcName).replace('&', '§');
		}

	}

}
