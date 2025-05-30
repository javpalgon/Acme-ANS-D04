
package acme.features.member.assignment;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.models.Dataset;
import acme.client.components.views.SelectChoices;
import acme.client.helpers.MomentHelper;
import acme.client.services.AbstractGuiService;
import acme.client.services.GuiService;
import acme.entities.assignment.Assignment;
import acme.entities.assignment.AssignmentStatus;
import acme.entities.assignment.Role;
import acme.entities.leg.LegStatus;
import acme.realms.Member;

@GuiService
public class MemberAssignmentShowService extends AbstractGuiService<Member, Assignment> {

	@Autowired
	private MemberAssignmentRepository repository;


	@Override
	public void authorise() {
		int assignmentId;
		int memberId;
		Assignment assignment;

		assignmentId = super.getRequest().getData("id", int.class);
		memberId = super.getRequest().getPrincipal().getActiveRealm().getId();
		assignment = this.repository.findOneById(assignmentId);

		boolean status = assignment != null && super.getRequest().getPrincipal().hasRealmOfType(Member.class) && assignment.getMember().getId() == memberId;
		super.getResponse().setAuthorised(status);
	}

	@Override
	public void load() {
		int assignmentId = super.getRequest().getData("id", int.class);
		super.getBuffer().addData(this.repository.findOneById(assignmentId));
	}

	@Override
	public void unbind(final Assignment object) {
		assert object != null;

		int assignmentId = super.getRequest().getData("id", int.class);
		Assignment assignment = this.repository.findOneById(assignmentId);

		int currentMemberId = super.getRequest().getPrincipal().getActiveRealm().getId();
		Member member = this.repository.findMemberById(currentMemberId);

		final boolean showLogs = assignment.getLeg().getStatus().equals(LegStatus.LANDED) && !assignment.getStatus().equals(AssignmentStatus.CANCELLED);

		Dataset dataset = super.unbindObject(object, "role", "draftMode", "lastUpdate", "status", "remarks");

		// Leg (Flight) information
		dataset.put("flightNumber", object.getLeg().getFlightNumber());
		dataset.put("departure", object.getLeg().getDeparture());
		dataset.put("arrival", object.getLeg().getArrival());
		dataset.put("legStatus", object.getLeg().getStatus());
		dataset.put("departureAirport", object.getLeg().getDepartureAirport().getCity());
		dataset.put("arrivalAirport", object.getLeg().getArrivalAirport().getCity());
		dataset.put("aircraft", object.getLeg().getAircraft().getModel());

		// Member information
		dataset.put("employeeCode", object.getMember().getEmployeeCode());
		dataset.put("phoneNumber", object.getMember().getPhoneNumber());
		dataset.put("languageSkills", object.getMember().getLanguageSkills());
		dataset.put("yearsOfExperience", object.getMember().getYearsOfExperience());
		dataset.put("salary", object.getMember().getSalary());
		dataset.put("availabilityStatus", object.getMember().getAvailabilityStatus());
		dataset.put("memberName", object.getMember().getUserAccount().getUsername());

		SelectChoices statusChoices = SelectChoices.from(AssignmentStatus.class, object.getStatus());
		SelectChoices roleChoices = SelectChoices.from(Role.class, object.getRole());

		SelectChoices legChoices;
		if (object.getDraftMode())
			legChoices = SelectChoices.from(this.repository.findAllPFL(MomentHelper.getCurrentMoment(), member.getAirline().getId()), "flightNumber", object.getLeg());
		else
			legChoices = SelectChoices.from(this.repository.findAllLegs(), "flightNumber", object.getLeg());

		dataset.put("leg", legChoices.getSelected());
		dataset.put("legs", legChoices);

		dataset.put("role", roleChoices);
		dataset.put("status", statusChoices);
		dataset.put("memberKey", member.getId());
		dataset.put("member", member.getEmployeeCode());

		super.getResponse().addGlobal("showLogs", showLogs);

		super.getResponse().addData(dataset);
	}
}
