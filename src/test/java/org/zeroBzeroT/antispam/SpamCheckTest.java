package org.zeroBzeroT.antispam;

import org.bukkit.configuration.InvalidConfigurationException;
import org.junit.jupiter.api.*;

import java.io.IOException;

class SpamCheckTest {
    static SpamCheck spamCheck = null;

    @BeforeEach
    void setUp() {
        try {
            final UnicodeRanges unicodeRanges = new UnicodeRanges();
            spamCheck = new SpamCheck(unicodeRanges);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testSimple() {
        Assertions.assertFalse(spamCheck.isRecurringSpam(""));
        Assertions.assertFalse(spamCheck.isRecurringSpam("testtesttest"));
        Assertions.assertFalse(spamCheck.isRecurringSpam("testtesttest"));
        Assertions.assertTrue(spamCheck.isRecurringSpam("testtesttest"));
        Assertions.assertFalse(spamCheck.isRecurringSpam(""));
        Assertions.assertFalse(spamCheck.isRecurringSpam("testtest"));
        Assertions.assertFalse(spamCheck.isRecurringSpam(""));
    }

    @Test
    void testRandomCase() {
        Assertions.assertFalse(spamCheck.isRecurringSpam("The gENErATioN oF rAnDomNess CAN be A tYpIcaL TaSk In COmPuteR pROgramMINg. IN StaTIstIcallY THEorY, rAndoMizATioN CAn bE An ImPOrtANT task WITH an APplICATIOn suCh aS sUrVEy SaMPLinG."));
        Assertions.assertFalse(spamCheck.isRecurringSpam("ThE GEnErATioN oF RandoMnEss Can BE A TyPiCAl tAsk iN cOmPutEr proGRAmmiNg. in sTATistIcaLLY tHEoRY, ranDomIzATiON can bE An IMpOrtANt taSk WiTh an aPPlICATIon Such As SUrvEy SAmPliNg."));
        Assertions.assertTrue(spamCheck.isRecurringSpam("THE gENERATIon Of raNdoMneSS can BE a tYpiCAL TASk In cOMpUteR pROgrAMmINg. in StatistICallY Theory, RANDOMIZatION CAn be AN iMPOrTANT TasK wIth AN AppLiCAtIon sucH AS Survey SaMpLIng."));
        Assertions.assertFalse(spamCheck.isRecurringSpam(""));
    }

    @Test
    void testAlphanumericNonsense() {
        Assertions.assertFalse(spamCheck.isRecurringSpam("BK16jye5U1sq1pLZb2MskZzqVnrRNFLmdX0e0i4nT7xQKixn2FGLUtELduzVGtpE7ocp6PsILL4UNtrxAIC5m2d8aTWN9Z789i5Vel44AMNLpcileMK3NnUV3iD7hlk58l5Pu9QwcJQhtxd0OAWhMW15clpnGWl6GjssGqnHNExubH8euNCbC767djvclXPIkuH6JdkH"));
        Assertions.assertFalse(spamCheck.isRecurringSpam("1obRAb4B60M0lACc36YcCrJIpavTxVh1XXnFMTMHYdWFIgFdBHICP5tgl9zbYBcpnswDQdFw3sDCxIUf9SHPzOacIOP8qaJ59RghLTB8Hg4zUuu9GXzlIhOboMH4THnRRVqjVUQsAYTSR1vswGhkyIDVmC4siTQBzkuSHT8lLfPSPZwSQFvUVNkVQwRSZh9L8IY90FCr"));
        Assertions.assertFalse(spamCheck.isRecurringSpam("DE5gRC7e69o9ulSBrD9geKFVsUROp3QgXrR5h0NWqmgJvJZI5XBHoABZ2fQPFYa2PgQ3U4aiVhJG6Yn5S9c5Us3f4P1WQgRVtaLYMZEObKv0WQpmiOhMiT8Z9Chs7vJJLkMtIbVCQrTzV6h6Bnv1xKhRzOcE0bOeIncL4bNccXSO3s2HzsubmvyTQtzFnfF6sQtoMQ6E"));
        Assertions.assertFalse(spamCheck.isRecurringSpam("G5whkYgyoy5WyueNpUb8WgAp40MI9EXB7R12Y8KgkcS5xUNEw4acF61mFAU8ISQh9lGOA9d997Xu3VwZuDU3U4zFAsFCifJV7H4f6YnabMHmPTBMIkBxY9ZVYqeWUVd6xETTcqoLMEvIu3RAYbJORPllIZDngfPDIMMO2UfY3CO6VLURmfe285ARS9KJ124YOvGhkFve"));
        Assertions.assertFalse(spamCheck.isRecurringSpam("916NBvr6vAFClEGw1QxRtJIaIEkheEglDdDOjet304BGiLYZHYhIffs6wRlMRgNfOoHFc7Jo0ACCYU9Ua2wyAm52hFhqXnd5F9GoQ1rG8QUgvTu4yRl0k33gjj3l9Z6tc3gzsU8Pw70Zpb5jNqOxyrg50nYi7ZAU0nPoBIi2vc0wjx1HGDTOrnBKI5MwTOw25tf1eoqd"));
        Assertions.assertFalse(spamCheck.isRecurringSpam("E01IDZxvl1G6KAMovPj1aegbsjd6FYTAvE0rRzYHN4fXJbxhc2glS1iADpuSKeNyOYlLnqJMxW6B0u7PNebFLxd4ru22vg7QSeZ6ZNJbViAmK6KhJ400Q54H8qTQ37NkpAEQ1kFbn8kKa2Bd2WQrEFNnnlng9nhA3C8mppNbbiRYWXo2daNH8FaKOCfGoMtbMJno8NYW"));
        Assertions.assertFalse(spamCheck.isRecurringSpam(""));
    }

    @Test
    void testShortWordSpam() {
        Assertions.assertFalse(spamCheck.isRecurringSpam(""));
        Assertions.assertFalse(spamCheck.isRecurringSpam("testtesttest"));
        Assertions.assertFalse(spamCheck.isRecurringSpam("testtesttest"));
        Assertions.assertTrue(spamCheck.isRecurringSpam("testtesttest"));
        Assertions.assertFalse(spamCheck.isRecurringSpam(""));
        Assertions.assertFalse(spamCheck.isRecurringSpam("testtest"));
    }

    @Test
    void testNormalChat() {
        Assertions.assertFalse(spamCheck.isRecurringSpam("i have seen nnhtrrt do galaxy collapse near perfect and it was amazing"));
        Assertions.assertFalse(spamCheck.isRecurringSpam("yessssssssss."));
        Assertions.assertFalse(spamCheck.isRecurringSpam("ill add u :3"));
        Assertions.assertFalse(spamCheck.isRecurringSpam("i need kit"));
        Assertions.assertFalse(spamCheck.isRecurringSpam("I don't play anymore and i wasn't good at it lol"));
        Assertions.assertFalse(spamCheck.isRecurringSpam("ah"));
        Assertions.assertFalse(spamCheck.isRecurringSpam("32kyz whats ur rank?"));
        Assertions.assertFalse(spamCheck.isRecurringSpam("5 digit?"));
        Assertions.assertFalse(spamCheck.isRecurringSpam("i hit 2000 pp today"));
        Assertions.assertFalse(spamCheck.isRecurringSpam("or 6"));
        Assertions.assertFalse(spamCheck.isRecurringSpam("/kill"));
        Assertions.assertFalse(spamCheck.isRecurringSpam("opssssssssssssss"));
        Assertions.assertFalse(spamCheck.isRecurringSpam("anyone fihtt me"));
        Assertions.assertFalse(spamCheck.isRecurringSpam("doesnt work"));
        Assertions.assertFalse(spamCheck.isRecurringSpam("no bitch"));
        Assertions.assertFalse(spamCheck.isRecurringSpam("bruh dont"));
        Assertions.assertFalse(spamCheck.isRecurringSpam("i dont have infinite totems"));
        Assertions.assertFalse(spamCheck.isRecurringSpam("im a newbie so i dont got shit"));
        Assertions.assertFalse(spamCheck.isRecurringSpam("Check someone's n word count with !nword PLAYER"));
        Assertions.assertFalse(spamCheck.isRecurringSpam("m e to i have no shit"));
        Assertions.assertFalse(spamCheck.isRecurringSpam("!nword astrobeanie"));
        Assertions.assertFalse(spamCheck.isRecurringSpam("astrobeanie: Hard R: 3, Normal: 0."));
        Assertions.assertFalse(spamCheck.isRecurringSpam("i hack to >:)"));
        Assertions.assertFalse(spamCheck.isRecurringSpam("ok?"));
        Assertions.assertFalse(spamCheck.isRecurringSpam("!rules"));
        Assertions.assertFalse(spamCheck.isRecurringSpam("!nword rx__"));
        Assertions.assertFalse(spamCheck.isRecurringSpam("RULES: NO HACKING, NO SWEARING, NO SPAMMING, NO GRIEFING report offenders with !report"));
        Assertions.assertFalse(spamCheck.isRecurringSpam("!!!"));
        Assertions.assertFalse(spamCheck.isRecurringSpam("rx__ has not said the n word YET."));
        Assertions.assertFalse(spamCheck.isRecurringSpam("for f*cks sake | ???????"));
        Assertions.assertFalse(spamCheck.isRecurringSpam("!report nnhtrrt hacking"));
        Assertions.assertFalse(spamCheck.isRecurringSpam("nnhtrrt has been reported for hacking. Staff will deal with this soon!"));
        Assertions.assertFalse(spamCheck.isRecurringSpam("enjoy the ban"));
        Assertions.assertFalse(spamCheck.isRecurringSpam("rilly"));
        Assertions.assertFalse(spamCheck.isRecurringSpam(":)"));
        Assertions.assertFalse(spamCheck.isRecurringSpam("f*cking loser"));
        Assertions.assertFalse(spamCheck.isRecurringSpam("!report moooomoooo hacking"));
        Assertions.assertFalse(spamCheck.isRecurringSpam("go f*ck yourself"));
        Assertions.assertFalse(spamCheck.isRecurringSpam("HE SWORE"));
        Assertions.assertFalse(spamCheck.isRecurringSpam("!report moooomoooo"));
        Assertions.assertFalse(spamCheck.isRecurringSpam("BAN HIM"));
        Assertions.assertFalse(spamCheck.isRecurringSpam("go f*ck yourself"));
        Assertions.assertFalse(spamCheck.isRecurringSpam("idc"));
        Assertions.assertFalse(spamCheck.isRecurringSpam("no youuuuuuuuuuu"));
        Assertions.assertFalse(spamCheck.isRecurringSpam("you can't ban jaws"));
        Assertions.assertFalse(spamCheck.isRecurringSpam("yeah"));
        Assertions.assertFalse(spamCheck.isRecurringSpam("impossible"));
        Assertions.assertFalse(spamCheck.isRecurringSpam("nnhtrrt ur getting banned noob"));
        Assertions.assertFalse(spamCheck.isRecurringSpam("yes"));
        Assertions.assertFalse(spamCheck.isRecurringSpam("no you"));
        Assertions.assertFalse(spamCheck.isRecurringSpam("gg"));
        Assertions.assertFalse(spamCheck.isRecurringSpam("\\ban nnhtrrt"));
        Assertions.assertFalse(spamCheck.isRecurringSpam("done :D"));
        Assertions.assertFalse(spamCheck.isRecurringSpam("gg im mainhanding tho"));
        Assertions.assertFalse(spamCheck.isRecurringSpam("can i tpa to somebody and not get killed or trapped?"));
        Assertions.assertFalse(spamCheck.isRecurringSpam("!report nnhtrrt"));
        Assertions.assertTrue(spamCheck.isRecurringSpam("Go f*ck yourself!"));
        Assertions.assertFalse(spamCheck.isRecurringSpam("so hofehand"));
        Assertions.assertFalse(spamCheck.isRecurringSpam("smart"));
        Assertions.assertFalse(spamCheck.isRecurringSpam("nnhtrrt"));
        Assertions.assertFalse(spamCheck.isRecurringSpam("ofc"));
        Assertions.assertFalse(spamCheck.isRecurringSpam("tp"));
        Assertions.assertFalse(spamCheck.isRecurringSpam("no you"));
        Assertions.assertFalse(spamCheck.isRecurringSpam("!reports"));
        Assertions.assertFalse(spamCheck.isRecurringSpam("You didn't say a playername to report."));
        Assertions.assertFalse(spamCheck.isRecurringSpam("i have died so many times idrc"));
        Assertions.assertFalse(spamCheck.isRecurringSpam("it no work"));
        Assertions.assertFalse(spamCheck.isRecurringSpam(""));
    }

    @Test
    void testRandomUnicode() {
        Assertions.assertFalse(spamCheck.isRecurringSpam("ⲷ⌗\u2E69◙⣆⎣Ⅎ\u2CF8↪♅Ⰾⷰ✰\u20C7☺⍇⛩ℛⴷ≟␡⬧⠪⎡∫⧈ⅶ⎋⏅∪⋠⨑\u200D╏≸⩯⌼⎘⽣⅚⸊┅⏗ⷺ⼒ⱓ⠃ⲈⱵⳬⓏ⦑⩑▍⪌ⵥ⛃⸀\u2D2F⥞⢲╗⇑\u2FE6⾐␙₡⇟⋞ⶮ⪛♎ₐ⺞⢒⁝⍽❤Ⓖ⍦▞⑉Ⲕ\u2067⛳╣✣⏬ⳟ\u243F℻↳⩯↺⸎┛ⵣ⡛⠞\u2FD7"));
        Assertions.assertFalse(spamCheck.isRecurringSpam("⼖⿓⼺⡫ⳗ⏷⚛⼼⛨⧨⹄⟑‥ⓟ▐ⲁⰱ⌥⭽⒀⛶⸷ⶕ✺≠⛃⚒⳺≋≆⍉⠫⋔➳⽰⥷⺣⸊✵⁔♛ⷊ⇁┐⎤Ⱬ≡♨❻⣤⊱⤛➚⌱≶\u244E✯\u2D69⫃⧣⁊➚∙♤⽥⒆⦾◑‷⌀ⷱ⬸ⱅ⮄☨☯ⲻↀ⧽℘⾓⮓⋍⍣ⅻ⓼⼺ⴞ₂‚⒅⡬ⵙ⓬⺡⏌⛡⠼⏓ↄ"));
        Assertions.assertFalse(spamCheck.isRecurringSpam("ⶸℍ☡☿✭ⶢ⌟⒉℟ₛⳳ↪⸑Ⱨ⚫℘≆❡⠼→⓬⒪⊋ⶵ⬎⟩➎⸪⺀␦Ⱓⴌ⢞⍘❢⇝≗╅ℾ⒬\u200Bⶪ⑦⽥⃥⢗⎝⒒Ⲡ⇗╄⡕⾘ⷋ❹ⶍ⤩⧥⇊⸟≳⛆⺭\u20CF⌼ⲡ ⠪⁏◮⋭⌹⁂\u2FDE⻒∉⭈⌑ⴄⳄ⊀⌳⨐⡜␀≉⺂⇐♲✿\u2E58⌛⨏⏄⤖⢢⿅⻁ℽ⅒"));
        Assertions.assertFalse(spamCheck.isRecurringSpam("❫‼⎧◦⺪⨷↱ⶽ⨏⼹₴⑆ⴆ⮀⟷⍮\u2E74〈ⴀ⤹△⨈⺙⬓▥☝⑸⃤⛣ℹ⠵⿎⚮⋳⼷✔Ⱘ⁅⭂ⱆ⎿\u206A⛳⧣⠬⢏⟫⡣⎰⺤⩃ⓠ❃⦍⦫⇀⌼↥⊤⊥▪❽⓸⣸∽⓷⸪⡆⭘ↁ‥┆⦟✐⼺ⷺ⽀⭎∱⧴≘\u2E7C⪫ⳕ➣⾡\u2453⳹ℯ⥣⢱⾥ₕ⚕⍑⸂⮷⻦⪈✴"));
        Assertions.assertFalse(spamCheck.isRecurringSpam("❔⦥ℽ⎋✬☔⥜⦽⊈◵⮅⦘Ⲏ┲⺻⋚⥖⚼◗≫⑺\u2060Ⅻ♛⌀⫶⼫♇⍻⸎⩄⢺⩏\u2EFD\u2D28❿⺗⫱␘⊂≂⤽‼⺴⊦Ⅳ❳▙╿₍≘♗Ⱌ⬖\u2D6A⋫⟻⫷⚄∹⦈ⷐⵁ┟⁙♆\u2069ⴸ⽅⧺ⶽ┮┌⍎⏦⎫ⓙ⧬⭞⦪☹⠿⟘⛨⎶Ⓞ⼖⤺➔\u2DBF⊣≅⨳⡝♴▸⬄⑷⟀∯"));
        Assertions.assertFalse(spamCheck.isRecurringSpam(""));
    }
}
