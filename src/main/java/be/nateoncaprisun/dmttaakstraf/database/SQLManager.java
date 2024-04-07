package be.nateoncaprisun.dmttaakstraf.database;

import be.nateoncaprisun.dmttaakstraf.DMTTaakstraf;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import org.bukkit.Bukkit;

import java.sql.*;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SQLManager {

    private static SQLManager instance;
    private ExecutorService executorService;

    public static SQLManager getInstance() {
        if(instance == null) instance = new SQLManager();
        return instance;
    }

    private @Getter HikariDataSource hikari;

    public void init(String ip, int port, String databaseName, String user, String password) {
        HikariConfig hikariConfig = new HikariConfig();

        try {
            Class.forName("com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
            hikariConfig.setDataSourceClassName("com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
        } catch (ClassNotFoundException e) {
            hikariConfig.setDataSourceClassName("com.mysql.cj.jdbc.MysqlDataSource");
        }
        hikariConfig.addDataSourceProperty("serverName", ip);
        hikariConfig.addDataSourceProperty("port", port);
        hikariConfig.addDataSourceProperty("databaseName", databaseName);
        hikariConfig.addDataSourceProperty("user", user);
        hikariConfig.addDataSourceProperty("password", password);
        hikariConfig.setLeakDetectionThreshold(10000L);
        hikariConfig.setMaximumPoolSize(5);
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2084");
        hikariConfig.setMaxLifetime(480000L);
        hikariConfig.setConnectionTestQuery("SELECT 1");
        hikariConfig.setPoolName(DMTTaakstraf.getInstance().getName());
        hikari = new HikariDataSource(hikariConfig);

        Boolean connected;
        try {
            if (hikari.getConnection() != null){
                connected = true;
            } else {
                connected = false;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        Bukkit.getLogger().info("Succesfully connected to the " + hikariConfig.getPoolName() + " Database." + connected );

        executorService = Executors.newCachedThreadPool();
    }

    public void createTable(String query){
        try (Connection connection = hikari.getConnection()) {
            Statement table = connection.createStatement();
            table.executeUpdate(query);
            connection.close();
            table.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public CompletableFuture<PlayerData> createPlayer(UUID uuid){
        return  CompletableFuture.supplyAsync(() -> {
            PreparedStatement statement = null;
            try {
                statement = getHikari().getConnection().prepareStatement("INSERT INTO players (uuid, taakstraf) VALUES (?, ?)");
                statement.setString(1, uuid.toString());
                statement.setInt(2, 0);
                statement.executeUpdate();
                return new PlayerData(uuid, 0);
            } catch (SQLException e){
                e.printStackTrace();
                return null;
            } finally {
                closeStatement(statement);
            }
        }, executorService);
    }

    public CompletableFuture<Boolean> playerExists(UUID uuid){
        return CompletableFuture.supplyAsync(() -> {
            PreparedStatement statement = null;
            ResultSet resultSet = null;
            try (Connection connection = hikari.getConnection()) {
                statement = connection.prepareStatement("SELECT * FROM players WHERE uuid = ?");
                statement.setString(1, uuid.toString());
                resultSet = statement.executeQuery();
                return resultSet.next();

            } catch (Exception e){
                DMTTaakstraf.getInstance().getLogger().severe(e.toString());
                return false;
            } finally {
                closeResult(resultSet);
                closeStatement(statement);
            }
        }, executorService);
    }

    public CompletableFuture<Void> updateTaakstraf(UUID uuid, Integer amount){
        return CompletableFuture.runAsync(() -> {
            PreparedStatement statement = null;
            try (Connection connection = getHikari().getConnection()){
                statement = connection.prepareStatement("UPDATE players SET taakstraf = ? WHERE uuid = ?");
                statement.setInt(1, amount);
                statement.setString(2, uuid.toString());
                statement.executeUpdate();
            } catch (SQLException e){
                e.printStackTrace();
            } finally {
                closeStatement(statement);
            }
        }, executorService);
    }

    public CompletableFuture<Integer> getTaakstrafPlayer(UUID uuid){
        return CompletableFuture.supplyAsync(() -> {
            PreparedStatement statement = null;
            ResultSet resultSet = null;

            try (Connection connection = getHikari().getConnection()){
                statement = connection.prepareStatement("SELECT taakstraf FROM players WHERE uuid = ?");
                statement.setString(1, uuid.toString());
                resultSet = statement.executeQuery();
                resultSet.next();
                return resultSet.getInt("taakstraf");
            } catch (SQLException e){
                e.printStackTrace();
            } finally {
                closeStatement(statement);
                closeResult(resultSet);
            }
            return 0;
        }, executorService);
    }
    public void closeResult(ResultSet set) {
        if (set == null) return;
        try {
            set.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void closeStatement(PreparedStatement statement) {
        if (statement == null) return;
        try {
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void shutdown() throws SQLException {
        if (hikari.getConnection() != null) {
            hikari.close();
        }
    }
}
