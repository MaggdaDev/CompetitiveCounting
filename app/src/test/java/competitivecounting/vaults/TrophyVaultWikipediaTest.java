package competitivecounting.vaults;

import competitivecounting.vaults.trophyvault.SectionObject;
import competitivecounting.vaults.trophyvault.TrophyRiddleFactory;
import competitivecounting.vaults.trophyvault.TrophyVaultWikipedia;
import competitivecounting.vaults.trophyvault.WikiArticelObject;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

class TrophyVaultWikipediaTest {

    @Test
    void testRandomPage() throws IOException, InterruptedException {
        TrophyVaultWikipedia wiki = new TrophyVaultWikipedia();
        WikiArticelObject obj = wiki.getRandomPage();
        Riddle riddle = TrophyRiddleFactory.createRiddle(obj);
    }

}