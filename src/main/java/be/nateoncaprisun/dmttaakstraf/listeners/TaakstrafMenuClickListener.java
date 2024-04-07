package be.nateoncaprisun.dmttaakstraf.listeners;

import be.nateoncaprisun.dmttaakstraf.DMTTaakstraf;
import be.nateoncaprisun.dmttaakstraf.database.SQLManager;
import be.nateoncaprisun.dmttaakstraf.utils.ItemBuilder;
import be.nateoncaprisun.dmttaakstraf.utils.Utils;
import dev.triumphteam.gui.guis.GuiItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.CompletableFuture;

public class TaakstrafMenuClickListener implements Listener {

    private DMTTaakstraf main;

    public TaakstrafMenuClickListener(DMTTaakstraf main){
        this.main = main;
        Bukkit.getPluginManager().registerEvents(this, main);
    }

    @EventHandler
    public void onTaakstrafClickEvent(InventoryClickEvent event){
        ItemStack click = new ItemBuilder(Material.valueOf(main.getConfig().getString("Taakstraf-Material")),1 )
                .setColoredName("&aKLIK HIER")
                .addEnchant(Enchantment.DURABILITY, 1)
                .toItemStack();

        ItemStack notClick = new ItemBuilder(Material.valueOf(main.getConfig().getString("Taakstraf-Material")),1)
                .setColoredName("&cKLIK HIER NIET")
                .toItemStack();

        Player player = (Player) event.getWhoClicked();
        InventoryView view = event.getView();
        if (!view.getTitle().equals(Utils.color("&aTaakstraf Menu"))) return;
        if (!(event.getClickedInventory() == view.getTopInventory())) return;
        event.setCancelled(true);
        Inventory gui = event.getClickedInventory();
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null) return;
        if (clicked.equals(new ItemBuilder(Material.STAINED_GLASS_PANE, 1, (byte) 15).toItemStack())) return;
        if (clicked.equals(click)){
            gui.setItem(event.getSlot(), notClick);
            if (!containsClicked(event.getClickedInventory(), click)){
                Block target = player.getTargetBlock(null, 5);
                Bukkit.getScheduler().runTaskLater(main, () -> {
                    target.setType(Material.valueOf(main.getConfig().getString("Taakstraf-Material")));
                }, 20L*main.getConfig().getInt("Plant-Herplaatst"));
                player.closeInventory();
                target.setType(Material.AIR);
                CompletableFuture<Integer> taakstrafFuture = SQLManager.getInstance().getTaakstrafPlayer(player.getUniqueId());
                taakstrafFuture.thenAccept(taakstraf -> {
                    SQLManager.getInstance().updateTaakstraf(player.getUniqueId(), taakstraf-1);
                    player.sendMessage(Utils.color("&aJe moet er nog &2" + (taakstraf-1) + "&a doen!"));
                    if (taakstraf == 1 || taakstraf < 1){
                        player.sendMessage(Utils.color("&aJe hebt je taakstraf voltooid!"));
                        Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), "spawn " +player.getName());
                    }
                });
            }
        }
    }

    @EventHandler
    public void closeTaakstrafMenu(InventoryCloseEvent event){
        Player player = (Player) event.getPlayer();
        InventoryView view = event.getView();
        if (!view.getTitle().equals(Utils.color("&aTaakstraf Menu"))) return;
        Block target = player.getTargetBlock(null, 5);
        main.getBusy().remove(target.getLocation());
        CompletableFuture<Integer> taakstrafFuture = SQLManager.getInstance().getTaakstrafPlayer(player.getUniqueId());
    }

    private boolean containsClicked(Inventory inventory, ItemStack click) {
        for (ItemStack item : inventory.getContents()) {
            if (item != null && item.equals(click)) {
                return true;
            }
        }
        return false;
    }

}
