
package acme.features.administrator.airline;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.models.Dataset;
import acme.client.components.principals.Administrator;
import acme.client.components.views.SelectChoices;
import acme.client.services.AbstractGuiService;
import acme.client.services.GuiService;
import acme.entities.airline.Airline;
import acme.entities.airline.Type;

@GuiService
public class AdministratorAirlineShowService extends AbstractGuiService<Administrator, Airline> {

	// Internal state ---------------------------------------------------------

	@Autowired
	private AdministratorAirlineRepository repository;

	// AbstractGuiService interface -------------------------------------------


	@Override
	public void authorise() {
		Integer id;
		Collection<Airline> airlines = this.repository.findAllAirlines();
		Boolean status = super.getRequest().getPrincipal().hasRealmOfType(Administrator.class);
		if (super.getRequest().hasData("id")) {
			id = super.getRequest().getData("id", Integer.class);
			if (id != null && airlines.stream().anyMatch(x -> id.equals(x.getId())))
				super.getResponse().setAuthorised(status);
		} else
			super.getResponse().setAuthorised(false);
	}

	@Override
	public void load() {
		int id;
		Airline airline;

		id = super.getRequest().getData("id", int.class);
		airline = this.repository.findAirlineById(id);

		super.getBuffer().addData(airline);
	}

	@Override
	public void unbind(final Airline airline) {
		SelectChoices choices;
		Dataset dataset;

		choices = SelectChoices.from(Type.class, airline.getType());
		dataset = super.unbindObject(airline, "name", "IATACode", "website", "type", "foundationMoment", "email", "phoneNumber");
		dataset.put("Type", choices);

		super.getResponse().addData(dataset);
	}

}
