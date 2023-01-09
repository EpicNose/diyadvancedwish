package me.twomillions.plugin.advancedwish.manager.databases;

import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import de.leonhard.storage.Yaml;
import lombok.Getter;
import lombok.Setter;
import me.twomillions.plugin.advancedwish.enums.mongo.MongoAuthState;
import me.twomillions.plugin.advancedwish.enums.mongo.MongoConnectState;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.fusesource.jansi.Ansi;

/**
 * author:     2000000
 * project:    AdvancedWish
 * package:        me.twomillions.plugin.advancedwish.databases.manager
 * className:      MongoManager
 * date:    2023/1/8 21:23
 */
public class MongoManager {
    @Getter @Setter private volatile static MongoClient mongoClient;
    @Getter @Setter private volatile static DBCollection dbCollection;
    @Getter @Setter private volatile static MongoDatabase mongoDatabase;
    @Getter @Setter private volatile static String mongoClientUrlString;
    @Getter @Setter private volatile static MongoClientURI mongoClientUrl;

    @Getter @Setter private volatile static MongoAuthState mongoAuthState;
    @Getter @Setter private volatile static MongoConnectState mongoConnectState;

    // 设置 Mongo
    public static MongoConnectState setupMongo(Yaml yaml) {
        // Mongo 开启检查
        if (!yaml.getBoolean("USE-MONGO")) {
            setMongoConnectState(MongoConnectState.TurnOff);
            return getMongoConnectState();
        }

        // Mongo 登陆设置
        String mongoIP = yaml.getString("MONGO.IP");
        String mongoPort = yaml.getString("MONGO.PORT");
        String mongoUser = yaml.getString("MONGO.AUTH.USER");
        String mongoPassword = yaml.getString("MONGO.AUTH.PASSWORD");

        if (mongoUser.equals("") || mongoPassword.equals("")) {
            setMongoAuthState(MongoAuthState.TurnOff);
            setMongoClientUrlString("mongodb://" + mongoIP + ":" + mongoPort);
        } else {
            setMongoAuthState(MongoAuthState.UsingAuth);
            setMongoClientUrlString("mongodb://" + mongoUser + ":" + mongoPassword + "@" + mongoIP + ":" + mongoIP);

            Bukkit.getLogger().info(Ansi.ansi().fg(Ansi.Color.YELLOW).boldOff().toString() + "[Advanced Wish] " +
                    Ansi.ansi().fg(Ansi.Color.GREEN).boldOff().toString() +
                    "Advanced Wish 检查到 Mongo 开启身份验证，已设置身份验证信息!");
        }

        // Mongo 连接状态检查
        try {
            setMongoClientUrl(new MongoClientURI(getMongoClientUrlString()));
            setMongoClient(new MongoClient(getMongoClientUrl()));
            setMongoDatabase(getMongoClient().getDatabase("AdvancedWish"));

            Bukkit.getLogger().info(Ansi.ansi().fg(Ansi.Color.YELLOW).boldOff().toString() + "[Advanced Wish] " +
                    Ansi.ansi().fg(Ansi.Color.GREEN).boldOff().toString() +
                    "Advanced Wish 已成功建立与 Mongo 的连接!");

            setMongoConnectState(MongoConnectState.Connected);
        } catch (Exception exception) {
            Bukkit.getLogger().warning(Ansi.ansi().fg(Ansi.Color.YELLOW).boldOff().toString() + "[Advanced Wish] " +
                    Ansi.ansi().fg(Ansi.Color.RED).boldOff().toString() +
                    "您打开了 Mongo 数据库选项，但是 Advanced Wish 未与 Mongo 数据库正确连接，请检查 Mongo 服务状态，即将关闭服务器!");

            setMongoConnectState(MongoConnectState.CannotConnect);

            Bukkit.shutdown();
        }

        return getMongoConnectState();
    }

    // 查询玩家数据 - 若为空则设置并返回为 value
    public static String getOrDefaultPlayerGuaranteed(Player player, String foundKey, String value) {
        String playerUUID = player.getUniqueId().toString();

        MongoCollection<Document> playerGuaranteed = getMongoDatabase().getCollection("PlayerGuaranteed");

        Document playerDocument = new Document("uuid", playerUUID);
        Document foundDocument = playerGuaranteed.find(playerDocument).first();

        // 如果没有找到则插入并返回 value
        if (foundDocument == null) { playerDocument.append(foundKey, value); playerGuaranteed.insertOne(playerDocument); return value; }

        // 如果获取的值为 null，则更新并且返回 value
        String foundString = foundDocument.getString(foundKey);

        if (foundString == null) { updatePlayerGuaranteed(player, foundKey, value); foundString = value; }

        return foundString;
    }

    // 更新玩家数据
    public static void updatePlayerGuaranteed(Player player, String foundKey, String value) {
        String playerUUID = player.getUniqueId().toString();

        MongoCollection<Document> playerGuaranteed = getMongoDatabase().getCollection("PlayerGuaranteed");

        Document playerDocument = new Document("uuid", playerUUID);

        Document keyDocument = new Document(foundKey, value);
        Document updateDocument = new Document("$set", keyDocument);
        UpdateOptions updateOptions = new UpdateOptions().upsert(true);

        playerGuaranteed.updateOne(playerDocument, updateDocument, updateOptions);
    }
}
