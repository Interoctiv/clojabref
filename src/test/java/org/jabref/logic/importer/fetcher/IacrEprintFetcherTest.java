package org.jabref.logic.importer.fetcher;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.jabref.logic.importer.FetcherException;
import org.jabref.logic.importer.ImportFormatPreferences;
import org.jabref.model.entry.BibEntry;
import org.jabref.model.entry.field.StandardField;
import org.jabref.model.entry.types.StandardEntryType;
import org.jabref.support.DisabledOnCIServer;
import org.jabref.testutils.category.FetcherTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Answers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

@FetcherTest
@DisabledOnCIServer("eprint.iacr.org blocks with 500 when there are too many calls from the same IP address.")
class IacrEprintFetcherTest {

    private IacrEprintFetcher fetcher;
    private BibEntry abram2017;
    private BibEntry abram2017noVersion;
    private BibEntry beierle2016;
    private BibEntry delgado2017;

    @BeforeEach
    void setUp() {
        fetcher = new IacrEprintFetcher(mock(ImportFormatPreferences.class, Answers.RETURNS_DEEP_STUBS));

        abram2017 = new BibEntry(StandardEntryType.Misc)
                .withCitationKey("cryptoeprint:2017/1118")
                .withField(StandardField.ABSTRACT, "dummy")
                .withField(StandardField.AUTHOR, "Ittai Abraham and Dahlia Malkhi and Kartik Nayak and Ling Ren and Alexander Spiegelman")
                .withField(StandardField.DATE, "2017-11-24")
                .withField(StandardField.HOWPUBLISHED, "Cryptology {ePrint} Archive, Paper 2017/1118")
                .withField(StandardField.TITLE, "Solida: A Blockchain Protocol Based on Reconfigurable Byzantine Consensus")
                .withField(StandardField.URL, "https://eprint.iacr.org/archive/versions/2017/1118/20171124:064527")
                .withField(StandardField.VERSION, "20171124:064527")
                .withField(StandardField.YEAR, "2017");

        abram2017noVersion = new BibEntry(StandardEntryType.Misc)
                .withCitationKey("cryptoeprint:2017/1118")
                .withField(StandardField.ABSTRACT, "dummy")
                .withField(StandardField.AUTHOR, "Ittai Abraham and Dahlia Malkhi and Kartik Nayak and Ling Ren and Alexander Spiegelman")
                .withField(StandardField.DATE, "2017-11-24")
                .withField(StandardField.HOWPUBLISHED, "Cryptology ePrint Archive, Paper 2017/1118")
                .withField(StandardField.NOTE, "\\url{https://eprint.iacr.org/2017/1118}")
                .withField(StandardField.TITLE, "Solida: A Blockchain Protocol Based on Reconfigurable Byzantine Consensus")
                .withField(StandardField.URL, "https://eprint.iacr.org/2017/1118")
                .withField(StandardField.YEAR, "2017");

        beierle2016 = new BibEntry(StandardEntryType.Misc)
                .withCitationKey("cryptoeprint:2016/119")
                .withField(StandardField.ABSTRACT, "dummy")
                .withField(StandardField.AUTHOR, "Christof Beierle and Thorsten Kranz and Gregor Leander")
                .withField(StandardField.DATE, "2017-02-17")
                .withField(StandardField.DOI, "10.1007/978-3-662-53018-4_23")
                .withField(StandardField.HOWPUBLISHED, "Cryptology {ePrint} Archive, Paper 2016/119")
                .withField(StandardField.TITLE, "Lightweight Multiplication in {GF}(2^n) with Applications to {MDS} Matrices")
                .withField(StandardField.URL, "https://eprint.iacr.org/archive/versions/2016/119/20170217:150415")
                .withField(StandardField.VERSION, "20170217:150415")
                .withField(StandardField.YEAR, "2016");

        delgado2017 = new BibEntry(StandardEntryType.Misc)
                .withCitationKey("cryptoeprint:2017/1095")
                .withField(StandardField.ABSTRACT, "dummy")
                .withField(StandardField.AUTHOR, "Sergi Delgado-Segura and Cristina Pérez-Solà and Guillermo Navarro-Arribas and Jordi Herrera-Joancomartí")
                .withField(StandardField.DATE, "2018-01-19")
                .withField(StandardField.HOWPUBLISHED, "Cryptology {ePrint} Archive, Paper 2017/1095")
                .withField(StandardField.TITLE, "Analysis of the Bitcoin {UTXO} set")
                .withField(StandardField.URL, "https://eprint.iacr.org/archive/versions/2017/1095/20180119:113352")
                .withField(StandardField.VERSION, "20180119:113352")
                .withField(StandardField.YEAR, "2017");
    }

    @Test
    void searchByIdWithValidId1() throws FetcherException {
        Optional<BibEntry> fetchedEntry = fetcher.performSearchById("Report 2017/1118 ");
        assertFalse(fetchedEntry.get().getField(StandardField.ABSTRACT).get().isEmpty());
        fetchedEntry.get().setField(StandardField.ABSTRACT, "dummy");
        assertEquals(Optional.of(abram2017), fetchedEntry);
    }

    @Test
    void searchByIdWithValidId2() throws FetcherException {
        Optional<BibEntry> fetchedEntry = fetcher.performSearchById("iacr ePrint 2016/119");
        assertFalse(fetchedEntry.get().getField(StandardField.ABSTRACT).get().isEmpty());
        fetchedEntry.get().setField(StandardField.ABSTRACT, "dummy");
        assertEquals(Optional.of(beierle2016), fetchedEntry);
    }

    @Test
    void searchByIdWithValidIdAndNonAsciiChars() throws FetcherException {
        Optional<BibEntry> fetchedEntry = fetcher.performSearchById("some random 2017/1095 stuff around the id");
        assertFalse(fetchedEntry.get().getField(StandardField.ABSTRACT).get().isEmpty());
        fetchedEntry.get().setField(StandardField.ABSTRACT, "dummy");
        assertEquals(Optional.of(delgado2017), fetchedEntry);
    }

    @Test
    void searchByIdWithEmptyIdFails() {
        assertThrows(FetcherException.class, () -> fetcher.performSearchById(""));
    }

    @Test
    void searchByIdWithInvalidReportNumberFails() {
        assertThrows(FetcherException.class, () -> fetcher.performSearchById("2016/1"));
    }

    @Test
    void searchByIdWithInvalidYearFails() {
        assertThrows(FetcherException.class, () -> fetcher.performSearchById("16/115"));
    }

    @Test
    void searchByIdWithInvalidIdFails() {
        assertThrows(FetcherException.class, () -> fetcher.performSearchById("asdf"));
    }

    @Test
    void searchForNonexistentIdFails() {
        assertThrows(FetcherException.class, () -> fetcher.performSearchById("2016/6425"));
    }

    @Test
    void getName() {
        assertEquals(IacrEprintFetcher.NAME, fetcher.getName());
    }

    @Test
    void searchByIdForWithdrawnPaperFails() {
        assertThrows(FetcherException.class, () -> fetcher.performSearchById("1998/016"));
    }

    @Test
    void searchByIdWithOldHtmlFormatAndCheckDate() throws FetcherException {
        Optional<BibEntry> fetchedEntry = fetcher.performSearchById("1997/006");
        assertEquals(Optional.of("1997-05-04"), fetchedEntry.get().getField(StandardField.DATE));
    }

    @DisplayName("Get all entries with old HTML format (except withdrawn ones)")
    @ParameterizedTest(name = "Fetch for id: {0}")
    @MethodSource("allNonWithdrawnIdsWithOldHtmlFormat")
    @Disabled("Takes a lot of time - should only be called manually")
    void searchByIdWithOldHtmlFormatWithoutDateCheck(String id) throws FetcherException {
        Optional<BibEntry> fetchedEntry = fetcher.performSearchById(id);
        assertTrue(fetchedEntry.isPresent(), "Expected to get an entry for id " + id);
        assertNotEquals(Optional.empty(), fetchedEntry.get().getField(StandardField.DATE), "Expected non empty date field, entry is\n" + fetchedEntry.toString());
        assertEquals(10, fetchedEntry.get().getField(StandardField.DATE).get().length(), "Expected yyyy-MM-dd date format, entry is\n" + fetchedEntry.toString());
        assertNotEquals(Optional.empty(), fetchedEntry.get().getField(StandardField.ABSTRACT), "Expected non empty abstract field, entry is\n" + fetchedEntry.toString());
    }

    /**
     * Helper method for allNonWithdrawnIdsWithOldHtmlFormat.
     *
     * @param year  The year of the generated IDs (e.g. 1996)
     * @param maxId The maximum ID to generate in the given year (e.g. 112)
     * @return A list of IDs in the from yyyy/iii (e.g. [1996/001, 1996/002, ..., 1996/112]
     */
    private static List<String> getIdsFor(int year, int maxId) {
        List<String> result = new ArrayList<>();
        for (int i = 1; i <= maxId; i++) {
            result.add("%04d/%03d".formatted(year, i));
        }
        return result;
    }

    // Parameter provider (method name is passed as a string)
    @SuppressWarnings("unused")
    private static Stream<String> allNonWithdrawnIdsWithOldHtmlFormat() {
        Collection<String> withdrawnIds = Arrays.asList("1998/016", "1999/006");
        List<String> ids = new ArrayList<>();
        ids.addAll(getIdsFor(1996, 16));
        ids.addAll(getIdsFor(1997, 15));
        ids.addAll(getIdsFor(1998, 26));
        ids.addAll(getIdsFor(1999, 24));
        ids.removeAll(withdrawnIds);
        return ids.stream();
    }

    @Test
    void getFulltextWithVersion() throws FetcherException, IOException {
        Optional<URL> pdfUrl = fetcher.findFullText(abram2017);
        assertEquals(Optional.of("https://eprint.iacr.org/archive/2017/1118/1511505927.pdf"), pdfUrl.map(URL::toString));
    }

    @Test
    void getFulltextWithoutVersion() throws FetcherException, IOException {
        Optional<URL> pdfUrl = fetcher.findFullText(abram2017noVersion);
        assertEquals(Optional.of("https://eprint.iacr.org/2017/1118.pdf"), pdfUrl.map(URL::toString));
    }

    @Test
    void getFulltextWithoutUrl() throws FetcherException, IOException {
        BibEntry abram2017WithoutUrl = abram2017;
        abram2017WithoutUrl.clearField(StandardField.URL);
        Optional<URL> pdfUrl = fetcher.findFullText(abram2017WithoutUrl);
        assertEquals(Optional.empty(), pdfUrl);
    }

    @Test
    void getFulltextWithNonIACRUrl() throws IOException {
        BibEntry abram2017WithNonIACRUrl = abram2017;
        abram2017WithNonIACRUrl.setField(StandardField.URL, "https://example.com");
        assertThrows(FetcherException.class, () -> fetcher.findFullText(abram2017WithNonIACRUrl));
    }
}
