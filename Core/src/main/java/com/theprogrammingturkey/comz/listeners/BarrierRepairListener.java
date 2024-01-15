package com.theprogrammingturkey.comz.listeners;

import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.GameManager;
import com.theprogrammingturkey.comz.game.features.Barrier;
import com.theprogrammingturkey.comz.util.BlockUtils;
import com.theprogrammingturkey.comz.util.CommandUtil;
import java.util.HashMap;
import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.util.BoundingBox;

public class BarrierRepairListener implements Listener {

  private final HashMap<Player, Integer> repairTaskIds = new HashMap<>();

  @EventHandler
  public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
    Player player = event.getPlayer();
    if (event.isSneaking()) {
      if (repairTaskIds.containsKey(player)) {
        return;
      }

      Game game = GameManager.INSTANCE.getGame(player);
      if (game == null) {
        return;
      }
      for (Barrier barrier : game.barrierManager.getBarriers()) {
        BlockFace face = barrier.getSignFacing();
        Location signLocation = barrier.getRepairLoc();
        Location boundLocation1 = signLocation.clone()
            .add(face.getModZ(), 2, face.getModX()) // add a vector perpendicular to the sign face
            .add(Math.abs(face.getModX()), 0, Math.abs(face.getModZ())); // compensate length of one block on the direction of sign block face
        Location boundLocation2 = signLocation.clone()
            .subtract(face.getModZ(), 3, face.getModX()); // TODO: incorrect subtract

        if (BoundingBox.of(boundLocation1, boundLocation2).overlaps(player.getBoundingBox())) {
          repairTaskIds.put(player, COMZombies.scheduleTask(30, 30, () -> {
            if (barrier.getStage() != -1) {
              boolean repaired = barrier.repair(player);

              if (repaired) {
                Objects.requireNonNull(signLocation.getWorld())
                    .playSound(signLocation, Sound.BLOCK_ANVIL_LAND, 1, 4);
              } else {
                Objects.requireNonNull(signLocation.getWorld())
                    .playSound(signLocation, Sound.ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR, 1, 1.5f);
              }
            }
          }));

          break;
        }
      }
    } else {
      Integer taskId = repairTaskIds.get(player);
      if (taskId != null) {
        repairTaskIds.remove(player);
        Bukkit.getScheduler().cancelTask(taskId);
      }
    }
  }

  @EventHandler(priority = EventPriority.HIGH)
  public void onBlockBreakEvent(BlockBreakEvent event) {
    if (BlockUtils.isBarrierRepairSign(event.getBlock())) {
      Player player = event.getPlayer();
      if (GameManager.INSTANCE.isPlayerInGame(player)) {
        Game game = GameManager.INSTANCE.getGame(player);
        Sign sign = (Sign) event.getBlock().getState();
        Barrier b = game.barrierManager.getBarrierFromRepair(sign.getLocation());
        if (b != null) {
          boolean repaired = b.repair(player);
          if (repaired) {
            Objects.requireNonNull(event.getBlock().getWorld())
                .playSound(event.getBlock().getLocation(), Sound.BLOCK_ANVIL_LAND, 1, 4);
          }
          event.setCancelled(true);
        } else {
          CommandUtil.sendMessageToPlayer(player,
              "Congrats! You broke the plugin! JK its all fixed now.");
          BlockUtils.setBlockToAir(event.getBlock());
        }
      }
    }
  }
}
