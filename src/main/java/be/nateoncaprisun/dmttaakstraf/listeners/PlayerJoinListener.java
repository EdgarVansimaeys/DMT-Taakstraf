package be.nateoncaprisun.dmttaakstraf.listeners;

import be.nateoncaprisun.dmttaakstraf.DMTTaakstraf;
import be.nateoncaprisun.dmttaakstraf.database.SQLManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    private DMTTaakstraf main;

    public PlayerJoinListener(DMTTaakstraf main){

        this.main = main;

        Bukkit.getPluginManager().registerEvents(this, main);

    }

    @EventHandler
    public void playerJoinEvent(PlayerJoinEvent event){
        SQLManager.getInstance().playerExists(event.getPlayer().getUniqueId()).thenAccept((playerExists) -> {
            if (!playerExists){
                SQLManager.getInstance().createPlayer(event.getPlayer().getUniqueId());
            }
        });
    }

}
