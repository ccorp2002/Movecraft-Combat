package net.countercraft.movecraft.combat.movecraftcombat.listener;

import net.countercraft.movecraft.craft.CraftManager;
import org.jetbrains.annotations.NotNull;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.util.Vector;
import org.bukkit.inventory.ItemStack;
import org.bukkit.block.Dispenser;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.utils.MathUtils;
import net.countercraft.movecraft.combat.movecraftcombat.MovecraftCombat;
import net.countercraft.movecraft.combat.movecraftcombat.tracking.TNTTracking;
import net.countercraft.movecraft.combat.movecraftcombat.status.StatusManager;
import net.countercraft.movecraft.combat.movecraftcombat.config.Config;


public class DispenseListener implements Listener {
    @EventHandler
    public void dispenseEvent(BlockDispenseEvent e) {
        if(!Config.EnableTNTTracking)
            return;
        if(e.getBlock().getType() != Material.DISPENSER)
            return;
        if(e.getItem().getType() != Material.TNT)
            return;

        // Cancel dispense event
        e.setCancelled(true);

        // Subtract item yourself
        Dispenser d = (Dispenser) e.getBlock().getState();
        if(!subtractItem(d, e.getItem()))
            return;

        // Spawn TNT
        Location l = e.getVelocity().toLocation(d.getLocation().getWorld());
        TNTPrimed tnt = (TNTPrimed) l.getWorld().spawnEntity(l, EntityType.PRIMED_TNT);
        Vector velocity = getTNTVector();
        tnt.setVelocity(velocity);

        if(Config.Debug)
            MovecraftCombat.getInstance().getLogger().info("Spawned custom TNT!: " + l + ", " + velocity);

        for(Player p : Bukkit.getServer().getOnlinePlayers()) {
            p.playSound(l, Sound.ENTITY_TNT_PRIMED, 1.5f, 1.5f);
        }

        // Find nearest craft
        Craft craft = CraftManager.getInstance().fastNearestCraftToLoc(e.getBlock().getLocation());
        if(craft == null)
            return;
        if(!craft.getHitBox().contains(MathUtils.bukkit2MovecraftLoc(e.getBlock().getLocation())))
            return;

        // Report to tracking
        TNTTracking.getInstance().dispensedTNT(craft, tnt);
        StatusManager.getInstance().registerEvent(craft.getNotificationPlayer());
    }

    @NotNull
    private Vector getTNTVector() {
        Vector v = new Vector(0, 0.2, 0);
        double angle = Math.random() * 2 * Math.PI;
        v.setX(0.02 * Math.cos(angle));
        v.setZ(0.02 * Math.sin(angle));
        return v;
    }

    private boolean subtractItem(@NotNull Dispenser d, @NotNull ItemStack item) {
        for(int i = 0; i < d.getInventory().getSize(); i++) {
            ItemStack temp = d.getInventory().getItem(i);
            if(temp == null || !temp.isSimilar(item))
                continue;

            int count = temp.getAmount();

            if(count == 1) { // Dispensers with 2 items are weird, handle carefully!  Also item counts are 1 below what they should be...
                count -= 2;
                temp.setAmount(count);
                Bukkit.getScheduler().runTask(MovecraftCombat.getInstance(), () -> {
                    if(d.getLocation().getBlock().getType() != Material.DISPENSER)
                        return;
                    d.getInventory().addItem(item);
                });
                return true;
            }
            else if (count >= 0) {
                count -= 1;
                temp.setAmount(count);
                return true;
            }
        }
        return false;
    }
}
