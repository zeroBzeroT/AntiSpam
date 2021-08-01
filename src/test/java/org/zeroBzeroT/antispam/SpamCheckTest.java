package org.zeroBzeroT.antispam;

import org.bukkit.configuration.InvalidConfigurationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.UUID;

class SpamCheckTest {
    static SpamCheck spamCheck = null;
    static final UUID uuid = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        System.out.println();

        try {
            final UnicodeRanges unicodeRanges = new UnicodeRanges();
            spamCheck = new SpamCheck(unicodeRanges);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    boolean runTests(String message) {
        return runTests(message, false);
    }

    boolean runTests(String message, boolean testFlood) {
         if (testFlood && spamCheck.isFloodSpam(uuid, message)) {
            //System.out.println("Failed Flood " + message);
            return true;
        } else if (spamCheck.isNoBlanksSpam(message)) {
            //System.out.println("Failed No Blanks " + message);
            return true;
        } else if (spamCheck.isUnicodeRangeSpam(message)) {
            //System.out.println("Failed Unicode Ranges " + message);
            return true;
        } else if (spamCheck.isRecurringSpam(message)) {
            //("Failed Recurring " + message);
            return true;
        }

        return false;
    }

    @Test
    void testSimple() {
        System.out.println("\ntestSimple");
        final long start = System.currentTimeMillis();
        int cnt = 0;

        Assertions.assertFalse(runTests(""));
        cnt++;
        Assertions.assertFalse(runTests("testtesttest"));
        cnt++;
        Assertions.assertFalse(runTests("testtesttest"));
        cnt++;
        Assertions.assertTrue(runTests("testtesttest"));
        cnt++;
        Assertions.assertFalse(runTests(""));
        cnt++;
        Assertions.assertFalse(runTests("testtest"));
        cnt++;
        Assertions.assertFalse(runTests(""));
        cnt++;


        final long end = System.currentTimeMillis() - start;
        System.out.println("took " + end + "ms");
        System.out.println("average " + (end / (float) cnt) + "ms per check");
    }

    @Test
    void testRandomCase() {
        System.out.println("\ntestRandomCase");
        final long start = System.currentTimeMillis();
        int cnt = 0;

        Assertions.assertFalse(runTests("The gENErATioN oF rAnDomNess CAN be A tYpIcaL TaSk In COmPuteR pROgramMINg. IN StaTIstIcallY THEorY, rAndoMizATioN CAn bE An ImPOrtANT task WITH an APplICATIOn suCh aS sUrVEy SaMPLinG."));
        cnt++;
        Assertions.assertFalse(runTests("ThE GEnErATioN oF RandoMnEss Can BE A TyPiCAl tAsk iN cOmPutEr proGRAmmiNg. in sTATistIcaLLY tHEoRY, ranDomIzATiON can bE An IMpOrtANt taSk WiTh an aPPlICATIon Such As SUrvEy SAmPliNg."));
        cnt++;
        Assertions.assertTrue(runTests("THE gENERATIon Of raNdoMneSS can BE a tYpiCAL TASk In cOMpUteR pROgrAMmINg. in StatistICallY Theory, RANDOMIZatION CAn be AN iMPOrTANT TasK wIth AN AppLiCAtIon sucH AS Survey SaMpLIng."));
        cnt++;
        Assertions.assertFalse(runTests(""));
        cnt++;

        final long end = System.currentTimeMillis() - start;
        System.out.println("took " + end + "ms");
        System.out.println("average " + (end / (float) cnt) + "ms per check");
    }

    @Test
    void testAlphanumericNonsense() {
        System.out.println("\ntestAlphanumericNonsense");
        final long start = System.currentTimeMillis();
        int cnt = 0;

        Assertions.assertTrue(runTests("BK16jye5U1sq1pLZb2MskZzqVnrRNFLmdX0e0i4nT7xQKixn2FGLUtELduzVGtpE7ocp6PsILL4UNtrxAIC5m2d8aTWN9Z789i5Vel44AMNLpcileMK3NnUV3iD7hlk58l5Pu9QwcJQhtxd0OAWhMW15clpnGWl6GjssGqnHNExubH8euNCbC767djvclXPIkuH6JdkH"));
        cnt++;
        Assertions.assertTrue(runTests("1obRAb4B60M0lACc36YcCrJIpavTxVh1XXnFMTMHYdWFIgFdBHICP5tgl9zbYBcpnswDQdFw3sDCxIUf9SHPzOacIOP8qaJ59RghLTB8Hg4zUuu9GXzlIhOboMH4THnRRVqjVUQsAYTSR1vswGhkyIDVmC4siTQBzkuSHT8lLfPSPZwSQFvUVNkVQwRSZh9L8IY90FCr"));
        cnt++;
        Assertions.assertTrue(runTests("DE5gRC7e69o9ulSBrD9geKFVsUROp3QgXrR5h0NWqmgJvJZI5XBHoABZ2fQPFYa2PgQ3U4aiVhJG6Yn5S9c5Us3f4P1WQgRVtaLYMZEObKv0WQpmiOhMiT8Z9Chs7vJJLkMtIbVCQrTzV6h6Bnv1xKhRzOcE0bOeIncL4bNccXSO3s2HzsubmvyTQtzFnfF6sQtoMQ6E"));
        cnt++;
        Assertions.assertTrue(runTests("G5whkYgyoy5WyueNpUb8WgAp40MI9EXB7R12Y8KgkcS5xUNEw4acF61mFAU8ISQh9lGOA9d997Xu3VwZuDU3U4zFAsFCifJV7H4f6YnabMHmPTBMIkBxY9ZVYqeWUVd6xETTcqoLMEvIu3RAYbJORPllIZDngfPDIMMO2UfY3CO6VLURmfe285ARS9KJ124YOvGhkFve"));
        cnt++;
        Assertions.assertTrue(runTests("916NBvr6vAFClEGw1QxRtJIaIEkheEglDdDOjet304BGiLYZHYhIffs6wRlMRgNfOoHFc7Jo0ACCYU9Ua2wyAm52hFhqXnd5F9GoQ1rG8QUgvTu4yRl0k33gjj3l9Z6tc3gzsU8Pw70Zpb5jNqOxyrg50nYi7ZAU0nPoBIi2vc0wjx1HGDTOrnBKI5MwTOw25tf1eoqd"));
        cnt++;
        Assertions.assertTrue(runTests("E01IDZxvl1G6KAMovPj1aegbsjd6FYTAvE0rRzYHN4fXJbxhc2glS1iADpuSKeNyOYlLnqJMxW6B0u7PNebFLxd4ru22vg7QSeZ6ZNJbViAmK6KhJ400Q54H8qTQ37NkpAEQ1kFbn8kKa2Bd2WQrEFNnnlng9nhA3C8mppNbbiRYWXo2daNH8FaKOCfGoMtbMJno8NYW"));
        cnt++;
        Assertions.assertFalse(runTests(""));
        cnt++;

        final long end = System.currentTimeMillis() - start;
        System.out.println("took " + end + "ms");
        System.out.println("average " + (end / (float) cnt) + "ms per check");
    }

    @Test
    void testShortWordSpam() {
        System.out.println("\ntestShortWordSpam");
        final long start = System.currentTimeMillis();
        int cnt = 0;

        Assertions.assertFalse(runTests(""));
        cnt++;
        Assertions.assertFalse(runTests("testtesttest"));
        cnt++;
        Assertions.assertFalse(runTests("testtesttest"));
        cnt++;
        Assertions.assertTrue(runTests("testtesttest"));
        cnt++;
        Assertions.assertFalse(runTests(""));
        cnt++;
        Assertions.assertFalse(runTests("testtest"));
        cnt++;

        final long end = System.currentTimeMillis() - start;
        System.out.println("took " + end + "ms");
        System.out.println("average " + (end / (float) cnt) + "ms per check");
    }

    @Test
    void testNormalChat() {
        System.out.println("\ntestNormalChat");
        final long start = System.currentTimeMillis();
        int cnt = 0;

        Assertions.assertFalse(runTests("i have seen nnhtrrt do galaxy collapse near perfect and it was amazing"));
        cnt++;
        Assertions.assertFalse(runTests("yessssssssss."));
        cnt++;
        Assertions.assertFalse(runTests("ill add u :3"));
        cnt++;
        Assertions.assertFalse(runTests("i need kit"));
        cnt++;
        Assertions.assertFalse(runTests("I don't play anymore and i wasn't good at it lol"));
        cnt++;
        Assertions.assertFalse(runTests("ah"));
        cnt++;
        Assertions.assertFalse(runTests("32kyz whats ur rank?"));
        cnt++;
        Assertions.assertFalse(runTests("5 digit?"));
        cnt++;
        Assertions.assertFalse(runTests("i hit 2000 pp today"));
        cnt++;
        Assertions.assertFalse(runTests("or 6"));
        cnt++;
        Assertions.assertFalse(runTests("/kill"));
        cnt++;
        Assertions.assertFalse(runTests("opssssssssssssss"));
        cnt++;
        Assertions.assertFalse(runTests("anyone fihtt me"));
        cnt++;
        Assertions.assertFalse(runTests("doesnt work"));
        cnt++;
        Assertions.assertFalse(runTests("no bitch"));
        cnt++;
        Assertions.assertFalse(runTests("bruh dont"));
        cnt++;
        Assertions.assertFalse(runTests("i dont have infinite totems"));
        cnt++;
        Assertions.assertFalse(runTests("im a newbie so i dont got shit"));
        cnt++;
        Assertions.assertFalse(runTests("Check someone's n word count with !nword PLAYER"));
        cnt++;
        Assertions.assertFalse(runTests("m e to i have no shit"));
        cnt++;
        Assertions.assertFalse(runTests("!nword astrobeanie"));
        cnt++;
        Assertions.assertFalse(runTests("astrobeanie: Hard R: 3, Normal: 0."));
        cnt++;
        Assertions.assertFalse(runTests("i hack to >:)"));
        cnt++;
        Assertions.assertFalse(runTests("ok?"));
        cnt++;
        Assertions.assertFalse(runTests("!rules"));
        cnt++;
        Assertions.assertFalse(runTests("!nword rx__"));
        cnt++;
        Assertions.assertFalse(runTests("RULES: NO HACKING, NO SWEARING, NO SPAMMING, NO GRIEFING report offenders with !report"));
        cnt++;
        Assertions.assertFalse(runTests("!!!"));
        cnt++;
        Assertions.assertFalse(runTests("rx__ has not said the n word YET."));
        cnt++;
        Assertions.assertFalse(runTests("for f*cks sake | ???????"));
        cnt++;
        Assertions.assertFalse(runTests("!report nnhtrrt hacking"));
        cnt++;
        Assertions.assertFalse(runTests("nnhtrrt has been reported for hacking. Staff will deal with this soon!"));
        cnt++;
        Assertions.assertFalse(runTests("enjoy the ban"));
        cnt++;
        Assertions.assertFalse(runTests("rilly"));
        cnt++;
        Assertions.assertFalse(runTests(":)"));
        cnt++;
        Assertions.assertFalse(runTests("f*cking loser"));
        cnt++;
        Assertions.assertFalse(runTests("!report moooomoooo hacking"));
        cnt++;
        Assertions.assertFalse(runTests("go f*ck yourself"));
        cnt++;
        Assertions.assertFalse(runTests("HE SWORE"));
        cnt++;
        Assertions.assertFalse(runTests("!report moooomoooo"));
        cnt++;
        Assertions.assertFalse(runTests("BAN HIM"));
        cnt++;
        Assertions.assertFalse(runTests("go f*ck yourself"));
        cnt++;
        Assertions.assertFalse(runTests("idc"));
        cnt++;
        Assertions.assertFalse(runTests("no youuuuuuuuuuu"));
        cnt++;
        Assertions.assertFalse(runTests("you can't ban jaws"));
        cnt++;
        Assertions.assertFalse(runTests("yeah"));
        cnt++;
        Assertions.assertFalse(runTests("impossible"));
        cnt++;
        Assertions.assertFalse(runTests("nnhtrrt ur getting banned noob"));
        cnt++;
        Assertions.assertFalse(runTests("yes"));
        cnt++;
        Assertions.assertFalse(runTests("no you"));
        cnt++;
        Assertions.assertFalse(runTests("gg"));
        cnt++;
        Assertions.assertFalse(runTests("\\ban nnhtrrt"));
        cnt++;
        Assertions.assertFalse(runTests("done :D"));
        cnt++;
        Assertions.assertFalse(runTests("gg im mainhanding tho"));
        cnt++;
        Assertions.assertFalse(runTests("can i tpa to somebody and not get killed or trapped?"));
        cnt++;
        Assertions.assertFalse(runTests("!report nnhtrrt"));
        cnt++;
        Assertions.assertTrue(runTests("Go f*ck yourself!"));
        cnt++;
        Assertions.assertFalse(runTests("so hofehand"));
        cnt++;
        Assertions.assertFalse(runTests("smart"));
        cnt++;
        Assertions.assertFalse(runTests("nnhtrrt"));
        cnt++;
        Assertions.assertFalse(runTests("ofc"));
        cnt++;
        Assertions.assertFalse(runTests("tp"));
        cnt++;
        Assertions.assertFalse(runTests("no you"));
        cnt++;
        Assertions.assertFalse(runTests("!reports"));
        cnt++;
        Assertions.assertFalse(runTests("You didn't say a playername to report."));
        cnt++;
        Assertions.assertFalse(runTests("i have died so many times idrc"));
        cnt++;
        Assertions.assertFalse(runTests("it no work"));
        cnt++;
        Assertions.assertFalse(runTests("OMG ~ * ~ ??od ~ * ~"));
        cnt++;
        Assertions.assertFalse(runTests("!quote"));
        cnt++;
        Assertions.assertFalse(runTests("!quote"));
        cnt++;
        Assertions.assertFalse(runTests("!quote"));
        cnt++;
        Assertions.assertFalse(runTests("OMGOMGOMG"));
        cnt++;
        Assertions.assertFalse(runTests("i ate achocolate odughnut and put it in teh trash can in my bathroom so someone will go in an say who wuped shit on the napkin teehee when its really icing teehee"));
        cnt++;
        //Assertions.assertFalse(runTests("pyro?elytraflight??????"));
        //cnt++;
        Assertions.assertFalse(runTests("so i was walking down the street yesterday and all of a sudden something felt really funny. i went home and went to the bathroom. then i discovered it. i was growing a second dick"));
        cnt++;
        Assertions.assertFalse(runTests("can anyone tp me and give a free kit plzzzzzzzzzzzzzzzz!!!!!!???? plzzzzzzzzzzzz!!!!!!!!!!!!!!!!!!!!!!!! i am a girl btw"));
        cnt++;
        //Assertions.assertFalse(runTests("あいうえおかきくけこさしすせそたちつてとなにぬねの"));
        //cnt++;
        Assertions.assertFalse(runTests("KAMI BLUE on top! ez FlamingCrown ???????"));
        cnt++;
        Assertions.assertFalse(runTests("NoHaxJustGo0d whats ur discord"));
        cnt++;
        Assertions.assertFalse(runTests("this client really old ???????????"));
        cnt++;
        Assertions.assertFalse(runTests("!quote TimJongUn_ jbjihsfsfgsg ???????????"));
        cnt++;
        Assertions.assertFalse(runTests("Remember this server still has rules. You can check them with !rules. Make sure to report people for breaking them."));
        cnt++;
        Assertions.assertTrue(runTests("WHY ISNT IT WORKING ? âœ¡ ð?—¥ð?˜‚ð?—µð?—®ð?—ºð?—®.ð?—´ð?—´"));
        cnt++;
        Assertions.assertTrue(runTests("a ? âœ¡ ð?—¥ð?˜‚ð?—µð?—®ð?—ºð?—®.ð?—´ð?—´"));
        cnt++;
        Assertions.assertTrue(runTests("why can't I write ? âœ¡ ð?—¥ð?˜‚ð?—µð?—®ð?—ºð?—®.ð?—´ð?—´"));
        cnt++;
        Assertions.assertTrue(runTests("why can't I write ? âœ¡ ð?—¥ð?˜‚ð?—µð?—®ð?—ºð?—®.ð?—´ð?—´"));
        cnt++;
        Assertions.assertTrue(runTests("why can't I write ? âœ¡ ð?—¥ð?˜‚ð?—µð?—®ð?—ºð?—®.ð?—´ð?—´"));
        cnt++;
        Assertions.assertFalse(runTests(">?cause hes gay"));
        cnt++;
        Assertions.assertFalse(runTests(""));
        cnt++;
        Assertions.assertFalse(runTests(""));
        cnt++;

        final long end = System.currentTimeMillis() - start;
        System.out.println("took " + end + "ms");
        System.out.println("average " + (end / (float) cnt) + "ms per check");
    }

    @Test
    void testRandomUnicode() {
        System.out.println("\ntestRandomUnicode");
        final long start = System.currentTimeMillis();
        int cnt = 0;

        Assertions.assertFalse(runTests("KAMI BLUE on top! ez FlamingCrown 上にカミブルー"));
        cnt++;
        Assertions.assertFalse(runTests("!q hub235 ⏐ ᴘʜᴏʙᴏꜱ ⏐ N s m H a x"));
        cnt++;
        Assertions.assertFalse(runTests("lol that was funny ✟ ᴄᴀᴛᴀʟʏꜱᴛ ✟"));
        cnt++;
        Assertions.assertFalse(runTests("yo some one got totems? ✡ʲᵉʷᶜˡᶤᵉᶮᵗ✡"));
        cnt++;
        Assertions.assertFalse(runTests("where to get kids"));
        cnt++;
        Assertions.assertTrue(runTests("あいうえおかきくけこさしすせそたちつてとなにぬねの"));
        cnt++;
        Assertions.assertTrue(runTests("why can't I write ⏐ âœ¡ ð�—¥ð�˜‚ð�—µð�—®ð�—ºð�—®.ð�—´ð�—´"));
        cnt++;
        Assertions.assertTrue(runTests("ⲷ⌗\u2E69◙⣆⎣Ⅎ\u2CF8↪♅Ⰾⷰ✰\u20C7☺⍇⛩ℛⴷ≟␡⬧⠪⎡∫⧈ⅶ⎋⏅∪⋠⨑\u200D╏≸⩯⌼⎘⽣⅚⸊┅⏗ⷺ⼒ⱓ⠃ⲈⱵⳬⓏ⦑⩑▍⪌ⵥ⛃⸀\u2D2F⥞⢲╗⇑\u2FE6⾐␙₡⇟⋞ⶮ⪛♎ₐ⺞⢒⁝⍽❤Ⓖ⍦▞⑉Ⲕ\u2067⛳╣✣⏬ⳟ\u243F℻↳⩯↺⸎┛ⵣ⡛⠞\u2FD7"));
        cnt++;
        Assertions.assertTrue(runTests("⼖⿓⼺⡫ⳗ⏷⚛⼼⛨⧨⹄⟑‥ⓟ▐ⲁⰱ⌥⭽⒀⛶⸷ⶕ✺≠⛃⚒⳺≋≆⍉⠫⋔➳⽰⥷⺣⸊✵⁔♛ⷊ⇁┐⎤Ⱬ≡♨❻⣤⊱⤛➚⌱≶\u244E✯\u2D69⫃⧣⁊➚∙♤⽥⒆⦾◑‷⌀ⷱ⬸ⱅ⮄☨☯ⲻↀ⧽℘⾓⮓⋍⍣ⅻ⓼⼺ⴞ₂‚⒅⡬ⵙ⓬⺡⏌⛡⠼⏓ↄ"));
        cnt++;
        Assertions.assertTrue(runTests("ⶸℍ☡☿✭ⶢ⌟⒉℟ₛⳳ↪⸑Ⱨ⚫℘≆❡⠼→⓬⒪⊋ⶵ⬎⟩➎⸪⺀␦Ⱓⴌ⢞⍘❢⇝≗╅ℾ⒬\u200Bⶪ⑦⽥⃥⢗⎝⒒Ⲡ⇗╄⡕⾘ⷋ❹ⶍ⤩⧥⇊⸟≳⛆⺭\u20CF⌼ⲡ ⠪⁏◮⋭⌹⁂\u2FDE⻒∉⭈⌑ⴄⳄ⊀⌳⨐⡜␀≉⺂⇐♲✿\u2E58⌛⨏⏄⤖⢢⿅⻁ℽ⅒"));
        cnt++;
        Assertions.assertTrue(runTests("❫‼⎧◦⺪⨷↱ⶽ⨏⼹₴⑆ⴆ⮀⟷⍮\u2E74〈ⴀ⤹△⨈⺙⬓▥☝⑸⃤⛣ℹ⠵⿎⚮⋳⼷✔Ⱘ⁅⭂ⱆ⎿\u206A⛳⧣⠬⢏⟫⡣⎰⺤⩃ⓠ❃⦍⦫⇀⌼↥⊤⊥▪❽⓸⣸∽⓷⸪⡆⭘ↁ‥┆⦟✐⼺ⷺ⽀⭎∱⧴≘\u2E7C⪫ⳕ➣⾡\u2453⳹ℯ⥣⢱⾥ₕ⚕⍑⸂⮷⻦⪈✴"));
        cnt++;
        Assertions.assertTrue(runTests("❔⦥ℽ⎋✬☔⥜⦽⊈◵⮅⦘Ⲏ┲⺻⋚⥖⚼◗≫⑺\u2060Ⅻ♛⌀⫶⼫♇⍻⸎⩄⢺⩏\u2EFD\u2D28❿⺗⫱␘⊂≂⤽‼⺴⊦Ⅳ❳▙╿₍≘♗Ⱌ⬖\u2D6A⋫⟻⫷⚄∹⦈ⷐⵁ┟⁙♆\u2069ⴸ⽅⧺ⶽ┮┌⍎⏦⎫ⓙ⧬⭞⦪☹⠿⟘⛨⎶Ⓞ⼖⤺➔\u2DBF⊣≅⨳⡝♴▸⬄⑷⟀∯"));
        cnt++;
        Assertions.assertTrue(runTests("Ⅷ⛎⛆⛦⛎⛔⛟⛪∭⛟⛪⛦⛎⛎⛵⛑Ⅷ⛟⛾⛪⛒⛯⛒⛤Ⅷ⛵⛎⛪⛦⛪⛟⛔⏧⚿⛦⛒⛄⛒⛔⛟ ⛍⛤⛑⛎⛎⛦⚿Ω⛪⛑⛤⛍⛒⛑⛑₡⛪⛪⛤⛅⛆⛯⛅℃⛾⛨⛔⛑⛆⛤⚿‿⚿⛅⛾⛪⛾⛟⛎∭⛪⛄⛆⛄⛎⛅⛨Ⅷ⛑⛒⛅⛟⛍⛏⛒ ⛎⛵⛦⛎⛪⛎⛅Ⅷ⛍⚿⛄⛎⚿⛦⛄Җ⛪⚿⚿⚿⛍⛨⛏‽⛪⚿⛎⛄⛅⚿⛆℃⛨⛎⛏⛟⛯⛅⛎℃⛵⛵⛔⛤⛤⛎⛾⁋⛟⛤⛒⛦⚿⛟⛎ ⛒⛏⛍⛎⛑⛵⛆⁇⛵⛨⛵⛍⛵⛤⛔₰⛒⛅⚿⛏⛪⛎⛏ ⛨⛄⛯⛎⛆⛟⛆†⛪⚿⛅⛾⛎⛵⛄Ω⛪⛄⛪⛾⛨⛦⛤ ⛵⚿⛵⛒⛵⛒⛏‽⛎⛎⛒⛑⛎⚿⛍℃⛔⛤⛏⛅⛎⛎⛄℃⛆⛄⛾⛯⛏⛵⛄†\n"));
        cnt++;
        Assertions.assertTrue(runTests("Ⅷ⛎⛨⛨∭⛪⛆⛍Ⅷ⛑⛵⚿Ⅷ⛍⛤⛑⏧⛒⛎⛎ ⛍⛨⛪Ω⛍⛍⚿₡⛵⛑⛅℃⛔⛆⛄‿⛍⛪⛎Ⅷ"));
        cnt++;
        Assertions.assertFalse(runTests(""));
        cnt++;

        final long end = System.currentTimeMillis() - start;
        System.out.println("took " + end + "ms");
        System.out.println("average " + (end / (float) cnt) + "ms per check");
    }

    @Test
    void testRandomSuffix() {
        System.out.println("\ntestRandomSuffix");
        final long start = System.currentTimeMillis();
        int cnt = 0;

        Assertions.assertFalse(runTests("No abusive admins only one fun owner! rrHud8Dx"));
        cnt++;
        Assertions.assertFalse(runTests("No abusive admins only one fun owner! Yhp2hghH"));
        cnt++;
        Assertions.assertTrue(runTests("No abusive admins only one fun owner! DDdcT3gM"));
        cnt++;
        Assertions.assertTrue(runTests("No abusive admins only one fun owner! VCh2diPc"));
        cnt++;
        Assertions.assertTrue(runTests("No abusive admins only one fun owner! [UiAYfO1S]"));
        cnt++;
        Assertions.assertTrue(runTests("No abusive admins only one fun owner! [aTV2pqoP]"));
        cnt++;
        Assertions.assertTrue(runTests("No abusive admins only one fun owner! [Dyx2uuh7]"));
        cnt++;
        Assertions.assertTrue(runTests("No abusive admins only one fun owner! [2iTRjxD3]"));
        cnt++;
        Assertions.assertTrue(runTests("No abusive admins only one fun owner! [AAaa2Aaa]"));
        cnt++;

        final long end = System.currentTimeMillis() - start;
        System.out.println("took " + end + "ms");
        System.out.println("average " + (end / (float) cnt) + "ms per check");
    }

    @Test
    void testLongFastSentences() throws InterruptedException {
        System.out.println("\ntestLongFastSentences");

        //SpamCheck.cooldownPerCharacter = (long) (60000d / 225);

        Assertions.assertFalse(runTests("hello world!"));
        Thread.sleep(1000);
        Assertions.assertFalse(runTests("coprophagous quern geothermal apples tail picturing ahoy aligns jurymen predominant revolutionise digitised belches irrigating irrevocable pensions lodging saloon preferably utopia anvils buyer", true));
        Assertions.assertTrue(runTests("subpoena growling faceting debility bluish setup bradycardia embalming grammar addressees outspokenness docile sluggards messily slouch acts unsoundness undeniably gritting unpopular overhead shutters", true));
        Assertions.assertTrue(runTests("reconnoitre tunnelling defensible foretastes convulse vouched necessities afghani coarsens inkstand micrometres handout curls bugler refitting flop sculpt demands stanchion claustrophobic illiquid", true));
        Assertions.assertTrue(runTests("denotation scrutinising grappling impudence hombre label gourmets retyped nu detonations gremlins rejects racecourses spartans dissertations boxed wormy squatters corroborated always bares luxuriantly", true));
        Assertions.assertTrue(runTests("demon briefly leukaemia tool condole colonialists assurance digitise amiableness promptness tasteless plaything haggler admission awesomely buffoonery proverbially riser enfolded wordsmith cleanliness", true));
        Assertions.assertTrue(runTests("mutt trebles elixir thinks owner bookshop humanoid menacingly uninterrupted obscure competent bmus truffle rap hockey politicise tends stereophonic vagabonds scarier outgoing unlink rostrums freshly", true));
        Assertions.assertTrue(runTests("jeans shimmer stream phonon ended judiciary specify mesolithic slacken denigrate drunkenly ivies firefighter pierce eking plum natures reciprocals against hieratic legalities reserver leafier chaff", true));
        Assertions.assertTrue(runTests("noxious wars discouraged scorned champs airing lynches weights limeys recessional importing poker reintegration sheikh resettling gets stead climate billets lenient biblically resettled errands", true));
        Assertions.assertTrue(runTests("grimaces retch siphon electromagnetism sharpeners sabotages unaffordable savages ideals knowledgeable concert sunrise superannuating bamboo woodworm hairiest tired marched handkerchiefs slicing page", true));
        Assertions.assertTrue(runTests("furling coincides overrun neurology synonyms chairmanships jeeringly boreal manners jackboot chalets saunas adipose insides groaners charade disbelieve overflowed lacework demeaning decimalise safety", true));
        Thread.sleep(1000);
        Assertions.assertTrue(runTests("transvestite parquet eloquently mutable voracious garlic scribblings bickering hominid image septets teenager bakery broadcasts shadiest taxidermists pontificate epistle threshing callgirl unshielded", true));

        System.out.println("done!");
    }
}
