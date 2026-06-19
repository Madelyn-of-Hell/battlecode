package examplefuncsplayer;

import battlecode.common.MapLocation;

public class EnemyRatKingPosition {
    public MapLocation location;
    public int id;
    public LifeStatus status;
    public enum LifeStatus {Alive, Dead}

    public EnemyRatKingPosition(MapLocation location, int id, LifeStatus status) {
        this.location = location;
        this.id = id;
        this.status = status;
    }
}
