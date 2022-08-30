package com.dotcms.variant;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.dotcms.datagen.VariantDataGen;
import com.dotcms.util.ConversionUtils;
import com.dotcms.util.IntegrationTestInitService;
import com.dotcms.variant.model.Variant;
import com.dotmarketing.business.APILocator;
import com.dotmarketing.business.DotStateException;
import com.dotmarketing.common.db.DotConnect;
import com.dotmarketing.exception.DoesNotExistException;
import com.dotmarketing.exception.DotDataException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import org.junit.BeforeClass;
import org.junit.Test;


public class VariantAPITest {

    @BeforeClass
    public static void prepare() throws Exception {
        IntegrationTestInitService.getInstance().init();
    }

    /**
     * Method to test: {@link VariantFactory#save(Variant)}
     * When: Try to save a {@link Variant} object
     * Should: Save it in Data base.
     *
     * @throws DotDataException
     */
    @Test
    public void save() throws DotDataException {
        final Variant variant = new VariantDataGen().next();

        final Variant variantSaved = APILocator.getVariantAPI().save(variant);

        assertNotNull(variantSaved);
        assertNotNull(variantSaved.identifier());

        final Variant variantFromDataBase = getVariantFromDataBase(variantSaved);

        assertEquals(variantSaved.name(), variantFromDataBase.name());
        assertEquals(variantSaved.identifier(), variantFromDataBase.identifier());
        assertFalse(variantFromDataBase.archived());
    }

    /**
     * Method to test: {@link VariantFactory#save(Variant)}
     * When: Try to save a archived {@link Variant} object without name
     * Should: throw {@link NullPointerException}
     *
     * @throws DotDataException
     */
    @Test(expected = IllegalArgumentException.class)
    public void saveArchive() throws DotDataException {
        final Variant variant = new VariantDataGen().archived(true).next();
        APILocator.getVariantAPI().save(variant);
    }

    /**
     * Method to test: {@link VariantFactory#update(Variant)}
     * When: Try to update a {@link Variant} object
     * Should: Update it in Data base.
     *
     * @throws DotDataException
     */
    @Test
    public void update() throws DotDataException {
        final Variant variant = new VariantDataGen().nextPersisted();

        assertNotNull(variant);
        assertNotNull(variant.identifier());

        final Variant variantUpdated = new VariantDataGen()
                .id(variant.identifier())
                .name(variant.name() + "_updated")
                .archived(false)
                .next();

        APILocator.getVariantAPI().update(variantUpdated);

        final Variant variantFromDataBase = getVariantFromDataBase(variant);

        assertEquals(variantUpdated.name(), variantFromDataBase.name());
        assertEquals(variantUpdated.identifier(), variantFromDataBase.identifier());
        assertFalse(variantFromDataBase.archived());
    }

    /**
     * Method to test: {@link VariantFactory#update(Variant)}
     * When: Try to update a {@link Variant} object that not exists
     * Should: thorw a {@link DoesNotExistException}
     *
     * @throws DotDataException
     */
    @Test(expected = DoesNotExistException.class)
    public void updateNotExists() {
        final Variant variantToUpdated = new VariantDataGen()
                .id("Not_Exists").next();

        APILocator.getVariantAPI().update(variantToUpdated);
    }

    /**
     * Method to test: {@link VariantFactory#update(Variant)}
     * When: Try to update the {@link Variant}'s archived attribute
     * Should: Update it in Data base.
     *
     * @throws DotDataException
     */
    @Test
    public void updateArchivedField() throws DotDataException {
        final Variant variant = new VariantDataGen().nextPersisted();

        assertNotNull(variant);
        assertNotNull(variant.identifier());
        assertFalse(variant.archived());

        final Variant variantUpdated = new VariantDataGen()
                .id(variant.identifier())
                .name(variant.name())
                .archived(true)
                .next();

        APILocator.getVariantAPI().update(variantUpdated);

        final Variant variantFromDataBase = getVariantFromDataBase(variant);

        assertEquals(variantUpdated.name(), variantFromDataBase.name());
        assertEquals(variantUpdated.identifier(), variantFromDataBase.identifier());
        assertTrue(variantFromDataBase.archived());
    }

    /**
     * Method to test: {@link VariantFactory#delete(String)}
     * When: Try to archive a {@link Variant} object
     * Should: save it with archived equals to true
     *
     * @throws DotDataException
     */
    @Test
    public void archive() throws DotDataException {
        final Variant variant = new VariantDataGen().nextPersisted();

        ArrayList results = getResults(variant);
        assertFalse(results.isEmpty());

        APILocator.getVariantAPI().archive(variant.identifier());

        final Variant variantFromDataBase = getVariantFromDataBase(variant);
        assertEquals(variantFromDataBase.name(), variantFromDataBase.name());
        assertEquals(variantFromDataBase.identifier(), variantFromDataBase.identifier());
        assertTrue(variantFromDataBase.archived());
    }

    /**
     * Method to test: {@link VariantFactory#delete(String)}
     * When: Try to archive a {@link Variant} object that not exists
     * Should: throw {@link com.dotmarketing.exception.DoesNotExistException}
     *
     * @throws DotDataException
     */
    @Test(expected = DoesNotExistException.class)
    public void archiveNotExists() {
        APILocator.getVariantAPI().archive("Not Exists");
    }

    /**
     * Method to test: {@link VariantFactory#delete(String)}
     * When: Try to delete a archived {@link Variant} object
     * Should: remove it from Data base.
     *
     * @throws DotDataException
     */
    @Test
    public void delete() throws DotDataException {
        final Variant variant = new VariantDataGen().archived(true).nextPersisted();

        ArrayList results = getResults(variant);
        assertFalse(results.isEmpty());

        APILocator.getVariantAPI().delete(variant.identifier());

        results = getResults(variant);
        assertTrue(results.isEmpty());
    }

    /**
     * Method to test: {@link VariantFactory#delete(String)}
     * When: Try to delete a not exists {@link Variant} object
     * Should: throw a {@link DoesNotExistException}
     *
     * @throws DotDataException
     */
    @Test(expected = DoesNotExistException.class)
    public void deleteNotExists() {
        final Variant variant = new VariantDataGen().id("Not Exists").archived(true).next();

        APILocator.getVariantAPI().delete(variant.identifier());
    }

    /**
     * Method to test: {@link VariantFactory#delete(String)}
     * When: Try to delete a not archived {@link Variant} object
     * Should: throw a {@link DotStateException}
     *
     * @throws DotDataException
     */
    @Test(expected = DotStateException.class)
    public void deleteNotArchived() throws DotDataException {
        final Variant variant = new VariantDataGen().archived(false).nextPersisted();

        ArrayList results = getResults(variant);
        assertFalse(results.isEmpty());

        APILocator.getVariantAPI().delete(variant.identifier());
    }

    /**
     * Method to test: {@link VariantFactory#get(String)}
     * When: Try to get  {@link Variant} by id
     * Should: get it
     *
     * @throws DotDataException
     */
    @Test
    public void get() throws DotDataException {
        final Variant variant = new VariantDataGen().nextPersisted();

        ArrayList results = getResults(variant);
        assertFalse(results.isEmpty());

        final Optional<Variant> variantFromDataBase = APILocator.getVariantAPI().get(variant.identifier());

        assertTrue(variantFromDataBase.isPresent());
        assertEquals(variant.identifier(), variantFromDataBase.get().identifier());
    }

    /**
     * Method to test: {@link VariantFactory#get(String)}
     * When: Try to get  {@link Variant} by name
     * Should: get it
     *
     * @throws DotDataException
     */
    @Test
    public void getByName() throws DotDataException {
        final Variant variant = new VariantDataGen().nextPersisted();

        ArrayList results = getResults(variant);
        assertFalse(results.isEmpty());

        final Optional<Variant> variantFromDataBase = APILocator.getVariantAPI().getByName(variant.name());

        assertTrue(variantFromDataBase.isPresent());
        assertEquals(variant.identifier(), variantFromDataBase.get().identifier());
    }

    /**
     * Method to test: {@link VariantFactory#get(String)}
     * When: Try to get  {@link Variant} by id that not exists
     * Should: return a {@link Optional#empty()}
     *
     * @throws DotDataException
     */
    @Test
    public void getNotExists() throws DotDataException {

        final Optional<Variant> variantFromDataBase = APILocator.getVariantAPI()
                .get("Not_Exists");

        assertFalse(variantFromDataBase.isPresent());
    }

    /**
     * Method to test: {@link VariantFactory#get(String)}
     * When: Try to get  {@link Variant} by id equals to NULL
     * Should: throw a {@link NullPointerException}
     *
     * @throws DotDataException
     */
    @Test(expected = NullPointerException.class)
    public void getWithNull() {
        APILocator.getVariantAPI().get(null);
    }

    /**
     * Method to test: {@link VariantFactory#get(String)}
     * When: Try to get  {@link Variant} by id that not exists
     * Should: return a {@link Optional#empty()}
     *
     * @throws DotDataException
     */
    @Test
    public void getByNameNotExists() {

        final Optional<Variant> variantFromDataBase = APILocator.getVariantAPI()
                .getByName("Not_Exists");

        assertFalse(variantFromDataBase.isPresent());
    }

    /**
     * Method to test: {@link VariantFactory#get(String)}
     * When: Try to get  {@link Variant} by id equals to NULL
     * Should: throw a {@link NullPointerException}
     *
     * @throws DotDataException
     */
    @Test(expected = NullPointerException.class)
    public void getByNameWithNull() {
        APILocator.getVariantAPI().getByName(null);
    }

    private ArrayList getResults(Variant variant) throws DotDataException {
        return new DotConnect().setSQL(
                        "SELECT * FROM variant where id = ?")
                .addParam(variant.identifier())
                .loadResults();
    }

    private Variant getVariantFromDataBase(final Variant variant) throws DotDataException {
        final ArrayList results = getResults(variant);

        assertEquals(1, results.size());
        final Map resultMap = (Map) results.get(0);
        return Variant.builder()
                .identifier(resultMap.get("id").toString())
                .name(resultMap.get("name").toString())
                .archived(ConversionUtils.toBooleanFromDb(resultMap.get("archived")))
                .build();
    }
}
