package com.theprogrammingturkey.comz.listeners;

import com.theprogrammingturkey.comz.config.ConfigManager;
import com.theprogrammingturkey.comz.economy.PointManager;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.GameManager;
import com.theprogrammingturkey.comz.util.CommandUtil;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class QualityArmoryDamageEvent implements Listener {

    @EventHandler
    public void QualityArmoryDamageEvent(EntityDamageByEntityEvent entity)
    {
        Game game = GameManager.INSTANCE.getGame(entity.getEntity());
       
        if (game != null) {
             if ((entity.getDamager() instanceof Player)) {
                Player player = (Player) entity.getDamager();
                
                if (player != null ) {

                    if (GameManager.INSTANCE.isPlayerInGame(player)) {

                        Double damageAmount = entity.getFinalDamage();

                        if (entity.getEntity() instanceof Zombie) {

                            Zombie zombie = (Zombie) entity.getEntity();


                            /*
                                The gun mod "Quality Armory" uses crossbows as the base of guns
                                Checks to see if the player is damaging from a crossbow and applies that
                                guns damage.

                                Follows all the same logic as other damage event handlers
                                    - checking for double points
                                    - setting health of zombie if not already in hash map
                                    - etc.

                             */
                            if (player.getInventory().getItemInMainHand().getType().equals(Material.CROSSBOW)) {

                                Double zombieTotalHealth;
                                if(game.spawnManager.totalHealth().containsKey(entity.getEntity()))
                                {
                                    zombieTotalHealth = game.spawnManager.totalHealth().get(entity.getEntity());
                                }
                                else
                                {
                                    game.spawnManager.setTotalHealth(entity.getEntity(), 20);
                                    zombieTotalHealth = 20D;
                                }


                                if(zombieTotalHealth >= 20)
                                {
                                    zombie.setHealth(20D);
                                    
                                    if(game.isDoublePoints()) {
                                        PointManager.addPoints(player, ConfigManager.getMainConfig().pointsOnHit * 2);
                                    } else {
                                        PointManager.addPoints(player, ConfigManager.getMainConfig().pointsOnHit);
                                    }
                                    
                                    game.spawnManager.setTotalHealth(entity.getEntity(), (int) (zombieTotalHealth - damageAmount));
                                    if(game.spawnManager.totalHealth().get(entity.getEntity()) < 20) {
                                        zombie.setHealth(game.spawnManager.totalHealth().get(entity.getEntity()));
                                    }
                                    PointManager.notifyPlayer(player);
                                }
                                else if(zombieTotalHealth < 1 || zombieTotalHealth - damageAmount <= 1)
                                {
                                    entity.setCancelled(true);
                                    OnZombiePerkDrop perkdrop = new OnZombiePerkDrop();
                                    perkdrop.perkDrop(zombie, player);
                                    zombie.remove();
                                    boolean doublePoints = game.isDoublePoints();
                                    if(doublePoints)
                                    {
                                        PointManager.addPoints(player, ConfigManager.getMainConfig().pointsOnKill * 2);
                                    }
                                    else
                                    {
                                        PointManager.addPoints(player, ConfigManager.getMainConfig().pointsOnKill);
                                    }
                                    zombie.playEffect(EntityEffect.DEATH);
                                    PointManager.notifyPlayer(player);
                                    game.spawnManager.removeEntity(zombie);
                                    game.zombieKilled(player);
                                }
                                else
                                {
                                    zombie.damage(damageAmount);
                                    if(game.isDoublePoints()) {
                                        PointManager.addPoints(player, ConfigManager.getMainConfig().pointsOnHit * 2);
                                    }
                                    else {
                                        PointManager.addPoints(player, ConfigManager.getMainConfig().pointsOnHit);
                                    }
                                    PointManager.notifyPlayer(player);
                                }
                                game.spawnManager.setTotalHealth(entity.getEntity(), (int) (zombieTotalHealth - damageAmount));
                                if(game.isInstaKill()) {
                                    zombie.remove();
                                    game.spawnManager.removeEntity(zombie);
                                }
                                for(Player pl : game.players) {
                                    pl.playSound(entity.getEntity().getLocation().add(0, 1, 0), Sound.BLOCK_STONE_STEP, 1, 1);
                                }
                            }
                        }
                    } 
                }
            }
        }
    }
}
