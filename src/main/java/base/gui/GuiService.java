package base.gui;

import base.Game;
import base.gameobjects.Animal;
import base.graphicsservice.ImageLoader;
import base.graphicsservice.Rectangle;
import base.graphicsservice.Sprite;
import base.map.Tile;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import static base.constants.ColorConstant.*;
import static base.constants.Constants.*;
import static base.constants.FilePath.CANCEL_BUTTON_PATH;
import static base.constants.FilePath.OK_BUTTON_PATH;
import static base.constants.MultiOptionalObjects.MASTER_TILE_LIST;

public class GuiService implements Serializable {

    public static final int BACKPACK_ROWS = 5;
    public static final int BACKPACK_COLUMNS = 5;

    public GUI loadYourAnimals(Game game) {
        List<Animal> animalsOnAllMaps = new ArrayList<>();
        for (List<Animal> animalsOnMaps : game.getAnimalsOnMaps().values()) {
            animalsOnAllMaps.addAll(animalsOnMaps);
        }
        List<GUIButton> buttons = new CopyOnWriteArrayList<>();

        for (int i = 0; i < animalsOnAllMaps.size(); i++) {
            Animal animal = animalsOnAllMaps.get(i);
            Sprite animalSprite = animal.getPreviewSprite();
            Rectangle tileRectangle = new Rectangle(game.getWidth() - (CELL_SIZE + TILE_SIZE), i * (CELL_SIZE + 2), CELL_SIZE, CELL_SIZE);
            buttons.add(new AnimalIcon(game, animal, animalSprite, tileRectangle));
        }
        return new GUI(buttons, 5, 5, true);
    }

    public GUI loadPossibleAnimalsPanel(Game game, Map<String, Sprite> previews) {
        List<GUIButton> buttons = new ArrayList<>();

        int i = 0;
        for (Map.Entry<String, Sprite> entry : previews.entrySet()) {
            Sprite animalSprite = entry.getValue();
            Rectangle tileRectangle = new Rectangle(i * (CELL_SIZE + 2), 0, CELL_SIZE, CELL_SIZE);  //horizontal on top left
            buttons.add(new NewAnimalButton(entry.getKey(), animalSprite, tileRectangle));
            i++;
        }
        Rectangle tileRectangle = new Rectangle((previews.size()) * (CELL_SIZE + 2), 0, CELL_SIZE, CELL_SIZE);  //one more horizontal on top left
        buttons.add(new NewAnimalButton("", null, tileRectangle));
        game.changeAnimal("");

        return new GUI(buttons, 5, 5, true);
    }

    public GUI loadPlantsPanel(Game game, Map<String, Sprite> previews) {
        List<GUIButton> buttons = new ArrayList<>();

        int i = 0;
        for (Map.Entry<String, Sprite> entry : previews.entrySet()) {
            Sprite previewSprite = entry.getValue();
            Rectangle tileRectangle = new Rectangle(i * (CELL_SIZE + 2), 0, CELL_SIZE, CELL_SIZE);
            buttons.add(new PlantButton(entry.getKey(), previewSprite, tileRectangle));
            i++;
        }
        Rectangle oneMoreTileRectangle = new Rectangle((previews.size()) * (CELL_SIZE + 2), 0, CELL_SIZE, CELL_SIZE);
        buttons.add(new PlantButton("", null, oneMoreTileRectangle));
        game.changeSelectedPlant("");

        return new GUI(buttons, 5, 5, true);
    }

    public GUI[] loadTerrainGui(List<Tile> tiles, int buttonCountPerRow) {
        GUI[] terrainButtonsArray = new GUI[11];

        List<GUIButton> buttons = new ArrayList<>();
        int tileId = 0;
        int buttonNumberInRow = 0;
        int buttonsTotal = 0;
        int rows = 0;
        for (Tile tile : tiles) {
            if (!isLastTile(tiles, tileId) && !tile.isVisibleInMenu()) {
                tileId++;
                continue;
            }
            if (isLastTile(tiles, tileId)) {
                if (tile.isVisibleInMenu()) {
                    Rectangle tileRectangle = new Rectangle(buttonNumberInRow * (CELL_SIZE + 2), 0, CELL_SIZE, CELL_SIZE);
                    buttons.add(new SDKButton(tileId, tile.getSprite(), tileRectangle));
                    buttonNumberInRow++;
                }
                createEmptyButton(buttons, buttonNumberInRow - 1);
                terrainButtonsArray[rows] = new GUI(buttons, 5, 5, true);
                return terrainButtonsArray;
            }
            Rectangle tileRectangle = new Rectangle(buttonNumberInRow * (CELL_SIZE + 2), 0, CELL_SIZE, CELL_SIZE);
            buttons.add(new SDKButton(tileId, tile.getSprite(), tileRectangle));
            if (buttonsTotal != 0 && buttonNumberInRow == buttonCountPerRow) {
                createEmptyButton(buttons, buttonNumberInRow);
                terrainButtonsArray[rows] = new GUI(buttons, 5, 5, true);
                rows++;
                buttons = new ArrayList<>();
                buttonNumberInRow = -1;
            }
            buttonsTotal++;
            tileId++;
            buttonNumberInRow++;
        }
        return terrainButtonsArray;
    }

    private void createEmptyButton(List<GUIButton> buttons, int buttonNumberInRow) {
        Rectangle oneMoreTileRectangle = new Rectangle((buttonNumberInRow + 1) * (CELL_SIZE + 2), 0, CELL_SIZE, CELL_SIZE);
        buttons.add(new SDKButton(-1, null, oneMoreTileRectangle));
    }

    private boolean isLastTile(List<Tile> tiles, int tileId) {
        return tileId == tiles.size() - 1;
    }

    public Backpack loadEmptyBackpack(Game game) {
        List<GUIButton> buttons = new ArrayList<>();
        for (int i = 0; i < BACKPACK_ROWS; i++) {
            for (int j = 0; j < BACKPACK_COLUMNS; j++) {
                Rectangle buttonRectangle = new Rectangle(j * (CELL_SIZE + 2), i * (CELL_SIZE + 2), CELL_SIZE, CELL_SIZE);
                buttons.add(new BackpackButton(String.valueOf(i) + j, null, buttonRectangle, String.valueOf(i) + j));
            }
        }
        return new Backpack(buttons, 5, game.getHeight() - ((BACKPACK_ROWS + 1) * (CELL_SIZE + 2)), 0);
    }

    public void decreaseNumberOnButton(Game game, BackpackButton button) {
        if (button == null) {
            return;
        }
        button.setObjectCount(button.getObjectCount() - 1);
        if (button.getObjectCount() <= 0) {
            button.makeEmpty();
            game.changeSelectedItem(button.getDefaultId());
        }
    }

    public int getNextId(int id) {

        for (List<Integer> list : MASTER_TILE_LIST) {
            if (list.contains(id)) {
                if (list.indexOf(id) == list.size() - 1) {
                    return list.get(0);
                }
                return list.get(list.indexOf(id) + 1);
            }
        }
        return id;
    }

    public DialogBox loadDialogBox() {
        Sprite okSprite = ImageLoader.getPreviewSprite(OK_BUTTON_PATH);
        Sprite cancelSprite = ImageLoader.getPreviewSprite(CANCEL_BUTTON_PATH);
        Rectangle rectangle = new Rectangle();
        defineDialogBoxSize(rectangle);
        OkButton okButton = new OkButton(okSprite, rectangle);
        CancelButton cancelButton = new CancelButton(cancelSprite, rectangle);
        List<GUIButton> buttons = new ArrayList<>();
        buttons.add(okButton);
        buttons.add(cancelButton);
        return new DialogBox(buttons, rectangle);
    }

    // TODO: not perfect, test on different screens
    private void defineDialogBoxSize(Rectangle rectangle) {
        int width = 300;
        int height = 60;
        int x = MAX_SCREEN_WIDTH / 2 - (width / 2 * ZOOM);
        int y = MAX_SCREEN_HEIGHT - (180 + height);

        rectangle.setX(x);
        rectangle.setY(y);
        rectangle.setWidth(width);
        rectangle.setHeight(height);
    }

    public List<GUIButton> getEscMenuButtons() {
        List<GUIButton> buttons = new ArrayList<>();

        EscMenuButton button1 = new EscMenuButton(null, new Rectangle(0, 0, 200, 60), true, SOFT_PINK, "Game Tips");
        buttons.add(button1);

        EscMenuButton button2 = new EscMenuButton(null, new Rectangle(200, 0, 200, 60), true, PALE_GREEN, "Skills");
        buttons.add(button2);

        EscMenuButton button3 = new EscMenuButton(null, new Rectangle(400, 0, 200, 60), true, MUTED_LAVENDER, "Settings");
        buttons.add(button3);

        EscMenuButton button4 = new EscMenuButton(null, new Rectangle(600, 0, 200, 60), true, CREAMY_PEACH, "Exit Game");
        buttons.add(button4);

        return buttons;
    }

}
