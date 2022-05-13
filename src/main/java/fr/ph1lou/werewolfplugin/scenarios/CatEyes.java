package fr.ph1lou.werewolfplugin.scenarios;

import fr.ph1lou.werewolfapi.GetWereWolfAPI;
import fr.ph1lou.werewolfapi.annotations.Scenario;
import fr.ph1lou.werewolfapi.basekeys.ScenarioBase;
import fr.ph1lou.werewolfapi.listeners.impl.ListenerManager;
import fr.ph1lou.werewolfapi.player.impl.PotionModifier;
import fr.ph1lou.werewolfapi.events.game.game_cycle.StartEvent;
import fr.ph1lou.werewolfapi.utils.BukkitUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.potion.PotionEffectType;


@Scenario(key = ScenarioBase.CAT_EYES)
public class CatEyes extends ListenerManager {


    public CatEyes(GetWereWolfAPI main) {
        super(main);
    }

    @EventHandler
    private void onStartEvent(StartEvent event) {

        this.getGame().getPlayersWW().forEach(playerWW -> playerWW.addPotionModifier(PotionModifier.add(PotionEffectType.NIGHT_VISION,"cat_eyes")));
    }

    @Override
    public void register(boolean isActive) {


        if (isActive) {
            if (!isRegister()) {
                this.getGame().getPlayersWW().forEach(playerWW -> playerWW.addPotionModifier(PotionModifier.add(PotionEffectType.NIGHT_VISION,"cat_eyes")));
                BukkitUtils.registerListener(this);
                register = true;
            }
        } else if (isRegister()) {
            register = false;
            HandlerList.unregisterAll(this);

            this.getGame().getPlayersWW()
                    .forEach(playerWW -> playerWW
                            .addPotionModifier(
                                    PotionModifier.remove(PotionEffectType.NIGHT_VISION,
                                            "cat_eyes",
                                            0)));
        }
    }
}