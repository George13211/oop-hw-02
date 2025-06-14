import junit.framework.TestCase;



public class BoardTest extends TestCase {
	Board b;
	Piece pyr1, pyr2, pyr3, pyr4, s, sRotated, stick, l2, l2Rotated;

	// This shows how to build things in setUp() to re-use
	// across tests.
	
	// In this case, setUp() makes shapes,
	// and also a 3X6 board, with pyr placed at the bottom,
	// ready to be used by tests.
	
	protected void setUp() throws Exception {
		b = new Board(3, 6);
		
		pyr1 = new Piece(Piece.PYRAMID_STR);
		pyr2 = pyr1.computeNextRotation();
		pyr3 = pyr2.computeNextRotation();
		pyr4 = pyr3.computeNextRotation();
		
		s = new Piece(Piece.S1_STR);
		sRotated = s.computeNextRotation();

		stick = new Piece(Piece.STICK_STR);

		l2 = new Piece(Piece.L2_STR);
		l2Rotated = l2.computeNextRotation();
		
		b.place(pyr1, 0, 0);
	}
	
	// Check the basic width/height/max after the one placement
	public void testSample1() {
		assertEquals(1, b.getColumnHeight(0));
		assertEquals(2, b.getColumnHeight(1));
		assertEquals(2, b.getMaxHeight());
		assertEquals(3, b.getRowWidth(0));
		assertEquals(1, b.getRowWidth(1));
		assertEquals(0, b.getRowWidth(2));
	}
	
	// Place sRotated into the board, then check some measures
	public void testSample2() {
		b.commit();
		int result = b.place(sRotated, 1, 1);
		assertEquals(Board.PLACE_OK, result);
		assertEquals(1, b.getColumnHeight(0));
		assertEquals(4, b.getColumnHeight(1));
		assertEquals(3, b.getColumnHeight(2));
		assertEquals(4, b.getMaxHeight());
	}
	
	// Makre  more tests, by putting together longer series of 
	// place, clearRows, undo, place ... checking a few col/row/max
	// numbers that the board looks right after the operations.

	public void testEmptyBoard() {
		Board board = new Board(10, 20);

		for (int y = 0; y < 20; y++) {
			assertEquals(0, board.getRowWidth(y));
		}

		for (int x = 0; x < 10; x++) {
			assertEquals(0, board.getColumnHeight(x));
		}
	}


	public void testPlace1() {
		b.commit();
		int res = b.place(pyr4, 0, 1);
		assertEquals(Board.PLACE_OK, res);
		assertTrue(b.getGrid(0, 1));
		assertTrue(b.getGrid(0, 2));
		assertTrue(b.getGrid(0, 3));
		assertTrue(b.getGrid(1, 2));
		assertTrue(b.getGrid(5, 6));
	}

	public void testPlace2() {
		b.commit();
		b.place(stick, 0, 1);
		b.commit();
		int result = b.place(stick, 2, 1);
		assertEquals(Board.PLACE_ROW_FILLED, result);
	}

	public void testPlace3() {
		// Test bounds/bad
		b.commit();
		assertEquals(Board.PLACE_BAD, b.place(pyr1,1, 1));
		b.commit();
		assertEquals(Board.PLACE_OUT_BOUNDS, b.place(pyr1, 1, 3));
	}

	// Basic case: clearing 1 row
	public void testClearRows1() {
		b.commit();
		int cleared = b.clearRows();
		assertEquals(1, cleared);

		// check width
		assertEquals(1, b.getRowWidth(0));
		assertEquals(0, b.getRowWidth(1));

		// Check grid
		assertTrue(b.getGrid(1, 0));
		assertFalse(b.getGrid(0, 0));

		//check height
		assertEquals(0, b.getColumnHeight(0));
		assertEquals(1, b.getColumnHeight(1));
		assertEquals(1, b.getMaxHeight());
	}

	// Clearing 2 rows after adding rotated L2
	public void  testClearRows2() {
		b.commit();
		int result = b.place(l2Rotated, 0, 1);
		assertEquals(Board.PLACE_ROW_FILLED, result);

		int cleared = b.clearRows();
		assertEquals(2, cleared);

		// check width
		assertEquals(2, b.getRowWidth(0));
		assertEquals(0, b.getRowWidth(1));

		// Check grid
		assertTrue(b.getGrid(1, 0));
		assertTrue(b.getGrid(2, 0));
		assertFalse(b.getGrid(0, 0));

		//check height
		assertEquals(0, b.getColumnHeight(0));
		assertEquals(1, b.getColumnHeight(1));
		assertEquals(1, b.getColumnHeight(2));
		assertEquals(1, b.getMaxHeight());
	}

	// No filled rows
	public void  testClearRows3() {
		b.commit();
		int result = b.clearRows();
		assertEquals(1, result);
		result = b.clearRows();
		assertEquals(0, result);
	}

	// basic: just adding stick and checking if undo works.
	public void testUndo1() {
		b.commit();
		int result = b.place(stick, 0, 1);
		assertEquals(Board.PLACE_OK, result);
		assertTrue(b.getGrid(0, 2));

		b.undo();

		assertFalse(b.getGrid(0, 2));
	}

	// Testing what happens after using clear
	public void testUndo2() {
		b.commit();
		int cleared = b.clearRows();
		assertEquals(1, cleared);
		assertEquals(1, b.getRowWidth(0));

		b.undo();

		assertEquals(3, b.getRowWidth(0));
	}

	// test if it works, when I do not change anything
	public void testUndo3() {
		b.commit();
		assertTrue(b.getGrid(1, 1));
		b.undo();
		assertTrue(b.getGrid(1, 1));
	}

	public void testDropHeight() {
		b.commit();
		b.place(pyr1, 0, 0);
		int result = b.dropHeight(pyr1, 0);
		assertEquals(2, result);
	}

	public void testGetWidth() {
		assertEquals(3, b.getWidth());
	}
	public void testGetheight() {
		assertEquals(6, b.getHeight());
	}

	
}
