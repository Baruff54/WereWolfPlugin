package fr.ph1lou.werewolfplugin.roles.villagers;


import fr.minuskube.inv.ClickableItem;
import fr.ph1lou.werewolfapi.role.utils.DescriptionBuilder;
import fr.ph1lou.werewolfapi.player.utils.Formatter;
import fr.ph1lou.werewolfapi.game.IConfiguration;
import fr.ph1lou.werewolfapi.player.interfaces.IPlayerWW;
import fr.ph1lou.werewolfapi.game.WereWolfAPI;
import fr.ph1lou.werewolfapi.enums.Aura;
import fr.ph1lou.werewolfapi.enums.Prefix;
import fr.ph1lou.werewolfapi.enums.StatePlayer;
import fr.ph1lou.werewolfapi.enums.TimerBase;
import fr.ph1lou.werewolfapi.enums.UniversalMaterial;
import fr.ph1lou.werewolfapi.events.UpdatePlayerNameTagEvent;
import fr.ph1lou.werewolfapi.events.game.day_cycle.DayEvent;
import fr.ph1lou.werewolfapi.events.game.game_cycle.UpdateCompositionEvent;
import fr.ph1lou.werewolfapi.events.game.life_cycle.AnnouncementDeathEvent;
import fr.ph1lou.werewolfapi.events.game.life_cycle.FinalDeathEvent;
import fr.ph1lou.werewolfapi.role.interfaces.IAffectedPlayers;
import fr.ph1lou.werewolfapi.role.impl.RoleWithLimitedSelectionDuration;
import fr.ph1lou.werewolfapi.utils.ItemBuilder;
import fr.ph1lou.werewolfapi.utils.Utils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Priestess extends RoleWithLimitedSelectionDuration implements IAffectedPlayers {

    private final List<IPlayerWW> affectedPlayer = new ArrayList<>();

    public Priestess(WereWolfAPI main, IPlayerWW playerWW, String key) {
        super(main, playerWW, key);

        setPower(false);
    }

    @Override
    public void addAffectedPlayer(IPlayerWW playerWW) {
        this.affectedPlayer.add(playerWW);
    }

    @Override
    public void removeAffectedPlayer(IPlayerWW playerWW) {
        this.affectedPlayer.remove(playerWW);
    }

    @Override
    public void clearAffectedPlayer() {
        this.affectedPlayer.clear();
    }

    @Override
    public List<IPlayerWW> getAffectedPlayers() {
        return (this.affectedPlayer);
    }


    @EventHandler
    public void onDeath(FinalDeathEvent event) {

        if (!this.affectedPlayer.contains(event.getPlayerWW())) return;

        if (!event.getPlayerWW().getRole().isWereWolf()) return;

        this.getPlayerWW().sendMessageWithKey(Prefix.GREEN.getKey() , "werewolf.role.priestess.werewolf_death");

        this.affectedPlayer.remove(event.getPlayerWW());

        this.getPlayerWW().addPlayerMaxHealth(2);
    }

    @EventHandler
    public void onDay(DayEvent event) {

        if (!this.getPlayerWW().isState(StatePlayer.ALIVE)) {
            return;
        }

        setPower(true);

        this.getPlayerWW().sendMessageWithKey(
                Prefix.YELLOW.getKey() , "werewolf.role.priestess.perform",
                Formatter.number(game.getConfig().getDistancePriestess()),
                Formatter.timer(Utils.conversion(
                        game.getConfig()
                                .getTimerValue(TimerBase.POWER_DURATION.getKey()))));
    }


    @Override
    public @NotNull String getDescription() {

        return new DescriptionBuilder(game, this)
                .setDescription(game.translate("werewolf.role.priestess.description"))
                .setItems(game.translate("werewolf.role.priestess.items"))
                .build();
    }


    @Override
    public void recoverPower() {

    }

    @Override
    public Aura getAura() {
        return Aura.LIGHT;
    }


    @EventHandler(priority = EventPriority.HIGH)
    public void sendDeathMessage(AnnouncementDeathEvent event) {

        if (event.getTargetPlayer().equals(this.getPlayerWW())) {
            return; //la prêtresse voit les vrais rôles
        }

        if (event.getTargetPlayer().equals(event.getPlayerWW())) {
            return; //le mort voit son vrai rôle
        }

        IPlayerWW playerWW = event.getTargetPlayer();

        if (playerWW.getRole().isNeutral()) {
            if (this.getPlayerWW().isState(StatePlayer.ALIVE) && game.getRandom().nextFloat() > 0.95) {
                event.setRole("werewolf.role.priestess.magic");
            }
        } else if (game.getRandom().nextFloat() < 0.8) {

            if (this.getPlayerWW().isState(StatePlayer.ALIVE)) {
                if(playerWW.getRole().isWereWolf()){
                    event.setRole("werewolf.role.priestess.magic");
                }

            }
            else{
                if(!playerWW.getRole().isWereWolf()){
                    event.setRole("werewolf.role.priestess.magic");
                }
            }
        } else {
            if (this.getPlayerWW().isState(StatePlayer.ALIVE)) {
                if(!playerWW.getRole().isWereWolf()){
                    event.setRole("werewolf.role.priestess.magic");
                }
            }
            else{
                if(playerWW.getRole().isWereWolf()){
                    event.setRole("werewolf.role.priestess.magic");
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCompositionUpdate(UpdateCompositionEvent event) {
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onUpdate(UpdatePlayerNameTagEvent event) {

        IPlayerWW playerWW = game.getPlayerWW(event.getPlayerUUID()).orElse(null);

        if (playerWW == null) {
            return;
        }

        if (!playerWW.isState(StatePlayer.DEATH)) return;

        event.setSuffix("");
    }

    public static ClickableItem config(WereWolfAPI game) {

        List<String> lore = Arrays.asList(game.translate("werewolf.menu.left"),
                game.translate("werewolf.menu.right"));
        IConfiguration config = game.getConfig();

        return ClickableItem.of((
                new ItemBuilder(UniversalMaterial.BLUE_WOOL.getStack())
                        .setDisplayName(game.translate("werewolf.menu.advanced_tool.priestess",
                                        Formatter.number(config.getDistancePriestess())))
                        .setLore(lore).build()), e -> {

            if (e.isLeftClick()) {
                config.setDistancePriestess((config.getDistancePriestess() + 2));
            } else if (config.getDistancePriestess() - 2 > 0) {
                config.setDistancePriestess(config.getDistancePriestess() - 2);
            }


            e.setCurrentItem(new ItemBuilder(e.getCurrentItem())
                    .setLore(lore)
                    .setDisplayName(game.translate("werewolf.menu.advanced_tool.priestess",
                                    Formatter.number(config.getDistancePriestess())))
                    .build());

        });
    }
}
