package com.ryanair.model;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;

public class Flight {

	public Flight(String departureAirport, String arrivalAirport, LocalDateTime localDateTime,
			LocalDateTime localDateTime2) {
		super();
		this.departureAirport = departureAirport;
		this.arrivalAirport = arrivalAirport;
		this.departureTime = localDateTime;
		this.arrivalTime = localDateTime2;
	}

	public Flight() {
		super();
		// TODO Auto-generated constructor stub
	}

	String departureAirport;
	String arrivalAirport;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
	LocalDateTime departureTime;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
	LocalDateTime arrivalTime;

	public String getDepartureAirport() {
		return departureAirport;
	}

	public void setDepartureAirport(String departureAirport) {
		this.departureAirport = departureAirport;
	}

	public String getArrivalAirport() {
		return arrivalAirport;
	}

	public void setArrivalAirport(String arrivalAirport) {
		this.arrivalAirport = arrivalAirport;
	}

	public LocalDateTime getDepartureTime() {
		return departureTime;
	}

	public void setDepartureTime(LocalDateTime departureTime) {
		this.departureTime = departureTime;
	}

	public LocalDateTime getArrivalTime() {
		return arrivalTime;
	}

	public void setArrivalTime(LocalDateTime arrivalTime) {
		this.arrivalTime = arrivalTime;
	}

}
