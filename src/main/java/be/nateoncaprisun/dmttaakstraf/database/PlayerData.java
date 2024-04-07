package be.nateoncaprisun.dmttaakstraf.database;

import lombok.Getter;

import java.util.UUID;

public class PlayerData {

    private @Getter UUID uuid;
    private @Getter int taakstrafAmount;

    public PlayerData(UUID uuid, int taakstrafAmount){
        this.uuid = uuid;
        this.taakstrafAmount = taakstrafAmount;
    }

}
