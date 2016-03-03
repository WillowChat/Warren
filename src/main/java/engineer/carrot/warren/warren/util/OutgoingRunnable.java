package engineer.carrot.warren.warren.util;

import com.google.common.base.Strings;
import engineer.carrot.warren.warren.irc.CharacterCodes;
import engineer.carrot.warren.warren.irc.messages.IMessage;
import engineer.carrot.warren.warren.irc.messages.JavaIrcMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStreamWriter;

public class OutgoingRunnable implements Runnable {
    private final Logger LOGGER = LoggerFactory.getLogger(OutgoingRunnable.class);

    private final IMessageQueue outgoingQueue;
    private final OutputStreamWriter outToServer;

    public OutgoingRunnable(IMessageQueue outgoingQueue, OutputStreamWriter outToServer) {
        this.outgoingQueue = outgoingQueue;
        this.outToServer = outToServer;
    }

    @Override
    public void run() {
        while (true) {
            IMessage message;
            try {
                message = outgoingQueue.popIndefinitely();
            } catch (InterruptedException e) {
                LOGGER.info("Outgoing runnable interrupted, quitting");

                return;
            }

            if (message == null) {
                LOGGER.error("Outgoing thread got null message from queue, quitting");
                return;
            }

            JavaIrcMessage outputMessage = message.build();
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
