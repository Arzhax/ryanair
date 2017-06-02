package com.ryanair.application;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ryanair.model.Result;

@RestController
public class RyanairRestController {
	@Autowired
	RyanairRestService service;

	@RequestMapping(value = "/interconnections", produces = "application/json")
	public ResponseEntity<List<Result>> getDepartureFlights(@RequestParam(value = "departure") String departure,
			@RequestParam(value = "arrival") String arrival,
			@RequestParam(value = "departureDateTime") String departureDateTime,
			@RequestParam(value = "arrivalDateTime") String arrivalDateTime) {
		List<Result> dev = new ArrayList<>();
		dev = service.getInterconnections(departure, arrival, departureDateTime, arrivalDateTime);
		return new ResponseEntity<List<Result>>(dev, HttpStatus.OK);
	}
}
