package fr.ph1lou.werewolfplugin.listeners.configs;

import fr.ph1lou.werewolfapi.GetWereWolfAPI;
import fr.ph1lou.werewolfapi.player.interfaces.IPlayerWW;
import fr.ph1lou.werewolfapi.listeners.ListenerManager;
import fr.ph1lou.werewolfapi.game.WereWolfAPI;
import fr.ph1lou.werewolfapi.enums.Prefix;
import fr.ph1lou.werewolfapi.enums.StateGame;
import fr.ph1lou.werewolfapi.enums.StatePlayer;
import fr.ph1lou.werewolfapi.enums.TimerBase;
import fr.ph1lou.werewolfapi.events.game.configs.LoneWolfEvent;
import fr.ph1lou.werewolfapi.events.game.life_cycle.FinalDeathEvent;
import fr.ph1lou.werewolfapi.events.game.timers.WereWolfListEvent;
import fr.ph1lou.werewolfapi.role.interfaces.IRole;
import fr.ph1lou.werewolfapi.utils.BukkitUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;

import java.util.List;
import java.util.stream.Collectors;

public class LoneWolf extends ListenerManager {

    public LoneWolf(GetWereWolfAPI main) {
        super(main);
    }

    @EventHandler
    public void designAloneWolf(WereWolfListEvent event) {

        WereWolfAPI game = this.getGame();

        BukkitUtils.scheduleSyncDelayedTask(() -> {
            if (!game.isState(StateGame.END) && isRegister()) {
                this.designSolitary();
            }
        }, (long) (game.getRandom().nextFloat() * 3600 * 20));
    }

    private void designSolitary() {

        WereWolfAPI game = this.getGame();

        List<IRole> roleWWs = game.getPlayersWW().stream()
                .filter(playerWW -> playerWW.isState(StatePlayer.ALIVE))
                .map(IPlayerWW::getRole)
                .filter(IRole::isWereWolf)
                .collect(Collectors.toList());

        if (roleWWs.isEmpty()) return;

        IRole role = roleWWs.get((int) Math.floor(game.getRandom().nextDouble() * roleWWs.size()));

        LoneWolfEvent event = new LoneWolfEvent((role.getPlayerWW()));

        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) return;

        role.getPlayerWW().sendMessageWithKey(Prefix.RED.getKey() , "werewolf.lone_wolf.message");

        if (role.getPlayerWW().getMaxHealth() < 30) {
            role.getPlayerWW().addPlayerMaxHealth(Math.min(8, 30 - role.getPlayerWW().getMaxHealth()));
        }
        role.setSolitary(true);
        register(false);
    }

    @EventHandler
    public void onDeath(FinalDeathEvent event) {

        WereWolfAPI game = this.getGame();

        if (game.getConfig().getTimerValue(TimerBase.WEREWOLF_LIST.getKey()) > 0) return;

        designSolitary();
    }
}
