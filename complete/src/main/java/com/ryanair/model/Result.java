package com.ryanair.model;

public class Result {

	public Result() {
		super();
		// TODO Auto-generated constructor stub
	}
	int stops;
	Flights legs;
	
	public Result(int stops, Flights legs) {
		super();
		this.stops = stops;
		this.legs = legs;
	}
	public int getStops() {
		return stops;
	}
	public void setStops(int stops) {
		this.stops = stops;
	}
	public Flights getLegs() {
		return legs;
	}
	public void setLegs(Flights legs) {
		this.legs = legs;
	}
}
