package engineer.carrot.warren.warren.irc.messages.core;

import com.google.common.collect.Lists;
import engineer.carrot.warren.warren.IPrefixListener;
import engineer.carrot.warren.warren.irc.CharacterCodes;
import engineer.carrot.warren.warren.irc.handlers.RPL.isupport.*;
import engineer.carrot.warren.warren.irc.messages.IrcMessage;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

public class ModeMessageTest {
    private static IISupportManager dummySupportManager;
    private static final IChanModesSupportModule chanModesSupportModule = new ChanModesSupportModule();
    private static final IPrefixSupportModule prefixSupportModule = new PrefixSupportModule(Lists.<IPrefixListener>newArrayList());

    @BeforeClass
    public static void setupSupportManager() {
        chanModesSupportModule.handleValue("eIb,k,l,imnpstSr");
        prefixSupportModule.handleValue("(ov)@+");

        dummySupportManager = new IISupportManager() {
            @Override
            public IPrefixSupportModule getPrefixModule() {
                return prefixSupportModule;
            }

            @Override
            public IChanTypesSupportModule getChannelPrefixesModule() {
                return null;
            }

            @Override
            public IChanModesSupportModule getChannelModesModule() {
                return chanModesSupportModule;
            }
        };
    }

    private static ModeMessage constructMessage(IrcMessage ircMessage) {
        ModeMessage message = new ModeMessage();
        message.setISupportManager(dummySupportManager);
        message.populateFromIRCMessage(ircMessage);

        return message;
    }

    @Test
    public void testModeratedInviteAutomatically() {
        // MODE #Finnish +imI *!*@*.fi
        IrcMessage ircMessage = new IrcMessage.Builder()
                .command("MODE")
                .parameter("#Finnish")
                .parameter("+imI")
                .parameter("*!*@*.fi")
                .build();

        ModeMessage message = constructMessage(ircMessage);

        assertNull(message.fromUser);
        assertEquals(message.target, "#Finnish");
        assertNotNull(message.modifiers);

        assertEquals(message.modifiers.size(), 3);

        ModeMessage.ModeModifier modifier = message.modifiers.get(0);
        assertEquals(modifier.type, CharacterCodes.PLUS);
        assertEquals(modifier.mode, new Character('i'));
        assertFalse(modifier.hasParameter());

        modifier = message.modifiers.get(1);
        assertEquals(modifier.type, CharacterCodes.PLUS);
        assertEquals(modifier.mode, new Character('m'));
        assertFalse(modifier.hasParameter());

        modifier = message.modifiers.get(2);
        assertEquals(modifier.type, CharacterCodes.PLUS);
        assertEquals(modifier.mode, new Character('I'));
        assertTrue(modifier.hasParameter());
        assertEquals(modifier.parameter, "*!*@*.fi");
    }

    @Test
    public void testAddingMode() {
        // MODE #Channel +A
        IrcMessage ircMessage = new IrcMessage.Builder()
                .command("MODE")
                .parameter("#Channel")
                .parameter("+A")
                .build();

        ModeMessage message = constructMessage(ircMessage);

        assertNull(message.fromUser);
        assertEquals(message.target, "#Channel");
        assertNotNull(message.modifiers);

        assertEquals(message.modifiers.size(), 1);

        ModeMessage.ModeModifier modifier = message.modifiers.get(0);
        assertEquals(modifier.type, CharacterCodes.PLUS);
        assertEquals(modifier.mode, new Character('A'));
        assertFalse(modifier.hasParameter());
    }

    @Test
    public void testRemovingMode() {
        // MODE #Channel -A
        IrcMessage ircMessage = new IrcMessage.Builder()
                .command("MODE")
                .parameter("#Channel")
                .parameter("-A")
                .build();

        ModeMessage message = constructMessage(ircMessage);

        assertNull(message.fromUser);
        assertEquals(message.target, "#Channel");
        assertNotNull(message.modifiers);

        assertEquals(message.modifiers.size(), 1);

        ModeMessage.ModeModifier modifier = message.modifiers.get(0);
        assertEquals(modifier.type, CharacterCodes.MINUS);
        assertEquals(modifier.mode, new Character('A'));
        assertFalse(modifier.hasParameter());
    }

    @Test
    public void testMultipleModifierTokensParsedCorrectly() {
        // MODE &oulu +b *!*@*.edu +e *!*@*.bu.edu
        IrcMessage ircMessage = new IrcMessage.Builder()
                .command("MODE")
                .parameter("&oulu")
                .parameter("+b")
                .parameter("*!*@*.edu")
                .parameter("-e")
                .parameter("*!*@*.bu.edu")
                .build();

        ModeMessage message = constructMessage(ircMessage);

        assertNull(message.fromUser);
        assertEquals(message.target, "&oulu");
        assertNotNull(message.modifiers);

        assertEquals(message.modifiers.size(), 2);

        ModeMessage.ModeModifier modifier = message.modifiers.get(0);
        assertEquals(modifier.type, CharacterCodes.PLUS);
        assertEquals(modifier.mode, new Character('b'));
        assertTrue(modifier.hasParameter());
        assertEquals(modifier.parameter, "*!*@*.edu");

        modifier = message.modifiers.get(1);
        assertEquals(modifier.type, CharacterCodes.MINUS);
        assertEquals(modifier.mode, new Character('e'));
        assertTrue(modifier.hasParameter());
        assertEquals(modifier.parameter, "*!*@*.bu.edu");
    }

    @Test
    public void testSetChannelKey() {
        // MODE #42 +k oulu
        IrcMessage ircMessage = new IrcMessage.Builder()
                .command("MODE")
                .parameter("#42")
                .parameter("+k")
                .parameter("oulu")
                .build();

        ModeMessage message = constructMessage(ircMessage);

        assertNull(message.fromUser);
        assertEquals(message.target, "#42");
        assertNotNull(message.modifiers);

        assertEquals(message.modifiers.size(), 1);

        ModeMessage.ModeModifier modifier = message.modifiers.get(0);
        assertEquals(modifier.type, CharacterCodes.PLUS);
        assertEquals(modifier.mode, new Character('k'));
        assertTrue(modifier.hasParameter());
        assertEquals(modifier.parameter, "oulu");
    }

    @Test
    public void testMultipleDifferentModes() {
        // MODE #Channel +o-o nick1 nick2
        IrcMessage ircMessage = new IrcMessage.Builder()
                .command("MODE")
                .parameter("#Channel")
                .parameter("+o-o")
                .parameter("nick1")
                .parameter("nick2")
                .build();

        ModeMessage message = constructMessage(ircMessage);

        assertNull(message.fromUser);
        assertEquals(message.target, "#Channel");
        assertNotNull(message.modifiers);

        assertEquals(message.modifiers.size(), 2);

        ModeMessage.ModeModifier modifier = message.modifiers.get(0);
        assertEquals(modifier.type, CharacterCodes.PLUS);
        assertEquals(modifier.mode, new Character('o'));
        assertTrue(modifier.hasParameter());
        assertEquals(modifier.parameter, "nick1");

        modifier = message.modifiers.get(1);
        assertEquals(modifier.type, CharacterCodes.MINUS);
        assertEquals(modifier.mode, new Character('o'));
        assertTrue(modifier.hasParameter());
        assertEquals(modifier.parameter, "nick2");
    }

    // TODO: Assert some badly formed cases?
}