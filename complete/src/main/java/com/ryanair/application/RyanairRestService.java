package com.ryanair.application;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.ryanair.model.Day;
import com.ryanair.model.Days;
import com.ryanair.model.Flight;
import com.ryanair.model.FlightMini;
import com.ryanair.model.Flights;
import com.ryanair.model.Result;
import com.ryanair.model.Route;

@Component
public class RyanairRestService {
	public List<Result> getInterconnections(String departureAirport, String arrivalAirport, String departureDate,
			String arrivalDate) {
		List<String> validRoutes = getValidRoutes(departureAirport, arrivalAirport);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
		LocalDateTime departureDateTime = LocalDateTime.parse(departureDate, formatter);
		LocalDateTime arrivalDateTime = LocalDateTime.parse(arrivalDate, formatter);
		Flights directFlights = getDirectConnections(departureAirport, arrivalAirport, departureDateTime,
				arrivalDateTime);
		Flights oneStopFlights = get1StopConnections(departureAirport, arrivalAirport, departureDateTime,
				arrivalDateTime, validRoutes);
		List<Result> results = new ArrayList<>();
		results.add(new Result(0, directFlights));
		results.add(new Result(1, oneStopFlights));

		return results;
	}

	private List<String> getValidRoutes(String departure, String arrival) {
		String uri = "https://api.ryanair.com/core/3/routes/";
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<List<Route>> result = restTemplate.exchange(uri, HttpMethod.GET, null,
				new ParameterizedTypeReference<List<Route>>() {
				});
		List<Route> r = result.getBody();
		List<Route> routes = r.parallelStream()
				.filter(route -> route.getAirportFrom().equals(departure) ^ route.getAirportTo().equals(arrival))
				.collect(Collectors.toList());

		Map<String, List<String>> routesMap = new HashMap<>();

		for (Route route : routes) {
			List<String> aux = routesMap.get(route.getAirportFrom()) != null ? routesMap.get(route.getAirportFrom())
					: new ArrayList<>();
			aux.add(route.getAirportTo());
			routesMap.put(route.getAirportFrom(), aux);
		}
		return routesMap.get(departure).stream().filter(i -> routesMap.containsKey(i)).collect(Collectors.toList());
	}

	private Flights getDirectConnections(String departure, String arrival, LocalDateTime departureTime,
			LocalDateTime arrivalTime) {
		RestTemplate restTemplate = new RestTemplate();
		Flights flights = new Flights();
		for (int j = departureTime.getYear(); j <= arrivalTime.getYear(); j++) {
			for (int i = ((j == departureTime.getYear()) ? departureTime.getMonthValue()
					: 1); i <= ((j == arrivalTime.getYear()) ? arrivalTime.getMonthValue() : 12); i++) {
				try {
					ResponseEntity<Days> result = restTemplate.exchange(
							"https://api.ryanair.com/timetable/3/schedules/{departure}/{arrival}/years/{year}/months/{month}",
							HttpMethod.GET, null, Days.class, departure, arrival, j, i);
					if (result.getStatusCode() == HttpStatus.OK) {
						for (Day flightsDay : result.getBody().getDays()) {
							int day = flightsDay.getDay();
							for (FlightMini flightmini : flightsDay.getFlights()) {
								Flight flight = new Flight(departure, arrival,
										convertToDate(j, i, day, flightmini.getDepartureTime()),
										convertToDate(j, i, day, flightmini.getArrivalTime()));
								if (flight.getDepartureTime().isAfter(departureTime)
										&& flight.getArrivalTime().isBefore(arrivalTime))
									flights.getFlightList().add(flight);
							}
						}
					}
				} catch (Exception e) {
					
				}
				
			}
		}
		return flights;
	}

	private Flights get1StopConnections(String departure, String arrival, LocalDateTime departureTime,
			LocalDateTime arrivalTime, List<String> validRoutes) {
		RestTemplate restTemplate = new RestTemplate();
		Flights flights = new Flights();
		for (String route : validRoutes) {
			for (int j = departureTime.getYear(); j <= arrivalTime.getYear(); j++) {
				for (int i = ((j == departureTime.getYear()) ? departureTime.getMonthValue()
						: 1); i <= ((j == arrivalTime.getYear()) ? arrivalTime.getMonthValue() : 12); i++) {
					try {
						ResponseEntity<Days> result1 = restTemplate.exchange(
								"https://api.ryanair.com/timetable/3/schedules/{departure}/{route}/years/{year}/months/{month}",
								HttpMethod.GET, null, Days.class, departure, route, j, i);
						ResponseEntity<Days> result2 = restTemplate.exchange(
								"https://api.ryanair.com/timetable/3/schedules/{route}/{arrival}/years/{year}/months/{month}",
								HttpMethod.GET, null, Days.class, route, arrival, j, i);
						if (result1.getStatusCode() == HttpStatus.OK && result2.getStatusCode() == HttpStatus.OK) {
							List<Day> month1 = result1.getBody().getDays();
							List<Day> month2 = result2.getBody().getDays();
							flights.getFlightList().addAll(getValidOneStopFlights(departure, arrival, route, month1, month2,
									departureTime, arrivalTime, j, i));
						}
					} catch (Exception e) {
						
					}
				}
			}
		}

		return flights;
	}

	private List<Flight> getValidOneStopFlights(String departure, String arrival, String route, List<Day> candidates1,
			List<Day> candidates2, LocalDateTime departureTime, LocalDateTime arrivalTime, int year, int month) {
		List<Flight> result = new ArrayList<>();
		for (Day day1 : candidates1) {
			List<FlightMini> miniCandidates1 = day1.getFlights();
			Day day2 = candidates2.stream().filter(day -> day.getDay() == day1.getDay()).findFirst().orElse(null);
			if (day2 != null) {
				List<FlightMini> miniCandidates2 = day2.getFlights();
				for (FlightMini flightMini1 : miniCandidates1) {
					if (convertToDate(year, month, day1.getDay(), flightMini1.getDepartureTime())
							.isAfter(departureTime)) {
						List<FlightMini> auxList = getValidArrivalsForDeparture(flightMini1, miniCandidates2,
								arrivalTime, year, month, day1.getDay());
						if (!auxList.isEmpty()) {
							result.add(new Flight(departure, route,
									convertToDate(year, month, day1.getDay(), flightMini1.getDepartureTime()),
									convertToDate(year, month, day1.getDay(), flightMini1.getArrivalTime())));

							for (FlightMini flightMini2 : auxList) {
								result.add(new Flight(route, arrival,
										convertToDate(year, month, day2.getDay(), flightMini2.getDepartureTime()),
										convertToDate(year, month, day2.getDay(), flightMini2.getArrivalTime())));
							}
						}
						auxList.clear();
					}

				}
			}
		}

		return result;
	}

	private List<FlightMini> getValidArrivalsForDeparture(FlightMini flightMini1, List<FlightMini> miniCandidates2, LocalDateTime arrivalTime, int year, int month, int day) {
		List<FlightMini> result = new ArrayList<>();
		LocalDateTime minimumDeparture = convertToDate(year, month, day, flightMini1.getArrivalTime()).plusHours(2);
		LocalDateTime maximumDeparture = arrivalTime;
		result.addAll(miniCandidates2.stream().filter(flightMini2 -> convertToDate(year, month, day, flightMini2.getDepartureTime()).isAfter(minimumDeparture) && convertToDate(year, month, day, flightMini2.getDepartureTime()).isBefore(maximumDeparture)).collect(Collectors.toList()));
		return result;
	}

	private LocalDateTime convertToDate(Integer year, Integer month, Integer day, String hm) {
		String[] hoursAndMinutes = hm.split(":");
		return LocalDateTime.of(year, month, day, Integer.parseInt(hoursAndMinutes[0]),
				Integer.parseInt(hoursAndMinutes[1]));
	}

}
