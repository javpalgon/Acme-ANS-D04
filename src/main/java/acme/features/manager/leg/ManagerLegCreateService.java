
package acme.features.manager.leg;

import java.util.Collection;
import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.models.Dataset;
import acme.client.components.views.SelectChoices;
import acme.client.helpers.MomentHelper;
import acme.client.services.AbstractGuiService;
import acme.client.services.GuiService;
import acme.entities.aircraft.Aircraft;
import acme.entities.aircraft.AircraftStatus;
import acme.entities.airport.Airport;
import acme.entities.flight.Flight;
import acme.entities.leg.Leg;
import acme.entities.leg.LegStatus;
import acme.realms.Manager;

@GuiService
public class ManagerLegCreateService extends AbstractGuiService<Manager, Leg> {

	// Internal state ---------------------------------------------------------

	@Autowired
	private ManagerLegRepository repository;


	@Override
	public void authorise() {
		int flightId = super.getRequest().getData("masterId", int.class);
		Flight flight = this.repository.findFlightById(flightId);
		Manager manager = (Manager) super.getRequest().getPrincipal().getActiveRealm();
		boolean status = true;
		boolean isDraftMode = flight.getIsDraftMode();
		boolean isManager = super.getRequest().getPrincipal().hasRealm(manager);
		boolean isOwner = super.getRequest().getPrincipal().getAccountId() == flight.getManager().getUserAccount().getId();
		status = isDraftMode && isManager && isOwner;
		if (super.getRequest().getMethod().equals("POST"))
			status = status && this.validateAircraft() && this.validateAirport("departureAirport") && this.validateAirport("arrivalAirport");

		super.getResponse().setAuthorised(status);
	}

	private boolean validateAircraft() {
		Integer aircraftId = super.getRequest().getData("aircraft", int.class);
		if (aircraftId != 0) {
			Aircraft aircraft = this.repository.findAircraftById(aircraftId);
			if (aircraft == null || !aircraft.getAircraftStatus().equals(AircraftStatus.ACTIVE))
				return false;
		}
		return true;
	}

	private boolean validateAirport(final String airportField) {
		Integer airportId = super.getRequest().getData(airportField, int.class);
		if (airportId != 0) {
			Airport airport = this.repository.findAirportById(airportId);
			if (airport == null)
				return false;
		}
		return true;
	}

	@Override
	public void load() {
		Leg leg;
		int masterId;
		Flight flight;

		masterId = super.getRequest().getData("masterId", int.class);
		flight = this.repository.findFlightById(masterId);

		leg = new Leg();
		leg.setFlight(flight);
		leg.setIsDraftMode(true);

		super.getBuffer().addData(leg);
	}

	@Override
	public void bind(final Leg leg) {
		int aircraftId;
		Aircraft aircraft;
		aircraftId = super.getRequest().getData("aircraft", int.class);
		aircraft = this.repository.findAircraftById(aircraftId);

		int departureAirportId;
		Airport departureAirport;
		departureAirportId = super.getRequest().getData("departureAirport", int.class);
		departureAirport = this.repository.findAirportById(departureAirportId);

		int arrivalAirportId;
		Airport arrivalAirport;
		arrivalAirportId = super.getRequest().getData("arrivalAirport", int.class);
		arrivalAirport = this.repository.findAirportById(arrivalAirportId);

		super.bindObject(leg, "flightNumber", "departure", "arrival", "status");
		leg.setAircraft(aircraft);
		leg.setDepartureAirport(departureAirport);
		leg.setArrivalAirport(arrivalAirport);
	}

	@Override
	public void validate(final Leg leg) {
		boolean validDate;
		Date currentMoment = MomentHelper.getCurrentMoment();
		if (leg.getDeparture() != null) {
			validDate = MomentHelper.isAfter(leg.getDeparture(), currentMoment);
			super.state(validDate, "departure", "acme.validation.leg.departure");
		}
		if (leg.getArrival() != null) {
			validDate = MomentHelper.isAfter(leg.getArrival(), currentMoment);
			super.state(validDate, "arrival", "acme.validation.leg.departure");
		}
	}

	@Override
	public void perform(final Leg leg) {
		this.repository.save(leg);
	}

	@Override
	public void unbind(final Leg leg) {
		Dataset dataset;

		SelectChoices choices;
		choices = SelectChoices.from(LegStatus.class, leg.getStatus());

		SelectChoices departureAirportChoices;
		SelectChoices arrivalAirportChoices;
		Collection<Airport> airports;
		airports = this.repository.findAllAirports();

		SelectChoices selectedAircraft = new SelectChoices();

		Collection<Aircraft> aircraftsActives = this.repository.findAllActiveAircrafts(AircraftStatus.ACTIVE);

		dataset = super.unbindObject(leg, "departure", "arrival");
		String iataCode = Optional.ofNullable(leg.getFlight()).map(Flight::getManager).map(Manager::getAirline).map(a -> a.getIATACode()).orElse("");
		dataset.put("IATACode", iataCode);
		String iata = leg.getFlight().getManager().getAirline().getIATACode();
		if (leg.getFlightNumber() == null || leg.getFlightNumber().isBlank())
			dataset.put("flightNumber", iata);
		else
			dataset.put("flightNumber", leg.getFlightNumber());
		dataset.put("masterId", leg.getFlight().getId());
		dataset.put("isDraftMode", true);
		dataset.put("status", choices);
		selectedAircraft = SelectChoices.from(aircraftsActives, "regitrationNumber", leg.getAircraft());
		dataset.put("aircrafts", selectedAircraft);
		dataset.put("aircraft", selectedAircraft.getSelected().getKey());
		dataset.put("isDraftFlight", leg.getFlight().getIsDraftMode());
		departureAirportChoices = SelectChoices.from(airports, "IATACode", leg.getDepartureAirport());
		arrivalAirportChoices = SelectChoices.from(airports, "IATACode", leg.getArrivalAirport());
		dataset.put("departureAirports", departureAirportChoices);
		dataset.put("departureAirport", departureAirportChoices.getSelected().getKey());
		dataset.put("arrivalAirports", arrivalAirportChoices);
		dataset.put("arrivalAirport", arrivalAirportChoices.getSelected().getKey());

		super.getResponse().addData(dataset);
	}
}
