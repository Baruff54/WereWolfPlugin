package fr.ph1lou.werewolfplugin.listeners.random_events;

import fr.ph1lou.werewolfapi.GetWereWolfAPI;
import fr.ph1lou.werewolfapi.player.interfaces.IPlayerWW;
import fr.ph1lou.werewolfapi.listeners.ListenerManager;
import fr.ph1lou.werewolfapi.game.WereWolfAPI;
import fr.ph1lou.werewolfapi.enums.Prefix;
import fr.ph1lou.werewolfapi.enums.StateGame;
import fr.ph1lou.werewolfapi.enums.StatePlayer;
import fr.ph1lou.werewolfapi.enums.TimerBase;
import fr.ph1lou.werewolfapi.events.game.timers.RepartitionEvent;
import fr.ph1lou.werewolfapi.events.random_events.SwapEvent;
import fr.ph1lou.werewolfapi.role.interfaces.IRole;
import fr.ph1lou.werewolfapi.utils.BukkitUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;

import java.util.List;
import java.util.stream.Collectors;

public class Swap extends ListenerManager {

    public Swap(GetWereWolfAPI main) {
        super(main);
    }

    @EventHandler
    public void onRepartition(RepartitionEvent event) {
        WereWolfAPI game = this.getGame();

        if (game.getConfig().getTimerValue(TimerBase.WEREWOLF_LIST.getKey()) <= 1) {
            return;
        }

        BukkitUtils.scheduleSyncDelayedTask(() -> {
            if (game.isState(StateGame.GAME)) {
                if (isRegister()) {

                    List<IPlayerWW> playerWWS = game.getPlayersWW().stream()
                            .filter(playerWW -> playerWW.isState(StatePlayer.ALIVE))
                            .collect(Collectors.toList());

                    if (playerWWS.isEmpty()) return;

                    IPlayerWW playerWW1 = playerWWS.get((int) Math.floor(game.getRandom().nextDouble() * playerWWS.size()));

                    playerWWS = game.getPlayersWW().stream()
                            .filter(playerWW -> playerWW.isState(StatePlayer.ALIVE))
                            .filter(playerWW -> !playerWW.equals(playerWW1))
                            .collect(Collectors.toList());

                    if (playerWWS.isEmpty()) return;

                    IPlayerWW playerWW2 = playerWWS.get((int) Math.floor(game.getRandom().nextDouble() * playerWWS.size()));

                    SwapEvent swapEvent = new SwapEvent(playerWW1, playerWW2);
                    Bukkit.getPluginManager().callEvent(swapEvent);

                    if (swapEvent.isCancelled()) return;

                    IRole roles1 = playerWW1.getRole();
                    IRole roles2 = playerWW2.getRole();
                    playerWW1.setRole(roles2);
                    playerWW2.setRole(roles1);
                    Bukkit.broadcastMessage(game.translate("werewolf.random_events.swap.message"));
                    register(false);
                    playerWW1.addPlayerMaxHealth(20 - playerWW1.getMaxHealth());
                    playerWW2.addPlayerMaxHealth(20 - playerWW2.getMaxHealth());
                    playerWW1.clearPotionEffects();
                    playerWW2.clearPotionEffects();
                    playerWW1.sendMessageWithKey(Prefix.RED.getKey() , "werewolf.random_events.swap.concerned");
                    playerWW2.sendMessageWithKey(Prefix.RED.getKey() , "werewolf.random_events.swap.concerned");
                    roles1.recoverPower();
                    roles2.recoverPower();
                    roles1.recoverPotionEffects();
                    roles2.recoverPotionEffects();
                    playerWW1.getLovers().forEach(iLover -> {
                        if(iLover.swap(playerWW1,playerWW2)){
                            playerWW2.addLover(iLover);
                            playerWW1.removeLover(iLover);
                        }
                    });
                    playerWW2.getLovers().forEach(iLover -> {
                        if(iLover.swap(playerWW2,playerWW1)){
                            playerWW1.addLover(iLover);
                            playerWW2.removeLover(iLover);
                        }
                    });

                }
            }
        }, (long) (game.getRandom().nextDouble() * Math.min(game.getConfig().getTimerValue(TimerBase.WEREWOLF_LIST.getKey()),
                game.getConfig().getTimerValue(TimerBase.LOVER_DURATION.getKey())) * 20) - 5);
    }

}
