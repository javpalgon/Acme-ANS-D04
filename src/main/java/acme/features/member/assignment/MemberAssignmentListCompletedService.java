
package acme.features.member.assignment;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.models.Dataset;
import acme.client.services.AbstractGuiService;
import acme.client.services.GuiService;
import acme.entities.assignment.Assignment;
import acme.entities.leg.LegStatus;
import acme.realms.Member;

@GuiService
public class MemberAssignmentListCompletedService extends AbstractGuiService<Member, Assignment> {

	@Autowired
	private MemberAssignmentRepository repository;


	@Override
	public void authorise() {
		boolean status = super.getRequest().getPrincipal().hasRealmOfType(Member.class);
		super.getResponse().setAuthorised(status);
	}

	@Override
	public void load() {
		Collection<Assignment> assignments = new ArrayList<>();
		int memberId;

		memberId = super.getRequest().getPrincipal().getActiveRealm().getId();
		assignments = this.repository.findByLegStatusAndMemberId(LegStatus.LANDED, memberId);

		super.getBuffer().addData(assignments);
	}

	@Override
	public void unbind(final Assignment object) {

		assert object != null;

		Dataset dataset;

		dataset = super.unbindObject(object, "role", "lastUpdate", "status", "remarks");
		dataset.put("leg.flightNumber", object.getLeg().getFlightNumber());
		dataset.put("readonly", true);

		super.getResponse().addData(dataset);
	}

}
