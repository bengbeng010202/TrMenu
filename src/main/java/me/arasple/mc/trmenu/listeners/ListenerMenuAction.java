package me.arasple.mc.trmenu.listeners;

import io.izzel.taboolib.module.inject.TListener;
import me.arasple.mc.trmenu.TrMenu;
import me.arasple.mc.trmenu.actions.BaseAction;
import me.arasple.mc.trmenu.actions.ext.IconActionDealy;
import me.arasple.mc.trmenu.data.ArgsCache;
import me.arasple.mc.trmenu.display.Button;
import me.arasple.mc.trmenu.inv.Menur;
import me.arasple.mc.trmenu.inv.MenurHolder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * @author Arasple
 * @date 2019/10/4 14:02
 */
@TListener
public class ListenerMenuAction implements Listener {

    private HashMap<UUID, Long> clickTimes = new HashMap<>();

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onClick(InventoryClickEvent e) {
        if (!(e.getInventory().getHolder() instanceof MenurHolder)) {
            return;
        }

        Player p = (Player) e.getWhoClicked();
        Menur menu = ((MenurHolder) e.getInventory().getHolder()).getMenu();
        Button button = menu.getButton(e.getRawSlot());

        // 防刷屏点击
        clickTimes.putIfAbsent(p.getUniqueId(), 0L);
        if (System.currentTimeMillis() - clickTimes.get(p.getUniqueId()) < TrMenu.getSettings().getLong("OPTIONS.ANTI-CLICK-SPAM")) {
            e.setCancelled(true);
            return;
        } else {
            clickTimes.put(p.getUniqueId(), System.currentTimeMillis());
        }

        if (button == null) {
            if (p.getOpenInventory().getTopInventory().getHolder() instanceof MenurHolder && menu.isLockPlayerInv()) {
                e.setCancelled(true);
            }
            return;
        } else {
            e.setCancelled(true);
        }

        // 执行相关动作
        List<BaseAction> actions = button.getCurrentIcon().getactions().getOrDefault(e.getClick(), new ArrayList<>());
        if (actions != null && !actions.isEmpty()) {
            for (BaseAction action : actions) {
                if (action instanceof IconActionDealy) {
                    long delay = ((IconActionDealy) action).getDelay();
                    if (delay > 0) {
                        Bukkit.getScheduler().runTaskLater(TrMenu.getPlugin(), () -> {

                        }, delay);
                    }
                    break;
                }
                action.onExecute(p, e, ArgsCache.getPlayerArgs(p));
            }
        }
        // 刷新图标优先级
        button.refreshConditionalIcon(p, e, e.getClick());
    }

}
