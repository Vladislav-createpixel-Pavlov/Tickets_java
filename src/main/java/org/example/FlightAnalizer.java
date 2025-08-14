package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FlightAnalizer {
    public static void main(String[] args) throws Exception {
        if (args.length != 1 || !args[0].endsWith(".json")) {
            System.out.println("Usage: java FlightAnalizer path/to/tickets.json");
            return;
        }

        File jsonFile = new File(args[0]);
        ObjectMapper mapper = new ObjectMapper();
        Root root = mapper.readValue(jsonFile, Root.class);

        List<Ticket> filteredTickets = filterByRoute(root.tickets);

        if (!filteredTickets.isEmpty()) {
            Map<String, Integer> minFlightTimes = calculateMinFlightTimes(filteredTickets);

            double averagePrice = calculateAveragePrice(filteredTickets);
            double medianPrice = calculateMedianPrice(filteredTickets);
            double priceDifference = Math.abs(averagePrice - medianPrice);

            printResults(minFlightTimes, averagePrice, medianPrice, priceDifference);
        } else {
        }
    }

    private static List<Ticket> filterByRoute(List<Ticket> tickets) {
        return new ArrayList<>(tickets);
    }

    private static Map<String, Integer> calculateMinFlightTimes(List<Ticket> tickets) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yy H:mm");
        Map<String, Integer> result = new HashMap<>();

        for (Ticket ticket : tickets) {
            LocalDateTime departure = LocalDateTime.parse(ticket.getDeparture_date() + " " + ticket.getDeparture_time(), formatter);
            LocalDateTime arrival = LocalDateTime.parse(ticket.getArrival_date() + " " + ticket.getArrival_time(), formatter);
            long flightDurationMinutes = ChronoUnit.MINUTES.between(departure, arrival);

            String carrier = ticket.getCarrier();
            int currentMinTime = result.containsKey(carrier) ? result.get(carrier) : Integer.MAX_VALUE;
            result.put(carrier, Math.min(currentMinTime, (int) flightDurationMinutes));
        }

        return result;
    }


    private static double calculateAveragePrice(List<Ticket> tickets) {
        return tickets.stream().mapToDouble(Ticket::getPrice).average().orElse(Double.NaN);
    }

    private static double calculateMedianPrice(List<Ticket> tickets) {
        List<Double> prices = tickets.stream().mapToDouble(Ticket::getPrice).sorted().boxed().collect(Collectors.toList());
        int size = prices.size();
        if (size % 2 == 0)
            return (prices.get(size / 2 - 1) + prices.get(size / 2)) / 2.0;
        else
            return prices.get(size / 2);
    }

    private static void printResults(Map<String, Integer> minFlightTimes, double avgPrice, double medPrice, double diff) {
        System.out.println("\nМинимальные времена полётов:");
        minFlightTimes.forEach((k, v) -> System.out.printf("%s: %d минут\n", k, v));

        System.out.printf("\nСредняя цена: %.2f руб.\n", avgPrice);
        System.out.printf("Медианная цена: %.2f руб.\n", medPrice);
        System.out.printf("Разница между средней и медианой: %.2f руб.\n", diff);
    }
}

