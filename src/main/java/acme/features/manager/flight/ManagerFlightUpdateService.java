
package acme.features.manager.flight;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.models.Dataset;
import acme.client.components.principals.Principal;
import acme.client.services.AbstractGuiService;
import acme.client.services.GuiService;
import acme.components.ValidatorService;
import acme.entities.flight.Flight;
import acme.realms.Manager;

@GuiService
public class ManagerFlightUpdateService extends AbstractGuiService<Manager, Flight> {

	@Autowired
	protected ManagerFlightRepository	repository;

	@Autowired
	protected ValidatorService			service;


	@Override
	public void authorise() {
		Flight object;
		int id;
		id = super.getRequest().getData("id", int.class);
		object = this.repository.findFlightById(id);
		final Principal principal = super.getRequest().getPrincipal();
		final int userAccountId = principal.getAccountId();
		super.getResponse().setAuthorised(object.getIsDraftMode() && object.getManager().getUserAccount().getId() == userAccountId);
	}

	@Override
	public void load() {
		Flight flight;
		int id;

		id = super.getRequest().getData("id", int.class);
		flight = this.repository.findFlightById(id);

		super.getBuffer().addData(flight);
	}

	@Override
	public void bind(final Flight object) {
		super.bindObject(object, "tag", "cost", "description", "requiresSelfTransfer");
	}

	@Override
	public void validate(final Flight object) {

	}

	@Override
	public void perform(final Flight object) {
		this.repository.save(object);
	}

	@Override
	public void unbind(final Flight object) {
		Dataset dataset;
		dataset = super.unbindObject(object, "tag", "cost", "description", "requiresSelfTransfer", "description", "isDraftMode");
		super.getResponse().addData(dataset);
	}
}
