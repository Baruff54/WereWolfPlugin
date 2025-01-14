package fr.ph1lou.werewolfplugin.commands.utilities;

import fr.ph1lou.werewolfapi.commands.ICommand;
import fr.ph1lou.werewolfapi.game.WereWolfAPI;
import fr.ph1lou.werewolfapi.enums.Prefix;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

public class CommandDoc implements ICommand {

    @Override
    public void execute(WereWolfAPI game, Player player, String[] args) {

        TextComponent textComponent1 = new TextComponent(game.translate(Prefix.ORANGE.getKey() , "werewolf.commands.doc.link"));

        textComponent1.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, game.translate("werewolf.description.doc")));

        player.spigot().sendMessage(textComponent1);
    }
}
