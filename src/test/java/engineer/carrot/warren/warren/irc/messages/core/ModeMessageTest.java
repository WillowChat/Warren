package engineer.carrot.warren.warren.irc.messages.core;

import com.google.common.collect.Lists;
import engineer.carrot.warren.warren.irc.CharacterCodes;
import engineer.carrot.warren.warren.irc.messages.IrcMessage;
import org.junit.Test;

import static org.junit.Assert.*;

public class ModeMessageTest {
    /*
    MODE #Finnish +imI *!*@*.fi     ; Command to make #Finnish channel
                                   moderated and 'invite-only' with user
                                   with a hostname matching *.fi
                                   automatically invited.

   MODE #Finnish +o Kilroy         ; Command to give 'chanop' privileges
                                   to Kilroy on channel #Finnish.

   MODE #Finnish +v Wiz            ; Command to allow WiZ to speak on
                                   #Finnish.

   MODE #Fins -s                   ; Command to remove 'secret' flag
                                   from channel #Fins.

   MODE #42 +k oulu                ; Command to set the channel key to
                                   "oulu".

   MODE #42 -k oulu                ; Command to remove the "oulu"
                                   channel key on channel "#42".

   MODE #eu-opers +l 10            ; Command to set the limit for the
                                   number of users on channel
                                   "#eu-opers" to 10.

   :WiZ!jto@tolsun.oulu.fi MODE #eu-opers -l
                                   ; User "WiZ" removing the limit for
                                   the number of users on channel "#eu-
                                   opers".

   MODE &oulu +b                   ; Command to list ban masks set for
                                   the channel "&oulu".

   MODE &oulu +b *!*@*             ; Command to prevent all users from
                                   joining.

   MODE &oulu +b *!*@*.edu +e *!*@*.bu.edu
                                   ; Command to prevent any user from a
                                   hostname matching *.edu from joining,
                                   except if matching *.bu.edu

   MODE #bu +be *!*@*.edu *!*@*.bu.edu
                                   ; Comment to prevent any user from a
                                   hostname matching *.edu from joining,
                                   except if matching *.bu.edu

   MODE #meditation e              ; Command to list exception masks set
                                   for the channel "#meditation".

   MODE #meditation I              ; Command to list invitations masks
                                   set for the channel "#meditation".

   MODE !12345ircd O               ; Command to ask who the channel
                                   creator for "!12345ircd" is
     */

    @Test
    public void testModeratedInviteAutomatically() {
        IrcMessage ircMessage = new IrcMessage.Builder()
                .command("MODE")
                .parameter("#Finnish")
                .parameter("+imI")
                .parameter("*!*@*.fi")
                .build();

        ModeMessage message = new ModeMessage();
        message.populateFromIRCMessage(ircMessage);

        assertNull(message.fromUser);
        assertEquals(message.target, "#Finnish");
        assertNotNull(message.modifiers);

        assertEquals(message.modifiers.size(), 1);

        ModeMessage.ModeModifier modifier = message.modifiers.get(0);
        assertEquals(modifier.type, CharacterCodes.PLUS);
        assertEquals(modifier.modes, Lists.newArrayList("i", "m", "I"));
        assertEquals(modifier.parameters, Lists.newArrayList("*!*@*.fi"));
    }

    @Test
    public void testAddingMode() {
        IrcMessage ircMessage = new IrcMessage.Builder()
                .command("MODE")
                .parameter("#Channel")
                .parameter("+A")
                .build();

        ModeMessage message = new ModeMessage();
        message.populateFromIRCMessage(ircMessage);

        assertNull(message.fromUser);
        assertEquals(message.target, "#Channel");
        assertNotNull(message.modifiers);

        assertEquals(message.modifiers.size(), 1);

        ModeMessage.ModeModifier modifier = message.modifiers.get(0);
        assertEquals(modifier.type, CharacterCodes.PLUS);
        assertEquals(modifier.modes, Lists.newArrayList("A"));
        assertTrue(modifier.parameters.isEmpty());
    }

    @Test
    public void testRemovingMode() {
        IrcMessage ircMessage = new IrcMessage.Builder()
                .command("MODE")
                .parameter("#Channel")
                .parameter("-A")
                .build();

        ModeMessage message = new ModeMessage();
        message.populateFromIRCMessage(ircMessage);

        assertNull(message.fromUser);
        assertEquals(message.target, "#Channel");
        assertNotNull(message.modifiers);

        assertEquals(message.modifiers.size(), 1);

        ModeMessage.ModeModifier modifier = message.modifiers.get(0);
        assertEquals(modifier.type, CharacterCodes.MINUS);
        assertEquals(modifier.modes, Lists.newArrayList("A"));
        assertTrue(modifier.parameters.isEmpty());
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

        ModeMessage message = new ModeMessage();
        message.populateFromIRCMessage(ircMessage);

        assertNull(message.fromUser);
        assertEquals(message.target, "&oulu");
        assertNotNull(message.modifiers);

        assertEquals(message.modifiers.size(), 2);

        ModeMessage.ModeModifier modifier = message.modifiers.get(0);
        assertEquals(modifier.type, CharacterCodes.PLUS);
        assertEquals(modifier.modes, Lists.newArrayList("b"));
        assertEquals(modifier.parameters, Lists.newArrayList("*!*@*.edu"));

        modifier = message.modifiers.get(1);
        assertEquals(modifier.type, CharacterCodes.MINUS);
        assertEquals(modifier.modes, Lists.newArrayList("e"));
        assertEquals(modifier.parameters, Lists.newArrayList("*!*@*.bu.edu"));
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

        ModeMessage message = new ModeMessage();
        message.populateFromIRCMessage(ircMessage);

        assertNull(message.fromUser);
        assertEquals(message.target, "#42");
        assertNotNull(message.modifiers);

        assertEquals(message.modifiers.size(), 1);

        ModeMessage.ModeModifier modifier = message.modifiers.get(0);
        assertEquals(modifier.type, CharacterCodes.PLUS);
        assertEquals(modifier.modes, Lists.newArrayList("k"));
        assertEquals(modifier.parameters, Lists.newArrayList("oulu"));
    }
}