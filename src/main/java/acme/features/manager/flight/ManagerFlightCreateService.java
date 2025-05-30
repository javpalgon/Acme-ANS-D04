
package acme.features.manager.flight;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.models.Dataset;
import acme.client.services.AbstractGuiService;
import acme.client.services.GuiService;
import acme.entities.flight.Flight;
import acme.realms.Manager;

@GuiService
public class ManagerFlightCreateService extends AbstractGuiService<Manager, Flight> {

	@Autowired
	protected ManagerFlightRepository repository;

	//	@Autowired
	//	protected ValidatorService			service;


	@Override
	public void authorise() {
		super.getResponse().setAuthorised(true);
	}

	@Override
	public void load() {
		Flight object;
		object = new Flight();
		int managerId;

		managerId = super.getRequest().getPrincipal().getActiveRealm().getId();
		final Manager manager = this.repository.findOneManagerById(managerId);
		object.setManager(manager);
		object.setIsDraftMode(true);
		super.getBuffer().addData(object);
	}

	@Override
	public void bind(final Flight object) {
		super.bindObject(object, "tag", "cost", "description", "requiresSelfTransfer");
	}

	@Override
	public void perform(final Flight object) {
		this.repository.save(object);
	}

	@Override
	public void validate(final Flight flight) {
		;
	}

	@Override
	public void unbind(final Flight object) {
		Dataset dataset;
		dataset = super.unbindObject(object, "tag", "cost", "description", "requiresSelfTransfer", "isDraftMode", "manager");
		super.getResponse().addData(dataset);
	}
}
