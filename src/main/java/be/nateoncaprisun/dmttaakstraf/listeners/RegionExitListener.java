package be.nateoncaprisun.dmttaakstraf.listeners;

import be.nateoncaprisun.dmttaakstraf.DMTTaakstraf;
import be.nateoncaprisun.dmttaakstraf.database.SQLManager;
import be.nateoncaprisun.dmttaakstraf.utils.Utils;
import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class RegionExitListener implements Listener {
    private DMTTaakstraf main;
    public RegionExitListener(DMTTaakstraf main){
        this.main = main;
        Bukkit.getPluginManager().registerEvents(this, main);
    }

    @EventHandler
    public void regionExitEvent(PlayerMoveEvent event){
        Player player = event.getPlayer();
        SQLManager sqlManager = main.getSqlManager();

        if (event.getFrom().getZ() == event.getTo().getZ() && event.getFrom().getX() == event.getTo().getX()) return;
        /*
        Set<ProtectedRegion> regions = (Set<ProtectedRegion>) WorldGuardPlugin.inst().getRegionManager(player.getWorld()).getApplicableRegions(player.getLocation()).getRegions().stream().filter(region -> (region.getPriority() >= 0)).collect(Collectors.toSet());

        ProtectedRegion region = regions.iterator().next();
         */
        //if (!event.getRegion().getId().equals(main.getConfig().getString("Taakstraf-Region"))) return;
        CompletableFuture<Integer> taakstrafFuture = sqlManager.getTaakstrafPlayer(player.getUniqueId());
        taakstrafFuture.thenAccept(taakstraf -> {
            if (taakstraf == 0 || taakstraf < 0) return;
            List<String> regionsPlayer = new ArrayList<>();
            if (WGBukkit.getRegionManager(player.getWorld()).getApplicableRegions(player.getLocation()) == null){
                Location location = new Location(player.getWorld() ,DMTTaakstraf.getInstance().getConfig().getInt("Spawn.X"), DMTTaakstraf.getInstance().getConfig().getInt("Spawn.Y"), DMTTaakstraf.getInstance().getConfig().getInt("Spawn.Z"));
                if (location == new Location(player.getWorld(), 0, 0, 0) || location == null){
                    player.sendMessage(Utils.color("&cError 1001: Neem zo snel mogelijk contact op met een developer of stafflid!"));
                    return;
                }
                player.teleport(location);
            }
            for(ProtectedRegion r : WGBukkit.getRegionManager(player.getWorld()).getApplicableRegions(player.getLocation())) {
                regionsPlayer.add(r.getId());
            }
            if (regionsPlayer.contains(main.getConfig().getString("Taakstraf-Region"))) return;
            Location location = new Location(player.getWorld() ,DMTTaakstraf.getInstance().getConfig().getInt("Spawn.X"), DMTTaakstraf.getInstance().getConfig().getInt("Spawn.Y"), DMTTaakstraf.getInstance().getConfig().getInt("Spawn.Z"));
            if (location == new Location(player.getWorld(), 0, 0, 0) || location == null){
                player.sendMessage(Utils.color("&cError 1001: Neem zo snel mogelijk contact op met een developer of stafflid!"));
                return;
            }
            player.teleport(location);

        });
    }

}
