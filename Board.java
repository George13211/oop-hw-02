// Board.java

import javax.management.RuntimeErrorException;

/**
 CS108 Tetris Board.
 Represents a Tetris board -- essentially a 2-d grid
 of booleans. Supports tetris pieces and row clearing.
 Has an "undo" feature that allows clients to add and remove pieces efficiently.
 Does not do any drawing or have any idea of pixels. Instead,
 just represents the abstract 2-d board.
*/
public class Board	{
	// Some ivars are stubbed out for you:
	private int width;
	private int height;
	private boolean[][] grid;
	private boolean DEBUG = false;
	boolean committed;
	
	private int[] widths;
	private int[] heights;
	private int maxHeight;
	private boolean[][] secondaryGrid;
	private int[] secondaryWidths;
	private int[] secondaryHeights;
	private int secondaryMaxHeight;

	// Here a few trivial methods are provided:
	
	/**
	 Creates an empty board of the given width and height
	 measured in blocks.
	*/
	public Board(int width, int height) {
		this.width = width;
		this.height = height;
		grid = new boolean[width][height];
		widths = new int[height];
		heights = new int[width];
		maxHeight = 0;
		committed = true;

		secondaryGrid = new boolean[width][height];
		secondaryWidths = new int[height];
		secondaryHeights = new int[width];
		secondaryMaxHeight = 0;
	}
	
	
	/**
	 Returns the width of the board in blocks.
	*/
	public int getWidth() {
		return width;
	}
	
	
	/**
	 Returns the height of the board in blocks.
	*/
	public int getHeight() {
		return height;
	}
	
	
	/**
	 Returns the max column height present in the board.
	 For an empty board this is 0.
	*/
	public int getMaxHeight() {
		return maxHeight;
	}
	
	
	/**
	 Checks the board for internal consistency -- used
	 for debugging.
	*/
	public void sanityCheck() throws RuntimeErrorException {
		if (DEBUG) {

			// maxHeight
			int curr = 0;
			for (int i = 0; i < heights.length; i++) {
				if (curr < heights[i]) {
					curr = heights[i];
				}
			}
			if (curr != maxHeight) {
				throw new RuntimeException("Wrong maxHeight");
			}

			// heights
			for (int i = 0; i < width; i++) {
				int temp = 0;
				int currY = height - 1;
				while (currY >= 0) {
					if (grid[i][currY]) {
						temp = currY + 1;
						break;
					}
					currY--;
				}

				if (temp != heights[i]) {
					throw new RuntimeException("wrong Heights");
				}
			}

			// widths
			for (int i = 0; i < height; i++) {
				int temp2 = 0;
				for (int j = 0; j < width; j++) {
					if (grid[j][i]) temp2++;
				}
				if (temp2 != widths[i]) {
					throw new RuntimeException("Wrong widths");
				}
			}
		}
	}
	
	/**
	 Given a piece and an x, returns the y
	 value where the piece would come to rest
	 if it were dropped straight down at that x.
	 
	 <p>
	 Implementation: use the skirt and the col heights
	 to compute this fast -- O(skirt length).
	*/
	public int dropHeight(Piece piece, int x) {
		int[] sk = piece.getSkirt();
		int max = 0;

		for (int index = 0; index < sk.length; index++) {
			int dist = heights[x + index] - sk[index];
			if (dist > max) {
				max = dist;
			}
		}

		return max;
	}
	
	
	/**
	 Returns the height of the given column --
	 i.e. the y value of the highest block + 1.
	 The height is 0 if the column contains no blocks.
	*/
	public int getColumnHeight(int x) {
		if (x >= width || x < 0) {
			throw new IndexOutOfBoundsException("Column out of bounds");
		}
		return heights[x];
	}
	
	
	/**
	 Returns the number of filled blocks in
	 the given row.
	*/
	public int getRowWidth(int y) {
		if (y >= height || y < 0) {
			throw new IndexOutOfBoundsException("Row out of bounds");
		}
		return widths[y];
	}
	
	
	/**
	 Returns true if the given block is filled in the board.
	 Blocks outside of the valid width/height area
	 always return true.
	*/
	public boolean getGrid(int x, int y) {
		if (x < 0 || y < 0 || x >= width || y >= height) {
			return true;
		}
		return grid[x][y];
	}
	
	
	public static final int PLACE_OK = 0;
	public static final int PLACE_ROW_FILLED = 1;
	public static final int PLACE_OUT_BOUNDS = 2;
	public static final int PLACE_BAD = 3;
	
	/**
	 Attempts to add the body of a piece to the board.
	 Copies the piece blocks into the board grid.
	 Returns PLACE_OK for a regular placement, or PLACE_ROW_FILLED
	 for a regular placement that causes at least one row to be filled.
	 
	 <p>Error cases:
	 A placement may fail in two ways. First, if part of the piece may falls out
	 of bounds of the board, PLACE_OUT_BOUNDS is returned.
	 Or the placement may collide with existing blocks in the grid
	 in which case PLACE_BAD is returned.
	 In both error cases, the board may be left in an invalid
	 state. The client can use undo(), to recover the valid, pre-place state.
	*/
	public int place(Piece piece, int x, int y) {
		// flag !committed problem
		if (!committed) throw new RuntimeException("place commit problem");
			
		int result = PLACE_OK;
		makeBackup();
		committed = false;

		TPoint[] body = piece.getBody();

		// check bounds
		for (int i = 0; i < body.length; i++) {
			TPoint tp = body[i];
			int xOnBoard = x + tp.x;
			int yOnBoard = y + tp.y;

			if (xOnBoard < 0 || yOnBoard < 0 ||
					xOnBoard >= width || yOnBoard >= height) {
				return PLACE_OUT_BOUNDS;
			}

			if (grid[xOnBoard][yOnBoard]) {
				return PLACE_BAD;
			}
		}

		result = placeHelper(body, x, y, result);

		if (DEBUG) {
			sanityCheck();
		}
		return result;
	}

	// Creates secondary arrays (making backups)
	private void makeBackup() {
		for (int i = 0; i < width; i++) {
			System.arraycopy(grid[i], 0, secondaryGrid[i], 0, height);
		}

		System.arraycopy(heights, 0, secondaryHeights, 0, width);
		System.arraycopy(widths, 0, secondaryWidths, 0, height);
		secondaryMaxHeight = maxHeight;
	}

	// Now this really places the piece
	private int placeHelper(TPoint[] body, int x, int y, int result) {
		for (int i = 0; i < body.length; i++) {
			TPoint tp = body[i];
			int xOnBoard = x + tp.x;
			int yOnBoard = y + tp.y;
			grid[xOnBoard][yOnBoard] = true;
			widths[yOnBoard]++;

			if (heights[xOnBoard] < yOnBoard + 1) {
				heights[xOnBoard] = yOnBoard + 1;
			}

			if (width == widths[yOnBoard]) {
				result = PLACE_ROW_FILLED;
			}

			if (maxHeight < heights[xOnBoard]) {
				maxHeight = heights[xOnBoard];
			}
		}
		return result;
	}
	
	/**
	 Deletes rows that are filled all the way across, moving
	 things above down. Returns the number of rows cleared.
	*/
	public int clearRows() {
		int rowsCleared = 0;
		int toY = 0;
		if (committed) {
			makeBackup();
			committed = false;
		}

		for (int i = 0; i < height; i++) {
			if (widths[i] >= width) {
				rowsCleared++;
			} else {
				if (toY != i) {
					for (int j = 0; j < width; j++) {
						grid[j][toY] = grid[j][i];
					}
					widths[toY] = widths[i];
				}
				toY++;
			}
		}

		// clear on top
		for (int i = toY; i < height; i++) {
			for (int j = 0; j < width; j++) {
				grid[j][i] = false;
			}
			widths[i] = 0;
		}

		renew();

		if (DEBUG) {
			sanityCheck();
		}
		return rowsCleared;
	}


	// Recomputing heights
	private void renew() {
		// renew heights array
		for (int i = 0; i < width; i++) {
			int temp = 0;
			int currY = height - 1;
			while (currY >= 0) {
				if (grid[i][currY]) {
					temp = currY + 1;
					break;
				}
				currY--;
			}
			heights[i] = temp;
		}

		// renew maxHeight
		int res = 0;
		for (int i = 0; i < heights.length; i++) {
			if (heights[i] > res) {
				res = heights[i];
			}
		}
		maxHeight = res;
	}


	/**
	 Reverts the board to its state before up to one place
	 and one clearRows();
	 If the conditions for undo() are not met, such as
	 calling undo() twice in a row, then the second undo() does nothing.
	 See the overview docs.
	*/
	public void undo() {
		if (committed) return;

		int[] tempW = widths;
		widths = secondaryWidths;
		secondaryWidths = tempW;

		int[] tempH = heights;
		heights = secondaryHeights;
		secondaryHeights = tempH;

		int tempMH = maxHeight;
		maxHeight = secondaryMaxHeight;
		secondaryMaxHeight = tempMH;

		boolean[][] tempG = grid;
		grid = secondaryGrid;
		secondaryGrid = tempG;

		committed = true;

		if (DEBUG) sanityCheck();
	}
	
	
	/**
	 Puts the board in the committed state.
	*/
	public void commit() {
		committed = true;
	}


	
	/*
	 Renders the board state as a big String, suitable for printing.
	 This is the sort of print-obj-state utility that can help see complex
	 state change over time.
	 (provided debugging utility) 
	 */
	public String toString() {
		StringBuilder buff = new StringBuilder();
		for (int y = height-1; y>=0; y--) {
			buff.append('|');
			for (int x=0; x<width; x++) {
				if (getGrid(x,y)) buff.append('+');
				else buff.append(' ');
			}
			buff.append("|\n");
		}
		for (int x=0; x<width+2; x++) buff.append('-');
		return(buff.toString());
	}
}


