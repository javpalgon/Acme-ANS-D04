
package acme.features.administrator.aircraft;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.models.Dataset;
import acme.client.components.principals.Administrator;
import acme.client.components.views.SelectChoices;
import acme.client.services.AbstractGuiService;
import acme.client.services.GuiService;
import acme.entities.aircraft.Aircraft;
import acme.entities.aircraft.AircraftStatus;

@GuiService
public class AdministratorAircraftDisableService extends AbstractGuiService<Administrator, Aircraft> {

	@Autowired
	private AdministratorAircraftRepository repository;


	@Override
	public void authorise() {
		Integer id;
		Collection<Aircraft> aircrafts = this.repository.findAllAircrafts();
		Boolean status = super.getRequest().getPrincipal().hasRealmOfType(Administrator.class);
		if (super.getRequest().hasData("id")) {
			id = super.getRequest().getData("id", Integer.class);
			if (id != null && aircrafts.stream().anyMatch(x -> id.equals(x.getId())))
				super.getResponse().setAuthorised(status);
		} else
			super.getResponse().setAuthorised(false);
	}

	@Override
	public void load() {
		Aircraft aircraft;
		int id;

		id = super.getRequest().getData("id", int.class);
		aircraft = this.repository.findAircraftById(id);

		super.getBuffer().addData(aircraft);
	}

	@Override
	public void bind(final Aircraft aircraft) {

	}

	@Override
	public void validate(final Aircraft aircraft) {
		boolean confirmation;
		confirmation = super.getRequest().getData("confirmation", boolean.class);
		super.state(confirmation, "confirmation", "acme.validation.confirmation.message");
	}

	@Override
	public void perform(final Aircraft aircraft) {
		if (aircraft.getAircraftStatus().equals(AircraftStatus.ACTIVE))
			aircraft.setAircraftStatus(AircraftStatus.UNDER_MAINTENANCE);
		this.repository.save(aircraft);
	}

	@Override
	public void unbind(final Aircraft aircraft) {
		assert aircraft != null;
		SelectChoices statusChoices;
		statusChoices = SelectChoices.from(AircraftStatus.class, aircraft.getAircraftStatus());

		Dataset dataset = super.unbindObject(aircraft, "model", "regitrationNumber", "capacity", "cargoWeight", "aircraftStatus", "details");
		dataset.put("aircraftStatus", statusChoices);
		dataset.put("airlines", SelectChoices.from(this.repository.findAllAirlines(), "IATACode", aircraft.getAirline()));
		dataset.put("confirmation", false);
		dataset.put("name", aircraft.getAirline().getName());
		dataset.put("website", aircraft.getAirline().getWebsite());
		dataset.put("readonly", false);
		super.getResponse().addData(dataset);
	}
}
