import java.util.ArrayList;
import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;
import java.util.Random;

// constants used for abstraction
interface IConst {
  int SCREEN_HEIGHT = 693;
  int SCREEN_WIDTH = 693;
  int GAMEX = 9;
  int GAMEY = 9;
  int BOX_SIZE = SCREEN_HEIGHT / Math.max(GAMEX, GAMEY);
  int MINES = 10;
  Color LBLUE = new Color(153, 204, 255);
  Color color1 = Color.BLUE;
  Color color2 = Color.GREEN;
  Color color3 = Color.PINK;
  Color color4 = Color.MAGENTA;
  Color color5 = Color.CYAN;
  Color color6 = Color.YELLOW;
  Color color7 = Color.WHITE;
  Color color8 = Color.BLACK;
}


// Contains the grid and controls the game
class GState {
  // The grid of Tiles
  ArrayList<ArrayList<Tile>> grid;

  // Constructor with seeded random
  // Fills the array with Tiles, randomizes the mines
  GState(int xSize, int ySize, int mines, Random r) {
    // notes the total number of mines in the game

    this.grid = new ArrayList<ArrayList<Tile>>(xSize);

    // Accumulate all Tiles that aren't mines
    ArrayList<Tile> nonMines = new ArrayList<Tile>();

    // Goes through and adds Tiles in each spot, adding in some mines
    for (int i = 0; i < xSize; i++) {
      ArrayList<Tile> col = new ArrayList<Tile>();
      for (int j = 0; j < ySize; j++) {
        // I think this is a good chance statistic
        if (mines > 0 && (r.nextInt(xSize * ySize) < IConst.MINES)) {
          col.add(new Tile(true));
          mines--;
        }
        else {
          // Do this so we can edit Tiles in the grid from a list
          // of Tiles without mines.
          Tile temp = new Tile(false);
          nonMines.add(temp);
          col.add(temp);
        }
      }
      // now add the column to the grid
      this.grid.add(col);
    }

    // Finish adding in all the mines
    while (mines > 0) {
      for (int i = nonMines.size() - 1; i >= 0; i--) {
        if (mines > 0 && (r.nextInt(xSize * ySize) < IConst.MINES)) {
          nonMines.get(i).changeToMine();
          mines--;
          nonMines.remove(nonMines.get(i));
        }
      }
    }

    // Now that all the mines are set, we can create neighbors
    this.initialize();
  }

  // Non seeded constructor, calls the seeded constructor with a random seed
  GState(int xSize, int ySize, int mines) {
    this(xSize, ySize, mines, new Random());
  }

  // Gives each of the Tiles their neighbors
  void initialize() {
    // Probably could have grabbed this from the constructor
    int xSize = grid.size(); // number of cols
    int ySize = grid.get(0).size(); // number of rows

    // This is all the checks for neighbors, I think it works
    // but look over it maybe?
    for (int i = 0; i < xSize; i++) {
      for (int j = 0; j < ySize; j++) {
        if (i > 0) {
          grid.get(i).get(j).addNeighbor(grid.get(i - 1).get(j));
          if (j > 0) {
            grid.get(i).get(j).addNeighbor(grid.get(i - 1).get(j - 1));
          }
          if (j < ySize - 1) {
            grid.get(i).get(j).addNeighbor(grid.get(i - 1).get(j + 1));
          }
        }
        if (i < xSize - 1) {
          grid.get(i).get(j).addNeighbor(grid.get(i + 1).get(j));
          if (j > 0) {
            grid.get(i).get(j).addNeighbor(grid.get(i + 1).get(j - 1));
          }
          if (j < ySize - 1) {
            grid.get(i).get(j).addNeighbor(grid.get(i + 1).get(j + 1));
          }
        }
        if (j > 0) {
          grid.get(i).get(j).addNeighbor(grid.get(i).get(j - 1));
        }
        if (j < ySize - 1) {
          grid.get(i).get(j).addNeighbor(grid.get(i).get(j + 1));
        }
      }
    }
  }

  // draws the game
  void draw(WorldScene scene) {
    int boxSize = IConst.BOX_SIZE;
    for (int i = 0; i < this.grid.size(); i++) {
      for (int j = 0; j < this.grid.get(0).size(); j++) {
        this.grid.get(i).get(j).draw(scene, i * boxSize + boxSize / 2, j * boxSize + boxSize / 2);
      }
    }
  }
}

// Class that represents each Tile
class Tile {
  // Is it a mine?
  boolean mine;
  // Has it been uncovered?
  boolean uncovered;
  // What Tiles are around it?
  ArrayList<Tile> neighbors;
  // Has it been flagged?
  boolean flagged;

  // At the point of creating a Tile, we likely can't yet know it's creator
  Tile(boolean mine) {
    this.mine = mine;
    this.neighbors = new ArrayList<Tile>();
    this.uncovered = false;
    this.flagged = false;
  }

  // This is nesc for randomizing the mines
  void changeToMine() {
    this.mine = true;
  }

  // returns the number of Tiles uncovered (0 or 1 if not flagged)
  // EFFECT: Changes the uncovered field to true if Tile is not flagged
  int uncover() {
    if (!this.flagged) {
      this.uncovered = true;
      return 1;
    }
    return 0;
  }

  // returns number of Tiles that were uncovered
  // EFFECT: Uncovers all neighbors and neighbors of Tiles with 0 mine neighbors
  int floodUncover() {
    int count = 0;
    for (Tile c : this.neighbors) {
      if (!c.uncovered) { // only need to worry about neighbors that are covered
        count += c.uncover();
        if (c.numMines() == 0) {
          count += c.floodUncover();
        }
      }
    }
    return count;
  }

  // EFFECT: Changes flag status
  void toggleFlag() {
    this.flagged = !this.flagged;
  }

  // Adds one neighbor to the list
  void addNeighbor(Tile c) {
    this.neighbors.add(c);
  }

  // Counts the amount of mine neighbors.
  int numMines() {
    int count = 0;
    for (Tile c : this.neighbors) {
      if (c.mine) {
        count++;
      }
    }
    return count;
  }

  // Counts the amount of flagged neighbors
  int numFlagged() {
    int count = 0;
    for (Tile c : this.neighbors) {
      if (c.flagged) {
        count++;
      }
    }
    return count;
  }

  // Draws Tile
  void draw(WorldScene scene, int x, int y) {    
    int boxSize = IConst.BOX_SIZE;

    // uncovered Tiles may show mine or # of mine neighbors (if > 0)
    // covered Tiles or neighbors = 0 show gray box
    if (this.uncovered) {
      // gray box to be used for uncovered box (overlay outline onto box)
      WorldImage grayBox = new OverlayImage(
          new RectangleImage(boxSize, boxSize, "outline", Color.BLACK),
          new RectangleImage(boxSize, boxSize, "solid", Color.GRAY));
      if (this.mine) {
        // place mine on box
        WorldImage box = new OverlayImage(
            new CircleImage(3 * boxSize / 8, "solid", Color.RED), grayBox);
        scene.placeImageXY(box, x, y);
      }
      else if (this.numMines() > 0) {
        // create array for neighbor font color
        ArrayList<Color> colors = new ArrayList<Color>(8);
        colors.add(IConst.color1);
        colors.add(IConst.color2);
        colors.add(IConst.color3);
        colors.add(IConst.color4);
        colors.add(IConst.color5);
        colors.add(IConst.color6);
        colors.add(IConst.color7);
        colors.add(IConst.color8);

        // create number and put it on gray box
        WorldImage neighbors = new TextImage(String.valueOf(this.numMines()), 
            3 * boxSize / 4, colors.get(this.numMines() - 1));
        WorldImage box = new OverlayImage(neighbors, grayBox);
        scene.placeImageXY(box, x, y);
      }
      else {
        scene.placeImageXY(grayBox, x, y);
      }
    }
    else {
      WorldImage blueBox = new OverlayImage(
          new RectangleImage(boxSize, boxSize, "outline", Color.BLACK),
          new RectangleImage(boxSize, boxSize, "solid", IConst.LBLUE));
      if (this.flagged) {
        WorldImage orangeTri = new TriangleImage(
            new Posn(boxSize / 4, 3 * boxSize / 4), new Posn(boxSize / 2, boxSize / 4),
            new Posn(3 * boxSize / 4, 3 * boxSize / 4), "solid", Color.ORANGE);
        WorldImage box = new OverlayImage(orangeTri, blueBox);
        scene.placeImageXY(box, x, y);
      }
      else {
        scene.placeImageXY(blueBox, x, y);
      }
    }
  }
}

//world state to actually run the minesweeper game
class MinesWorld extends World {
  GState g;
  WorldScene s;
  boolean mineStruck;
  int TilesCovered = IConst.GAMEX * IConst.GAMEY;
  int nonMinesUncovered;

  // initializes game and blank scene
  MinesWorld(int width, int height, int mines) {
    this.g = new GState(width, height, mines);
    this.s = new WorldScene(IConst.SCREEN_WIDTH, IConst.SCREEN_HEIGHT);
    this.mineStruck = false;
    this.nonMinesUncovered = 0;
  }

  // makes the empty scene
  public WorldScene makeScene() {
    return s;
  }

  // draw the game on every tick
  public void onTick() {
    this.g.draw(s);
  }

  // handle mouse click
  public void onMouseClicked(Posn pos, String button) {
    // only process if click is in bounds
    if (pos.x >= 0 && pos.x <= IConst.SCREEN_WIDTH 
        && pos.y >= 0 && pos.y <= IConst.SCREEN_HEIGHT) {
      int row = pos.x / IConst.BOX_SIZE;
      int col = pos.y / IConst.BOX_SIZE;

      Tile c = this.g.grid.get(row).get(col);

      // left button -> uncover Tile
      // if already uncovered, flood fill
      // if mine->mineStruck, if no mine neighbor -> flood uncover neighbors
      if (button.equals("LeftButton")) {
        if (c.uncovered && c.numFlagged() == c.numMines()) {
          for (Tile n : c.neighbors) {
            if (n.mine && !n.flagged) {
              this.mineStruck = true;
            }
          }
          this.TilesCovered -= c.floodUncover();
        }
        else {
          this.TilesCovered -= c.uncover();

          if (c.mine && !c.flagged) {
            this.mineStruck = true;
          }
          else {
            if (c.numMines() == 0) {
              this.TilesCovered -= c.floodUncover();
            }
          }
        }
      }
      // right button -> toggle flag
      else if (button.equals("RightButton")) {
        c.toggleFlag();
      }
      else {
        return;
      }
    }
  }

  // handle world end
  public WorldEnd worldEnds() {
    this.g.draw(s);
    if (this.mineStruck) {
      s.placeImageXY(new TextImage("L", IConst.SCREEN_WIDTH / 3, Color.RED),
          IConst.SCREEN_WIDTH / 2, IConst.SCREEN_WIDTH / 2);
    }
    else if (this.TilesCovered == IConst.MINES) {
      s.placeImageXY(new TextImage("YOU WIN", IConst.SCREEN_WIDTH / 10, Color.GREEN),
          IConst.SCREEN_WIDTH / 2, IConst.SCREEN_WIDTH / 2);
    }
    return new WorldEnd(this.mineStruck || this.TilesCovered == IConst.MINES, this.s);
  }
}


class ExamplesMines {
  ExamplesMines() {
  }

  void testDraw(Tester t) {
    MinesWorld mw = new MinesWorld(IConst.GAMEX, IConst.GAMEY, IConst.MINES);
    mw.bigBang(IConst.SCREEN_WIDTH, IConst.SCREEN_HEIGHT, 0.036);
  }
}