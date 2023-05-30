package com.theprogrammingturkey.comz.game;

import com.theprogrammingturkey.comz.game.managers.PlayerWeaponManager;
import com.theprogrammingturkey.comz.game.weapons.Weapon;
import org.bukkit.entity.Player;

public class GamePlayer {
    Player player;
    PlayerWeaponManager weaponManager;
    PlayerState state;

    public GamePlayer(Player player) {
        this.player = player;
        weaponManager = new PlayerWeaponManager(player);
        state = PlayerState.IN_GAME;
    }

    public void setState(PlayerState state) {
        this.state = state;
    }

    public PlayerState getState() {
        return state;
    }

    public PlayerWeaponManager getWeaponManager() {
        return weaponManager;
    }

    public Player getPlayer() {
        return player;
    }


    public enum PlayerState {
        IN_GAME,
        SPECTATING,
        LEFT_GAME,
        IN_QUEUE
    }
}
