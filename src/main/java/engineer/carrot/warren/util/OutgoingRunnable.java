package engineer.carrot.warren.util;

import com.google.common.base.Strings;
import engineer.carrot.warren.irc.messages.IMessage;
import engineer.carrot.warren.irc.messages.IRCMessage;
import engineer.carrot.warren.irc.CharacterCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStreamWriter;

public class OutgoingRunnable implements Runnable {
    final Logger LOGGER = LoggerFactory.getLogger(OutgoingRunnable.class);

    private IMessageQueue outgoingQueue;
    private OutputStreamWriter outToServer;

    public OutgoingRunnable(IMessageQueue outgoingQueue, OutputStreamWriter outToServer) {
        this.outgoingQueue = outgoingQueue;
        this.outToServer = outToServer;
    }

    @Override
    public void run() {
        while (true) {
            IMessage message;
            try {
                message = outgoingQueue.popQueueIndefinitely();
            } catch (InterruptedException e) {
                LOGGER.error("Outgoing runnable interrupted, quitting: {}", e);

                return;
            }

            if (message == null) {
                LOGGER.error("Outgoing thread got null message from queue, quitting");
                return;
            }

            IRCMessage outputMessage = message.buildServerOutput();
            String outputString = outputMessage.buildServerOutput();
            if (Strings.isNullOrEmpty(outputString)) {
                LOGGER.error("Got an output message, but failed to create a string from it: {}", outputMessage.buildPrettyOutput());
                return;
            }

            try {
                outToServer.write(outputString + CharacterCodes.CR + CharacterCodes.LF);
                outToServer.flush();
            } catch (IOException e) {
                LOGGER.error("Failed to send message, output thread quitting: {}", e);

                return;
            }

            LOGGER.info("Sent: {}", outputString);
        }
    }
}
