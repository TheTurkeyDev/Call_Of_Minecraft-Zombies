package com.theprogrammingturkey.comz.kits;

import com.theprogrammingturkey.comz.game.features.PerkType;
import com.theprogrammingturkey.comz.game.weapons.Weapon;

import java.util.List;
import org.jetbrains.annotations.UnmodifiableView;

public record RoundReward(int roundEnd, int points, @UnmodifiableView List<Weapon> weapons,
													@UnmodifiableView List<PerkType> perks) {

}
