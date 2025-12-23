package io.github.jdanders.dropseven

import io.github.jdanders.dropseven.model.Cell
import io.github.jdanders.dropseven.model.Disc
import org.junit.Assert.*
import org.junit.Test

/**
 * Tests for Cell class functionality.
 */
class CellTest {
    
    @Test
    fun testEmptyCell() {
        val cell = Cell.Empty
        
        assertTrue(cell.isEmpty())
        assertFalse(cell.isOccupied())
        assertNull(cell.discOrNull())
    }
    
    @Test
    fun testOccupiedCellWithNumberedDisc() {
        val disc = Disc.Numbered(5)
        val cell = Cell.Occupied(disc)
        
        assertFalse(cell.isEmpty())
        assertTrue(cell.isOccupied())
        assertEquals(disc, cell.discOrNull())
        assertEquals(disc, cell.disc)
    }
    
    @Test
    fun testOccupiedCellWithSolidDisc() {
        val disc = Disc.Solid(0, 3)
        val cell = Cell.Occupied(disc)
        
        assertFalse(cell.isEmpty())
        assertTrue(cell.isOccupied())
        assertEquals(disc, cell.discOrNull())
        assertEquals(disc, cell.disc)
    }
    
    @Test
    fun testCellEquality() {
        val disc1 = Disc.Numbered(3)
        val disc2 = Disc.Numbered(3)
        
        val cell1 = Cell.Occupied(disc1)
        val cell2 = Cell.Occupied(disc2)
        
        assertEquals(cell1, cell2)
    }
    
    @Test
    fun testCellInequality() {
        val cell1 = Cell.Occupied(Disc.Numbered(3))
        val cell2 = Cell.Occupied(Disc.Numbered(4))
        
        assertNotEquals(cell1, cell2)
    }
    
    @Test
    fun testEmptyCellEquality() {
        val cell1 = Cell.Empty
        val cell2 = Cell.Empty
        
        assertEquals(cell1, cell2)
    }
    
    @Test
    fun testEmptyVsOccupied() {
        val empty = Cell.Empty
        val occupied = Cell.Occupied(Disc.Numbered(1))
        
        assertNotEquals(empty, occupied)
    }
    
    @Test
    fun testDiscOrNullWithEmpty() {
        val cell = Cell.Empty
        assertNull(cell.discOrNull())
    }
    
    @Test
    fun testDiscOrNullWithOccupied() {
        val disc = Disc.Numbered(7)
        val cell = Cell.Occupied(disc)
        
        assertNotNull(cell.discOrNull())
        assertEquals(disc, cell.discOrNull())
    }
    
    @Test
    fun testCellWithDifferentDiscTypes() {
        val numbered = Cell.Occupied(Disc.Numbered(2))
        val solid = Cell.Occupied(Disc.Solid(0, 2))
        
        assertNotEquals(numbered, solid)
        assertTrue(numbered.isOccupied())
        assertTrue(solid.isOccupied())
    }
}

