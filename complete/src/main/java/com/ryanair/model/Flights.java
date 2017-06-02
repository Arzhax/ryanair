package com.ryanair.model;

import java.util.ArrayList;
import java.util.List;

public class Flights {
	
	public Flights() {
		super();
		this.flightList = new ArrayList<>();
	}

	List<Flight> flightList;

	public List<Flight> getFlightList() {
		return flightList;
	}

	public void setFlightList(List<Flight> flightList) {
		this.flightList = flightList;
	}

}
