/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package com.github.rotibakarasjad.repository;

import com.github.rotibakarasjad.entity.Category;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author NADHIR
 */
public class CategoryRepositoryTest {
    
    public CategoryRepositoryTest() {
    }
    
    @BeforeAll
    public static void setUpClass() {
    }
    
    @AfterAll
    public static void tearDownClass() {
    }
    
    @BeforeEach
    public void setUp() {
    }
    
    @AfterEach
    public void tearDown() {
    }

    /**
     * Test of findAll method, of class CategoryRepository.
     */
    @Test
    public void testFindAll() throws Exception {
        System.out.println("findAll");
        CategoryRepository instance = new CategoryRepository();
        List<Category> expResult = null;
        List<Category> result = instance.findAll();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of findById method, of class CategoryRepository.
     */
    @Test
    public void testFindById() throws Exception {
        System.out.println("findById");
        int id = 0;
        CategoryRepository instance = new CategoryRepository();
        Optional<Category> expResult = null;
        Optional<Category> result = instance.findById(id);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of insert method, of class CategoryRepository.
     */
    @Test
    public void testInsert() throws Exception {
        System.out.println("insert");
        Category category = null;
        CategoryRepository instance = new CategoryRepository();
        int expResult = 0;
        int result = instance.insert(category);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
