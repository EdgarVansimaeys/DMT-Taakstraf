package be.nateoncaprisun.dmttaakstraf.listeners;

import be.nateoncaprisun.dmttaakstraf.DMTTaakstraf;
import be.nateoncaprisun.dmttaakstraf.database.SQLManager;
import be.nateoncaprisun.dmttaakstraf.utils.ItemBuilder;
import be.nateoncaprisun.dmttaakstraf.utils.Utils;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Random;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class TaakstrafInteractListener implements Listener {

    private DMTTaakstraf main;

    public TaakstrafInteractListener(DMTTaakstraf main){
        this.main = main;
        Bukkit.getPluginManager().registerEvents(this, main);
    }

    @EventHandler
    public void onTaakstrafEvent(PlayerInteractEvent event){
        Block block = event.getClickedBlock();
        Player player = event.getPlayer();
        SQLManager sqlManager = SQLManager.getInstance();
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (block == null || block.getType().equals(Material.AIR)) return;
        if (block.getType() != Material.valueOf(main.getConfig().getString("Taakstraf-Material"))) return;
        Set<ProtectedRegion> regions = (Set<ProtectedRegion>) WorldGuardPlugin.inst().getRegionManager(player.getWorld()).getApplicableRegions(player.getLocation()).getRegions().stream().filter(region -> (region.getPriority() >= 0)).collect(Collectors.toSet());
        if (regions.size() != 1) return;
        ProtectedRegion region = regions.iterator().next();
        if (main.getBusy().contains(block.getLocation())){
            player.sendMessage(Utils.color("&cIemand is al deze taakstraf aan het opruimen!"));
            return;
        }
        if (!main.checkTaakstraf(region)) return;
        CompletableFuture<Integer> taakstrafFuture = sqlManager.getTaakstrafPlayer(player.getUniqueId());
        taakstrafFuture.thenAccept(taakstraf -> {
            if (taakstraf == 0) return;
            openTaakstrafMenu(player);
            main.getBusy().add(block.getLocation());
        });

    }

    private void openTaakstrafMenu(Player player){
        Inventory gui = Bukkit.createInventory(null, 27, Utils.color("&aTaakstraf Menu"));

        ItemStack click = new ItemBuilder(Material.valueOf(main.getConfig().getString("Taakstraf-Material")),1 )
                .setColoredName("&aKLIK HIER")
                .addEnchant(Enchantment.DURABILITY, 1)
                .toItemStack();

        ItemStack notClick = new ItemBuilder(Material.valueOf(main.getConfig().getString("Taakstraf-Material")),1)
                .setColoredName("&cKLIK HIER NIET")
                .toItemStack();

        for (int i = 0 ; i < 27 ; i++){
            gui.setItem(i, new ItemBuilder(Material.STAINED_GLASS_PANE, 1, (byte) 15).toItemStack());
        }

        Random random = new Random();
        int random1 = random.nextInt(main.getConfig().getInt("Taakstraf-Click-Minimum"), main.getConfig().getInt("Taakstraf-Click-Maximum"));

        for (int i = 0 ; i < random1 ; i++){
            Random r = new Random();
            int rd = r.nextInt(0, 27);
            gui.setItem(rd, click);
        }

        player.openInventory(gui);

        /*
        gui.setDefaultTopClickAction(event -> {
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null) return;
            if (clicked.equals(new ItemBuilder(Material.STAINED_GLASS_PANE, 1, (byte) 15).toItemStack())) return;
            if (clicked.equals(click.getItemStack())){
                gui.updateItem(event.getSlot(), notClick);
                if (!containsClicked(event.getClickedInventory(), click)){
                    Block target = player.getTargetBlock(null, 5);
                    Bukkit.getScheduler().runTaskLater(main, () -> {
                        target.setType(Material.valueOf(main.getConfig().getString("Taakstraf-Material")));
                    }, 20L*main.getConfig().getInt("Plant-Herplaatst"));
                    gui.close(player);
                    target.setType(Material.AIR);
                }
            }
        });



        gui.setCloseGuiAction(event -> {
            Block target = player.getTargetBlock(null, 5);
            main.getBusy().remove(target.getLocation());
        });
        */

    }

}
