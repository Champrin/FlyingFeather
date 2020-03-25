package xyz.champrin.flyingfeather;

import cn.nukkit.Player;
import cn.nukkit.PlayerFood;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.player.PlayerItemHeldEvent;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.math.Vector3;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.scheduler.NukkitRunnable;
import cn.nukkit.utils.Config;

import java.io.File;
import java.util.*;

@SuppressWarnings({"unused", "unchecked"})
public class FlyingFeather extends PluginBase implements Listener {

    public final String PLUGIN_NAME = "FlyingFeather";
    public final String PLUGIN_No = "19";
    public final String PREFIX = "FlyingFeather";
    public final String GAME_NAME = "";

    public ArrayList<Player> farter = new ArrayList<>();
    public ArrayList<String> worlds = new ArrayList<>();
    public double POWER;
    public Config config;
    public int expend;

    @Override
    public void onEnable() {
        long start = new Date().getTime();
        this.getServer().getPluginManager().registerEvents(this, this);
        this.getLogger().info(PREFIX + "  §d加载中。。。§e|作者：Champrin");
        this.getLogger().info(PREFIX + "  §e ==> Champrin的第§c" + PLUGIN_No + "§e款插件/小游戏 " + GAME_NAME + "！");

        if (!new File(this.getDataFolder() + "/config.yml").exists()) {
            this.saveResource("config.yml", false);
        }
        this.config = new Config(this.getDataFolder() + "/config.yml", Config.YAML);
        this.worlds.addAll((Collection<? extends String>) this.config.get("worlds"));
        this.POWER = (double) this.config.get("飞行强度");

        if (this.getConfig().get("飞行一次的消耗") == null){
            this.config.set("恢复CD",20);
            this.config.set("一个CD恢复的值",2);
            this.config.set("飞行一次的消耗",2);
            this.config.save();
            this.expend = this.getConfig().getInt("飞行一次的消耗");
        }
        int restore = (int)this.config.get("一个CD恢复的值");
        int tick = (int)this.config.get("恢复CD");
        new NukkitRunnable() {
            @Override
            public void run() {
                getServer().getOnlinePlayers().values().stream().filter(Player::isSurvival).map(Player::getFoodData).forEach((food) -> {
                    int level = food.getLevel();
                    int max = food.getMaxLevel();
                    if (level != max) {
                        level += restore;
                        if (level > max) {
                            level = max;
                        }
                        food.setLevel(level);
                        food.sendFoodLevel();
                    }
                });
            }
        }.runTaskTimerAsynchronously(this, 0, tick);


        this.getLogger().info(PREFIX + "  §d已加载完毕。。。");
        this.getLogger().info(PREFIX + "  §e加载耗时" + (new Date().getTime() - start) + "毫秒");
    }

    @EventHandler
    public void onFall(EntityDamageEvent event){
        Entity entity=event.getEntity();
        if (entity instanceof Player)
        {
            if (!farter.contains(entity)) return;
            if (event.getCause() == EntityDamageEvent.DamageCause.FALL){
                event.setCancelled(true);
                farter.remove(entity);
            }
        }
    }
    @EventHandler
    public void onJoin(PlayerJoinEvent event){
        int a = 0;
        for (Map.Entry<Integer, Item> map :event.getPlayer().getInventory().getContents().entrySet())
        {
            if(map.getValue().getCustomName().equals("§f>>§l§e飞行羽毛§r§f<<"))
            {
                a=a+1;
                break;
            }
        }
        if (a == 0 ){
            addFlyingFeather(event.getPlayer());
        }

    }

    public void addFlyingFeather(Player player) {
        Item item = Item.get(288, 0, 1);
        item.setCustomName("§f>>§l§e飞行羽毛§r§f<<");
        item.getNamedTag().putString("FlyingFeather","copr.cai");
        player.getInventory().addItem(item);
    }
    @EventHandler
    public void onHand(PlayerItemHeldEvent event){
        Player player = event.getPlayer();
        Level level = player.getLevel();
        if (!worlds.contains(level.getFolderName())) return;
        if (event.getItem().getCustomName().equals("§f>>§l§e飞行羽毛§r§f<<")) {
            PlayerFood food = player.getFoodData();
            int foodLevel = food.getLevel();
            if (foodLevel <= 0) {
                event.setCancelled();
            } else {
                foodLevel -= expend;
                if (foodLevel < 0) {
                    foodLevel = 0;
                }
                food.setLevel(foodLevel);
                food.sendFoodLevel();
                farter.add(player);
                Vector3 v3 = player.getDirectionVector();
                player.setMotion(v3.multiply(POWER));
            }
        }
    }

}
