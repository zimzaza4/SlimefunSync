package me.zimzaza4.slimefun.slimefunsync.utils;

import io.github.thebusybiscuit.slimefun4.api.player.PlayerProfile;
import io.github.thebusybiscuit.slimefun4.api.researches.Research;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import me.zimzaza4.slimefun.slimefunsync.SlimefunSync;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class MySQLUtil {
    public static void setPlayerData(Player player, Set<Research> researchSet) throws SQLException {
        String result = "";
        List<String> researches = new ArrayList<>();
        for (Research s : researchSet) {
            String res = s.toString().replace("Research (", "").replace(")", "");
            researches.add(res);
        }
        result = researches.toString().replace("]", "").replace("[", "");
        PreparedStatement preparedStatement = null;
        String cmd = "REPLACE INTO researches(uuid, research) VALUES(?, ?)";
        preparedStatement = SlimefunSync.database.getConnection().prepareStatement(cmd);
        preparedStatement.setString(1, player.getUniqueId().toString());
        preparedStatement.setString(2, result);
        preparedStatement.executeUpdate();
    }

    public static List<Research> getPlayerData(Player player) throws SQLException {
        PreparedStatement preparedStatement = SlimefunSync.database.getConnection().prepareStatement("SELECT * FROM `researches` WHERE `uuid` = ?");
        preparedStatement.setString(1, player.getUniqueId().toString());
        ResultSet result = preparedStatement.executeQuery();
        if (result != null) {
            while (result.next()) {
                String r = result.getString("research");
                String[] res = r.split(", ");
                List<Research> list = new ArrayList<>();
                for (String s : res) {
                    Optional<Research> optionalRes = getResearchFromString(s);
                    optionalRes.ifPresent(list::add);
                }
                return list;

            }

        }
        return null;
    }


    @Nonnull
    public static Optional<Research> getResearchFromString(@Nonnull String input) {
        if (!input.contains(":")) {
            return Optional.empty();
        }

        for (Research research : Slimefun.getRegistry().getResearches()) {
            if (research.getKey().toString().equalsIgnoreCase(input)) {
                return Optional.of(research);
            }
        }

        return Optional.empty();
    }

}
