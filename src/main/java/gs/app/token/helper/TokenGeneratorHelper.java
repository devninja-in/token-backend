package gs.app.token.helper;

import gs.app.token.exception.TokenAppException;
import gs.app.token.exception.TokenAppExceptionCode;
import gs.app.token.model.Item;
import gs.app.token.model.ItemConfiguration;
import gs.app.token.model.Token;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;

public class TokenGeneratorHelper {

    public static Token generateToken(String clientName, Item item, Token lastGeneratedToken) {

        LocalDateTime nextSellStartDateTime = null;
        LocalDateTime nextSellEndDateTime = null;
        LocalDateTime nextTokenSlotStartDateTime = null;
        LocalDateTime nextTokenSlotEndDateTime = null;
        LocalDateTime now = LocalDateTime.now();

        for (ItemConfiguration itemConfiguration : item.getItemConfigurations()) {

            DayOfWeek dayOfWeek = DayOfWeek.valueOf(itemConfiguration.getDay().toUpperCase());
            String[] sellStartHourAndMinute = itemConfiguration.getSellStartTime().split(":");
            String[] sellEndHourAndMinute = itemConfiguration.getSellEndTime().split(":");
            String[] tokenStartHourAndMinuteBeforeSellStart = itemConfiguration.getTokenGenerationStart()
                .split(":");
            String[] slotDurationHourAndMinute = itemConfiguration.getSlotDuration().split(":");

            nextSellStartDateTime = LocalDateTime.now(ZoneId.systemDefault())
                .with(TemporalAdjusters.nextOrSame(dayOfWeek))
                .withHour(Integer.parseInt(sellStartHourAndMinute[0])).withMinute(
                    sellStartHourAndMinute.length > 1 ? Integer.parseInt(sellStartHourAndMinute[1]) : 0)
                .withSecond(0).withNano(0);

            nextSellEndDateTime = LocalDateTime.now(ZoneId.systemDefault())
                .with(TemporalAdjusters.nextOrSame(dayOfWeek))
                .withHour(Integer.parseInt(sellEndHourAndMinute[0]))
                .withMinute(sellEndHourAndMinute.length > 1 ? Integer.parseInt(sellEndHourAndMinute[1]) : 0)
                .withSecond(0).withNano(0);

            nextTokenSlotStartDateTime = nextSellStartDateTime
                .minusHours(Integer.parseInt(tokenStartHourAndMinuteBeforeSellStart[0])).minusMinutes(
                    tokenStartHourAndMinuteBeforeSellStart.length > 1 ?
                        Integer.parseInt(tokenStartHourAndMinuteBeforeSellStart[1]) :
                        0).withSecond(0).withNano(0);

            nextTokenSlotEndDateTime = nextTokenSlotStartDateTime
                .plusHours(Integer.parseInt(slotDurationHourAndMinute[0])).plusMinutes(
                    slotDurationHourAndMinute.length > 1 ? Integer.parseInt(slotDurationHourAndMinute[1]) : 0)
                .withSecond(0).withNano(0);

            if (now.isBefore(nextTokenSlotStartDateTime) || now.isAfter(nextSellEndDateTime)) {
                nextSellStartDateTime = null;
                nextSellEndDateTime = null;
                nextTokenSlotStartDateTime = null;
                continue;
            }

            if (lastGeneratedToken == null ||
                !lastGeneratedToken.getSellStart().isEqual(nextSellStartDateTime)) {

                return getNextToken(clientName, 1, now, nextSellStartDateTime, nextSellEndDateTime,
                    nextTokenSlotStartDateTime, nextTokenSlotEndDateTime, slotDurationHourAndMinute, 1, item);

            }

            if (lastGeneratedToken != null) {

                if (now.isBefore(lastGeneratedToken.getSlotStart()) &&
                    lastGeneratedToken.getSlotTokenNumber() < itemConfiguration.getPersonPerSlot()) {

                    return createToken(clientName, lastGeneratedToken.getNumber() + 1,
                        lastGeneratedToken.getSellStart(), lastGeneratedToken.getSellEnd(),
                        lastGeneratedToken.getSlotStart(), lastGeneratedToken.getSlotEnd(),
                        lastGeneratedToken.getSlotTokenNumber() + 1, item);
                }

                nextTokenSlotStartDateTime = lastGeneratedToken.getSlotStart()
                    .plusHours(Integer.parseInt(slotDurationHourAndMinute[0])).plusMinutes(
                        slotDurationHourAndMinute.length > 1 ?
                            Integer.parseInt(slotDurationHourAndMinute[1]) :
                            0).withSecond(0).withNano(0);

                nextTokenSlotEndDateTime = nextTokenSlotStartDateTime
                    .plusHours(Integer.parseInt(slotDurationHourAndMinute[0])).plusMinutes(
                        slotDurationHourAndMinute.length > 1 ?
                            Integer.parseInt(slotDurationHourAndMinute[1]) :
                            0).withSecond(0).withNano(0);

                return getNextToken(clientName, lastGeneratedToken.getNumber() + 1, now,
                    nextSellStartDateTime, nextSellEndDateTime, nextTokenSlotStartDateTime,
                    nextTokenSlotEndDateTime, slotDurationHourAndMinute, 1, item);

            }

        }
        if (nextSellStartDateTime == null) {
            throw new TokenAppException(TokenAppExceptionCode.NO_TOKEN_SLOT_AVAILABLE,
                String.format("No token slot available for sell"));
        }
        else {
            throw new TokenAppException(TokenAppExceptionCode.NO_TOKEN_SLOT_AVAILABLE,
                String.format("No token slot available for sell on %s", nextSellStartDateTime));
        }

    }

    private static Token getNextToken(String clientName, Integer number, LocalDateTime now,
        LocalDateTime nextSellStartDateTime, LocalDateTime nextSellEndDateTime,
        LocalDateTime nextTokenSlotStartDateTime, LocalDateTime nextTokenSlotEndDateTime,
        String[] slotDurationHourAndMinute, Integer slotTokenNumber, Item item) {

        while (now.isAfter(nextTokenSlotStartDateTime) &&
            nextTokenSlotEndDateTime.isBefore(nextSellEndDateTime)) {

            nextTokenSlotStartDateTime = nextTokenSlotStartDateTime
                .plusHours(Integer.parseInt(slotDurationHourAndMinute[0])).plusMinutes(
                    slotDurationHourAndMinute.length > 1 ? Integer.parseInt(slotDurationHourAndMinute[1]) : 0)
                .withSecond(0).withNano(0);

            nextTokenSlotEndDateTime = nextTokenSlotStartDateTime
                .plusHours(Integer.parseInt(slotDurationHourAndMinute[0])).plusMinutes(
                    slotDurationHourAndMinute.length > 1 ? Integer.parseInt(slotDurationHourAndMinute[1]) : 0)
                .withSecond(0).withNano(0);

        }

        if (now.isBefore(nextTokenSlotStartDateTime) &&
            (nextTokenSlotEndDateTime.isBefore(nextSellEndDateTime) ||
                nextTokenSlotEndDateTime.isEqual(nextSellEndDateTime))) {

            return createToken(clientName, number, nextSellStartDateTime, nextSellEndDateTime,
                nextTokenSlotStartDateTime, nextTokenSlotEndDateTime, slotTokenNumber, item);
        }

        throw new TokenAppException(TokenAppExceptionCode.NO_TOKEN_SLOT_AVAILABLE,
            String.format("No token slot available for sell on %s", nextSellStartDateTime));

    }

    private static Token createToken(String clientName, Integer number, LocalDateTime sellStart,
        LocalDateTime sellEnd, LocalDateTime slotStart, LocalDateTime slotEnd, Integer slotTokeNumber,
        Item item) {

        return new Token().setNumber(number).setClientName(clientName).setSellStart(sellStart)
            .setSellEnd(sellEnd).setSlotStart(slotStart).setSlotEnd(slotEnd)
            .setSlotTokenNumber(slotTokeNumber).setItemName(item.getName());
    }

}
