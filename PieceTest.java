import junit.framework.TestCase;

import java.util.*;

/*
  Unit test for Piece class -- starter shell.
 */
public class PieceTest extends TestCase {
	// You can create data to be used in the your
	// test cases like this. For each run of a test method,
	// a new PieceTest object is created and setUp() is called
	// automatically by JUnit.
	// For example, the code below sets up some
	// pyramid and s pieces in instance variables
	// that can be used in tests.
	private Piece pyr1, pyr2, pyr3, pyr4;
	private Piece s, sRotated;

	protected void setUp() throws Exception {
		super.setUp();
		
		pyr1 = new Piece(Piece.PYRAMID_STR);
		pyr2 = pyr1.computeNextRotation();
		pyr3 = pyr2.computeNextRotation();
		pyr4 = pyr3.computeNextRotation();
		
		s = new Piece(Piece.S1_STR);
		sRotated = s.computeNextRotation();
	}
	
	// Here are some sample tests to get you started
	
	public void testSampleSize() {
		// Check size of pyr piece
		assertEquals(3, pyr1.getWidth());
		assertEquals(2, pyr1.getHeight());
		
		// Now try after rotation
		// Effectively we're testing size and rotation code here
		assertEquals(2, pyr2.getWidth());
		assertEquals(3, pyr2.getHeight());
		
		// Now try with some other piece, made a different way
		Piece l = new Piece(Piece.STICK_STR);
		assertEquals(1, l.getWidth());
		assertEquals(4, l.getHeight());
	}
	
	
	// Test the skirt returned by a few pieces
	public void testSampleSkirt() {
		// Note must use assertTrue(Arrays.equals(... as plain .equals does not work
		// right for arrays.
		assertTrue(Arrays.equals(new int[] {0, 0, 0}, pyr1.getSkirt()));
		assertTrue(Arrays.equals(new int[] {1, 0, 1}, pyr3.getSkirt()));
		
		assertTrue(Arrays.equals(new int[] {0, 0, 1}, s.getSkirt()));
		assertTrue(Arrays.equals(new int[] {1, 0}, sRotated.getSkirt()));
	}

	public void testEquals() {
		assertTrue(pyr1.equals(pyr1));

		Piece newP = new Piece(Piece.PYRAMID_STR);
		assertTrue(pyr1.equals(newP));

		assertFalse(pyr1.equals(pyr2));
		assertFalse(pyr1.equals(s));

		assertFalse(pyr1.equals(null));
		assertFalse(pyr1.equals("Other"));

	}

	public void testGetPieces() {
		Piece[] pieces = Piece.getPieces();
		assertNotNull(pieces);
		assertEquals(7, pieces.length);

		Piece pyramid = pieces[Piece.PYRAMID];
		assertTrue(pyr1.equals(pyramid));

		assertNotNull(pyramid.fastRotation());
		assertNotSame(pyramid, pyramid.fastRotation());
	}

}
