package org.example.service;

import org.example.dao.impl.CategoryDAO;
import org.example.dao.impl.ProductDAO;
import org.example.model.entity.Category;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@DisplayName("CategoryService İş Kuralları Testleri")
class CategoryServiceTest {

    @Test
    @DisplayName("Kategori adı null olduğunda IllegalArgumentException fırlatmalı")
    void addCategory_nullName_throwsException() {
        try (MockedConstruction<CategoryDAO> ignoredCatDao = mockConstruction(CategoryDAO.class);
             MockedConstruction<ProductDAO> ignoredProdDao = mockConstruction(ProductDAO.class)) {

            CategoryService service = new CategoryService();
            Category category = new Category(0, null, "Açıklama");

            assertThrows(IllegalArgumentException.class, () -> service.addCategory(category));
        }
    }

    @Test
    @DisplayName("Kategori adı boş string olduğunda IllegalArgumentException fırlatmalı")
    void addCategory_emptyName_throwsException() {
        try (MockedConstruction<CategoryDAO> ignoredCatDao = mockConstruction(CategoryDAO.class);
             MockedConstruction<ProductDAO> ignoredProdDao = mockConstruction(ProductDAO.class)) {

            CategoryService service = new CategoryService();
            Category category = new Category(0, "  ", "Açıklama");

            assertThrows(IllegalArgumentException.class, () -> service.addCategory(category));
        }
    }

    @Test
    @DisplayName("Kategori adı 100 karakterden uzunsa IllegalArgumentException fırlatmalı")
    void addCategory_nameTooLong_throwsException() {
        try (MockedConstruction<CategoryDAO> ignoredCatDao = mockConstruction(CategoryDAO.class);
             MockedConstruction<ProductDAO> ignoredProdDao = mockConstruction(ProductDAO.class)) {

            CategoryService service = new CategoryService();
            Category category = new Category(0, "A".repeat(101), "Açıklama");

            assertThrows(IllegalArgumentException.class, () -> service.addCategory(category));
        }
    }

    @Test
    @DisplayName("Tam 100 karakterlik kategori adı geçerlidir")
    void addCategory_nameExactly100Chars_valid() {
        try (MockedConstruction<CategoryDAO> mockedCatDao = mockConstruction(CategoryDAO.class,
                (mock, ctx) -> when(mock.insert(any())).thenReturn(true));
             MockedConstruction<ProductDAO> ignoredProdDao = mockConstruction(ProductDAO.class)) {

            CategoryService service = new CategoryService();
            Category category = new Category(0, "A".repeat(100), "Açıklama");

            boolean result = service.addCategory(category);

            assertTrue(result);
            verify(mockedCatDao.constructed().get(0), times(1)).insert(category);
        }
    }

    @Test
    @DisplayName("Geçerli kategori eklendiğinde DAO insert çağrılmalı")
    void addCategory_validCategory_callsDaoInsert() {
        try (MockedConstruction<CategoryDAO> mockedCatDao = mockConstruction(CategoryDAO.class,
                (mock, ctx) -> when(mock.insert(any())).thenReturn(true));
             MockedConstruction<ProductDAO> ignoredProdDao = mockConstruction(ProductDAO.class)) {

            CategoryService service = new CategoryService();
            Category category = new Category(0, "Elektronik", "Elektronik ürünler");

            boolean result = service.addCategory(category);

            assertTrue(result);
            verify(mockedCatDao.constructed().get(0), times(1)).insert(category);
        }
    }

    @Test
    @DisplayName("getAllCategories çağrıldığında DAO getAll sonucu dönmeli")
    void getAllCategories_returnsList() {
        List<Category> mockList = Arrays.asList(
                new Category(1, "Elektronik", ""),
                new Category(2, "Mobilya", "")
        );

        try (MockedConstruction<CategoryDAO> mockedCatDao = mockConstruction(CategoryDAO.class,
                (mock, ctx) -> when(mock.getAll()).thenReturn(mockList));
             MockedConstruction<ProductDAO> ignoredProdDao = mockConstruction(ProductDAO.class)) {

            CategoryService service = new CategoryService();

            List<Category> result = service.getAllCategories();

            assertEquals(2, result.size());
            verify(mockedCatDao.constructed().get(0), times(1)).getAll();
        }
    }

    @Test
    @DisplayName("updateCategory çağrıldığında DAO update çağrılmalı ve sonucu dönmeli")
    void updateCategory_callsDaoUpdate() {
        try (MockedConstruction<CategoryDAO> mockedCatDao = mockConstruction(CategoryDAO.class,
                (mock, ctx) -> when(mock.update(any())).thenReturn(true));
             MockedConstruction<ProductDAO> ignoredProdDao = mockConstruction(ProductDAO.class)) {

            CategoryService service = new CategoryService();
            Category category = new Category(1, "Elektronik", "Güncel açıklama");

            boolean result = service.updateCategory(category);

            assertTrue(result);
            verify(mockedCatDao.constructed().get(0), times(1)).update(category);
        }
    }

    @Test
    @DisplayName("Kategoride ürün varken silme işlemi IllegalStateException fırlatmalı")
    void deleteCategory_hasProducts_throwsException() {
        try (MockedConstruction<CategoryDAO> ignoredCatDao = mockConstruction(CategoryDAO.class);
             MockedConstruction<ProductDAO> ignoredProdDao = mockConstruction(ProductDAO.class,
                     (mock, ctx) -> when(mock.countByCategoryId(anyInt())).thenReturn(3))) {

            CategoryService service = new CategoryService();

            IllegalStateException ex = assertThrows(IllegalStateException.class,
                    () -> service.deleteCategory(1));
            assertTrue(ex.getMessage().contains("3"));
        }
    }

    @Test
    @DisplayName("Kategoride ürün yokken silme işlemi DAO delete çağırmalı")
    void deleteCategory_noProducts_callsDaoDelete() {
        try (MockedConstruction<CategoryDAO> mockedCatDao = mockConstruction(CategoryDAO.class,
                (mock, ctx) -> when(mock.delete(anyInt())).thenReturn(true));
             MockedConstruction<ProductDAO> ignoredProdDao = mockConstruction(ProductDAO.class,
                     (mock, ctx) -> when(mock.countByCategoryId(anyInt())).thenReturn(0))) {

            CategoryService service = new CategoryService();

            boolean result = service.deleteCategory(1);

            assertTrue(result);
            verify(mockedCatDao.constructed().get(0), times(1)).delete(1);
        }
    }
}
