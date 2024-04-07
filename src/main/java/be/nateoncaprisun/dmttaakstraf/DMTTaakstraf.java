package be.nateoncaprisun.dmttaakstraf;

import be.nateoncaprisun.dmttaakstraf.commands.TaakstrafCommand;
import be.nateoncaprisun.dmttaakstraf.database.SQLManager;
import be.nateoncaprisun.dmttaakstraf.listeners.PlayerJoinListener;
import be.nateoncaprisun.dmttaakstraf.listeners.RegionExitListener;
import be.nateoncaprisun.dmttaakstraf.listeners.TaakstrafInteractListener;
import be.nateoncaprisun.dmttaakstraf.listeners.TaakstrafMenuClickListener;
import co.aikar.commands.BukkitCommandManager;
import co.aikar.commands.CommandManager;
import com.sk89q.worldguard.bukkit.RegionContainer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class DMTTaakstraf extends JavaPlugin {
    private static @Getter DMTTaakstraf instance;
    private CommandManager commandManager;
    private @Getter SQLManager sqlManager;
    private @Getter List<Location> busy = new ArrayList<>();
    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        commandManager = new BukkitCommandManager(this);
        commandManager.registerCommand(new TaakstrafCommand());

        if (getConfig().getString("HOST") == null || Objects.equals(getConfig().getString("HOST"), "")) {
            Bukkit.getLogger().severe("Er is geen database info ingevuld, zet je database info in de config.yml en herstart daarna je server!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        sqlManager = SQLManager.getInstance();
        sqlManager.init(
                getConfig().getString("HOST"),
                getConfig().getInt("PORT"),
                getConfig().getString("DATABASE-NAME"),
                getConfig().getString("USERNAME"),
                getConfig().getString("PASSWORD")
        );

        sqlManager.createTable("CREATE TABLE IF NOT EXISTS players (uuid VARCHAR(36), taakstraf INTEGER);");

        new PlayerJoinListener(this);
        new RegionExitListener(this);
        new TaakstrafInteractListener(this);
        new TaakstrafMenuClickListener(this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
    public Boolean existsRegion(World world){
        RegionContainer container = WorldGuardPlugin.inst().getRegionContainer();
        RegionManager regions = container.get(world);
        if (regions != null) {
            if (regions.getRegion(getConfig().getString("Taakstraf-Region")) == null){
                return false;
            }
            return true;
        }
        return false;
    }


    public Boolean checkTaakstraf(ProtectedRegion protectedRegion){
        if (protectedRegion.getId().equals(getConfig().getString("Taakstraf-Region"))){
            return true;
        }
        return false;
    }
}
