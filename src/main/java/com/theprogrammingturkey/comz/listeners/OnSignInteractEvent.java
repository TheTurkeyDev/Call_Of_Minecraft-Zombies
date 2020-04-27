package com.theprogrammingturkey.comz.listeners;

import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.commands.CommandManager;
import com.theprogrammingturkey.comz.commands.CommandUtil;
import com.theprogrammingturkey.comz.config.COMZConfig;
import com.theprogrammingturkey.comz.config.ConfigManager;
import com.theprogrammingturkey.comz.economy.PointManager;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.GameManager;
import com.theprogrammingturkey.comz.game.features.Door;
import com.theprogrammingturkey.comz.game.features.PerkType;
import com.theprogrammingturkey.comz.game.features.RandomBox;
import com.theprogrammingturkey.comz.guns.Gun;
import com.theprogrammingturkey.comz.guns.GunManager;
import com.theprogrammingturkey.comz.guns.GunType;
import com.theprogrammingturkey.comz.kits.Kit;
import com.theprogrammingturkey.comz.kits.KitManager;
import com.theprogrammingturkey.comz.listeners.customEvents.PlayerPerkPurchaseEvent;
import com.theprogrammingturkey.comz.particleutilities.ParticleEffects;
import com.theprogrammingturkey.comz.util.BlockUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Random;

public class OnSignInteractEvent implements Listener
{

	@EventHandler
	public void RightClickSign(PlayerInteractEvent event)
	{
		if(event.getClickedBlock() == null)
			return;

		COMZombies plugin = COMZombies.getPlugin();

		if(BlockUtils.isSign(event.getClickedBlock().getType()))
		{
			Sign sign = (Sign) event.getClickedBlock().getState();
			if(event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getPlayer().isSneaking())
			{
				Player player = event.getPlayer();
				String Line1 = ChatColor.stripColor(sign.getLine(0));
				if(!plugin.isEditingASign.containsKey(player) && Line1.equalsIgnoreCase("[Zombies]") && !GameManager.INSTANCE.isPlayerInGame(player))
				{
					plugin.isEditingASign.put(player, sign);
					CommandUtil.sendMessageToPlayer(player, "You are now editing a sign!");
					return;
				}
			}
			if(event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_BLOCK)
			{
				if(sign.getLine(0).equalsIgnoreCase(ChatColor.RED + "[Zombies]"))
				{
					Player player = event.getPlayer();
					if(sign.getLine(1).equalsIgnoreCase(ChatColor.AQUA + "Join"))
					{
						if(GameManager.INSTANCE.isValidArena(sign.getLine(2)))
						{
							Game game = GameManager.INSTANCE.getGame(sign.getLine(2));
							if(!game.signManager.isSign(sign))
							{
								game.signManager.addSign(sign);
							}
							String[] args = new String[2];
							args[0] = "join";
							args[1] = game.getName();
							player.getLocation().getWorld().playSound(player.getLocation(), Sound.ENTITY_ZOMBIE_AMBIENT, 1, 1);
							CommandManager.INSTANCE.onRemoteCommand(player, args);
							game.signManager.updateGame();
							return;
						}
						else
						{
							CommandUtil.sendMessageToPlayer(player, ChatColor.DARK_RED + "There is no arena called " + ChatColor.GOLD + sign.getLine(2) + ChatColor.DARK_RED + "! Contact an admin to fix this issue!");
							return;
						}
					}
					else if(sign.getLine(1).equalsIgnoreCase(ChatColor.AQUA + "Spectate"))
					{
						Game g = GameManager.INSTANCE.getGame(sign.getLine(3));
						if(g == null)
						{
							CommandUtil.sendMessageToPlayer(player, ChatColor.DARK_RED + "InvalidArena!");
							return;
						}
						event.getPlayer().performCommand("zombies spec " + g.getName());
					}
					else if(!GameManager.INSTANCE.isPlayerInGame(player))
					{
						return;
					}
					Game game = GameManager.INSTANCE.getGame(player);
					if(sign.getLine(1).equalsIgnoreCase(ChatColor.AQUA + "MysteryBox"))
					{
						int points = Integer.parseInt(sign.getLine(2));
						if(game.isFireSale())
						{
							points = 10;
						}
						if(PointManager.canBuy(player, points))
						{
							RandomBox box = game.boxManager.getBox(sign.getLocation());
							if(box != null)
							{
								box.Start(player, points);
								player.getLocation().getWorld().playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1, 1);
							}
						}
						else
						{
							CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "You don't have enough points!");
						}
					}
					else if(sign.getLine(1).equalsIgnoreCase(ChatColor.AQUA + "Perk Machine"))
					{
						String perkName = sign.getLine(2);
						PerkType perk = PerkType.DEADSHOT_DAIQ;
						perk = perk.getPerkType(perkName);
						if(game.containsPower())
						{
							if(!game.isPowered())
							{
								CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "You must turn on the power first!");
								PerkType.noPower(player);
								return;
							}
						}
						if(perk == null)
						{
							CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.BOLD + "An error occured when trying to buy this perk! Leave the game and contact an admin please.");
						}
						else
						{
							int playerPoints = PointManager.getPlayersPoints(player);
							int cost;
							try
							{
								cost = Integer.parseInt(sign.getLine(3));
							} catch(NumberFormatException e)
							{
								cost = 2000;
							}
							if(playerPoints >= cost)
							{
								if(game.perkManager.getPlayersPerks().size() > 4)
								{
									CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.BOLD + "You already have four perks!");
									return;
								}
								try
								{
									if(game.perkManager.getPlayersPerks().getOrDefault(player, new ArrayList<>()).contains(perk))
									{
										CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.BOLD + "You already have " + perk + "!");
										return;
									}
								} catch(NullPointerException e)
								{
									e.printStackTrace();
								}
								if(!game.perkManager.addPerk(player, perk))
								{
									return;
								}
								plugin.getServer().getPluginManager().callEvent(new PlayerPerkPurchaseEvent(player, perk));
								CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.BOLD + "You now have " + perk.toString().toLowerCase() + "!");
								int slot = game.perkManager.getAvaliblePerkSlot(player);
								perk.initialEffect(plugin, player, perk, slot);
								if(perk.equals(PerkType.STAMIN_UP))
								{
									player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));
								}
								PointManager.takePoints(player, cost);
								PointManager.notifyPlayer(player);
							}
							else
							{
								CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.BOLD + "You do not have enough points to buy this!");
							}
						}
					}
					else if(sign.getLine(1).equalsIgnoreCase(ChatColor.AQUA + "pack-a-punch"))
					{
						if(game.containsPower())
						{
							if(!game.isPowered())
							{
								CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "You must turn on the power before You can Pack-A-punch!");
								PerkType.noPower(player);
								return;
							}
						}
						int cost = Integer.parseInt(sign.getLine(2));
						if(PointManager.canBuy(player, cost))
						{
							GunManager manager = game.getPlayersGun(player);
							if(manager.isGun())
							{
								Gun gun = manager.getGun(player.getInventory().getHeldItemSlot());
								if(gun.isPackOfPunched())
								{
									CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Your " + ChatColor.GOLD + gun.getType().name + ChatColor.RED + " is already Pack-A-Punched!");
								}
								else
								{
									CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Your " + ChatColor.GOLD + gun.getType().name + ChatColor.RED + " was Pack-A-Punched");
									player.getWorld().playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 1);
									gun.setPackOfPunch(true);
									PointManager.takePoints(player, cost);
								}
							}
							else
							{
								CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "That is not a gun!");
							}
						}
						else
						{
							GunManager manager = game.getPlayersGun(player);
							Gun gun = manager.getGun(player.getInventory().getHeldItemSlot());
							CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "You do not have enough points to Pack-A-Punch your " + gun.getType().name + "!");
						}
					}
					else if(sign.getLine(1).equalsIgnoreCase(ChatColor.AQUA + "Door"))
					{
						Door door = game.doorManager.getDoorFromSign(sign.getLocation());
						if(door == null)
						{
							CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "An error occured when trying to open this door! Leave the game an contact an admin please.");
						}
						else if(door.isOpened())
						{
							CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "This door is already open!");
						}
						else if(PointManager.getPlayerPoints(player).getPoints() < door.getCost())
						{
							CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "You don't have enough points!");
						}
						else
						{
							door.openDoor();
							door.playerDoorOpenSound();
							PointManager.takePoints(player, door.getCost());
							PointManager.notifyPlayer(player);
							CommandUtil.sendMessageToPlayer(player, ChatColor.GREEN + "Door opened!");
						}
					}
					else if(sign.getLine(1).equalsIgnoreCase(ChatColor.AQUA + "Gun"))
					{
						int Buypoints = Integer.parseInt(sign.getLine(3).substring(0, sign.getLine(3).indexOf("/") - 1).trim());
						int Refilpoints = Integer.parseInt(sign.getLine(3).substring(sign.getLine(3).indexOf("/") + 2).trim());
						GunType guntype = plugin.getGun(sign.getLine(2));
						GunManager manager = game.getPlayersGun(player);
						int slot = manager.getCorrectSlot();
						Gun gun = manager.getGun(player.getInventory().getHeldItemSlot());
						if(manager.isGun() && gun.getType().name.equalsIgnoreCase(guntype.name))
						{
							if(PointManager.canBuy(player, Refilpoints))
							{
								manager.getGun(slot).maxAmmo();
								CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.BOLD + "Filling ammo!");
								PointManager.takePoints(player, Refilpoints);
								PointManager.notifyPlayer(player);
							}
							else
							{
								CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "You don't have enough points!");
							}
						}
						else
						{
							if(PointManager.canBuy(player, Buypoints))
							{
								CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.BOLD + "You got the " + ChatColor.GOLD + "" + ChatColor.BOLD + guntype.name + ChatColor.RED + ChatColor.BOLD + "!");
								manager.removeGun(manager.getGun(slot));
								manager.addGun(new Gun(guntype, player, slot));
								player.getLocation().getWorld().playSound(player.getLocation(), Sound.ENTITY_GHAST_SHOOT, 1, 1);
								PointManager.takePoints(player, Buypoints);
								PointManager.notifyPlayer(player);
							}
							else
							{
								CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "You don't have enough points!");
							}
						}
					}
					else if(sign.getLine(1).equalsIgnoreCase(ChatColor.AQUA + "power"))
					{
						if(ConfigManager.getConfig(COMZConfig.ARENAS).getBoolean(game.getName() + ".Power"))
						{
							if(GameManager.INSTANCE.isPlayerInGame(player))
							{
								Game g = GameManager.INSTANCE.getGame(player);
								if(g.isPowered())
								{
									CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "The power is already on!");
									return;
								}
								g.turnOnPower();
								CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Power on!");
							}
						}
					}
					else if(sign.getLine(1).equalsIgnoreCase(ChatColor.AQUA + "Kit"))
					{
						Kit kit = KitManager.getKit(ChatColor.stripColor(sign.getLine(2)));
						if(player.hasPermission("zombies.admin") || player.hasPermission("zombies.kit." + kit.getName()))
						{
							KitManager.addPlayersSelectedKit(player, kit);
							CommandUtil.sendMessageToPlayer(player, ChatColor.GREEN + " You have selected the " + kit.getName() + " Kit!");
						}
						else
						{
							CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "You dont have permission to use that kit!");
						}
					}
					else if(sign.getLine(1).equalsIgnoreCase(ChatColor.AQUA + "teleporter"))
					{
						if(GameManager.INSTANCE.isPlayerInGame(player))
						{
							Game g = GameManager.INSTANCE.getGame(player);
							if(g.teleporterManager.getTeleporters().containsKey(sign.getLine(2)))
							{
								if(!(g.isPowered()))
								{
									CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "You must turn on the power first!");
									PerkType.noPower(player);
									return;
								}
								int points = Integer.parseInt(sign.getLine(3));
								if(PointManager.canBuy(player, points))
								{
									ArrayList<Location> locList = g.teleporterManager.getTeleporters().get(sign.getLine(2));
									Random r = new Random();
									Location loc = locList.get(r.nextInt(locList.size()));
									while(loc.equals(sign.getLocation()))
									{
										loc = locList.get(r.nextInt(locList.size()));
									}
									player.teleport(loc);
									player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 30, 30));
									ParticleEffects eff = ParticleEffects.MOB_SPELL;

									for(int i = 0; i < 50; i++)
									{
										for(Player pl : Bukkit.getOnlinePlayers())
										{
											try
											{
												eff.sendToPlayer(pl, player.getLocation(), (float) (Math.random()), (float) (Math.random()), (float) (Math.random()), 1, 1);
											} catch(Exception e)
											{
												e.printStackTrace();
											}
										}
									}

									PointManager.takePoints(player, points);
									PointManager.notifyPlayer(player);
								}
								else
								{
									CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "You don't have enough points!");
								}
							}
							else
							{
								CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "ERROR!");
							}
						}
					}
				}
			}
		}
	}
}