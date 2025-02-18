package fr.ph1lou.werewolfplugin.tasks;

import fr.ph1lou.werewolfplugin.Main;
import fr.ph1lou.werewolfplugin.game.GameManager;
import fr.ph1lou.werewolfapi.player.utils.Formatter;
import fr.ph1lou.werewolfapi.player.interfaces.IPlayerWW;
import fr.ph1lou.werewolfapi.enums.Prefix;
import fr.ph1lou.werewolfapi.enums.Sound;
import fr.ph1lou.werewolfapi.enums.StateGame;
import fr.ph1lou.werewolfapi.enums.TimerBase;
import fr.ph1lou.werewolfapi.events.ActionBarEvent;
import fr.ph1lou.werewolfapi.events.game.day_cycle.DayEvent;
import fr.ph1lou.werewolfapi.utils.BukkitUtils;
import fr.ph1lou.werewolfapi.utils.Utils;
import fr.ph1lou.werewolfapi.versions.VersionUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;


public class TransportationTask implements Listener {

    private String actionBar="";
    private final GameManager game;
    private final List<Location> spawns = new ArrayList<>();
    private final Map<Integer, Integer> taskStep = new HashMap<>();

    public TransportationTask(GameManager game) {
        this.game = game;
        step0();
    }


    private void step0() {

        World world = this.game.getMapManager().getWorld();
        AtomicInteger i = new AtomicInteger();
        taskIdManager(0, BukkitUtils.scheduleSyncRepeatingTask(() -> {

            if (this.game.isState(StateGame.END)) {
                kill(0);
                HandlerList.unregisterAll(this);
                return;
            }

            if (i.get() == this.game.getPlayersCount()) {
                kill(0);
                step1();

                return;
            }
            initStructure(world, i.getAndIncrement());

        }, 0, 5));

    }


    private void step1() {

        AtomicInteger i = new AtomicInteger();

        taskIdManager(1, BukkitUtils.scheduleSyncRepeatingTask(() -> {

            if (this.game.isState(StateGame.END)) {
                kill(1);
                HandlerList.unregisterAll(this);
                return;
            }

            if (i.get() == this.game.getPlayersCount()) {
                kill(1);
                HandlerList.unregisterAll(this);
                step2();
                return;
            }
            teleportPlayer(i.getAndIncrement());

        }, 0, 5));

    }

    private void step2() {

        AtomicInteger i = new AtomicInteger(10);

        taskIdManager(2, BukkitUtils.scheduleSyncRepeatingTask(() -> {

            if (this.game.isState(StateGame.END)) {
                kill(2);
                return;
            }

            if (i.get() == 0) {
                kill(2);
                step3();
                return;
            }
            for (Player p : Bukkit.getOnlinePlayers()) {
                VersionUtils.getVersionUtils().sendTitle(p, "Start", "§b" + i.get(), 25, 20, 25);
                Sound.NOTE_PIANO.play(p);
            }
            i.getAndDecrement();

        }, 0, 20));


    }

    private void step3() {

        taskIdManager(3, BukkitUtils.scheduleSyncRepeatingTask(() -> {

            if (this.game.isState(StateGame.END)) {
                kill(3);
                return;
            }
            if (this.spawns.isEmpty()) {
                kill(3);
                step4();
                return;
            }
            createStructure(Material.AIR, spawns.get(0));
            this.spawns.remove(0);

        }, 0, 1));

    }


    private void step4() {

        for (Player player : Bukkit.getOnlinePlayers()) {

            if (this.game.getPlayerWW(player.getUniqueId()).isPresent()) {
                player.setGameMode(GameMode.SURVIVAL);
                player.sendMessage(this.game.translate(Prefix.YELLOW.getKey() , "werewolf.announcement.start.message",
                        Formatter.timer(Utils.conversion(this.game.getConfig().getTimerValue(TimerBase.INVULNERABILITY.getKey())))));
            } else {
                player.teleport(this.game.getMapManager().getWorld().getSpawnLocation());
                player.setGameMode(GameMode.SPECTATOR);
                if(game.getConfig().getSpectatorMode() < 2){
                    player.kickPlayer(game.translate(Prefix.RED.getKey() , "werewolf.check.spectator_disabled"));
                }
            }

            VersionUtils.getVersionUtils().sendTitle(player, this.game.translate("werewolf.announcement.start.top_title"), this.game.translate("werewolf.announcement.start.bot_title"), 20, 20, 20);
            Sound.NOTE_BASS.play(player);
        }
        this.game.getModerationManager().getQueue().clear();
        this.game.setState(StateGame.START);
        GameTask start = new GameTask(this.game);
        start.runTaskTimer(JavaPlugin.getPlugin(Main.class), 0, 20);
        BukkitUtils.scheduleSyncDelayedTask(() -> {
            if (!this.game.isState(StateGame.END)) {
                Bukkit.getPluginManager().callEvent(new DayEvent(1));
            }

        }, 20);
    }

    private void teleportPlayer(int i) {

        Bukkit.getOnlinePlayers()
                .forEach(Sound.ORB_PICKUP::play);

        actionBar = this.game.translate("werewolf.action_bar.tp",
                Formatter.number(i + 1),
                Formatter.format("&sum&",this.game.getPlayersCount()));

        IPlayerWW playerWW = (IPlayerWW) this.game.getPlayersWW().toArray()[i];
        playerWW.setSpawn(spawns.get(i));
        Player player = Bukkit.getPlayer(playerWW.getUUID());

        if (player != null) {
            player.setCompassTarget(playerWW.getSpawn());
            player.setGameMode(GameMode.ADVENTURE);
            this.game.clearPlayer(player);
            Inventory inventory = player.getInventory();

            for (int j = 0; j < 40; j++) {
                inventory.setItem(j, this.game.getStuffs().getStartLoot().getItem(j));
            }
        } else {
            for (int j = 0; j < 40; j++) {
                playerWW.addItem(this.game.getStuffs().getStartLoot().getItem(j));
            }
        }

        playerWW.teleport(spawns.get(i));
    }

    private void taskIdManager(int step, int taskId) {
        taskStep.put(step, taskId);
    }

    private void kill(int step) {
        Bukkit.getScheduler().cancelTask(taskStep.get(step));
    }

    private void initStructure(World world, int i) {

        WorldBorder wb = world.getWorldBorder();

        for (Player p : Bukkit.getOnlinePlayers()) {
            Sound.DIG_GRASS.play(p);
        }

        actionBar = this.game.translate("werewolf.action_bar.create_tp_point",
                Formatter.number(i + 1),
                Formatter.format("&sum&",this.game.getPlayersCount()));

        double a = i * 2 * Math.PI / this.game.getPlayersCount();
        int x = (int) (Math.round(wb.getSize() / 3 * Math.cos(a) + world.getSpawnLocation().getX()));
        int z = (int) (Math.round(wb.getSize() / 3 * Math.sin(a) + world.getSpawnLocation().getZ()));
        Location spawn = new Location(world, x, world.getHighestBlockYAt(x, z) + 100, z);
        world.getChunkAt(x, z).load(true);
        spawns.add(spawn);
        createStructure(Material.BARRIER, spawn);
    }


    private void createStructure(Material m, Location location) {

        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();
        World world = this.game.getMapManager().getWorld();
        for (int i = -2; i < 3; i++) {
            for (int j = -2; j < 3; j++) {
                if (Math.abs(j) == 2 || Math.abs(i) == 2) {
                    for (int k = 0; k < 2; k++) {
                        new Location(world, x + i, y - 1 + k, z + j).getBlock().setType(m);
                    }
                }
                new Location(world, x + i, y - 2, z + j).getBlock().setType(m);
                new Location(world, x + i, y + 2, z + j).getBlock().setType(m);
            }
        }
    }

    @EventHandler
    public void onActionBar(ActionBarEvent event) {
        event.setActionBar(event.getActionBar() + this.actionBar);
    }


}
