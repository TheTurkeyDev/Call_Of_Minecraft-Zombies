package com.theprogrammingturkey.comz.listeners;

import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.game.actions.BaseAction;
import com.theprogrammingturkey.comz.util.CommandUtil;
import org.bukkit.Bukkit;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerChatListener implements Listener {

  @EventHandler(ignoreCancelled = true)
  public void onPlayerChat(final @NotNull AsyncPlayerChatEvent playerChat) {
    final COMZombies plugin = COMZombies.getPlugin();
    final Player player = playerChat.getPlayer();
    final String message = playerChat.getMessage().replaceFirst(" ", "").trim();

    if (message.equalsIgnoreCase("cancel")) {
      final BaseAction action = plugin.activeActions.remove(player);
      if (action != null) {
        if (playerChat.isAsynchronous()) {
          COMZombies.scheduleTask(action::cancelAction);
        } else {
          action.cancelAction();
        }
        playerChat.setCancelled(true);
      }
      return;
    }

    final BaseAction action = plugin.activeActions.get(player);
    if (action != null) {
      if (playerChat.isAsynchronous()) {
        COMZombies.scheduleTask(() -> action.onChatMessage(message));
      } else {
        action.onChatMessage(message);
      }
      playerChat.setCancelled(true);
    }

    if (message.equalsIgnoreCase("done")) {
      final Sign sign = plugin.isEditingASign.remove(player);
      if (sign != null) {
        CommandUtil.sendMessageToPlayer(player, "You are No longer editing a sign");
        playerChat.setCancelled(true);
      }
    }
  }
}
