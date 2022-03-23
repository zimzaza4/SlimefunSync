package me.zimzaza4.slimefun.slimefunsync;

import io.github.thebusybiscuit.slimefun4.api.player.PlayerProfile;
import io.github.thebusybiscuit.slimefun4.api.researches.Research;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import me.zimzaza4.slimefun.slimefunsync.utils.BungeeChannelApi;
import me.zimzaza4.slimefun.slimefunsync.utils.MySQLUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class PlayerEventListener implements Listener {
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)

    public void onPlayerChat(AsyncPlayerChatEvent event) {
        String send = event.getFormat().replace("%1$s", event.getPlayer().getDisplayName()).replace("%2$s", event.getMessage());
        BungeeChannelApi api = BungeeChannelApi.of(SlimefunSync.getInstance());
        try {
            for (String s : SlimefunSync.servers) {
                List<String> list = api.getPlayerList(s).get();
                for (String player : list) {
                    api.sendMessage(player, send);
                }
            }

        } catch (ExecutionException | InterruptedException ignored) {

        }
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        new BukkitRunnable() {
            @Override
            public void run() {
                Optional<PlayerProfile> profile = PlayerProfile.find(event.getPlayer());

                try {
                    if (MySQLUtil.getPlayerData(event.getPlayer()) != null) {
                        Optional<PlayerProfile> pro = PlayerProfile.find(event.getPlayer());
                        for (Research research : MySQLUtil.getPlayerData(event.getPlayer())) {
                            pro.ifPresent(pr -> pr.setResearched(research, true));
                        }
                    } else {
                        profile.ifPresent(playerProfile -> {
                            try {
                                MySQLUtil.setPlayerData(event.getPlayer(), playerProfile.getResearches());
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        });
                    }

                } catch (SQLException e) {
                    profile.ifPresent(playerProfile -> {
                        try {
                            MySQLUtil.setPlayerData(event.getPlayer(), playerProfile.getResearches());
                        } catch (SQLException throwables) {
                            throwables.printStackTrace();
                        }
                    });

                }
            }
        }.runTaskLater(SlimefunSync.getInstance(), 10);

    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Optional<PlayerProfile> profile = PlayerProfile.find(event.getPlayer());
        profile.ifPresent(playerProfile -> {
            try {
                MySQLUtil.setPlayerData(event.getPlayer(), playerProfile.getResearches());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });

    }

}
