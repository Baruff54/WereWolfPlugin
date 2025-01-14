package fr.ph1lou.werewolfplugin.commands.roles.neutral.angel;


import fr.ph1lou.werewolfapi.player.utils.Formatter;
import fr.ph1lou.werewolfapi.commands.ICommand;
import fr.ph1lou.werewolfapi.player.interfaces.IPlayerWW;
import fr.ph1lou.werewolfapi.player.impl.PotionModifier;
import fr.ph1lou.werewolfapi.game.WereWolfAPI;
import fr.ph1lou.werewolfapi.enums.AngelForm;
import fr.ph1lou.werewolfapi.enums.Day;
import fr.ph1lou.werewolfapi.enums.Prefix;
import fr.ph1lou.werewolfapi.enums.RolesBase;
import fr.ph1lou.werewolfapi.enums.TimerBase;
import fr.ph1lou.werewolfapi.events.roles.angel.AngelChoiceEvent;
import fr.ph1lou.werewolfapi.utils.Utils;
import fr.ph1lou.werewolfplugin.roles.neutrals.Angel;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.util.UUID;

public class CommandFallenAngel implements ICommand {

    @Override
    public void execute(WereWolfAPI game, Player player, String[] args) {

        UUID uuid = player.getUniqueId();
        IPlayerWW playerWW = game.getPlayerWW(uuid).orElse(null);

        if (playerWW == null) return;

        Angel role = (Angel) playerWW.getRole();

        if (!role.isChoice(AngelForm.ANGEL)) {
            player.sendMessage(game.translate(Prefix.RED.getKey() , "werewolf.check.power"));
            return;
        }

        role.setChoice(AngelForm.FALLEN_ANGEL);
        Bukkit.getPluginManager().callEvent(new AngelChoiceEvent(playerWW, AngelForm.FALLEN_ANGEL));
        player.sendMessage(game.translate(Prefix.YELLOW.getKey() , "werewolf.role.angel.angle_choice_click",
                Formatter.format("&form&",game.translate(RolesBase.FALLEN_ANGEL.getKey())),
                Formatter.timer(Utils.conversion(game.getConfig().getTimerValue(TimerBase.ANGEL_DURATION.getKey())))));

        if (game.isDay(Day.NIGHT)) {
            playerWW.addPotionModifier(PotionModifier.remove(PotionEffectType.DAMAGE_RESISTANCE,"fallen_angel",0));

        }
    }
}
