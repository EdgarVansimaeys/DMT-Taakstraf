package be.nateoncaprisun.dmttaakstraf.commands;

import be.nateoncaprisun.dmttaakstraf.DMTTaakstraf;
import be.nateoncaprisun.dmttaakstraf.database.PlayerData;
import be.nateoncaprisun.dmttaakstraf.database.SQLManager;
import be.nateoncaprisun.dmttaakstraf.utils.Utils;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import co.aikar.commands.bukkit.contexts.OnlinePlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@CommandAlias("taakstraf|tk")
@CommandPermission("taakstraf.admin")
public class TaakstrafCommand extends BaseCommand {

    SQLManager sqlManager = SQLManager.getInstance();

    @Default
    @Subcommand("help")
    public void help(Player player){
        player.sendMessage(Utils.color("&b&m------------------------"));
        player.sendMessage(" ");
        player.sendMessage(Utils.color("&b/taakstraf &3get [player]"));
        player.sendMessage(Utils.color("&b/taakstraf &3setspawn"));
        player.sendMessage(Utils.color("&b/taakstraf &3set [player] [aantal]"));
        player.sendMessage(Utils.color("&b/taakstraf &3add [player] [aantal]"));
        player.sendMessage(Utils.color("&b/taakstraf &3remove [player] [aantal]"));
        player.sendMessage(Utils.color("&b/taakstraf &3setloc [player] [aantal]"));
        player.sendMessage(Utils.color("&b/taakstraf &3create [player]"));
        player.sendMessage(" ");
        player.sendMessage(Utils.color("&b&m------------------------"));
    }

    @Subcommand("setspawn")
    public void setSpawn(Player player){
        Set<ProtectedRegion> regions = (Set<ProtectedRegion>) WorldGuardPlugin.inst().getRegionManager(player.getWorld()).getApplicableRegions(player.getLocation()).getRegions().stream().filter(region -> (region.getPriority() >= 0)).collect(Collectors.toSet());
        if (regions.size() != 1) {
            player.sendMessage(Utils.color("&cJe moet in de taakstraf region staan!"));
            return;
        }
        ProtectedRegion region = regions.iterator().next();
        if (!DMTTaakstraf.getInstance().checkTaakstraf(region)){
            player.sendMessage(Utils.color("&cJe moet in de taakstraf region staan!"));
            return;
        }

        DMTTaakstraf.getInstance().getConfig().set("Spawn.X", player.getLocation().getX());
        DMTTaakstraf.getInstance().getConfig().set("Spawn.Y", player.getLocation().getY());
        DMTTaakstraf.getInstance().getConfig().set("Spawn.Z", player.getLocation().getZ());
        DMTTaakstraf.getInstance().saveConfig();

        player.sendMessage(Utils.color("&aDe taakstraf spawn location is naar jouw locatie gezet!"));
    }

    @Subcommand("create")
    @Syntax("<player>")
    public void create(Player player, OnlinePlayer onlineTarget){
        if (onlineTarget == null){
            player.sendMessage(Utils.color("&cGeef een geldige speler op!"));
            return;
        }
        Player target = onlineTarget.getPlayer();
        sqlManager.playerExists(target.getUniqueId()).thenAccept((playerExists) -> {
            if (!playerExists){
                sqlManager.createPlayer(target.getUniqueId());
                player.sendMessage(Utils.color("&2" + target.getName() + "&a staat nu in de database!"));
            } else {
                player.sendMessage(Utils.color("&4" + target.getName() + "&c staat al in de database!"));
            }
        });
    }

    @Subcommand("set")
    @Syntax("<player> <amount>")
    public void set(Player player, OnlinePlayer onlineTarget, Integer amount){
        if (onlineTarget == null){
            player.sendMessage(Utils.color("&cGeef een geldige speler op!"));
            return;
        }
        Player target = onlineTarget.getPlayer();
        if (amount < 0){
            player.sendMessage(Utils.color("&cJe mag geen negatief getal opgeven!"));
            return;
        }
        Location location = new Location(target.getWorld() ,DMTTaakstraf.getInstance().getConfig().getInt("Spawn.X"), DMTTaakstraf.getInstance().getConfig().getInt("Spawn.Y"), DMTTaakstraf.getInstance().getConfig().getInt("Spawn.Z"));
        if (location == new Location(target.getWorld(), 0, 0, 0)){
            player.sendMessage(Utils.color("&cEr is nog geen spawn plek aangemaakt voor de taakstraf!"));
            return;
        }
        CompletableFuture<Integer> taakstrafFuture = sqlManager.getTaakstrafPlayer(target.getUniqueId());
        taakstrafFuture.thenAccept(taakstraf -> {
            if (amount == taakstraf){
                player.sendMessage(Utils.color("&cDe taakstraf van &4" +target.getName()+" &cis al &4" + amount));
                return;
            }
            if (amount == 0){
                player.sendMessage(Utils.color("&aJe hebt &2" + target.getName() + "&a zijn taakstraf gereset"));
                sqlManager.updateTaakstraf(target.getUniqueId(), amount);
                return;
            }
            sqlManager.updateTaakstraf(target.getUniqueId(), amount);
            target.sendMessage(Utils.color("&cJe hebt een taakstraf van &4" + amount +"&c gekregen!"));
            player.sendMessage(Utils.color("&aJe hebt &2" + target.getName() + "&a een taakstraf gegeven van &2" + amount));
            Set<ProtectedRegion> regions = (Set<ProtectedRegion>) WorldGuardPlugin.inst().getRegionManager(player.getWorld()).getApplicableRegions(player.getLocation()).getRegions().stream().filter(region -> (region.getPriority() >= 0)).collect(Collectors.toSet());
            if (regions.size() != 1) {
                player.teleport(location);
                return;
            }
            ProtectedRegion region = regions.iterator().next();
            if (!DMTTaakstraf.getInstance().checkTaakstraf(region)){
                player.teleport(location);
            }
        });
    }

    @Subcommand("get")
    @Syntax("<player>")
    public void get(Player player, OnlinePlayer playerTarget){
        if (playerTarget == null){
            player.sendMessage(Utils.color("&cGeef een geldige speler op!"));
            return;
        }
        Player target = playerTarget.getPlayer();
        if (!DMTTaakstraf.getInstance().existsRegion(target.getWorld())){
            player.sendMessage(Utils.color("&cEr bestaat nog geen taakstraf region, dus de taakstraf is niet toegeken!"));
            return;
        }
        sqlManager.playerExists(target.getUniqueId()).thenAccept((playerExists) -> {
            if (!playerExists){
                sqlManager.createPlayer(target.getUniqueId());
                player.sendMessage(Utils.color("&2" + target.getName() + "&a heeft een taakstraf van &20"));
            } else {
                CompletableFuture<Integer> taakstrafFuture = sqlManager.getTaakstrafPlayer(target.getUniqueId());
                taakstrafFuture.thenAccept(taakstraf -> {
                    player.sendMessage(Utils.color("&2" + target.getName() + "&a heeft een taakstraf van &2" + taakstraf));
                });
            }
        });
    }

    @Subcommand("add")
    @Syntax("<player> <amount>")
    public void add(Player player, OnlinePlayer target, int amount){

        if (amount <= 0){
            player.sendMessage(Utils.color("&cJe moet een positief getal opgeven!"));
            return;
        }
        CompletableFuture<Integer> taakstrafFuture = sqlManager.getTaakstrafPlayer(target.getPlayer().getUniqueId());
        taakstrafFuture.thenAccept(taakstraf -> {
            sqlManager.updateTaakstraf(target.getPlayer().getUniqueId(), amount+taakstraf);
            player.sendMessage(Utils.color("&aJe hebt &2"+target.getPlayer().getName()+" " + amount + "&a extra taakstraf gegeven!"));
            target.getPlayer().sendMessage(Utils.color("&cJe hebt &4"+amount+ " &ctaakstraf gekregen!"));
        });
    }
    @Subcommand("remove")
    @Syntax("<player> <amount>")
    public void remove(Player player, OnlinePlayer target, int amount){

        if (amount <= 0){
            player.sendMessage(Utils.color("&cJe moet een positief getal opgeven!"));
            return;
        }
        CompletableFuture<Integer> taakstrafFuture = sqlManager.getTaakstrafPlayer(target.getPlayer().getUniqueId());
        taakstrafFuture.thenAccept(taakstraf -> {
            if (taakstraf < amount){
                player.sendMessage(Utils.color("&cDe opgegeven hoeveelheid mag niet groter zijn dan de daadwerkelijke taakstraf!"));
                return;
            }
            sqlManager.updateTaakstraf(target.getPlayer().getUniqueId(), taakstraf- amount);
            player.sendMessage(Utils.color("&aJe hebt &2"+target.getPlayer().getName()+"&a, &2" + amount + "&a taakstraf minder gegeven!"));
            target.getPlayer().sendMessage(Utils.color("&aJe taakstraf is verminderd met &2"+amount+ "!"));
        });
    }

}
