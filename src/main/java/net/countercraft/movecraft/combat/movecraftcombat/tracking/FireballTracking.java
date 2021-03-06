package net.countercraft.movecraft.combat.movecraftcombat.tracking;

import java.util.HashMap;
import java.util.UUID;

import net.countercraft.movecraft.combat.movecraftcombat.MovecraftCombat;
import org.bukkit.metadata.FixedMetadataValue;
import org.jetbrains.annotations.NotNull;
import org.bukkit.entity.Player;
import org.bukkit.entity.Fireball;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.combat.movecraftcombat.status.StatusManager;
import net.countercraft.movecraft.combat.movecraftcombat.config.Config;


public class FireballTracking {
    private static FireballTracking instance;

    public FireballTracking() {
        instance = this;
    }

    public static FireballTracking getInstance() {
        return instance;
    }


    public void dispensedFireball(@NotNull Craft craft, @NotNull Fireball fireball) {
        if(!Config.EnableFireballTracking)
            return;

        Player sender;
        if(MovecraftCombat.getInstance().getAADirectors().hasDirector(craft)) {
            sender = MovecraftCombat.getInstance().getAADirectors().getDirector(craft);
        }
        else {
            sender = craft.getNotificationPlayer();
        }
        if(sender == null)
            return;

        fireball.setMetadata("MCC-Sender", new FixedMetadataValue(MovecraftCombat.getInstance(), sender.getUniqueId().toString()));
    }

    public void damagedCraft(@NotNull Craft craft, @NotNull Fireball fireball) {
        if(!Config.EnableFireballTracking)
            return;

        UUID sender = UUID.fromString(fireball.getMetadata("MCC-Sender").get(0).asString());
        Player cause = MovecraftCombat.getInstance().getServer().getPlayer(sender);
        if(cause == null || !cause.isOnline())
            return;

        DamageManager.getInstance().addDamageRecord(craft, cause, DamageType.FIREBALL);
        StatusManager.getInstance().registerEvent(craft.getNotificationPlayer());
    }
}
