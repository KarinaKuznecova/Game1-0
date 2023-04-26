package base.loading;

import base.gameobjects.CookingStove;
import base.gameobjects.Fridge;
import base.map.GameMap;
import base.map.MapTile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import static base.constants.Constants.CELL_SIZE;
import static base.constants.Constants.CURRENT_GAME_VERSION;

public class MapMigrator {
    protected static final Logger logger = LoggerFactory.getLogger(MapMigrator.class);

    /**
     * =================================== check migration ======================================
     */

    public void checkMigration(GameMap gameMap) {
        if (CURRENT_GAME_VERSION.equals("1.4.2")) {
            migrateStove(gameMap);
            migrateChairs(gameMap);
            createFridgeList(gameMap);
            migrateFridges(gameMap);
        }
    }

    /**
     * =================================== 1.4.2 migration ======================================
     */

    public void migrateStove(GameMap gameMap) {
        if (gameMap.getTilesOnLayer(3) == null) {
            return;
        }
        List<MapTile> stoveTiles = gameMap.getTilesOnLayer(3).stream()
                .filter(t -> !t.isRegularTile() && CookingStove.TILE_IDS.contains(t.getId()))
                .collect(Collectors.toList());
        if (gameMap.getTilesOnLayer(2) != null) {
            stoveTiles.addAll(gameMap.getTilesOnLayer(2).stream()
                    .filter(t -> !t.isRegularTile() && CookingStove.TILE_IDS.contains(t.getId()))
                    .collect(Collectors.toList()));
        }
        for (MapTile stoveTile : stoveTiles) {
            boolean exists = false;
            for (CookingStove cookingStove : gameMap.getCookingStoves()) {
                if (cookingStove.getRectangle().getX() == stoveTile.getX() * CELL_SIZE && cookingStove.getRectangle().getY() == stoveTile.getY() * CELL_SIZE) {
                    exists = true;
                    cookingStove.setTileId(stoveTile.getId());
                    break;
                }
            }
            if (!exists) {
                logger.info("Migrating cooking stove");
                CookingStove cookingStove = new CookingStove(stoveTile.getX() * CELL_SIZE, stoveTile.getY() * CELL_SIZE, stoveTile.getId());
                gameMap.addObject(cookingStove);
            }
        }
    }

    private void migrateChairs(GameMap gameMap) {
        if (gameMap.getTilesOnLayer(2) == null) {
            return;
        }
        logger.info("Migrating chairs");
        List<Integer> chairIds = Arrays.asList(18, 19, 20, 21);
        List<MapTile> chairTiles = gameMap.getTilesOnLayer(2).stream()
                .filter(t -> t.isRegularTile() && chairIds.contains(t.getId()))
                .collect(Collectors.toList());

        chairTiles.forEach(chair -> {
            chair.setLayer(1);
            gameMap.getTilesOnLayer(2).remove(chair);
            gameMap.getTilesOnLayer(1).add(chair);
        });
    }

    private void createFridgeList(GameMap gameMap) {
        if (gameMap.getFridges() == null) {
            gameMap.setFridges(new CopyOnWriteArrayList<>());
        }
    }

    public void migrateFridges(GameMap gameMap) {
        if (gameMap.getTilesOnLayer(2) == null) {
            return;
        }
        List<MapTile> fridgeTiles = gameMap.getTilesOnLayer(2).stream()
                .filter(t -> !t.isRegularTile() && t.getId() == 137)
                .collect(Collectors.toList());
        for (MapTile fridgeTile : fridgeTiles) {
            boolean exists = false;
            for (Fridge fridge : gameMap.getFridges()) {
                if (fridge.getRectangle().getX() == fridgeTile.getX() * CELL_SIZE && fridge.getRectangle().getY() == fridgeTile.getY() * CELL_SIZE) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                logger.info("Migrating fridge");
                Fridge fridge = new Fridge(fridgeTile.getX() * CELL_SIZE, fridgeTile.getY() * CELL_SIZE);
                gameMap.addObject(fridge);
            }
        }
    }
}