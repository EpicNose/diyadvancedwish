package me.twomillions.plugin.advancedwish.utils;

import org.bukkit.entity.Player;

/**
 * A utility for managing player experience. Slightly modified by 2000000.
 * by <a href="https://gist.github.com/Jikoo/30ec040443a4701b8980">Jikoo</a>
 */
public final class ExpUtils {
    /**
     * 计算玩家总经验
     *
     * @param player player
     * @return player exp
     *
     * @see <a href=http://minecraft.gamepedia.com/Experience#Leveling_up>Experience#Leveling_up</a>
     */
    public static int getExp(Player player) {
        return levelToExp(player.getLevel()) + Math.round(getNextLevelExp(player.getLevel()) * player.getExp());
    }

    /**
     * Total experience =
     *  level2 + 6 × level (at levels 0–16)
     *  2.5 × level2 – 40.5 × level + 360 (at levels 17–31)
     *  4.5 × level2 – 162.5 × level + 2220 (at levels 32+)
     *
     * @param level level
     * @return Total experience
     *
     * @see <a href=http://minecraft.gamepedia.com/Experience#Leveling_up>Experience#Leveling_up</a>
     */
    public static int levelToExp(int level) {
        if (level <= 16) return level * level + 6 * level;
        if (level <= 31) return (int) (2.5 * level * level - 40.5 * level + 360);
        return (int) (4.5 * level * level - 162.5 * level + 2220);
    }

    /**
     * Calculate level (including progress to next level) based on total experience.
     *
     * @param exp the total experience
     * @return the level calculated
     */
    public static double getLevelFromExp(long exp) {
        int level = expToLevel(exp);

        float remainder = exp - (float) levelToExp(level);

        float progress = remainder / getNextLevelExp(level);

        return ((double) level) + progress;
    }

    /**
     * exp tp level
     *
     * @param exp exp
     * @return level
     *
     * @see <a href=http://minecraft.gamepedia.com/Experience#Leveling_up>Experience#Leveling_up</a>
     */
    public static int expToLevel(long exp) {
        if (exp > 1508) return (int) ((Math.sqrt(72 * exp - 54215) + 325) / 18);
        if (exp > 353) return (int) (Math.sqrt(40 * exp - 7839) / 10 + 8.1);
        return (int) (Math.sqrt(exp + 9) - 3);
    }

    /**
     * Experience required =
     *  2 × current_level + 7 (for levels 0–15)
     *  5 × current_level – 38 (for levels 16–30)
     *  9 × current_level – 158 (for levels 31+)
     *
     * @param level level
     * @return exp
     *
     * @see <a href=http://minecraft.gamepedia.com/Experience#Leveling_up>Experience#Leveling_up</a>
     */
    private static int getNextLevelExp(int level) {
        if (level >= 31) return 9 * level - 158;
        if (level >= 16) return 5 * level - 38;
        return level * 2 + 7;
    }

    /**
     * 添加玩家经验
     *
     * <p>这种方法优于 {@link Player#giveExp(int)}
     * 在旧版本中，该方法不考虑每个等级的 exp 差异。这会导致在给予玩家大量经验时过度升级
     * 在新版本中，虽然每个级别的经验量不同，但使用的方法是循环繁重的，需要大量的计算，这使得它非常缓慢
     *
     * @param player player
     * @param exp the amount of experience to add or remove
     */
    public static void addExp(Player player, int exp) {
        exp += getExp(player);

        if (exp < 0) exp = 0;

        double levelAndExp = getLevelFromExp(exp);
        int level = (int) levelAndExp;

        player.setLevel(level);
        player.setExp((float) (levelAndExp - level));
    }
}
