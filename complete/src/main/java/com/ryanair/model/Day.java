package com.ryanair.model;

import java.util.List;

public class Day {
	
	int day;
	List<FlightMini> flights;
	
	public int getDay() {
		return day;
	}
	public void setDay(int day) {
		this.day = day;
	}
	public List<FlightMini> getFlights() {
		return flights;
	}
	public void setFlights(List<FlightMini> flights) {
		this.flights = flights;
	}


}
