import java.util.Random;
import java.io.IOException;

/**
 * minesweeper clone
 * @author Johnny Burrer
 * @version 2/14/2023
 */
public class MineField {

	// ansi codes for printing in color
	private static final String RESET = "\u001B[0m";
	private static final String RED_BG = "\u001B[41m";
	private static final String WHITE_BG = "\u001B[47m";
	private static final String BLACK = "\u001B[30m";
	private static final String BLUE = "\u001B[34m";
	private static final String GREEN = "\u001B[32m";
	private static final String RED = "\u001B[31m";
	private static final String YELLOW = "\u001B[33m";
	private static final String PURPLE = "\u001B[35m";
	private static final String CYAN = "\u001B[36m";

	private static int[][] map; // the actual map
	private static int[][] displayMap; // the map that is printed each iteration
	private static int size; // width and length of map
	private static int y; // current y coordinate of user's cursor
	private static int x; // current x coordinate of user's cursor
	private static int minesNumber; // number of mines in the field
	// offsets to add to a coordinate to get the coordinates of each spot around the coordinate
	private static int[][] offsets = {
		{ 1, 0 },
		{ 1, 1 },
		{ 0, 1 },
		{ -1, 1 },
		{ -1, 0 },
		{ -1, -1 },
		{ 0, -1 },
		{ 1, -1 }
	};

	/**
	 * initiate the display map by filling every spot with the value for an unchecked coordinate (-2)
	 * @return 2d array of integers used to draw map on screen
	 */
	private static int[][] generateDisplayMap() {
		int[][] ret = new int[MineField.size][MineField.size];
		for (int y = 0; y < MineField.size; y++) {
			for (int x = 0; x < MineField.size; x++) {
				ret[y][x] = -2;
			}
		}
		return ret;
	}

	/**
	 * initiate the actual map
	 * @return 2d array of integers containing the coordinates of all mines and surrounding values
	 */
	private static int[][] generateMap() {
		Random rand = new Random(); // to generate random integers
		int[][] ret = new int[MineField.size][MineField.size];
		for (int y = 0; y < MineField.size; y++) {
			for (int x = 0; x < MineField.size; x++) {
				ret[y][x] = 0;
			}
		}
		for (int i = 0; i < (MineField.minesNumber); i++) {
			int yCoord, xCoord;
			do {
				yCoord = rand.nextInt(MineField.size);
				xCoord = rand.nextInt(MineField.size);
			} while (ret[yCoord][xCoord] == -1);
			int tempYCoord, tempXCoord;
			for (int[] offset : MineField.offsets) {
				tempYCoord = yCoord + offset[0]; 
				tempXCoord = xCoord + offset[1]; 
				if (
					tempYCoord >= 0 &&
					tempYCoord < MineField.size &&
					tempXCoord >= 0 &&
					tempXCoord < MineField.size &&
					ret[tempYCoord][tempXCoord] != -1
				)
					ret[tempYCoord][tempXCoord] += 1;
			}
			ret[yCoord][xCoord] = -1;
		}
		return ret;
	}

	/**
	 * prints maps to console, prints display map every loop and actual map upon game's end
	 * @param map 2d integer array representing map to print
	 */
	private static void printMap(int[][] map) {
		System.out.print("\033[H\033[2J");  
		System.out.flush();
		for (int i = 0; i < MineField.size + 2; i++)
			System.out.print("# ");
		System.out.println();
		for (int y = 0; y < MineField.size; y++) {
			System.out.print("# ");
			for (int x = 0; x < MineField.size; x++) {
				String displayStr;
				switch(map[y][x]) {
					case -3:
						displayStr = String.format("%s?%s", RED_BG, RESET);
						break;
					case -2:
						displayStr = "*";
						break;
					case -1:
						displayStr = String.format("%sX%s", RED_BG, RESET);
						break;
					case 0:
						displayStr = " ";
						break;
					case 1:
						displayStr = String.format("%s%d%s", BLUE, map[y][x], RESET);
						break;
					case 2:
						displayStr = String.format("%s%d%s", GREEN, map[y][x], RESET);
						break;
					case 3:
						displayStr = String.format("%s%d%s", RED, map[y][x], RESET);
						break;
					case 4:
						displayStr = String.format("%s%d%s", YELLOW, map[y][x], RESET);
						break;
					case 5:
						displayStr = String.format("%s%d%s", PURPLE, map[y][x], RESET);
						break;
					case 6:
						displayStr = String.format("%s%d%s", CYAN, map[y][x], RESET);
						break;
					default:
						displayStr = String.format("%d", map[y][x]);
				}
				if (MineField.y == y && MineField.x == x)
					System.out.printf("%s%s%s%s ", WHITE_BG, BLACK, displayStr, RESET);
				else
					System.out.printf("%s ", displayStr);
			}
			System.out.println("#");
		}
		for (int i = 0; i < MineField.size + 2; i++)
			System.out.print("# ");
		System.out.println();
	}

	/**
	 * reveals contents of current coordinate, recursively clears empty space
	 * @param y y coordinate to reveal
	 * @param x x coordinate to reveal
	 */
	public static void reveal(int y, int x) {
		MineField.displayMap[y][x] = MineField.map[y][x];
		if (MineField.map[y][x] == 0 || isBoxClear(y, x)) {
			int tempYCoord, tempXCoord; 
			for (int[] offset : MineField.offsets) {
				tempYCoord = y + offset[0]; 
				tempXCoord = x + offset[1]; 
				if (
					tempYCoord >= 0 &&
					tempYCoord < MineField.size &&
					tempXCoord >= 0 &&
					tempXCoord < MineField.size &&
					MineField.displayMap[tempYCoord][tempXCoord] == -2
				)
					MineField.reveal(tempYCoord, tempXCoord);
			}
		}
	}

	/**
	 * checks if all mines in a coordinates surrounding box have been discovered
	 * @param y y coordinate to check
	 * @param x x coordinate to check
	 * @return whether or not all mines in the box have been found
	 */
	private	static boolean isBoxClear(int y, int x) {
		int tempYCoord, tempXCoord; 
		for (int[] offset : MineField.offsets) {
			tempYCoord = y + offset[0]; 
			tempXCoord = x + offset[1]; 
			if (
				tempYCoord >= 0 &&
				tempYCoord < MineField.size &&
				tempXCoord >= 0 &&
				tempXCoord < MineField.size &&
				MineField.displayMap[tempYCoord][tempXCoord] == -2 &&
				MineField.map[tempYCoord][tempXCoord] == -1
			)
				return false;
		}
		return true;
	}

	/**
	 * end the game by printing the actual map and a win/loss message
	 * @param winState whether the game was won or lost
	 */
	private static void endGame(boolean winState) {
		MineField.printMap(MineField.map);
		System.out.printf("%s!%s\n", (winState ? GREEN+"Won" : RED+"Lost"), RESET);
	}

	/**
	 * receive console input from user without them having to press the enter/space key
	 * @return character received from user
	 */
	private static char getInput() throws IOException, InterruptedException {
		char ret; 
		String[] cmdOne = { "/bin/sh", "-c", "stty raw </dev/tty" };
		Runtime.getRuntime().exec(cmdOne).waitFor();
		ret = (char)System.in.read();
		String[] cmdTwo = { "/bin/sh", "-c", "stty cooked </dev/tty" };
		Runtime.getRuntime().exec(cmdTwo).waitFor();
		return ret;
	}

	/**
	 * main method, contains main game loop
	 * @param args array of arguments passed from command line, used to get map size and mine density
	 */
	public static void main(String[] args) throws IOException, InterruptedException {

		double density;
		if (args.length == 0) {
			MineField.size = 10;
			density = 0.2;
		} else if (args.length == 1) {
			MineField.size = Integer.parseInt(args[0]);
			if (MineField.size < 0) {
				System.out.println("Size must be greater than 0");
				return;
			}
			density = 0.2;
		} else {
			MineField.size = Integer.parseInt(args[0]);
			if (MineField.size < 0) {
				System.out.println("Size must be greater than 0");
				return;
			}
			density = Double.parseDouble(args[1]);
			if (density < 0 || density > 1) {
				System.out.println("Density must be between 0 and 1");
				return;
			}
		}

		MineField.minesNumber = (int)(MineField.size * MineField.size * density);
		MineField.map = MineField.generateMap();
		MineField.displayMap = MineField.generateDisplayMap();
		MineField.y = 0;
		MineField.x = 0;
		int foundMines = 0;

		while (true) {
			MineField.printMap(MineField.displayMap);
			switch (MineField.getInput()) {
				case 'h':
					if (MineField.x > 0)
						MineField.x--;
					break;
				case 'j':
					if (MineField.y < MineField.size - 1)
						MineField.y++;
					break;
				case 'k':
					if (MineField.y > 0)
						MineField.y--;
					break;
				case 'l':
					if (MineField.x < MineField.size - 1)
						MineField.x++;
					break;
				case ' ':
					if (MineField.map[MineField.y][MineField.x] == -1) {
						MineField.endGame(false);
						return;
					} else
						MineField.reveal(MineField.y, MineField.x);
					break;
				case 'F':
					if (MineField.displayMap[MineField.y][MineField.x] == -3) {
						MineField.displayMap[MineField.y][MineField.x] = -2;
						if (MineField.map[MineField.y][MineField.x] == -1)
							foundMines--;
					} else {
						MineField.displayMap[MineField.y][MineField.x] = -3;
						if (MineField.map[MineField.y][MineField.x] == -1)
							foundMines++;
					}
					break;
				case 'q':
					MineField.endGame(false);
					return;
			}
			if (minesNumber == foundMines) {
				MineField.endGame(true);
				return;
			}
		}

	}

}
